package com.lvmama.vst.order.service.impl;

import com.google.common.collect.Lists;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.client.ord.service.OrderSendSMSService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 订单详情页面 各种活动功能调用，其他地方调用请慎重
 * @author 张伟
 *
 */
@Service("orderStatusManageService")
public class OrderStatusManageServiceImpl implements IOrderStatusManageService {

	
	private static Logger logger = LoggerFactory.getLogger(OrderStatusManageServiceImpl.class);

	@Autowired
	private OrdOrderDao ordOrderDao;

	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private OrderService orderService;
	@Autowired
	private IOrderLocalService orderLocalService;
	
	
	@Autowired
	private IOrderAttachmentService orderAttachmentService;
	
	//短信发送业务接口
	@Autowired
	OrderSendSMSService orderSmsSendService;

	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;
	//推送服务接口
	@Autowired
	private ComPushClientService comPushClientService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	protected IOrderUpdateService orderUpdateService;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Resource(name="teamOutboundTourismContractService")
	private IOrderElectricContactService teamOutboundTourismContractService;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Resource(name="orderTravelElectricContactService")
	private IOrderElectricContactService orderTravelElectricContactService;
	@Autowired
	private DestOrderWorkflowService destOrderWorkflowService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	public ResultHandle updateOrderMemo(Long orderId, String memo){
		
		ResultHandle result=new ResultHandle();
		OrdOrder orderObj=new OrdOrder();
		orderObj.setOrderId(orderId);
		orderObj.setOrderMemo(memo);
		int n=ordOrderDao.updateByPrimaryKeySelective(orderObj);
		if (n!=1 ) {
			result.setMsg("订单更新失败");
		}
		
		return result;
	}
	/**
	 * @param orderId
	 * @param auditType
	 * @param assignor
	 * @param memo
	 * @param appendMessage
	 * 保存日志
	 */
	private void insertOrderLog(final Long orderId,String auditType,String assignor,String memo){
		this.insertOrderLog(orderId, auditType, assignor, memo, "");
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertOrderLog(final Long orderId,String auditType,String assignor,String memo,String appendMessage){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				assignor, 
				"将编号为["+orderId+"]的订单活动变更["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]"+appendMessage, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]",
				memo);
	}
	
	/**
	 * @param orderId
	 * @param orderItemId
	 * @param auditType
	 * @param assignor
	 * @param memo
	 */
	private void insertChildOrderLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo){
		this.insertChildOrderLog(orderId, orderItemId, auditType, assignor, memo, "");
	}
	
	private void insertChildConfirmStatusLog(final Long orderId,final Long orderItemId,String confirmStatus,String assignor,String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				assignor, 
				"将编号为["+orderItemId+"]的子订单确认状态变更["+CONFIRM_STATUS.getCnName(confirmStatus)+"]",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
					+"["+CONFIRM_STATUS.getCnName(confirmStatus)+"更改]",
				memo);
	}
	
	private void insertChildConfirmAuditTypeLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				assignor, 
				"将编号为["+orderItemId+"]的子订单活动变更["
						+Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(auditType)+"通过]", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
					+"["+Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(auditType)+"通过]",
				memo);
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertChildOrderLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo,String appendMessage){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				assignor, 
				"将编号为["+orderItemId+"]的子订单活动变更["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]"+appendMessage, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]",
				memo);
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertEBKChildOrderLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo,String appendMessage,String operatorName){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				operatorName, 
				"EBK "+assignor+"将编号为["+orderItemId+"]的子订单活动变更["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]"+appendMessage, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]",
				memo);
	}
	
	@Override
	public ResultHandleT<ComAudit> updateResourceStatus(Long orderId, String newStatus,String resourceRetentionTime,String assignor,String memo) {
		// TODO Auto-generated method stub
		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId); 
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>(); 
		if(!OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())){ 
			result.setMsg("订单当前状态不可以更改为:"+OrderEnum.RESOURCE_STATUS.getCnName(newStatus)); 
			return result; 
		}
		
		List<OrdOrderItem> orderItemList = ordOrderItemDao.selectByOrderId(orderId);
		OrdOrderItem orderItem=orderItemList.get(0);
		
		OrdOrderItem ordOrderItemObj=new OrdOrderItem();
		ordOrderItemObj.setOrderId(orderId);
		ordOrderItemObj.setResourceStatus(newStatus);
		ordOrderItemObj.setResourceAmpleTime(new Date());
		
		if (!StringUtils.isEmpty(resourceRetentionTime)) {
			
			ordOrderItemObj.setContent(orderItem.getContent());
			ordOrderItemObj.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
		}
		
		int m=ordOrderItemDao.updateByOrderIdSelective(ordOrderItemObj);
		
		StringBuffer appendMessage = new StringBuffer(",支付等待值变更-("+resourceRetentionTime+")");
		order.setOrderMemo(memo);
		order.setResourceStatus(newStatus);
		if(order.hasNeedPrepaid()&&!order.isPayMentType()){
			Date date = new Date();
			orderUpdateService.setOrderWatiPaymentTime(order, date, true);
			
			/**
			 * 门票类支付等待时间计算  2015-05-15
			 */
			this.calPaymentWaitTime4Mp(order,orderItemList);
			//国内 跟团游 对接 支付等待时间为60分钟
			if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode()) && BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==order.getCategoryId().longValue() && "Y".equals(order.getSupplierApiFlag())){
				order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 50));
				logger.info("===orderId:"+order.getOrderId()+" set waitTimeout");
			}
			order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(), order.getWaitPaymentTime()));
			appendMessage.append(" 新值:").append(DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
			order.setApproveTime(date);
		}else{
			appendMessage.append(" 无变更 不满足变更条件 Prepaid:").append(order.hasNeedPrepaid()).append(" PayMentType:").append(order.isPayMentType());
		}
		order.setResourceAmpleTime(new Date());
		//start 2018-07-31 订单更新优化  避免出现脏读
		OrdOrder updateOrder = new OrdOrder();
		updateOrder.setOrderId(order.getOrderId());
		updateOrder.setOrderMemo(order.getOrderMemo());
		updateOrder.setResourceStatus(order.getResourceStatus());
		updateOrder.setWaitPaymentTime(order.getWaitPaymentTime());
		updateOrder.setApproveTime(order.getApproveTime());
		updateOrder.setResourceAmpleTime(order.getResourceAmpleTime());
		int n = ordOrderDao.updateByPrimaryKeySelective(updateOrder);
		//end
		
		
		if (n!=1 ) {
			result.setMsg("订单更新失败");
		}else if(m<=0) {
			result.setMsg("订单子项更新失败");
		}else{
			if(!OrdOrderUtils.isDestBuFrontOrderNew(order) && !OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
				//目前只考虑酒店模式，只产生一个活动，未来这块需要修改
				Map<String,Object> params= new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				params.put("auditType", OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode());
				params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
				List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
				if(auditList.isEmpty()){
					result.setMsg("活动更新失败");
					return result;
				}
				for(ComAudit audit : auditList){
					//start 2018-07-31 更新优化  避免出现脏读
					ComAudit updateComAudit = new ComAudit();
					updateComAudit.setAuditId(audit.getAuditId());
					if("SYSTEM".equals(assignor) && !StringUtils.isEmpty(audit.getOperatorName())){
						updateComAudit.setOperatorName(audit.getOperatorName());
					}else{
						updateComAudit.setOperatorName(assignor);
					}
					updateComAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateComAudit.setUpdateTime(new Date());
					orderAuditService.updateComAuditByCondition(updateComAudit);
					//end
				}
				ComAudit audit = auditList.get(0);
				result.setReturnContent(audit);
			}

			insertOrderLog(orderId, OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString());
		}
		return result;
	}
	
	/**
	 * 更新子订单资源审核状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildResourceStatus(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo,boolean ifEBK){
		return updateChildResourceStatus(orderItemId, newStatus, resourceRetentionTime, assignor, memo, null, ifEBK);
	}
	
	/**
	 * 更新子订单资源审核状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildResourceStatus(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo, String supplierName, boolean ifEBK){
		Log.info("OrderStatusManageServiceImpl.updateChildResourceStatus : orderItemId="+orderItemId+",resourceRetentionTime = " + resourceRetentionTime + ", supplierName = " + supplierName);
		// TODO Auto-generated method stub
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId); 
		Long orderId=orderItem.getOrderId();
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>(); 
		if(!OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(orderItem.getResourceStatus())){ 
			result.setMsg("子订单当前状态不可以更改为:"+OrderEnum.RESOURCE_STATUS.getCnName(newStatus)); 
			return result; 
		}
		Map<String,Object> paraMap = new HashMap<String, Object>();
		paraMap.put("objectId",orderItemId);
		paraMap.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
		paraMap.put("auditType", OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode());
		paraMap.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> audits = orderAuditService.queryAuditListByParam(paraMap);
		//对接机票，如果还没有生成资源审核活动，直接处理失败
		if(orderItem.isApiFlightTicket() && audits.isEmpty()){
			Log.info("未找到尚未处理的资源审核活动");
			result.setMsg("未找到尚未处理的资源审核活动");
			return result;
		}
		
		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		
		OrdOrderItem ordOrderItemObj=new OrdOrderItem();
		ordOrderItemObj.setOrderItemId(orderItemId);
		ordOrderItemObj.setResourceStatus(newStatus);
		ordOrderItemObj.setOrderMemo(memo);
		ordOrderItemObj.setResourceAmpleTime(new Date());
		
		ordOrderItemObj.setContent(orderItem.getContent());
		ordOrderItemObj.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
		
		//国内对接机票，如一次锁仓成功，修改为无需审核
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&orderItem.isApiFlightTicket()){
			Map<String,Object> paraMap1 = new HashMap<String, Object>();
			paraMap1.put("objectId",orderItemId);
			paraMap1.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
			paraMap1.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.getCode());
			paraMap1.put("auditSubtype", OrderEnum.AUDIT_SUB_TYPE.FLIGHT_LOCKSEAT_FAIL.getCode());
			List<ComAudit> audit = orderAuditService.queryAuditListByParam(paraMap1);
			if(null==audit||audit.size()<1){
				ordOrderItemObj.setNeedResourceConfirm("false");
			}
		}
		
		int m=ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItemObj);
		
		if(m<=0) {
			Log.info("订单子项更新失败");
			result.setMsg("订单子项更新失败");
			throw new BusinessException("订单子项更新失败");
		}else{
			String operatorName =assignor;
			if((!OrdOrderUtils.isDestBuFrontOrderNew(order) && !OrdOrderUtils.isDestBuFrontOrderNew_O2O(order))
					||(OrdOrderUtils.isBusHotelOrder(order))){
				Map<String,Object> params= new HashMap<String, Object>();
				params.put("objectId",ordOrderItemObj.getOrderItemId());
				params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
				params.put("auditType", OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode());
				params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
				List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
				if(auditList.isEmpty()){
					Log.info("活动更新失败");
					result.setMsg("活动更新失败");
					return result;
				}
				for(ComAudit audit : auditList){
					//start 2018-07-31 更新优化  避免出现脏读
					ComAudit updateComAudit = new ComAudit();
					updateComAudit.setAuditId(audit.getAuditId());
					if("SYSTEM".equals(assignor) && !StringUtils.isEmpty(audit.getOperatorName())){ //系统自动过不更新处理人
						// do nothing
					}else if(ifEBK){ //EBK不更新处理人
						// do nothing
					}else{
						updateComAudit.setOperatorName(assignor);
					}
					updateComAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					updateComAudit.setUpdateTime(new Date());
					orderAuditService.updateComAuditByCondition(updateComAudit);
					//end
				}
				ComAudit audit = auditList.get(0);
				result.setReturnContent(audit);
				operatorName =audit.getOperatorName();
			}
			//更新主订单资源状态逻辑
			Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
			paramsOrderItem.put("orderId",orderId);
			
			int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			paramsOrderItem.put("resourceStatus", OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
			
			int ordItemResAmpleNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			Date oldWaitPaymentTime=order.getWaitPaymentTime();
			StringBuffer appendMessage = new StringBuffer(",支付等待值变更-("+resourceRetentionTime+")");

			if(order != null && order.getOrderId() != null){
				List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
				if(CollectionUtils.isNotEmpty(ordOrderItemList)){
					order.setOrderItemList(ordOrderItemList);
				}
			}
			if (ordItemAllNum==ordItemResAmpleNum) {
				
				//定义资源审核时间,因为团结算的支付等待时间要设定为资源审核时间+24
				Date resourceAmpleTime=new Date();
				if(ordItemAllNum==ordItemResAmpleNum){
					order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
				}
				if(order.hasNeedPrepaid()&&!order.isPayMentType()){
					Date date = new Date();
					orderUpdateService.setOrderWatiPaymentTime(order, date, true);
					
					List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
					orderItemList.add(orderItem);
					if(StringUtils.isEmpty(resourceRetentionTime)){
						
						//团结算标识是"Y"的订单不设置支付等待时间
						logger.info("=============orderIdAndGroupSettleFlag========"+order.getOrderId()+" "+order.getGroupSettleFlag());
						if(StringUtil.isEmptyString(order.getGroupSettleFlag())||!"Y".equals(order.getGroupSettleFlag())){
							
							/**
							 * 门票类支付等待时间计算  2015-05-15
							 */
							this.calPaymentWaitTime4Mp(order,orderItemList);
	
							//国内 跟团游 对接 支付等待时间为60分钟
							if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(orderItem.getBuCode()) && BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==orderItem.getCategoryId().longValue() && "Y".equals(orderItem.getContentValueByKey("supplierApiFlag"))){
								order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 50));
								logger.info("===orderId:"+order.getOrderId()+" set waitTimeout");
							}
	
							// O2O景+酒支付时间为半小时 add by renjiangyi
							if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
								order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 30));//等待时间
							}
							String code=order.getDistributorCode();
							if(order.getDistributorId()==4&&(code.equals("DISTRIBUTOR_TAOBAO")||code.equals("DISTRIBUTOR_API")||code.equals("DISTRIBUTOR_B2B")||code.equals("DISTRIBUTOR_DAOMA")||code.equals("DISTRIBUTOR_YUYUE")||code.equals("DISTRIBUTOR_SG"))){
								order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 120));//等待时间
							}
							order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(), order.getWaitPaymentTime()));
						}else{
							//团结算订单 默认支付等待时间=资源审核日期+24h
							order.setWaitPaymentTime(DateUtil.DsDay_Minute(resourceAmpleTime, 24*60));
						}
					}else{
						order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(),DateUtil.toDate(resourceRetentionTime, "yyyy-MM-dd HH:mm")));
					}

					appendMessage.append(" 新值:").append(DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
					order.setApproveTime(date);
				}else{
					appendMessage.append(" 无变更 hasNeedPrepaid:").append(order.hasNeedPrepaid()).append(" isPayMentType:").append(order.isPayMentType());
				}
				order.setResourceAmpleTime(resourceAmpleTime);
				orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
				logger.info("===orderId:"+order.getOrderId()+",orderItem.isApiFlightTicket():"+orderItem.isApiFlightTicket()+",====");
				if(orderItem.isApiFlightTicket()){
					orderService.setResouceKeepTime(order, orderItem);
				}
                //start 2018-07-31 订单更新优化  避免出现脏读
                OrdOrder updateOrder = new OrdOrder();
                updateOrder.setOrderId(order.getOrderId());
                updateOrder.setOrderItemList(order.getOrderItemList());
                updateOrder.setResourceStatus(order.getResourceStatus());
                updateOrder.setWaitPaymentTime(order.getWaitPaymentTime());
                updateOrder.setApproveTime(order.getApproveTime());
                updateOrder.setResourceAmpleTime(order.getResourceAmpleTime());
				int n=ordOrderDao.updateByPrimaryKeySelective(updateOrder);
				//end
				if (n!=1) {
					throw new BusinessException("主订单资源状态更新失败");
				}

			}else{
				appendMessage.append(" 无变更<不满足变更条件>");
			}
			
			if (result.isSuccess()) {
				if(ifEBK){
					if(StringUtils.isNotEmpty(supplierName)) {
						memo += "供应商名称 : " + supplierName;
					}
					insertEBKChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString(),operatorName);
				}else{
					if(!StringUtils.isEmpty(resourceRetentionTime)&&order.hasNeedPrepaid()&&!order.isPayMentType()){
						insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString());

						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
								orderId,
								orderItemId,
								assignor,
								"将编号为["+orderItemId+"]的子订单，更新支付等待时间",
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新支付等待时间",
								"原有支付等待时间："+DateUtil.formatDate(oldWaitPaymentTime, "yyyy-MM-dd HH:mm")+"，新的支付等待时间："+DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm"));
					}else{
						insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString());
					}
				}
			}
		}
		return result;
	
		
	}
	
	/**
	 * 更新子订单资源审核状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateFlightOrderResourcePass(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo, String supplierName, boolean ifEBK){
		Log.info("OrderStatusManageServiceImpl.executeFlightOrderResourcePass : orderItemId="+orderItemId+",resourceRetentionTime = " + resourceRetentionTime + ", supplierName = " + supplierName);
		// TODO Auto-generated method stub
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId); 
		Long orderId=orderItem.getOrderId();
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>(); 
		if(!OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(orderItem.getResourceStatus())){ 
			result.setMsg("子订单当前状态不可以更改为:"+OrderEnum.RESOURCE_STATUS.getCnName(newStatus)); 
			return result; 
		}
		
		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		
		OrdOrderItem ordOrderItemObj=new OrdOrderItem();
		ordOrderItemObj.setOrderItemId(orderItemId);
		ordOrderItemObj.setResourceStatus(newStatus);
		ordOrderItemObj.setOrderMemo(memo);
		ordOrderItemObj.setResourceAmpleTime(new Date());
		
		ordOrderItemObj.setContent(orderItem.getContent());
		ordOrderItemObj.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
		
		//国内对接机票，如一次锁仓成功，修改为无需审核
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&orderItem.isApiFlightTicket()){
			Map<String,Object> paraMap1 = new HashMap<String, Object>();
			paraMap1.put("objectId",orderItemId);
			paraMap1.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
			paraMap1.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.getCode());
			paraMap1.put("auditSubtype", OrderEnum.AUDIT_SUB_TYPE.FLIGHT_LOCKSEAT_FAIL.getCode());
			List<ComAudit> audit = orderAuditService.queryAuditListByParam(paraMap1);
			if(null==audit||audit.size()<1){
				ordOrderItemObj.setNeedResourceConfirm("false");
			}
		}
		
		int m=ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItemObj);
		
		if(m<=0) {
			Log.info("订单子项更新失败");
			result.setMsg("订单子项更新失败");
			throw new BusinessException("订单子项更新失败");
		}else{
			String operatorName ="SYSTEM";
			//更新主订单资源状态逻辑
			Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
			paramsOrderItem.put("orderId",orderId);
			
			int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			paramsOrderItem.put("resourceStatus", OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
			
			int ordItemResAmpleNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			Date oldWaitPaymentTime=order.getWaitPaymentTime();
			StringBuffer appendMessage = new StringBuffer(",支付等待值变更-("+resourceRetentionTime+")");
			

			if(order != null && order.getOrderId() != null){
				List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
				if(CollectionUtils.isNotEmpty(ordOrderItemList)){
					order.setOrderItemList(ordOrderItemList);
				}
			}
			if (ordItemAllNum==ordItemResAmpleNum) {
				if(ordItemAllNum==ordItemResAmpleNum){
					order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
				}
				if(order.hasNeedPrepaid()&&!order.isPayMentType()){
					Date date = new Date();
					orderUpdateService.setOrderWatiPaymentTime(order, date, true);
					
					List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
					orderItemList.add(orderItem);
					if(StringUtils.isEmpty(resourceRetentionTime)){
						/**
						 * 门票类支付等待时间计算  2015-05-15
						 */
						this.calPaymentWaitTime4Mp(order,orderItemList);

						//国内 跟团游 对接 支付等待时间为60分钟
						if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(orderItem.getBuCode()) && BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==orderItem.getCategoryId().longValue() && "Y".equals(orderItem.getContentValueByKey("supplierApiFlag"))){
							order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 50));
							logger.info("===orderId:"+order.getOrderId()+" set waitTimeout");
						}

						// O2O景+酒支付时间为半小时 add by renjiangyi
						if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
							order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 30));//等待时间
						}
						String code=order.getDistributorCode();
						if(order.getDistributorId()==4&&(code.equals("DISTRIBUTOR_TAOBAO")||code.equals("DISTRIBUTOR_API")||code.equals("DISTRIBUTOR_B2B")||code.equals("DISTRIBUTOR_DAOMA")||code.equals("DISTRIBUTOR_YUYUE")||code.equals("DISTRIBUTOR_SG"))){
							order.setWaitPaymentTime(DateUtil.DsDay_Minute(new Date(), 120));//等待时间
						}
						order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(), order.getWaitPaymentTime()));
					}else{
						order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(),DateUtil.toDate(resourceRetentionTime, "yyyy-MM-dd HH:mm")));
					}

					appendMessage.append(" 新值:").append(DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
					order.setApproveTime(date);
				}else{
					appendMessage.append(" 无变更 hasNeedPrepaid:").append(order.hasNeedPrepaid()).append(" isPayMentType:").append(order.isPayMentType());
				}
				order.setResourceAmpleTime(new Date());
				orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
				logger.info("===orderId:"+order.getOrderId()+",orderItem.isApiFlightTicket():"+orderItem.isApiFlightTicket()+",====");
				if(orderItem.isApiFlightTicket()){
					orderService.setResouceKeepTime(order, orderItem);
				}
				int n=ordOrderDao.updateByPrimaryKey(order);
				if (n!=1) {
					throw new BusinessException("主订单资源状态更新失败");
				}

			}else{
				appendMessage.append(" 无变更<不满足变更条件>");
			}
			
			if (result.isSuccess()) {
				if(ifEBK){
					if(StringUtils.isNotEmpty(supplierName)) {
						memo += "供应商名称 : " + supplierName;
					}
					insertEBKChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString(),operatorName);
				}else{
					if(!StringUtils.isEmpty(resourceRetentionTime)&&order.hasNeedPrepaid()&&!order.isPayMentType()){
						insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString());

						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
								orderId,
								orderItemId,
								assignor,
								"将编号为["+orderItemId+"]的子订单，更新支付等待时间",
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
								ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新支付等待时间",
								"原有支付等待时间："+DateUtil.formatDate(oldWaitPaymentTime, "yyyy-MM-dd HH:mm")+"，新的支付等待时间："+DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm"));
					}else{
						insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), assignor,memo,appendMessage.toString());
					}
				}
			}
		}
		return result;
	
		
	}
	/**
	 * 补偿更新子订单资源审核状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> compensateUpdateChildResourceStatus(Long orderId, String newStatus, String resourceRetentionTime, String assignor, String memo, boolean ifEBK) {
		// 更新主订单资源状态逻辑
		Map<String, Object> paramsOrderItem = new HashMap<String, Object>();
		paramsOrderItem.put("orderId", orderId);
		int ordItemAllNum = ordOrderItemDao.getTotalCount(paramsOrderItem);
		paramsOrderItem.put("resourceStatus", OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
		int ordItemResAmpleNum = ordOrderItemDao.getTotalCount(paramsOrderItem);
		
		StringBuffer appendMessage = new StringBuffer(",支付等待值变更-(" + resourceRetentionTime + ")");

		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		if (ordItemAllNum == ordItemResAmpleNum) {
			if (ordItemAllNum == ordItemResAmpleNum) {
				order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
			}
			if (order.hasNeedPrepaid() && !order.isPayMentType()) {
				Date date = new Date();
				orderUpdateService.setOrderWatiPaymentTime(order, date, true);
				/**
				 * 门票类支付等待时间计算 2015-05-15
				 */
				List<OrdOrderItem> orderItemList = ordOrderItemDao.selectByOrderId(orderId);
				this.calPaymentWaitTime4Mp(order, orderItemList);

				order.setWaitPaymentTime(this.getMinDate(orderId, order.getLastCancelTime(), order.getWaitPaymentTime()));
				appendMessage.append(" 新值:").append(DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm:ss"));
				order.setApproveTime(date);
			} else {
				appendMessage.append(" 无变更 hasNeedPrepaid:").append(order.hasNeedPrepaid()).append(" isPayMentType:").append(order.isPayMentType());
			}
			order.setResourceAmpleTime(new Date());
			int n = ordOrderDao.updateByPrimaryKey(order);
			if (n != 1) {
				throw new BusinessException("主订单资源状态更新失败");
			}
		} else {
			appendMessage.append(" 无变更<不满足变更条件>");
		}
		return new ResultHandleT<ComAudit>();
	}

	/**
	 * @Discribe： 计算门票支付等待时间
	 * 	原因：当日门票的支付时间在资源审核的时候被覆盖，导致等待支付时间错误
	 * @user ZM
	 * @date 2015年5月15日下午4:13:12	
	 * @param order void
	 */
	public void calPaymentWaitTime4Mp(OrdOrder order,List<OrdOrderItem> orderItemList){
		if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()==order.getCategoryId()
                ||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()==order.getCategoryId()
                ||BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()==order.getCategoryId()
                ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()==order.getCategoryId()){
			if(order.getWaitPaymentTime()!=null){
			// 门票品类支付等待时间业务变更 20141205 start 
			logger.info("门票品类支付等待时间业务变更 20141205 orderId = "+order.getOrderId() +"start");
			List<Integer> minutes = new ArrayList<Integer>();
			//是否是门票品类
			boolean isTicketFlag = false; 
			//默认设为通用的支付等待时间
			Date newWaitPaymentTime = order.getWaitPaymentTime();
			logger.info("处理前支付等待时间是"+ newWaitPaymentTime );
			if(orderItemList!=null){
				for(OrdOrderItem orderItem : orderItemList){
					if(orderItem.hasTicketAperiodic()){
						continue;
					}
					//品类code
					String categoryCode = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					//门票品类
					logger.info("品类code是"+ categoryCode);
					// 景区票、其它票、组合套餐
					if(ProductCategoryUtil.isTicket(categoryCode)){
						//取得出游日期对应的时间价格数据
						ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = 
								suppGoodsTimePriceClientRemote.getBaseTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
						SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
						//取得出游日期对应的时间价格数据
						if(timePrice != null ) {
							//时间价格表中提前预定时间
							minutes.add(timePrice.getAheadBookTime().intValue());
							logger.info("时间价格表中提前预定时间是"+ timePrice.getAheadBookTime().intValue());
						}
						isTicketFlag = true;
					}
					logger.info("门票品类判断结果"+ isTicketFlag);
				}
			}
			if(isTicketFlag){
				logger.info("门票品类判断结果"+ isTicketFlag);
					//下单时间到出游日的剩余时间
					Long leftTime =(long)(DateUtil.getMinute(order.getCreateTime(),order.getVisitTime()));
					//品类中最大提前预定时间和下单时间之差
					Long waitMinute = leftTime - Collections.max(minutes);
					logger.info("提前预定时间和下单时间之差是"+ waitMinute );
					//提前预定时间和下单时间之差大于0小于120分钟的时候
					//if(waitMinute < 120 && waitMinute >0){

					 //设置新的支付等待时间
					 newWaitPaymentTime = DateUtils.addMinutes(order.getCreateTime(), waitMinute.intValue());
					 //
					if(newWaitPaymentTime.before(order.getWaitPaymentTime())){
						order.setWaitPaymentTime(newWaitPaymentTime);
					}
					
					 logger.info("处理后门票的支付等待时间是"+ newWaitPaymentTime);
					//}
				}
			 logger.info("处理后的支付等待时间是"+ newWaitPaymentTime);
			 //门票品类支付等待时间业务变更 20141205 end
			 logger.info("门票品类支付等待时间业务变更 20141205 end");
		}
		}
	}

	@Override
	public ResultHandleT<ComAudit> updateInfoStatus(OrdOrder order, String newStatus,String assignor,String memo) {
		// TODO Auto-generated method stub
		
		//供应商订单或者订单来源：驴妈妈后台      两种情况都默认信息审核通过，没有产生活动，无需更新活动，需要产生活动的地方，产生的时候直接活动状态就是已通过
		boolean isSupplierOrBack=false;
		if (order.isSupplierOrder()) {
			isSupplierOrBack=true;
		}
		if (Constants.DISTRIBUTOR_2.equals(order.getDistributorId())) {
			isSupplierOrBack=true;
		}
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		OrdOrder originOrder = ordOrderDao.selectByPrimaryKey(order.getOrderId()); 
		//修复订单支付状态被误修改bug
		OrdOrder ordOrder = new OrdOrder();
		ordOrder.setOrderId(originOrder.getOrderId());
		ordOrder.setInfoStatus(newStatus);
		ordOrder.setInfoPassTime(new Date());
		if (!isSupplierOrBack) {
			ordOrder.setOrderMemo(memo);
		}
		
		/*if(originOrder.hasNeedPrepaid()&&!originOrder.isPayMentType()){
			Date date = new Date();
			orderUpdateService.setOrderWatiPaymentTime(originOrder, date, true);
			originOrder.setWaitPaymentTime(this.getMinDate(originOrder.getOrderId(), originOrder.getLastCancelTime(), originOrder.getWaitPaymentTime()));
			originOrder.setApproveTime(date);
		}*/
		
		int n=ordOrderDao.updateByPrimaryKeySelective(ordOrder);

		if (n!=1 ) {
			result.setMsg("订单更新失败");
		}
		if (result.isSuccess()) {
			OrdOrderItem ordOrderItemObj=new OrdOrderItem();
			ordOrderItemObj.setOrderId(order.getOrderId());
			ordOrderItemObj.setInfoStatus(newStatus);
			ordOrderItemObj.setInfoPassTime(new Date());
			int m=ordOrderItemDao.updateByOrderIdSelective(ordOrderItemObj);
			if(m<=0) {
				result.setMsg("订单子项更新失败");
			}
		}
		if (result.isSuccess()) {
			//无需更新活动
			if(OrdOrderUtils.isDestBuFrontOrderNew(order)
					|| OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
				isSupplierOrBack =true;
			}
			if (!isSupplierOrBack) {
				Map<String,Object> params= new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				params.put("auditType", OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode());
				params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
				List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
				//int k=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor);
				if(auditList.isEmpty()){
					result.setMsg("活动更新失败");
					return result;
				}
				for(ComAudit audit : auditList){
					if("SYSTEM".equals(assignor) && !StringUtils.isEmpty(audit.getOperatorName())){
						audit.setOperatorName(audit.getOperatorName());
					}else{
						audit.setOperatorName(assignor);
					}
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					audit.setUpdateTime(new Date());
					orderAuditService.updateByPrimaryKey(audit);
				}
				ComAudit audit = auditList.get(0);
				result.setReturnContent(audit);
			}
		}
		if (result.isSuccess()) {
			insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor,memo);
		}
		return result;
	}

	@Override
	public ResultHandleT<ComAudit> updateChildInfoStatus(OrdOrderItem orderItem, String newStatus,String assignor,String memo) {
		// TODO Auto-generated method stub
		
		//供应商订单或者订单来源：驴妈妈后台      两种情况都默认信息审核通过，没有产生活动，无需更新活动，需要产生活动的地方，产生的时候直接活动状态就是已通过
		
		
		OrdOrder order=ordOrderDao.selectByPrimaryKey(orderItem.getOrderId());
		

		
//		ResultHandleT<BizCategory> resultCategory=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode());
//		BizCategory bizCategory=resultCategory.getReturnContent();
		
		
		boolean isBackOrder=false;
		/*if (Constants.DISTRIBUTOR_2.equals(order.getDistributorId()) && bizCategory.getCategoryId().equals(orderItem.getCategoryId())  ) {//后台下单并且是酒店
			//isBackOrder=true;
		}*/
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		OrdOrderItem orderItemObj=new OrdOrderItem();
		orderItemObj.setOrderItemId(orderItem.getOrderItemId());
		orderItemObj.setInfoStatus(newStatus);
		orderItemObj.setOrderMemo(memo);
		orderItemObj.setInfoPassTime(new Date());
		
		int n=ordOrderItemDao.updateByPrimaryKeySelective(orderItemObj);

		if (n!=1 ) {
			result.setMsg("子订单更新失败");
			throw new BusinessException("子订单更新失败");
		}
		
		if (result.isSuccess()) {
			//无需更新活动
			if(OrdOrderUtils.isDestBuFrontOrderNew(order)
					|| OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
				isBackOrder =true;
			}
			//其他巴士走国内流程
			if(OrdOrderUtils.isBusHotelOrder(order)){
				isBackOrder=false;
			}
			if (!isBackOrder) {
				Map<String,Object> params= new HashMap<String, Object>();
				params.put("objectId", orderItem.getOrderItemId());
				params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
				params.put("auditType", OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode());
//				params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
				List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
				//int k=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor);
				if(auditList.isEmpty()){
					
					result.setMsg("活动更新失败");
					throw new BusinessException("子订单更新失败");
				}
				for(ComAudit audit : auditList){
					if("SYSTEM".equals(assignor) && !StringUtils.isEmpty(audit.getOperatorName())){
						audit.setOperatorName(audit.getOperatorName());
					}else{
						audit.setOperatorName(assignor);
					}
					audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
					audit.setUpdateTime(new Date());
					orderAuditService.updateByPrimaryKey(audit);
				}
				ComAudit audit = auditList.get(0);
				result.setReturnContent(audit);
			}
			
			
			//更新主订单信息状态逻辑
			Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
			paramsOrderItem.put("orderId",orderItem.getOrderId());
			
			int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			paramsOrderItem.put("infoStatus", OrderEnum.INFO_STATUS.INFOPASS.getCode());
			
			int ordItemInfoPassNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			if (ordItemAllNum==ordItemInfoPassNum) {
				OrdOrder ordOrder = new OrdOrder();
				ordOrder.setInfoStatus(OrderEnum.INFO_STATUS.INFOPASS.getCode());
				/*if(order.hasNeedPrepaid()&&!order.isPayMentType()){
					if(order.hasResourceAmple()&&(order.getWaitPaymentTime()==null||order.getWaitPaymentTime().before(new Date()))){
						Date date = new Date();
						orderUpdateService.setOrderWatiPaymentTime(order, date, true);
						order.setWaitPaymentTime(this.getMinDate(order.getOrderId(), order.getLastCancelTime(), order.getWaitPaymentTime()));
						order.setApproveTime(date);
					}
				}*/
				ordOrder.setInfoPassTime(new Date());
				ordOrder.setOrderId(order.getOrderId());
				int m=ordOrderDao.updateByPrimaryKeySelective(ordOrder);
				if (m!=1) {
					throw new BusinessException("主订单信息状态更新失败");
				}
			}
		}
		if (result.isSuccess()) {
			insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor,memo);
		}
		return result;
	}
	
	@Override
	public ResultHandleT<ComAudit> updatePretrialAudit(OrdOrder order, String operator, String remark){
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId", order.getOrderId());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		params.put("auditType", OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.getCode());
		params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
		if(auditList.isEmpty()){
			result.setMsg("活动更新失败");
			return result;
		}
		for(ComAudit audit : auditList){
			audit.setOperatorName(operator);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			audit.setUpdateTime(new Date());
			orderAuditService.updateByPrimaryKey(audit);
		}
		ComAudit audit = auditList.get(0);
		result.setReturnContent(audit);
		this.insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.getCode(), operator,remark);
		
		return result;
	}

	@Override
	public ResultHandleT<ComAudit> updateCertificateStatus(OrdOrder order,OrderAttachment orderAttachment,String assignor,String memo) {
		// TODO Auto-generated method stub
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		
		Long fileId=orderAttachment.getFileId();
		if (fileId!=null) {
			orderAttachmentService.saveOrderAttachment(orderAttachment,assignor,"凭证确认附件");
		}
		
		//活动更新，日志生成
		//int n=orderAuditService.updateComAuditToProcessed(orderAttachment.getOrderId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), assignor);
		if(!OrdOrderUtils.isDestBuFrontOrderNew(order) && !OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
			Map<String,Object> params= new HashMap<String, Object>();
			params.put("objectId", order.getOrderId());
			params.put("auditType", OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode());
			params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
			//int k=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor);
			if(auditList.isEmpty()){
				result.setMsg("活动更新失败");
				return result;
			}
			for(ComAudit audit : auditList){
				if("SYSTEM".equals(assignor) && !StringUtils.isEmpty(audit.getOperatorName())){ //系统自动过不更新处理人
					// do nothing
				}else{
					audit.setOperatorName(assignor);
				}
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
				audit.setUpdateTime(new Date());
				orderAuditService.updateByPrimaryKey(audit);
			}
			ComAudit audit = auditList.get(0);
			result.setReturnContent(audit);
		}
		//订单子项凭证确认状态
		if (order.getOrderItemList() != null) {
			OrdOrderItem updateItem = null;
			for (OrdOrderItem item : order.getOrderItemList()) {
				if (item !=  null) {
					updateItem = new OrdOrderItem();
					updateItem.setOrderItemId(item.getOrderItemId());
					updateItem.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.CONFIRMED.name());
					ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
				}
			}
		}
		//订单凭证确认状态
		OrdOrder updateOrder = new OrdOrder();
		updateOrder.setOrderId(order.getOrderId());
		updateOrder.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name());
		updateOrder.setOrderMemo(memo);
		
		ordOrderDao.updateByPrimaryKeySelective(updateOrder);
		this.insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), assignor,memo);
		
		try {
			//机酒自由行产品，在资审通过、凭证确认且支付成功后发送防诈骗短信
			if(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
					&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())
					&&CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
					&&order.hasPayed()&&order.hasResourceAmple()&&!OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode().equals(order.getCertConfirmStatus())){
				orderSendSmsService.sendPreventCheatSms(order.getOrderId());
			}
		} catch (Exception e) {
			logger.error("发送防诈骗短信失败， msg = " + e.getMessage());
		}
		
		return result;
	}

	/**
	 * 更新子订单履行状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildCertificateStatus(OrdOrderItem orderItem,OrderAttachment orderAttachment,String assignor,String memo){
		

		// TODO Auto-generated method stub
		
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		
		Long fileId=orderAttachment.getFileId();
		if (fileId!=null) {
			orderAttachmentService.saveOrderAttachment(orderAttachment,assignor,"凭证确认附件");
		}
		
		//订单子项凭证确认状态
		OrdOrderItem updateItem = new OrdOrderItem();
		updateItem.setOrderItemId(orderItem.getOrderItemId());
		updateItem.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.CONFIRMED.name());
		ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
		
		

		//更新主订单凭证确认状态
		Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
		paramsOrderItem.put("orderId",orderItem.getOrderId());
		
//		int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
		
		List<OrdOrderItem> orderItemList = ordOrderItemDao.selectByOrderId(orderItem.getOrderId());
		String orderCertConfirmStatus = OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name();
		//只考虑需要凭证的子订单
		if(CollectionUtils.isNotEmpty(orderItemList)) {
			for(OrdOrderItem ordItem : orderItemList) {
				if(ordItem.isNeedCertificate() && !OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(ordItem.getCertConfirmStatus())) {
					orderCertConfirmStatus =  OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name();
				}
			}
		}
		
//		paramsOrderItem.put("certConfirmStatus", OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode());
//		
//		int ordItemCertConfirmedNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
		
		if (OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(orderCertConfirmStatus)) {
			
			OrdOrder currOrder = ordOrderDao.selectByPrimaryKey(orderItem.getOrderId()); 
			OrdOrder orderObj = new OrdOrder();
			orderObj.setOrderId(orderItem.getOrderId());
			orderObj.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode());
			String orderMemo = "子订单凭证确认状态审核全部通过，更新主订单凭证确认状态通过";
			orderObj.setOrderMemo(StringUtils.isBlank(currOrder.getOrderMemo()) ? orderMemo : currOrder.getOrderMemo() + "\r\n" + orderMemo);
			int n = ordOrderDao.updateByPrimaryKeySelective(orderObj);
			
			//更新主订单的凭证确认状态
			if(n != 0) {
				try {
					//机酒自由行产品，在资审通过、凭证确认且支付成功后发送防诈骗短信
					if(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == currOrder.getCategoryId()
							&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(currOrder.getSubCategoryId())
							&&CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(currOrder.getBuCode())
							&&currOrder.hasPayed()&&currOrder.hasResourceAmple()&&!OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode().equals(currOrder.getCertConfirmStatus())){
						orderSendSmsService.sendPreventCheatSms(currOrder.getOrderId());
					}
				} catch (Exception e1) {
					logger.error("发送防诈骗短信失败， msg = " + e1.getMessage());
				}
				try {
					//品类为线路时发送合同
					sendOrdTravelContractEmail(currOrder.getOrderId(), assignor);
				} catch (Exception e) {
					logger.error("更新主订单凭证确认状态时发送合同失败， msg = " + e.getMessage());
				}
			} else {
				logger.error("更新主订单的凭证确认状态失败");
			}
		}
				
		//活动更新，日志生成
		//int n=orderAuditService.updateComAuditToProcessed(orderAttachment.getOrderId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), assignor);
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId", orderItem.getOrderItemId());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
		params.put("auditType", OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode());
		params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
		//int k=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), assignor);
		if(auditList.isEmpty()){
			//result.setMsg("活动更新失败");
			logger.info("门票EBK订单无需创业凭证确认活动");
			return result;
		}
		for(ComAudit audit : auditList){
			audit.setOperatorName(assignor);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			audit.setUpdateTime(new Date());
			orderAuditService.updateByPrimaryKey(audit);
		}
		ComAudit audit = auditList.get(0);
		result.setReturnContent(audit);
		
		this.insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), assignor,memo);
		
		return result;
		
	}
	
	//针对线路品类
	private void sendOrdTravelContractEmail(Long orderId, String assignor) {
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		Long categoryId = order.getCategoryId();
		//非线路产品返回
		if(!isRouteProduct(categoryId)) {
			return;
		}
		// 发送合同
		List<OrdTravelContract> ordTravelContractList = order.getOrdTravelContractList();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		if(ordTravelContractList == null) {
			ordTravelContractList = ordTravelContractDAO.selectByParam(params);
		}
		if(ordTravelContractList != null) {
			for(OrdTravelContract ordTravelContract : ordTravelContractList) {
				if(!"Y".equalsIgnoreCase(ordTravelContract.getSendEmailFlag()) && !"Y".equals(order.getTravellerDelayFlag())) {
					ResultHandle resultHandle = teamOutboundTourismContractService.sendContractEmail(order, ordTravelContract.getOrdContractId(), assignor);
					if(resultHandle.isSuccess()) {
						//修改合同状态
						if(StringUtils.isEmpty(ordTravelContract.getStatus()) 
								|| ordTravelContract.getStatus().equals(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode())){
							ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.getCode());
							ordTravelContract.setSendEmailFlag("Y");
							ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract);
							try {
								orderTravelElectricContactService.insertOrderLog(orderId, ordTravelContract.getOrdContractId(), assignor, "修改订单【"+orderId+"】所有合同状态为:"+OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCnName(), "");
							} catch (Exception e) {
								logger.error("插入日志异常", e);
							}
						}
					}
				}
			}
		}
	}
	
	//非线路产品返回
	private boolean isRouteProduct(Long categoryId) {
		if(!(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId) 
				|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryId)
				|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)
				|| BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)
				|| BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(categoryId))) {
			return false;
		}
		return true;
	}
		
	public ResultHandle updatePaymentAudit(OrdOrder order,String assignor,String memo,String code)
	{
		ResultHandle result=new ResultHandle();
		
		int n=orderAuditService.updateComAuditToProcessed(order.getOrderId(), code, assignor);
		
		if ( n!=1) {
			result.setMsg("活动更新失败");
		}else{
			this.updateOrderMemo(order.getOrderId(), memo);
			this.insertOrderLog(order.getOrderId(), code, assignor,memo);
		}
		
		return result;
	}
	/**
	 * 更新订单通知出团活动，且记录日志
	 * @param order
	 * @return
	 */
	public ResultHandle updateNoticeRegimentAudit(OrdOrder order,String assignor,String memo){
		

		ResultHandle result=new ResultHandle();
		int n=orderAuditService.updateChildOrderAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.getCode(), assignor);
		
		if ( n!=1) {
			result.setMsg("活动更新失败");
		}else{
			
			/*OrdAdditionStatus ordAdditionStatus=new OrdAdditionStatus();
			ordAdditionStatus.setOrderId(order.getOrderId());//订单号
			ordAdditionStatus.setStatusType(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
			ordAdditionStatus.setStatus(status);
			ordAdditionStatusService.updateByPrimaryKeySelective(ordAdditionStatus);
			*/
			this.updateOrderMemo(order.getOrderId(), memo);
			this.insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.getCode(), assignor,memo);
			
		}
		
		return result;
	
		
		
		
	}
	
	/**
	 * 订单取消已确认
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateCancelConfim(OrdOrder order,String assignor,String memo){
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		
		//int n=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode(), assignor);
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId", order.getOrderId());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		params.put("auditType", OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode());
		params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
		if(auditList.isEmpty()){
			result.setMsg("活动更新失败");
			return result;
		}
		for(ComAudit audit : auditList){
			audit.setOperatorName(assignor);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			audit.setUpdateTime(new Date());
			orderAuditService.updateByPrimaryKey(audit);
		}
		ComAudit audit = auditList.get(0);
		result.setReturnContent(audit);
		
		//订单子项取消确认
		if (order.getOrderItemList() != null) {
			OrdOrderItem updateItem = null;
			for (OrdOrderItem item : order.getOrderItemList()) {
				if (item !=  null) {
					updateItem = new OrdOrderItem();
					updateItem.setOrderItemId(item.getOrderItemId());
					updateItem.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.CONFIRMED.name());
					ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
				}
			}
		}
		//订单取消凭证确认
		OrdOrder updateOrder = new OrdOrder();
		updateOrder.setOrderId(order.getOrderId());
		updateOrder.setCancelCertConfirmStatus(OrderEnum.CANCEL_CERTCONFIRM_STATUS.CONFIRMED.name());
		updateOrder.setOrderMemo(memo);
		
		ordOrderDao.updateByPrimaryKeySelective(updateOrder);
		
		insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode(), assignor,memo);

		
		//需要发送传真给供应商，传真确认后，需要update order表或者子表，目前需要等待传真功能完成后才能做
		
		
		
		return result;
	}
	
	public ResultHandleT<ComAudit> updateChildCancelConfim(OrdOrderItem orderItem,String assignor,String memo){
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		
		//int n=orderAuditService.updateComAuditToProcessed(order.getOrderId(), OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode(), assignor);
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId", orderItem.getOrderItemId());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
		params.put("auditType", OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode());
		params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
		if(auditList.isEmpty()){
			result.setMsg("活动更新失败");
			return result;
		}
		for(ComAudit audit : auditList){
			audit.setOperatorName(assignor);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			audit.setUpdateTime(new Date());
			orderAuditService.updateByPrimaryKey(audit);
		}
		ComAudit audit = auditList.get(0);
		result.setReturnContent(audit);
		
		
		//订单子项取消确认
		OrdOrderItem updateItem = new OrdOrderItem();
		updateItem.setOrderItemId(orderItem.getOrderItemId());
		updateItem.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.CONFIRMED.name());
		ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
		
		
		
		
		
		insertChildOrderLog(orderItem.getOrderId(),orderItem.getOrderItemId(), OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode(), assignor,memo);

		
		
		
		
		
		return result;
	}
	public ResultHandle saveOrderFaxRemark(Long orderId,String faxFlag,String faxRemark,String assignor,String memo){
		
		ResultHandle result=new ResultHandle();
		//Long orderId=NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		
		
		OrdOrderItem newOrderItem=new OrdOrderItem();
		newOrderItem.setOrderItemId(orderItem.getOrderItemId());
		newOrderItem.setContent(orderItem.getContent());
		
		newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxRemark);
		
		int n=ordOrderItemDao.updateByPrimaryKeySelective(newOrderItem);//保存传真备注
		
		if ("Y".equals(faxFlag)) {
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, 
					orderId, 
					assignor, 
					"将编号为["+orderId+"]的订单发送传真", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.getCnName()+"[发送传真]",
					memo);
		}else if("N".equals(faxFlag)){
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, 
					orderId, 
					assignor, 
					"将编号为["+orderId+"]的订单发送ebk", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName()+"[发送ebk]",
					memo);
		}
		

		if (n!=1) {
			result.setMsg("订单子项更新传真备注失败");
		}
		
		
		
		return result;
	}
	
	public ResultHandle manualSendOrderFax(OrdOrder order ,String toFax,String faxRemark,String assignor, String memo){
		
		ResultHandle result=new ResultHandle();
		Long orderId=order.getOrderId();
		OrdOrderItem orderItem=order.getMainOrderItem();
		String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
		String mailFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.mail_flag.name());

		OrdOrderItem newOrderItem=new OrdOrderItem();
		newOrderItem.setOrderItemId(orderItem.getOrderItemId());
		newOrderItem.setContent(orderItem.getContent());
		
		newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxRemark);
		newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.mail_remark.name(), faxRemark);

		int n=ordOrderItemDao.updateByPrimaryKeySelective(newOrderItem);//保存传真备注

		if (n!=1) {
			result.setMsg("订单子项更新传真备注失败");
			return result;
		}
		//String memo="";
		if ("Y".equals(faxFlag)) {

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, 
					orderId, 
					assignor, 
					"将编号为["+orderId+"]的订单发送传真【"+toFax+"】", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.getCnName()+"[发送传真]",
					memo);
		}else if("N".equals(faxFlag) && "Y".equals(mailFlag)){

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId,
					orderId,
					assignor,
					"将编号为["+orderId+"]的订单发送邮件",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_MAIL.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_MAIL.getCnName()+"[发送邮件]",
					memo);
		}else if("N".equals(faxFlag)){

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, 
					orderId, 
					assignor, 
					"将编号为["+orderId+"]的订单发送ebk", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName()+"[发送ebk]",
					memo);
		}
		return result;
		
	}

	/**
	 * 子订单详情页面人工发送传真
	 * @param orderId
	 * @param faxReark
	 * @param assignor
	 * @param memo
	 * @return
	 */
	public ResultHandle manualSendOrderItemFax(OrdOrderItem orderItem,String toFax,String faxRemark,String assignor, String memo){
		

		Long orderId=orderItem.getOrderId();
		Long orderItemId=orderItem.getOrderItemId();
		
		ResultHandle result=new ResultHandle();
		
		String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
		String mailFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.mail_flag.name());
		
		OrdOrderItem newOrderItem=new OrdOrderItem();
		newOrderItem.setOrderItemId(orderItem.getOrderItemId());
		newOrderItem.setContent(orderItem.getContent());
		
		newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxRemark);
		newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.mail_remark.name(), faxRemark);
		
		int n=ordOrderItemDao.updateByPrimaryKeySelective(newOrderItem);//保存传真备注
		if (n!=1) {
			result.setMsg("订单子项更新传真备注失败");
			return result;
		}
		//String memo="";
		if ("Y".equals(faxFlag)) {
			
			
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId, 
					orderItemId, 
					assignor, 
					"将子订单编号为["+orderItemId+"]的子订单发送传真【"+toFax+"】", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_FAX.getCnName()+"[发送传真]",
					memo);
		}else if("N".equals(faxFlag) && "Y".equals(mailFlag)){

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId,
					orderItemId,
					assignor,
					"将子订单编号为["+orderItemId+"]的子订单发送邮件",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_MAIL.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_MAIL.getCnName()+"[发送邮件]",
					memo);
		}else if("N".equals(faxFlag)){
			
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId, 
					orderItemId, 
					assignor, 
					"将子订单编号为["+orderItemId+"]的订单发送ebk", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName()+"[发送ebk]",
					memo);
		}
		//放到ebk去做。
		/*else{
			if((BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId()))
					&&"Y".equals(faxFlag)){
				ComMessage comMessage = new ComMessage(); 
				comMessage.setMessageContent("子订单号"+orderItem.getOrderItemId()+"，修改凭证备注，请进行跟踪确认."); 
				comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.name()); 
				orderLocalService.saveReservation(comMessage, OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER, orderItem.getOrderItemId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM, OrderEnum.AUDIT_TYPE.BOOKING_AUDIT);
			}
		}*/
		
		return result;
		
	
	}

	public ResultHandleT<ComAudit> updateOnlineRefundConfim(OrdOrder order,String assignor,String memo){
		ResultHandleT<ComAudit> result=new ResultHandleT<ComAudit>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", order.getOrderId());
		params.put("auditType", OrderEnum.AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		params.put("auditStatusArray", Arrays.asList("UNPROCESSED","POOL"));
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);

		if(auditList.isEmpty()){
			result.setMsg("活动更新失败");
			return result;
		}
		for(ComAudit audit : auditList){
			audit.setOperatorName(assignor);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			audit.setUpdateTime(new Date());
			orderAuditService.updateByPrimaryKey(audit);
		}
		ComAudit audit = auditList.get(0);
		result.setReturnContent(audit);
		
		
		insertOrderLog(order.getOrderId(), OrderEnum.AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode(), assignor,memo);
		
		return result;
	}
	
	/**
	 * 资源保留时间、最晚无损取消时间中最小的
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	public Date getMinDate(Long orderId, Date lastCancelTime){
		
		Date minDate=null;
		//最小资源保留时间
		Date minRetentionDate=null;
		boolean isFirstDate=true;
		List<OrdOrderItem> orderItemsList = orderUpdateService.queryOrderItemByOrderId(orderId);
		for (int i = 0; i < orderItemsList.size(); i++) {
			
			Map<String,Object> contentMap = orderItemsList.get(i).getContentMap();
			String resourceRetentionTime =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
			
			if (!StringUtils.isEmpty(resourceRetentionTime)) {
				Date retentionTime=DateUtil.toDate(resourceRetentionTime, "yyyy-MM-dd HH:mm:ss");
				if (isFirstDate) {
					minRetentionDate=retentionTime;
					isFirstDate=false;
				}else if(retentionTime.before(minRetentionDate)){
					minRetentionDate=retentionTime;
				}
			}
		}
		if (minRetentionDate!=null && lastCancelTime==null) {
			minDate=minRetentionDate;
		}else if (lastCancelTime!=null && minRetentionDate==null) {
			minDate=lastCancelTime;
		}else if (lastCancelTime!=null && minRetentionDate!=null) {
			
			if (lastCancelTime.before(minRetentionDate)) {
				minDate=lastCancelTime;
			}else{
				minDate=minRetentionDate;
			}
		}
		return minDate;
	
	}
	
	
	/**
	 * 资源保留时间、最晚无损取消时间、支付等待时间中最小的 
	 * 逻辑修改 2015-01-12当 资源审核时间不为空的情况下,取该时间为支付等待时间
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	public Date getMinDate(Long orderId, Date lastCancelTime, Date watiPaymentTime){
		//1.获取资源审核时间最小时间
		Date minDate=this.getMinDate(orderId, null);
		//2,在minDate不为空的情况下，返回即当前的值<子订单设置>
		if(minDate != null){
			return minDate;
		}
		//3,在资源审核时间为空，watiPaymentTime不为空则为watiPaymentTime
		if (watiPaymentTime != null && minDate == null ) {
			minDate=watiPaymentTime;
		}else if (watiPaymentTime!=null && minDate!=null ) {
			//4,在资源审核时间与watiPaymentTime都不为空的情况下，取最小值
			if (watiPaymentTime.before(minDate)) {
				minDate=watiPaymentTime;
			}
		}
		return minDate;
	}
	@Override
	public ResultHandleT<ComAudit> updateChildConfirmStatus(OrdOrderItem orderItem, CONFIRM_STATUS newStatus
		      ,String operator,String memo) throws Exception{
		ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
		//更新子订单确认状态
		OrdOrderItem orderItemObj=new OrdOrderItem();
		orderItemObj.setOrderItemId(orderItem.getOrderItemId());
		orderItemObj.setConfirmStatus(newStatus.name());
		orderItemObj.setOrderMemo(memo);
		int n=ordOrderItemDao.updateByPrimaryKeySelective(orderItemObj);
		if (n!=1 ) {
			result.setMsg("子订单更新失败");
			throw new BusinessException("orderItemId=" +orderItem.getOrderItemId() +",子订单更新失败");
		}
		//更新子订单确认活动
		result =updateChildConfirmAudit(orderItemObj, operator, memo);
		//插入日志
		if (result.isSuccess()) {
			insertChildConfirmStatusLog(orderItem.getOrderId(),orderItem.getOrderItemId()
					, newStatus.name(), operator,memo);
		}else{
			result.setMsg("子订单活动更新失败");
			throw new BusinessException("orderItemId=" +orderItem.getOrderItemId() 
					+",子订单活动[" +newStatus.name() +"]更新失败");
		}
		return result;
	}
	/**
	 * 更新子订单确认活动
	 * @param orderItem
	 * @param operator
	 * @param memo
	 * @return
	 */
	private ResultHandleT<ComAudit> updateChildConfirmAudit(OrdOrderItem orderItem
			,String operator,String memo) {
		ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
		//更新子订单确认活动
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId", orderItem.getOrderItemId());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		params.put("auditStatusArray", Arrays.asList(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()
				,OrderEnum.AUDIT_STATUS.POOL.name()));
		List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
		if(!auditList.isEmpty()){
			for(ComAudit audit : auditList){
				//过滤预定通知,避免获取非新版流程节点活动,造成卡单
				if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())){
					continue;
				}
				if("SYSTEM".equals(operator) && !StringUtils.isEmpty(audit.getOperatorName())){
					audit.setOperatorName(audit.getOperatorName());
				}else{
					audit.setOperatorName(operator);
				}
				audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
				audit.setUpdateTime(new Date());
				orderAuditService.updateByPrimaryKey(audit);
			}
			result.setReturnContent(auditList.get(0));
		}
		//插入日志
		if (result.isSuccess()
				&& result.getReturnContent() !=null) {
			insertChildConfirmAuditTypeLog(orderItem.getOrderId(),orderItem.getOrderItemId()
					, result.getReturnContent().getAuditType(), operator,memo);
		}
		return result;
	}
	
	/**
	 * 判断前提条件
	 * 1、国内bu
	 * 2、包含机票和酒店
	 * 
	 * @param order
	 * @return
	 */
	private boolean judgmentCondition(OrdOrder order){
		boolean flag = false;
		if(order != null && CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			List<Long> categoryIds = Lists.newArrayList();
			if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				for(OrdOrderItem orderItem : order.getOrderItemList()){
					if(orderItem != null){
						categoryIds.add(orderItem.getCategoryId());
					}
				}
			}
			if(CollectionUtils.isNotEmpty(categoryIds)){
				if(categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId())
						&& categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId())){
					flag = true;
				}
			}
		}
		return flag;
	}
	
}
