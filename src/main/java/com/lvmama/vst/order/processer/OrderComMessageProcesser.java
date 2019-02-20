/**
 * 
 */
package com.lvmama.vst.order.processer;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.utils.TestOrderUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单预定通知处理器
 * @author 张伟
 *
 */
public class OrderComMessageProcesser implements MessageProcesser{
	private static final Log LOG = LogFactory.getLog(OrderComMessageProcesser.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	ProdProductClientService prodProductClientService;
	
	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
	
	@Autowired
	private ProdPackageGroupClientService prodPackageGroupClientRemote;

	@Override
	public void process(Message message) {
		LOG.info("==="+message);
		// ==判断消息来源是否新订单系统，若是，则不处理 start== 2017-09-20 by zhujingfeng
		if (NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType())) {
			LOG.info("AutoOrderInfoPassProcesser process message from new order system,no need to deal !message:" + message.toString());
			return;
		}
		if(messageCheck(message)){
			
			Long orderId = message.getObjectId();
			OrdOrder order = getOrderWithOrderItemByOrderId(orderId);
			Long categoryId=order.getCategoryId();
			
			OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
			boolean isbusAndHotel=isBusAndhotel(ordOrder);
			LOG.info("===orderId:"+order.getOrderId()+".isbusAndHotel:"+isbusAndHotel+"===");
			if(isbusAndHotel){
				return;
			}
			boolean isLocalO2O = isLocalO2O(ordOrder);
			//渠道为门店、门店APP的国内度假事业部 跟团SBU、机酒SBU订单屏蔽预定通知
			LOG.info("===orderId:"+order.getOrderId()+".isLocalO2O:"+isLocalO2O+"===");
			if(isLocalO2O){
				return;
			}
			LOG.info("===orderId:"+order.getOrderId()+".hasCanceled:"+order.hasCanceled()+".getCancelCode:"+order.getCancelCode()+".hasPayed:"+order.hasPayed()+".hasPartPayed:"+order.hasPartPayed());
			//渠道为门店、门店APP的出境跟团，自由行，当地游，邮轮订单屏蔽预定通知
			boolean isOutBoundO2O = isOutBoundO2O(ordOrder);
			LOG.info("===orderId:"+order.getOrderId()+".isOutBoundO2O:"+isOutBoundO2O+"===");
			if(isOutBoundO2O){
				return;
			}
			if(MessageUtils.isOrderPaymentMsg(message) || MessageUtils.isOrderCancelMsg(message)){
				//订单超时且支付成功
				if (order.hasCanceled() && OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(order.getCancelCode()) && (order.hasPayed() || order.hasPartPayed())) {
                    String memo = "支付超时取消且支付成功";
					boolean isNew = OrdOrderUtils.isDestBuFrontOrderNew(order);
					boolean isO2O = OrdOrderUtils.isDestBuFrontOrderNew_O2O(order);
                    //dongningbo 酒店、酒套餐、景酒 订单走新版工作台
					LOG.info("===orderId:"+order.getOrderId()+"isNew:"+isNew+" || isO2O:"+isO2O+"====workVersion="+order.getWorkVersion());
                    if (isNew || isO2O) {
						saveComMessageByConfirm(orderId, Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_CANCEL_ORDER_PAY_SUCCESS.name(), memo, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE);
                    } else {
                        saveComMessage(orderId, OrderEnum.AUDIT_SUB_TYPE.CANCEL_ORDER_PAY_SUCCESS.getCode(), memo, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE, false);
                    }
				}
			}
			
			if(!(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)
					||BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
					||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)
					||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(categoryId))
                    ){
			
				if(MessageUtils.isOrderInfoPassMsg(message) || MessageUtils.isOrderResourcePassMsg(message)){
					if (order.isNormal() && order.hasInfoAndResourcePass()) {
						String memo="主订单审核通过，请联系用户已经通过审核";
						saveComMessage(orderId,  OrderEnum.AUDIT_SUB_TYPE.ORDER_PASS.getCode(), memo, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE,true);
					}
					createCollectionMaterial(order);//新增收取材料活动
				}else if(MessageUtils.isOrderPaymentMsg(message)){
					if(isSendComMessage(order)){
						if (order.isNormal() && order.hasPayed()) {
							String memo="订单已经支付";
							saveComMessage(orderId,  OrderEnum.AUDIT_SUB_TYPE.PAYED_REMIND.getCode(), memo, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE,true);
						}
						
					}
					createCollectionMaterial(order);//新增收取材料活动
				} else if (MessageUtils.isOrderCancelMsg(message)) {					
					if(isSendComMessage(order) && !OrdOrderUtils.isDestBuFrontOrder(order)){
						if (order.hasCanceled() && OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(order.getCancelCode()) ) {
							String memo = "订单超时支付而取消";
							saveComMessage(orderId,OrderEnum.AUDIT_SUB_TYPE.CANCEL_ORDER_OVER.getCode(),memo,ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE,false);
						}
					}					
				}
			}
			
			if( (BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(categoryId)
					|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(categoryId)
					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)
					|| BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryId)
					|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId))
					){
				//出境bu预定通知-取消提醒
				if(MessageUtils.isOrderCancelMsg(message)
						&& order.hasCanceled()&&CommEnumSet.BU_NAME.OUTBOUND_BU.name().equals(order.getBuCode())
						&& !TestOrderUtil.isTestOrderForMarkTaskValid(order)){
					String memo = "订单已取消，请联系用户进行沟通确认"; 
					saveComMessage(orderId,OrderEnum.AUDIT_SUB_TYPE.CANCEL_ORDER_NOTIFY.getCode(),memo,ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE,false);
				
				}
				
			}
			
			
			
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
		//分销商渠道ID
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
			if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
					&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
			return false;
		}
		if(isInclueLocalBuOrDestinationBu(order)){
			LOG.info("===orderId:"+order.getOrderId()+".productId:"+order.getProductId()+"===");
			long productId=0l;
			if(null!=order.getOrdOrderPack()){
				productId=order.getOrdOrderPack().getProductId();
			}else{
				productId=order.getMainOrderItem().getProductId();
			}
			ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(productId);
			List<ProdPackageGroup> prodPackgeGroupList = prodPackageGroupClientRemote.getProdPackageGroupByProductId(productId);
			List<ProdProduct> listChildProduct=prodProductClientService.findProductByProductId(productId);
			ProdProduct product = result.getReturnContent();
			if(product==null){
				LOG.error("====orderId:"+order.getOrderId()+"====product is null===");
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
	
	/**
	 * 收取材料活动创建 add collection_material by zhaomingzhu
	 * @param order
	 * @return
	 */
	private void createCollectionMaterial(OrdOrder order){
		LOG.info("start method createCollectionMaterial_1：orderId[" + order.getOrderId() 
				+ "]order_categoryId[" + order.getCategoryId()
				+ "]isNormal[" + order.isNormal()
				+ "]hasPayed[" + order.hasPayed()
				+ "]hasResourceAmple[" + order.hasResourceAmple()
				+ "]order_buCode[" + order.getBuCode()
				+ "]");		
		List<OrdOrderItem>  ordOrderItems = order.getOrderItemList();
		if(ordOrderItems != null && ordOrderItems.size() > 0){
			for(OrdOrderItem ordOrderItem : ordOrderItems){
				Long categoryId = ordOrderItem.getCategoryId();
				LOG.info("start method createCollectionMaterial_2：orderId[" + order.getOrderId() 
						+ "]categoryId[" + categoryId
						+ "]realBuType[" + ordOrderItem.getRealBuType() 
						+ "]");				
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCategoryId().equals(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId().equals(categoryId)
						|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId().equals(categoryId)){
					if(StringUtil.isNotEmptyString(order.getBuCode()) 
							&& CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
						if(order.isNormal() && order.hasPayed() && order.hasResourceAmple()){
							//判断是否已有
							Map<String, Object> param = new HashMap<String, Object>();
							param.put("objectId", ordOrderItem.getOrderItemId());
							param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
							param.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.getCode());
							param.put("auditSubtype",OrderEnum.AUDIT_SUB_TYPE.COLLECTION_MATERIAL.getCode());
							List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);					
							if(comAuditList != null && comAuditList.size() > 0){
								LOG.info("start method createCollectionMaterial for create：orderId[" + order.getOrderId() + "OrderItemId][" + ordOrderItem.getOrderItemId() + "]已经存在收取材料活动");
								return;
							}else{
								LOG.info("start method createCollectionMaterial for create：orderId[" + order.getOrderId() + "OrderItemId][" + ordOrderItem.getOrderItemId() + "]");
								ComAudit audit = new ComAudit();
								audit.setObjectId(ordOrderItem.getOrderItemId());
								audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
								audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
								audit.setAuditSubtype(OrderEnum.AUDIT_SUB_TYPE.COLLECTION_MATERIAL.name());
								audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
								audit.setCreateTime(Calendar.getInstance().getTime());
								audit.setUpdateTime(Calendar.getInstance().getTime());
								orderAuditService.saveAudit(audit);

								ComMessage comMessage=new ComMessage();
								comMessage.setAuditId(audit.getAuditId());
								comMessage.setCreateTime(Calendar.getInstance().getTime());
								comMessage.setMessageContent("新增预定通知-收取材料");
								comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
								comMessage.setSender("SYSTEM");
								comMessageService.addComMessage(comMessage);
								
								ComAudit audit1 = distributionBusiness.makeOrderAudit(audit);
								if(audit1 == null){
									comMessage.setReceiver(Constants.NO_PERSON);
								}else{
									comMessage.setReceiver(audit1.getOperatorName());
								}
								comMessageService.updateComMessage(comMessage);
								
								LOG.info("end method createCollectionMaterial for create：orderId[" + order.getOrderId() + "OrderItemId][" + ordOrderItem.getOrderItemId() + "]");
								break;
							}							
						}
					}
				}
			}
		}
	}
	
	private void saveComMessage(Long orderId, String subType,String memo,ComLog.COM_LOG_LOG_TYPE logType,boolean  bigTrafficValidate) {
		try {
			LOG.info("start method saveComMessage");
			comMessageService.saveReservationOrderNew(orderId,  subType, "system", memo, bigTrafficValidate);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			
			if (Constants.NO_PERSON.equals(e.getMessage())) {
				
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId, 
						orderId, 
						"system", 
						memo, 
						logType.name(), 
						logType.getCnName()+"["+ memo +"]",
						memo);
			}else{
				LOG.info(" saveComMessage has no person exception");
			}
		}
	}

	private void saveComMessageByConfirm(Long orderId, String auditSubType, String memo, ComLog.COM_LOG_LOG_TYPE logType) {
		try {
			LOG.info("start method saveAuditMessage");
			ComAudit audit = new ComAudit();
			audit.setObjectId(orderId);
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			audit.setAuditType(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
			audit.setAuditSubtype(auditSubType);
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
			audit.setCreateTime(Calendar.getInstance().getTime());
			orderAuditService.saveAudit(audit);
			LOG.info("saveAuditMessage auditId="+audit.getAuditId());
			insertOrderLog(orderId, auditSubType);
			distributionBusiness.makeOrderAudit(audit);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionFormatUtil.getTrace(e));

			if (Constants.NO_PERSON.equals(e.getMessage())) {

				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId,
						orderId,
						"system",
						memo,
						logType.name(),
						logType.getCnName()+"["+ memo +"]",
						memo);
			}else{
				LOG.info(" saveAuditMessage has no person exception");
			}
		}
	}

	/**
	 *
	 * 保存日志
	 *
	 */
	private void insertOrderLog(final Long orderId,String auditType){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId,
				orderId,
				Constants.SYSTEM,
				"编号为["+orderId+"]的订单,系统自动创建订单活动["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+ ConfirmEnumUtils.getCnName(auditType)+"]",
				null);
	}
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message) {
		if (MessageUtils.isOrderInfoPassMsg(message)
				|| MessageUtils.isOrderResourcePassMsg(message)
				|| MessageUtils.isOrderPaymentMsg(message)
				|| MessageUtils.isOrderCancelMsg(message)
				|| MessageUtils.hasOrderModifyPersonMessage(message)
				|| MessageUtils.isOrderOnlineRefundMsg(message)) {
			return true;
		}
		return false;
	}
	

	
	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		
		return order;
	}
	
	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		condition.setOrderIndentityParam(orderIndentityParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		
		return order;
	}
	
	/**
	 * 根据订单的属性判断是否需要发送预订通知
	 * @param order
	 * @return
	 */
	private boolean isSendComMessage(OrdOrder order){
		LOG.info("start isSendComMessage orderId is"+order.getOrderId());
		Long categoryId = order.getCategoryId();	
		LOG.info("order categoryId is"+categoryId);
		OrdOrderItem orderItem = order.getMainOrderItem();
		//单酒店不发送预订通知消息
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)){
			return false;
		}
		//酒店套餐
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)){								
			//ProdProduct product = prodProductClientService.findProdProductById(orderItem.getProductId(), true, true);
			ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(orderItem.getProductId());
			ProdProduct product = result.getReturnContent();
			LOG.info("orderId is "+order.getOrderId()+"and  productType is"+product.getProductType()+"and packageType is"+product.getPackageType());
			//类别为国内且打包类型为供应商打包
			if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType()) 
					|| PRODUCTTYPE.INNERLINE.getCode().equalsIgnoreCase(product.getProductType())
					|| PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(product.getProductType())){
				if(ProdProduct.PACKAGETYPE.SUPPLIER.name().equalsIgnoreCase(product.getPackageType())){
					LOG.info("end isSendComMessage orderId is"+order.getOrderId()+"return false");
					return false;
				}
			}
		}
		//如果订单属于自由行
		if(BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryId)){
			//如果自由行的类别为国内
			ProdProduct product = prodProductClientService.findProdProductById(order.getProductId(), Boolean.TRUE, Boolean.TRUE);
			boolean isTraffic = isTraffic(product);
			LOG.info("orderId is "+order.getOrderId()+"and  productType is"+product.getProductType()+"and packageType is"+product.getPackageType()+"and isTraffic is"+isTraffic);
			if(PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(product.getProductType()) 
					|| PRODUCTTYPE.INNERLINE.getCode().equalsIgnoreCase(product.getProductType())
					|| PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(product.getProductType())){					
				//如果打包类型为自主打包&&大交通为否
				if(ProdProduct.PACKAGETYPE.LVMAMA.name().equalsIgnoreCase(product.getPackageType()) && !isTraffic){				
					LOG.info("end isSendComMessage orderId is"+order.getOrderId()+"return false");
					return false;
				}				
			}
		}
		LOG.info("end isSendComMessage orderId is"+order.getOrderId()+"return true");
		return true;	
	}
	
	/**
	 * 判断产品是否含大交通
	 * @return
	 */
	public boolean isTraffic(ProdProduct product){
		Map<String, Object> propValue=product.getPropValue();
		String trafficFlag=(String)propValue.get("traffic_flag");
		if (trafficFlag==null || "N".equals(trafficFlag)) {
			//不含大交通
			return false;
		}
		//含大交通
		return true;
	}
	
}
