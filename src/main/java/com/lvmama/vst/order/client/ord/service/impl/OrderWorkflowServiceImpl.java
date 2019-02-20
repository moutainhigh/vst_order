package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderWorkflowService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_OBJECT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComActivitiRelation.OBJECT_TYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_TYPE;
import com.lvmama.vst.order.processer.OrderSmsSendProcesser;
import com.lvmama.vst.order.processer.SupplierOrderItemProcesser;
import com.lvmama.vst.order.processer.SupplierOrderItemTicketsProcesser;
import com.lvmama.vst.order.processer.SupplierOrderProcesser;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.TestOrderUtil;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author lancey
 *
 */
@Component("orderWorkflowServiceRemote")
public class OrderWorkflowServiceImpl extends AbstractOrderClientService implements OrderWorkflowService {
	private static final Log LOG = LogFactory.getLog(OrderWorkflowServiceImpl.class);
	private static final String[] AUDIT_TYPE_LIST = {
			OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.VISIT_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.name(),
			OrderEnum.AUDIT_TYPE.FULL_HOUSE_AUDIT.name()
	};

	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	@Autowired
	private IOrderSmsSendService orderSmsSendService;	
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
	
	@Autowired
	private SupplierOrderProcesser supplierOrderProcesser;
	
	@Autowired
	private OrderSmsSendProcesser orderSmsSendProcesser;
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	
	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;
	
	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	
	// Added by yangzhenzhong at 2015/9/15 begin
	@Autowired
	private SupplierOrderItemTicketsProcesser supplierOrderItemTicketsProcesser;
	//end 
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Resource(name="allocationMessageProducer")
	private TopicMessageProducer allocationMessageProducer;
	
	private final Logger logger = LoggerFactory.getLogger(OrderWorkflowServiceImpl.class);
	
	@Autowired
	private IOrderStatusManageService orderStatusManageService;
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;

	/* (non-Javadoc)
	 * @see com.lvmama.vst.back.client.ord.service.OrderWorkflowService#sendOrderSms(java.lang.Long, com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE)
	 */
	@Override
	public void sendOrderSms(Long objectId, SEND_NODE sendNode,String orderType) {
		logger.info("OrderWorkflowServiceImpl.sendOrderSms:订单ID={}, sendNode={}, orderType={}", new Object[]{objectId, sendNode, orderType});
		try {
			//有可用的发送节点，使用节点发送，无使用发送节点从订单类弄来判断
			if(sendNode==null){
				OrdOrder order = orderLocalService.queryOrdorderByOrderId(objectId);
				if(StringUtils.equalsIgnoreCase(orderType, "CREATE")){
					orderSmsSendProcesser.handle(MessageFactory.newOrderCreateMessage(objectId),order);
				}else if(StringUtils.equals(orderType, "AMPLE")){
					if(!order.isCancel()){
						orderSmsSendProcesser.handle(MessageFactory.newOrderResourceStatusMessage(objectId,"AMPLE"),order);
					}
				}else if(StringUtils.equals(orderType, "CANCEL")){
					orderSmsSendProcesser.handle(MessageFactory.newOrderCancelMessage(objectId, ""),order);
				}else if(StringUtils.equals(orderType, "PAYMENT")){
					orderSmsSendProcesser.handle(MessageFactory.newOrderPaymentMessage(objectId, ""),order);
				}else if(StringUtils.equals(orderType, "REFUNDMENT")){
					orderSmsSendProcesser.handle(MessageFactory.newOrderRefundedSuccessMessage(objectId),order);
				}
			}else{
				//存在具体的发送节点的理解为订单号
				if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
					orderSmsSendService.sendSms(objectId, sendNode);
				}else{
					orderSendSmsService.sendSms(objectId, sendNode);
				}
			}
		} catch (Exception e) {
			//独立环境经常因orderSendSmsService为null 导致空指针，保证主流程，此处增加try-catch  by  李志强  2016-08-24
			LOG.error("Send Sms Excpetion is="+ ExceptionUtil.getExceptionDetails(e));
		}

	}

	/* (non-Javadoc)
	 * @see com.lvmama.vst.back.client.ord.service.OrderWorkflowService#createTask(java.lang.Long, com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE)
	 */
	@Override
	public Long createTask(Long orderId, Long objectId, AUDIT_TYPE type) {
		return createTask(orderId,objectId, type,null);
	}
	
	@Override
	public Long createTask(Long orderId, Long objectId, AUDIT_OBJECT_TYPE objectType, AUDIT_TYPE type) {
		return createTask(orderId, objectId, objectType, type, null);
	}
	
	public Long createTask(Long orderId, Long objectId,AUDIT_OBJECT_TYPE objectType, AUDIT_TYPE type, String operator) {
		logger.info("OrderWorkflowServiceImpl.createTask，orderId:"+orderId+",objectid:"+objectId+",type:"+type+",operator:"+operator);
		
		if (OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(type.name())
				|| OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(type.name())
				|| OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(type.name())
				|| OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equals(type.name())){ // 已创建则不创建
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("auditType", type.name());
			params.put("objectType", objectType);
			params.put("objectId", objectId);
			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
			if(auditList != null && auditList.size() > 0){
				return auditList.get(0).getAuditId();
			}
		}
		
		ComAudit audit = new ComAudit();
		if(StringUtils.isEmpty(operator)){
			//订单的凭证确认活动，如果不需要确认，则自动将活动分给SYSTEM
			if (OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equals(type.name())){
				//工作流表达式：${orderItem.categoryId==1&&orderItem.supplierId==5400}
				if( OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(
						objectType.name())){
					OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(objectId);
					if(orderItem != null){
						if(orderItem.getCategoryId() == 1L && orderItem.getSupplierId() == 5400L){
							audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
							audit.setOperatorName("SYSTEM");// 标记为自动过
						}
					}
				}
				//工作流表达式：${mainOrderItem.supplierId==5400}
				else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.name())){
					OrdOrder order = this.orderUpdateService.queryOrdOrderByOrderId(objectId);
					if(order.getCategoryId() == 1L){ 
						order.setOrderItemList(iOrdOrderItemService.selectByOrderId(objectId));
						if(order.getMainOrderItem().getSupplierId() == 5400L){ 
							audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
							audit.setOperatorName("SYSTEM");// 标记为自动过
						}
					}
				}
			}
			
			//子订单的资源审核活动，如果不需要资源审核，则自动将活动分给SYSTEM
			if (OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(type.name())){
				if( OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(
						objectType.name())){
					OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(objectId);
					if(orderItem != null){
						if(!"true".equalsIgnoreCase(orderItem.getNeedResourceConfirm())) {
							audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
							audit.setOperatorName("SYSTEM");
						}
						if(orderItem.getCategoryId() == 1L && orderItem.hasSupplierApi()
								&& !orderItem.hasContentValue("is_to_foreign", "Y") //目的地不是国外
								&& !orderItem.hasContentValue("is_from_foreign", "Y")){ // 对接酒店
							audit.setAuditFlag("SYSTEM"); // 标记为由对接系统处理
						}
					}
				}else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.name())){
					OrdOrder order = this.orderUpdateService.queryOrdOrderByOrderId(objectId);
					if(order.getCategoryId() == 1L){ 
						order.setOrderItemList(iOrdOrderItemService.selectByOrderId(objectId));
						if(order.getMainOrderItem().hasSupplierApi() && !order.getMainOrderItem().hasContentValue("is_to_foreign", "Y") //目的地不是国外
								&& !order.getMainOrderItem().hasContentValue("is_from_foreign", "Y")){
							audit.setAuditFlag("SYSTEM"); // 标记为由对接系统处理
						}
					}
				}
			}
//			// 主单订单预审,如果不需要人工审核,则自动将活动分给SYSTEM
//			if (OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(type.name())
//					&& OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.name())) {
//				OrdOrder order = this.orderUpdateService.queryOrdOrderByOrderId(objectId);
//				if(order != null && !order.hasPretrialAuditOrder()) {
//					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
//					audit.setOperatorName("SYSTEM");
//				}
//			}
			
			// 主单信息审核,如果不需要人工审核,则自动将活动分给SYSTEM
			if (OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(type.name())
					&& OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.name())) {
				OrdOrder order = this.orderUpdateService.queryOrdOrderByOrderId(objectId);
				if(order.getCategoryId() == 1L){ 
					// 流程自动过表达式：${order.distributorId==2||mainOrderItem.hasSupplierApi()}
					order.setOrderItemList(iOrdOrderItemService.selectByOrderId(objectId));
					if(order.getDistributorId()==Constants.DISTRIBUTOR_2 || order.getMainOrderItem().hasSupplierApi() || !order.getMainOrderItem().getInfoPassNeedConfirm()){
						audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
						audit.setOperatorName("SYSTEM");
					}
				}
			}

			if(StringUtils.isBlank(audit.getAuditStatus())) {
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
			}
		}else{
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
			audit.setOperatorName(operator);
		}
		audit.setAuditType(type.name());
		audit.setCreateTime(new Date());
		audit.setObjectId(objectId);
		audit.setObjectType(objectType.name());
		
		if("SYSTEM".equals(audit.getOperatorName()))// 标记为系统自动过
			audit.setAuditFlag("SYSTEM");
		
		int result = orderAuditService.saveAudit(audit);
		
		//活动创建日志
		try {
			if(result > 0){
				ComLog.COM_LOG_OBJECT_TYPE logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
					logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
				}
				//日志备注中添加订单可操作时间
				String memo = "";
				if (audit.getRemindTime() != null){
					String remindTime = DateUtil.formatDate(audit.getRemindTime(), DateUtil.HHMMSS_DATE_FORMAT);
					memo = "实际入库展示时间为：" + remindTime;
				}

				lvmmLogClientService.sendLog(logObjectType,
						audit.getObjectId(), 
						audit.getObjectId(), 
						"SYSTEM", 
						"创建编号为["+audit.getObjectId()+"]的订单活动["+OrderEnum.AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName()+"]", 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName()+"]",
						memo);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

//		String distributionChannel = distributionBusiness.getDistributionChannelByAudit(audit);// 查询渠道并设置了活动订单
//		if(distributionChannel != null && 
//				(OrderEnum.ORDER_DISTRIBUTION_CHANNEL.taobao.name().equals(distributionChannel)
//						|| OrderEnum.ORDER_DISTRIBUTION_CHANNEL.other.name().equals(distributionChannel))
//						&& (OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.name().equals(type.name())
//								|| OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(type.name()))){// 是否分销，且信息预审或信息审核
////			distributionBusiness.makeOrderAuditForDistribution(audit);
//			allocationMessageProducer.sendMsg(MessageFactory.newOrderAllocationMessage(audit.getAuditId(), "FALSE"));
//			return audit.getAuditId();
//		}

		
		if(StringUtils.isEmpty(audit.getOperatorName()) || "SYSTEM".equals(audit.getOperatorName())){//任务没有指到到操作人的时候做一次分单
			try{
				logger.info("自动过的订单ID："+orderId+",objectId:"+objectId);
//				distributionBusiness.makeOrderAudit(audit);
				allocationMessageProducer.sendMsg(MessageFactory.newOrderAllocationMessage(audit.getAuditId(), "TRUE"));
			}catch(Exception ex){
				LOG.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
		return audit.getAuditId();
	}
	
	@Override
	public void completeTask(List<Long> auditId, String operator,boolean isCover) {
		if(CollectionUtils.isEmpty(auditId)){
			logger.warn("update audit,but id is null");
			return;
		}
		for(Long id:auditId){
			ComAudit audit = orderAuditService.queryAuditById(id);
			if(audit!=null){
				if(OrderEnum.AUDIT_STATUS.PROCESSED.name().equalsIgnoreCase(audit.getAuditStatus())){
					logger.warn("task id:{} is PROCESSED",id);
				}else{
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					audit.setUpdateTime(new Date());
//					if(isCover){
//						audit.setOperatorName(operator);
//					}
					if(StringUtils.isEmpty(audit.getOperatorName())){

						audit.setOperatorName(operator);
					}
					audit.setAuditFlag("SYSTEM");// 标记为系统自动过
					int result=orderAuditService.updateByPrimaryKey(audit);
					if(result>0&&!OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
						ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
						if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
							objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
						}
						lvmmLogClientService.sendLog(objectType,
								audit.getObjectId(), 
								audit.getObjectId(), 
								operator, 
								"将编号为["+audit.getObjectId()+"]的订单活动变更["+OrderEnum.AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName()+"通过]", 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName()+"通过]",
								"");
					}
				}
			}
		}
	}

	@Override
	public List<Long> createBCertAndSend(Long orderId, EBK_CERTIFICATE_TYPE type) {
//		if(EBK_CERTIFICATE_TYPE.CANCEL==type){
//			supplierOrderProcesser.process(MessageFactory.newOrderInformationStatusMessage(orderId, OrderEnum.INFO_STATUS.INFOPASS.name()));			
//		}else if(EBK_CERTIFICATE_TYPE.CONFIRM==type){
//			supplierOrderProcesser.process(MessageFactory.newOrderCancelMessage(orderId, ""));
//		}else{
//			return null;
//		}
		return null;
	}

	@Override
	public boolean createSupplierOrder(Long orderId, String type) {
		//这个地方先调用消息处理器来操作，后面再改进
		if(StringUtils.equals(type, "CREATE")){
			supplierOrderProcesser.handle(MessageFactory.newOrderInformationStatusMessage(orderId, OrderEnum.INFO_STATUS.INFOPASS.name()));			
		}else if(StringUtils.equalsIgnoreCase(type, "CANCEL")){
			supplierOrderProcesser.handle(MessageFactory.newOrderCancelMessage(orderId, ""));
		}else if(StringUtils.equalsIgnoreCase(type, "PAYMENT")||StringUtils.equalsIgnoreCase(type, "AMPLE")){
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			//预付需要已经支付，现付只要是已经审核
			if(order.hasInfoAndResourcePass()&&((order.hasNeedPrepaid()&&order.hasPayed())||(order.hasNeedPay()))){
				supplierOrderProcesser.handle(MessageFactory.newOrderPaymentMessage(orderId, ""));
			}
		}else if(StringUtils.equalsIgnoreCase(type, "TRAVDELAY")){
		    //意外险后置，排除意外险，推送其余子单，创建供应商订单
		    supplierOrderProcesser.handle(MessageFactory.excludedInsAccOrderItemMessage(orderId, ""));
		}else if (StringUtils.equalsIgnoreCase(type, "INSACC")) {
		    supplierOrderProcesser.handle(MessageFactory.sendInsAccOrderItemMessage(orderId, ""));
        }else{
			return false;
		}
		return true;	
	}

	@Override
	public boolean createSupplierOrder(Long orderId,Long orderItemId,String type){
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		//调用时已经确认过，无需再次确认，而且这种确认方式有误
		/*OrdOrderItem orderItem = order.getOrderItemByOrderItemId(orderItemId);
		if(orderItem==null){
			return false;
		}
		*/
		if(StringUtils.equals(type, "CREATE")){
			supplierOrderItemProcesser.handle(MessageFactory.newOrderInformationStatusMessage(orderItemId, OrderEnum.INFO_STATUS.INFOPASS.name()),order);			
		}else if(StringUtils.equalsIgnoreCase(type, "CANCEL")){
			supplierOrderItemProcesser.handle(MessageFactory.newOrderCancelMessage(orderItemId, ""),order);
		}else if(StringUtils.equalsIgnoreCase(type, "PAYMENT")||StringUtils.equalsIgnoreCase(type, "AMPLE")){
			//预付需要已经支付，现付只要是已经审核
			if(order.hasInfoAndResourcePass()&&((order.hasNeedPrepaid()&&order.hasPayed())||(order.hasNeedPay()))){
				supplierOrderItemProcesser.handle(MessageFactory.newOrderPaymentMessage(orderItemId, ""),order);
			}
		}else{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean createSupplierOrder(Long orderId,List<Long> orderItemIds,String type) {
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		if(StringUtils.equals(type, "CREATE")){
			supplierOrderOperator.createSupplierOrder(orderId, new HashSet<Long>(orderItemIds));			
		}else if(StringUtils.equalsIgnoreCase(type, "CANCEL")){
			supplierOrderOperator.cancelSupplierOrder(orderId);
		}else if(StringUtils.equalsIgnoreCase(type, "PAYMENT")||StringUtils.equalsIgnoreCase(type, "AMPLE")){
			//预付需要已经支付，现付只要是已经审核
			if(order.hasInfoAndResourcePass()&&((order.hasNeedPrepaid()&&order.hasPayed())||(order.hasNeedPay()))){
				supplierOrderOperator.createSupplierOrder(orderId, new HashSet<Long>(orderItemIds));
			}
		}else{
			return false;
		}
		return true;
	}
	
	@Autowired
	private SupplierOrderItemProcesser supplierOrderItemProcesser;
	
	@Override
	public String assignTask(Long auditId) {
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		audit = distributionBusiness.makeOrderAudit(audit);
		if(audit!=null){
			return audit.getOperatorName();
		}
		return null;
	}
	
	@Override
	public String assignTask(Long auditId,String operatorName) {
		Assert.hasText(operatorName);
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		if(audit!=null&&!OrderEnum.AUDIT_STATUS.PROCESSED.name().equalsIgnoreCase(audit.getAuditStatus())){
			audit.setOperatorName(operatorName);
			orderAuditService.updateByPrimaryKey(audit);
		}
		return operatorName;
	}

	@Override
	public void updateViewStatus(Long orderId) {
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		OrderEnum.ORDER_VIEW_STATUS viewStatus=null;	
		//判断是否属于目的地订单
		boolean isDestBu = OrdOrderUtils.isAllDestBuFrontOrder(order);				
		if(order!=null){
			if(isDestBu&&!OrdOrderUtils.isBusHotelOrder(order)){				
				viewStatus = setDestBuOrderViewStatus(order);
			}else
			{
				if(((BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
						&&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue())
						&&OrdOrderUtils.isLocalBuFrontOrder(order)){
					if(order.hasInfoAndResourcePass()){
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.WAIT_VISIT;
					}else if(order.hasPayed()){
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED;
					}else{
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY;
					}
					if(order.hasCanceled()){
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.CANCEL;
					}
				}else if(OrdOrderUtils.isBusHotelOrder(order)
						||(StringUtils.isNotBlank(order.getProcessKey())&&"local_order_prelockseat_main".equals(order.getProcessKey()))){
					if(order.hasInfoAndResourcePass()){
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.WAIT_VISIT;
					}else if(order.hasPayed()){
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED;
					}else{
						viewStatus= OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY;
					}
					if(order.hasCanceled()){
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.CANCEL;
					}
				}else{
					if(order.hasPayed()){//如果资源已经审核通过
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.PAYED;
					}else if(order.hasInfoAndResourcePass()){
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY;
					}else{
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.APPROVING;				
					}
					if(order.hasCanceled()){
						viewStatus = OrderEnum.ORDER_VIEW_STATUS.CANCEL;
					}
				}
			}
			//
			if(viewStatus!=null){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("viewOrderStatus", viewStatus.name());
				map.put("orderId", order.getOrderId());
				orderUpdateService.updateViewStatus(map);
			}
		}
		
	}
	
	
	/**
	 * 设置目的地BU订单展示状态
	 * @param order
	 */
	private OrderEnum.ORDER_VIEW_STATUS setDestBuOrderViewStatus(OrdOrder order){
		OrderEnum.ORDER_VIEW_STATUS viewStatus=null;	
		Date currentTime = DateUtil.getTodayDate();
		//订单是否已经取消
		if(order.hasCanceled()){
			viewStatus = OrderEnum.ORDER_VIEW_STATUS.CANCEL;
			return viewStatus;
		}
		boolean isStockHotel = false;//是否保留房
		String paymentTarget = order.getPaymentTarget();//支付对象（预付/现付）
		
		isStockHotel = OrdOrderUtils.hasStockFlag(order);
		
		LOG.info("start setDestBuOrderViewStatus method and orderId ="+order.getOrderId()+"isStockHotel is:"+isStockHotel+"|currentTime is:"
		+DateUtil.formatDate(currentTime, DateUtil.HHMMSS_DATE_FORMAT)+"paymentTarget:"+paymentTarget+"order hasPayed:"+order.hasPayed());
		
		if(!isStockHotel){
			//非保留房预付
			if(SuppGoods.PAYTARGET.PREPAID.name().equals(paymentTarget)){
				if(order.hasPayed()){									
					viewStatus = getViewStatusByNoStock(order, currentTime);
				}				
			}else{
				//非保留房现付
				viewStatus = getViewStatusByNoStock(order, currentTime);
			}						
		}else
		{
			//保留房预付
			if(SuppGoods.PAYTARGET.PREPAID.name().equals(paymentTarget)){
				if(order.hasPayed())
				{
					viewStatus = getViewStatusByStock(order,currentTime);
				}
			}else{
				//保留房现付
				viewStatus = getViewStatusByStock(order,currentTime);
			}
		}
		return viewStatus;
	}
	
	/**
	 * 非保留房订单的当前时间和品类（酒店or线路）判断状态
	 * @param order
	 * @param currentTime
	 * @return
	 */
	private OrderEnum.ORDER_VIEW_STATUS getViewStatusByNoStock(OrdOrder order,Date currentTime){
		OrderEnum.ORDER_VIEW_STATUS viewStatus=null;	
		LOG.info("订单展示状态审核是否通过hasInfoAndResourcePass is"+order.hasInfoAndResourcePass()+"infoStatus="+order.getInfoStatus()+"resourceStatuc ="+order.getResourceStatus());
		if(!order.hasInfoAndResourcePass()){
			viewStatus = OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED;
		}else{
			if(OrderUtils.isHotelByCategoryId(order.getCategoryId())){
				if(currentTime.compareTo(order.getVisitTime())<=0){
					viewStatus = OrderEnum.ORDER_VIEW_STATUS.WAIT_HOTEL;
				}				
			}
			if(!OrderUtils.isHotelByCategoryId(order.getCategoryId())){
				if(currentTime.compareTo(order.getVisitTime())<=0){
					viewStatus = OrderEnum.ORDER_VIEW_STATUS.WAIT_VISIT;
				}				
			}								
		}
		return viewStatus;
	}
	
	
	/**
	 * 保留房订单的当前时间和品类（酒店or线路）判断状态
	 * @param order
	 * @return
	 */
	private OrderEnum.ORDER_VIEW_STATUS getViewStatusByStock(OrdOrder order,Date currentTime){	
		OrderEnum.ORDER_VIEW_STATUS viewStatus=null;	
		if(OrderUtils.isHotelByCategoryId(order.getCategoryId())){
			if(currentTime.compareTo(order.getVisitTime())<=0){
				viewStatus = OrderEnum.ORDER_VIEW_STATUS.WAIT_HOTEL;
			}
		}else{			
		  if(currentTime.compareTo(order.getVisitTime())<=0){
			 viewStatus = OrderEnum.ORDER_VIEW_STATUS.WAIT_VISIT;
		    }
		}
		return viewStatus;
	}
	
	
	@Override
	public ResultHandle cancelOrder(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		logger.info("WORKFLOW-----OrdOrderClientServiceImpl.cancelOrder: orderId=" + orderId);
		if (orderId != null) {
			logger.info("启动工作流来取消订单"+orderId);
			resultHandle= cancelOrderLocal(orderId, cancelCode, reason, operatorId, memo);
			logger.info("启动工作流来取消订单"+orderId+",取消成功：" + resultHandle.isSuccess());
			logger.info("OrdOrderClientServiceImpl.cancelOrder: resultHandle.isSuccess=" + resultHandle.isSuccess() + ", resultHandle.getMsg=" + resultHandle.getMsg());
			if(resultHandle.isFail()) {
				return resultHandle;
			}
			
			//重新查询取消后的订单
			OrdOrder canceledOrder = complexQueryService.queryOrderByOrderId(orderId);
			//日志入库
			insertOrderLog(canceledOrder, OrderEnum.ORDER_STATUS.CANCEL.getCode(), operatorId, memo,cancelCode,reason);
			//改为人工
			/*if(StringUtils.equals(OrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM.toString(), cancelCode)&&canceledOrder.hasNeedPrepaid()&& canceledOrder.hasPayed()){
				//退款申请
				if(StringUtils.isEmpty(memo)){
					memo="取消订单需要自动退款";
				}
				ordPrePayServiceAdapter.autoCreateOrderFullRefundVst(orderId, operatorId, memo);
			}*/
		}
		
		return resultHandle;
	}
	
	public ResultHandle cancelOrderLocal(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			if(order==null || order.isCancel()){
				resultHandle.setMsg("订单不存在/订单已经被废单");
				return resultHandle;
			}
			resultHandle = orderUpdateService.updateCancelOrder(orderId, cancelCode, reason, operatorId,memo);
			/*if(resultHandle.isSuccess()){
				logger.info("工作流取消订单，处理买断资源start");
				orderLocalService.updateResBackToPrecontrol(orderId);
				logger.info("工作流取消订单，处理买断资源end");
			}*/
			logger.info("OrdOrderClientServiceImpl.cancelOrderLocal: resultHandle.isSuccess=" + resultHandle.isSuccess() + ", resultHandle.getMsg=" + resultHandle.getMsg());
			if (resultHandle.isSuccess()) {
				String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;
				orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId, addition));
				logger.info("OrdOrderClientServiceImpl.cancelOrderLocal: send OrderCancelMessage");
			}
		} catch (Exception ex) {
			logger.error("{}", ex);
			resultHandle.setMsg(ex.getMessage());
			logger.info("OrdOrderClientServiceImpl.cancelOrderLocal: Exception msg=" + ex.getMessage());
			throw new RuntimeException(ex);
		}
		return resultHandle;
	}

	@Override
	public void saveTaskRelation(String taskId, Long objectId, OBJECT_TYPE type) {
		//任务与com_audit的关系创建
		comActivitiRelationService.saveRelation(type.name(), taskId, objectId, type);
	}

	@Override
	public ComAudit queryTask(Long auditId) {
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		if (AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equalsIgnoreCase(audit.getObjectType())) {
			logger.info("OrdOrderClientServiceImpl.queryTask: orderitemId:" + audit.getObjectId());
			OrdOrderItem item =  orderUpdateService.getOrderItem(audit.getObjectId());
			OrdOrder order = new OrdOrder();
			order.setOrderId(item.getOrderId());
			audit.setOrder(order);
		}else if (AUDIT_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(audit.getObjectType())) {
			OrdOrder order = new OrdOrder();
			order.setOrderId(audit.getObjectId());
			audit.setOrder(order);
		}

		return audit;
	}

	@Override
	public void autoOrderInfoPass(Long orderId) {
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
		orderLocalService.executeUpdateInfoStatus(order, OrderEnum.INFO_STATUS.INFOPASS.name(), "SYSTEM", "");
	}

	@Override
	public void markTaskValid(Long objectId, final AUDIT_OBJECT_TYPE objectType) {
		//TODO 临时解决storyID=17162，后期等工单上线优化
        //屏蔽活动类型列表
        List<String> auditTypeList = new ArrayList<String>();
        auditTypeList.addAll(Arrays.asList(AUDIT_TYPE_LIST));
		//获取主单信息
		Long orderId =objectId;
		if(AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
			OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderId);
			if(orderItem !=null) orderId = orderItem.getOrderId();
		}
        OrdOrder order =orderLocalService.querySimpleOrder(orderId);
		if (TestOrderUtil.isTestOrderForMarkTaskValid(order)){
			auditTypeList.add(AUDIT_TYPE.BOOKING_AUDIT.name());
		}
		doMarkTaskValid(objectId, objectType, auditTypeList);
	}
	/**
	 * 标记活动无效
	 * @param objectId
	 * @param objectType
	 * @param auditTypeList
	 */
	private void doMarkTaskValid(Long objectId, final AUDIT_OBJECT_TYPE objectType, List<String> auditTypeList) {
		LOG.info("objectId=" +objectId +",objectType=" +objectType+",auditTypeList=" +auditTypeList);

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("objectType", objectType.name());
		List<String> statusList = new ArrayList<String>();
		statusList.add(OrderEnum.AUDIT_STATUS.POOL.name());
		statusList.add(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		param.put("auditStatusArray", statusList);
		List<ComAudit> list = orderAuditService
				.queryAuditListByParam(param);
		for (ComAudit ca : list) {
			if (auditTypeList.contains(ca.getAuditType())) {
				orderAuditService.markValid(ca.getAuditId());
			}
		}

		// 更新已处理活动状态为不可再分单
		List<String> statusList2 = new ArrayList<String>();
		statusList2.add(OrderEnum.AUDIT_STATUS.PROCESSED.name());
		param.put("auditStatusArray", statusList2);
		List<ComAudit> list2 = orderAuditService
				.queryAuditListByParam(param);
		for (ComAudit ca : list2) {
			orderAuditService.markCanNotReaudit(ca.getAuditId());
		}
	}
	@Override
	public void createItemRelation(String processInstanceId,
			Long orderItemId, String type) {
		try{
			comActivitiRelationService.saveRelation(type, processInstanceId, orderItemId, ComActivitiRelation.OBJECT_TYPE.ORD_ORDER_ITEM);
		}catch(Exception ex){
			
		}
	}

	@Override
	public void executeAutoInfoPass(Long orderItemId) {
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if(orderItem!=null){
			String newStatus = OrderEnum.INFO_STATUS.INFOPASS.name();
			ResultHandleT<ComAudit> resultHandle = orderStatusManageService.updateChildInfoStatus(orderItem, newStatus,"SYSTEM",null);
			logger.info("OrdOrderClientServiceImpl.executeUpdateChildInfoStatus,OrderItemId=" + orderItem.getOrderItemId() + ",resultHandle.isSuccess=" + resultHandle.isSuccess()
					+ ", newInfoStatus=" + newStatus);
			
			if (resultHandle.isSuccess() ){
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
				
				if (order.hasInfoPass()) {
					orderMessageProducer.sendMsg(MessageFactory.newOrderInformationStatusMessage(orderItem.getOrderId(), newStatus));
				}
			}
		}
	}

	@Override
	public void addTicketPerformInfo(Long orderItemId) {
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		orderUpdateService.saveTicketPerform(orderItem);
	}

	@Override
	public void autoOrderCertTask(List<Long> auditId, String operator) {
		logger.warn("autoOrderCertTask,auditId="+auditId);
		// TODO Auto-generated method stub
		if(CollectionUtils.isEmpty(auditId)){
			logger.warn("update audit,but id is null");
			return;
		}
		for(Long id:auditId){
			ComAudit audit = orderAuditService.queryAuditById(id);
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){
				OrdOrder order=orderUpdateService.queryOrdOrderByOrderId(audit.getObjectId());
				orderLocalService.updateCertificateStatus(order, operator, "对接预付酒店无需凭证确认。");
			 }else{
				 orderLocalService.updateChildCertificateStatus(audit.getObjectId(),operator, "对接预付酒店无需凭证确认。");
			 }
		}
	}
	
	/**创建门票供应商订单
	 * Created by yangzhenzhong
	 * at 2015/9/15
	 */
	@Override
	public boolean createSupplierOrder(Long orderId, Set<Long> orderItemIds,String type) {
		
		if(StringUtils.equals(type, "CREATE")){
			supplierOrderItemTicketsProcesser.handle(MessageFactory.newOrderInformationStatusMessage(orderId, OrderEnum.INFO_STATUS.INFOPASS.name()),orderItemIds);			
		}
		else{
			return false;
		}
		return true;
}

	@Override
	public void markValid(Long auditId) {
		orderAuditService.markValid(auditId);
	}

	@Override
	public void completeResourceAmpleTask(Long auditId,String supplierFlag) {
		if(auditId == null || auditId == 0L){
			logger.warn("update audit,but id is null");
			return;
		}
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		if(audit == null || !OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
			logger.error("completeResourceAmpleTask流程错误,非资源审核活动！");
			return;
		}
		String memo = "凭证己确认，系统自动通过资源审核";
		if("Y".equals(supplierFlag)){
			memo = "对接供应商订单已生成，系统自动通过资源审核";
		}
		ResultHandleT<ComAudit> resultHandleT = orderLocalService.executeUpdateOrderResourceStatusAmple(audit, "", "SYSTEM", memo);
		if (!resultHandleT.isSuccess()) {
			logger.error("completeResourceAmpleTask,资源审核失败："+auditId);
		}
	}
	@Override
	public void compensateResourceAmpleTask(Long orderId) {
		String memo = "凭证己确认，系统自动通过资源审核";
		ResultHandleT<ComAudit> resultHandleT = orderLocalService.executeCompensateUpdateOrderResourceStatusAmple(orderId, "", "SYSTEM", memo);
		if (!resultHandleT.isSuccess()) {
			logger.error("compensateResourceAmpleTask,补偿资源审核失败：" + orderId);
		}
	}

	@Override
	public int createOrderReservation(String auditType, String auditSubType,
			String objectType, Long objectId) {
		Long orderId = 0L;
		Long orderItemId = 0L;
		int successStatus = 0;
		
		try {
			String memo = "";
			if(!StringUtils.isEmpty(auditSubType) && OrderEnum.AUDIT_SUB_TYPE.RESOURCE_CONFIRM_OVER.name().equals(auditSubType)){
				memo = "供应商已超过一小时没有确认订单资源，请跟进！";
			}
			logger.info("OrderWorkflowService.createOrderReservation start, objectId:" + objectId 
					+ ",objecttype:" + objectType + ",auditType" + auditType + ",auditSubType" + auditSubType + ",memo:" + memo);
			
			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType)){
				OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(objectId);
				if(orderItem != null){
					orderItemId = objectId;
					orderId = orderItem.getOrderId();
					ComMessage comMessage=new ComMessage();
					comMessage.setMessageContent(memo);
					successStatus = comMessageService.saveReservationChildOrder(comMessage, auditType, auditSubType, orderId, orderItemId, "SYSTEM", memo);
				}
			}else{
				orderId = objectId;	
				successStatus = comMessageService.saveReservationOrder(orderId, auditSubType, "SYSTEM", memo, false);
			}
		} catch (Exception e) {
			logger.error("OrderWorkflowService.createOrderReservation error,{}", e);
		}
		
		return successStatus;
	}

	@Override
    public Long createTask(Long orderId, Long objectId, AUDIT_TYPE type, String operator) {
		if (orderId != null && orderId.equals(objectId)) {
			return createTask(orderId, objectId, OrderEnum.AUDIT_OBJECT_TYPE.ORDER, type, operator);
		} else {
			return createTask(orderId, objectId, OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM, type, operator);
		}
    }

	@Override
	public int sendOrderReservation(String auditType, String objectType, Long objectId) {
		logger.info("OrderWorkflowService.sendOrderReservation start, objectId:" + objectId 
				+ ",objecttype:" + objectType + ",auditType" + auditType );
		String memo="";
		if(!StringUtils.isEmpty(auditType) && OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equals(auditType)){
			memo = "游玩人未锁定";
		}
		ComMessage comMessage = new ComMessage();
		comMessage.setMessageContent(memo);
		int successStatus = comMessageService.saveReservation(comMessage, null, objectId, "SYSTEM", memo);
		return successStatus;
	}

	@Override
	public boolean checkParallel(Long orderId) {
		OrdOrder ordOrder = orderLocalService.queryOrdorderByOrderId(orderId);
		boolean isParallel = false;
		isParallel = orderUpdateService.checkWorkflowOrderInfoParallelProcess(ordOrder);
		return isParallel;
	}
	
	/**
	 * 
	 * @Description: 根据orderId获取意外险子单列表 
	 * @author Wangsizhi
	 * @date 2016-12-21 下午8:03:54
	 */
    private List<Long> getInsAccOrderItems(Long orderId) {
        List<Long> insAccOrderItemList = new ArrayList<Long>();
        List<OrdOrderItem> orderItemList = orderUpdateService.queryOrderItemByOrderId(orderId);
        /*过滤出目的地就套餐、酒+景、自由行主单中意外险子单，此部分子单待游玩人补充完整后再推送供应商*/
        for (OrdOrderItem ordOrderItem : orderItemList) {
            String destBuAccFlag = ordOrderItem.getContentStringByKey("destBuAccFlag");
            if (StringUtils.isNotBlank(destBuAccFlag) && StringUtils.equalsIgnoreCase(destBuAccFlag, "Y")) {
                insAccOrderItemList.add(ordOrderItem.getOrderItemId());
            }
        }
        return insAccOrderItemList;
    }
}
