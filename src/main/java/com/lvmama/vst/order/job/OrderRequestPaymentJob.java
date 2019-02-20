/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.processer.sms.AbstractSms;
import com.lvmama.vst.order.processer.sms.OrderUrgingPaymentSms;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSmsSendService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.RainbowVIPWorkOrderService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.pet.adapter.PetOrderMessageServiceAdapter;

/**
 * 处理未支付的订单
 * @author lancey
 *
 */
public class OrderRequestPaymentJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrderRequestPaymentJob.class);

	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
	@Autowired
	private PetOrderMessageServiceAdapter petOrderMessageService;
	@Autowired
	protected IComplexQueryService complexQueryService;
	@Autowired
	private RainbowVIPWorkOrderService rainbowVIPWorkOrderService;
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
    private ProdPackageGroupClientService prodPackageGroupClientRemote;
	
	@Autowired
	private IOrder2RouteService order2RouteService;

	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			List<OrdOrder> list = orderUpdateService.queryRequestPaymentOrder();
			if(CollectionUtils.isNotEmpty(list)){
				for(OrdOrder order:list){		
					boolean msgAndJobSwitch=order2RouteService.isCategoryRouteToNewMsgAndJobSys(order.getCategoryId());
					if(msgAndJobSwitch){
						return;
					}
									
					OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
					if(ordOrder != null && ordOrder.hasCanceled()) {
						continue;
					}
					logger.info("orderId:"+order.getOrderId()+"==order.getBuCode():"+order.getBuCode()+"===order.getCategoryId():"+order.getCategoryId().longValue()+"==order.getSubCategoryId():"+order.getSubCategoryId());
					if(BU_NAME.LOCAL_BU.getCode().equals(ordOrder.getBuCode())
							&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==ordOrder.getCategoryId().longValue()
							&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==ordOrder.getSubCategoryId()){
						logger.info("orderId:"+ordOrder.getOrderId()+"==enter==");
						continue;
					}
					//工单提醒
					rainbowVIPWorkOrderService.pushWorkOrderRemindAfterOrder(ordOrder);
					//目的地订单不产生活动和短信
					if(OrdOrderUtils.isDestBuFrontOrder(ordOrder)){
						continue;
					}
					boolean isbusAndHotel=isBusAndhotel(order);
					logger.info("===orderId:"+order.getOrderId()+".isbusAndHotel:"+isbusAndHotel+"===");
					if(isbusAndHotel){
						continue;
					}
					boolean isLocalO2O = isLocalO2O(ordOrder);
					//渠道为门店、门店APP的国内度假事业部 跟团SBU、机酒SBU订单屏蔽预定通知
					logger.info("===orderId:"+ordOrder.getOrderId()+".isLocalO2O:"+isLocalO2O+"===");
					if(isLocalO2O){
						continue;
					}
					//渠道为门店、门店APP的出境跟团，自由行，当地游，邮轮订单屏蔽预定通知
					boolean isOutBoundO2O = isOutBoundO2O(ordOrder);
					logger.info("===orderId:"+ordOrder.getOrderId()+".isOutBoundO2O:"+isOutBoundO2O+"===");
					if(isOutBoundO2O){
						continue;
					}
		            /*金融品类订单 不发送此催支付短信*/
		            if (BIZ_CATEGORY_TYPE.category_finance.getCategoryId().longValue() == ordOrder.getCategoryId().longValue()
		            		|| BIZ_CATEGORY_TYPE.category_supermember.getCategoryId().longValue() == ordOrder.getCategoryId().longValue()) {
		                logger.info("orderId=" + order.getOrderId() + "---categoryId=" + order.getCategoryId() + "--- 不发送此催支付短信");
		                continue;
		            }
					try{
						try {
							logger.info("orderId:"+order.getOrderId()+"make audit");
							makeAudit(order);
						}catch (Exception e){
							logger.error(ExceptionFormatUtil.getTrace(e));
						}
						if(order!=null) {
							logger.info("OrderRequestPaymentJob start hasOutAndFreed: orderId" + order.getOrderId() + "," + order.getBuCode() + "," + order.getCategoryId());
						}
						if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
							if (OrderUtils.hasOutAndFreed(order)) {
								orderSmsSendService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND_OUTBOUND_FREED);
							} else {
								orderSmsSendService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND);
							}
						}else{//新
							// 催支付
							AbstractSms sms = new OrderUrgingPaymentSms();
							//取到短信发送规则
							List<String> smsNodeList = sms.exeSmsRule(order);
							//有短信发送
							if(smsNodeList != null && smsNodeList.size() > 0){
								for(String smsNode : smsNodeList){
									orderSendSmsService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
								}
							}
						}
						//发送jms消息给驴途使用
						sendMessageToLVTU(order.getOrderId());
					}catch(Exception ex){
						logger.error(ExceptionFormatUtil.getTrace(ex));
					}
				}
			}
		}
	}
	
	private void sendMessageToLVTU(Long orderId){
		logger.info("send message to lvtu begin");
		OrdOrder complexOrder = complexQueryService.queryOrderByOrderId(orderId);
		String addition=complexOrder.getProductId()+","+complexOrder.getUserNo();
		petOrderMessageService.sendOrderRemindPayMessage(orderId, addition);
		logger.info("send message to lvtu end");
	}

	private void makeAudit(OrdOrder order) {
		/*ComAudit audit = new ComAudit();
		audit.setObjectId(order.getOrderId());
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setCreateTime(new Date());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name());
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		orderAuditService.saveAudit(audit);
		*/		
		
		
		if(order.getCategoryId()==null||!ProductCategoryUtil.isTicket(order.getCategoryId())){
			ComAudit comAudit = orderAuditService.saveCreateOrderAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name());
			//生成催支付活动之后，马上进行一次分单操作，防止任务积压
			logger.info("生成催支付活动后立即分单，活动ID:" + comAudit.getAuditId());
			distributionBusiness.makeOrderAudit(comAudit);
		}else{ 
			ComAudit audit = new ComAudit(); 
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name()); 
			audit.setObjectId(order.getOrderId()); 
			audit.setAuditType(OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name()); 
			audit.setOperatorName("SYSTEM"); 
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()); 
			audit.setCreateTime(Calendar.getInstance().getTime()); 
			audit.setCompleteTime(Calendar.getInstance().getTime()); 
			audit.setUpdateTime(Calendar.getInstance().getTime()); 
			if("SYSTEM".equals(audit.getOperatorName()))// 标记为系统自动过
				audit.setAuditFlag("SYSTEM");
			orderAuditService.saveAudit(audit); 
			// 分到人
			distributionBusiness.makeOrderAudit(audit);
			// 自动过
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name()); 
			orderAuditService.updateByPrimaryKey(audit);
		}
	}
	
	private boolean isLocalO2O(OrdOrder order) {
		if(Constant.DIST_O2O_SELL==order.getDistributorId()||Constant.DIST_O2O_APP_SELL==order.getDistributorId()){
			if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				return true;
			}
		}
		return false;
	}

	private boolean isOutBoundO2O(OrdOrder order) {
		if(Constant.DIST_O2O_SELL==order.getDistributorId()||Constant.DIST_O2O_APP_SELL==order.getDistributorId()){
			if(Constant.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId().longValue())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId().longValue())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getSubCategoryId())
						|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getSubCategoryId())){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isBusAndhotel(OrdOrder order){
		if(null==order){
			return false;
		}
		//分销商渠道ID
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
			if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
					&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}
		
		if(isInclueLocalBuOrDestinationBu(order)){
			logger.info("===orderId:"+order.getOrderId()+".productId:"+order.getProductId()+"===");
			order=complexQueryService.queryOrderByOrderId(order.getOrderId());
			long productId=0l;
			if(null!=order.getOrdOrderPack()){
				productId=order.getOrdOrderPack().getProductId();
			}else if(null!=order.getMainOrderItem()){
				productId=order.getMainOrderItem().getProductId();
			}
			ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(productId);
			List<ProdPackageGroup> prodPackgeGroupList = prodPackageGroupClientRemote.getProdPackageGroupByProductId(productId);
			List<ProdProduct> listChildProduct=prodProductClientService.findProductByProductId(productId);
			ProdProduct product = result.getReturnContent();
			if(product==null){
				logger.error("====orderId:"+order.getOrderId()+"====product is null===");
				return false;
			}
			if(null!=prodPackgeGroupList){
				product.setProdPackgeGroupList(prodPackgeGroupList);
			}
			if(!isCategory(order,product)){
				return false;
			}
			//自主打包，交通只包含bus
			if(null!=product.getPackageType()&&ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(product.getPackageType())){
				if(null!=listChildProduct){
					boolean isBusOther=false;
					boolean isHotel=false;
					boolean isOther=false;
					for (ProdProduct element : listChildProduct) {
						if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId().longValue()==element.getBizCategoryId().longValue()){
							isBusOther=true;
						}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getBizCategoryId().longValue()){
							isHotel=true;
						}else{
							isOther=true;
						}
					}
					if(isBusOther&&isHotel&&!isOther){
						return true;
					}
				}
			}else if(null!=product.getPackageType()&&ProdProduct.PACKAGETYPE.SUPPLIER.getCode().equals(product.getPackageType())){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId().longValue())
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId().longValue())){
					if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断是否国内bu或者目的地bu
	 */
	public boolean isInclueLocalBuOrDestinationBu(OrdOrder order){
		//如果是目的地bu或者是国内bu
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
				Constant.BU_NAME.DESTINATION_BU.equals(order.getBuCode())){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断品类含单酒、景酒、酒套餐、国内跟团-长线、国内跟团-短线、国内当地游-长线、国内当地游-短线、机酒、交通+服务
	 */
	public boolean isCategory(OrdOrder order,ProdProduct product){
		long categoryId=order.getCategoryId().longValue();
		
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)){//单酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)){//酒套餐
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())){//景酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)){//国内跟团
			return findProductType(order,product);
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)){//国内当地游
			return findProductType(order,product);
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())){//机酒
			return true;
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)&&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().equals(order.getSubCategoryId())){//交通+服务
			return true;
		}
		return false;
	}
	
	/**
	 * 查询产品类型是否属于长线或短线
	 * @param order
	 * @return
	 */
	private boolean findProductType(OrdOrder order,ProdProduct product) {
		//-长线-短线
		if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType()) 
				|| PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(product.getProductType())){	
			return true;
		}
		return false;
	}

}
