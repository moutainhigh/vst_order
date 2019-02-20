package com.lvmama.vst.order.processer;

import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.processer.sms.*;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.pet.vo.PayAndPreVO;
import com.lvmama.vst.pet.vo.PayPaymentRefundmentVo;
import net.sf.json.JSONArray;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
/**
 * 短信
 * @author chenkeke
 *
 */
public class OrderSmsSendProcesser  implements MessageProcesser,IWorkflowProcesserT<OrdOrder>{
	
	protected transient final Log logger = LogFactory.getLog(getClass());
	public static final String[] DISTRIBUTOR_CODE_ARRAY = {"DISTRIBUTOR_B2B","DISTRIBUTOR_API"};
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	@Override
	public void handle(Message message,OrdOrder order){
		if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
			handleOld(message,order);
		}else{//新
			if(order.getDistributorCode() != null && "DISTRIBUTOR_DAOMA".equalsIgnoreCase(order.getDistributorCode())){
				//如果该订单的渠道代码为DISTRIBUTOR_DAOMA,则不调用短信发送接口发短信
				logger.info("OrderSmsSendProcesser====方法handle(Message message,OrdOrder order)订单号:" + order.getOrderId() + "渠道代码为DISTRIBUTOR_DAOMA则不发送短信");
			}else if(isTntOrder(order)){
				//判断是否帮分销发送短信--屏蔽分销短信
				logger.info("OrderSmsSendProcesser====方法handle(Message message,OrdOrder order)订单号:" + order.getOrderId() + "是分销B2B渠道或API渠道的订单，且isSendSmsDistribution='N', 故不发短信");
			}else{
			    sendSms(message,order);
			}
		}
	}
	public void handleOld(Message message,OrdOrder order) {
		if(message.hasOrderMessage()){
			//创建订单
			if(MessageUtils.isOrderCreateMsg(message)){

				/*ORDER_CREATE_UNVERIFIED_UNPREAUTH//("订单提交-待审核+非预授权"),
				ORDER_CREATE_UNVERIFIED_PREAUTH//("订单提交-待审核+预授权"),
				ORDER_CREATE_VERIFIED_PREPAID//("订单提交-审核成功+预付"),
				ORDER_CREATE_VERIFIED_PAY_UNGUARANTEE//("订单提交-审核成功+到付+非担保"),
				ORDER_CREATE_VERIFIED_PAY_GUARANTEE//("订单提交-审核成功+到付+担保")*/		
				
				//资源待审核
				if(!order.hasResourceAmple()){
					//订单提交-待审核+预授权 (PaymentType 为一律预授权 )
					if(order.getPaymentType()!=null && order.getPaymentType().equalsIgnoreCase(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name())){
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_PREAUTH);
					}
					//订单提交-待审核
					else{
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.ORDER_CREATE_UNVERIFIED_UNPREAUTH);
					}
				}
				//资源审核通过
				else if(order.hasResourceAmple()){
					//预付（驴妈妈）
					if(order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PREPAID.name())){
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PREPAID);
					}
					//现付（供应商）
					else if(order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PAY.name())){
						//担保 
						if(order.getGuarantee().equalsIgnoreCase(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name())){
							this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_GUARANTEE);
							if (isCancelStrategy(order)) {
								this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY);
							}
							if (isHotelCancelStrategy(order)) {
								this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_HOTELCOMB_CANCEL_STRATEGY);
							}
						}
						//非担保
						else{
							this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.ORDER_CREATE_VERIFIED_PAY_UNGUARANTEE);
						}
					}
				}
			}

			//资源审核通过
			else if(MessageUtils.isOrderResourcePassMsg(message)){
				/*VERIFIED_PREPAID("审核成功+预付"),
				VERIFIED_PAY_UNGUARANTEE("审核成功+到付+非担保"),
				VERIFIED_PAY_GUARANTEE("审核成功+到付+担保"),*/
				
				//预付（驴妈妈）
				if(order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PREPAID.name())){
					if(order.hasPayed()){
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.PAY_PREAUTH_VERIFIED);
					}else{
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.VERIFIED_PREPAID);
						if (isCancelStrategy(order)) {
							this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY);
						}
					}
				}
				//现付（供应商）
				else if(order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PAY.name())){
					//担保
					if(order.getGuarantee().equalsIgnoreCase(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name())){
						this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_GUARANTEE);
						if (isCancelStrategy(order)) {
							this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID_CANCEL_STRATEGY);
						}
					}
					//非担保
					else{
						this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.VERIFIED_PAY_UNGUARANTEE);
					}
				}
				
			}

			//取消
			else if(MessageUtils.isOrderCancelMsg(message)){
				//
				/*UNVERIFIED_UNPREAUTH("审核不通过-非预授权"),
				UNVERIFIED_PREAUTH_OK("审核不通过-预授权成功"),*/
				//资源审核不通过
				if(order.getCancelCode().equalsIgnoreCase(String.valueOf(Constants.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM))){
					//审核不通过-预授权成功(预授权，并支付成功)
					if(order.getPaymentType().equalsIgnoreCase(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name())
							&& order.getPaymentStatus().equalsIgnoreCase(OrderEnum.PAYMENT_STATUS.PAYED.name())
					){
						this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.UNVERIFIED_PREAUTH_PAYED);
					}
					//审核不通过-非预授权
					else{
						this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.UNVERIFIED_UNPREAUTH);
					}
				}

				/*CANCEL_TIMEOUT_PREPAID("取消-超时未支付取消+预付"),
				CANCEL_NO_REFUND("取消-订单取消+无退款"),
				CANCEL_REFUND_FIRST_BACK("取消-订单取消+有退款（已退款）+原路退回"),
				CANCEL_REFUND_UNFIRST_BACK("取消-订单取消+有退款（已退款）+非原路退回"),*/
				
				//预付（驴妈妈） 超时未支付取消
				else if(order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PREPAID.name())
						&&order.getCancelCode().equalsIgnoreCase(OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name())
						&& order.getPaymentStatus().equalsIgnoreCase(OrderEnum.PAYMENT_STATUS.UNPAY.name())
				){
					this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.CANCEL_TIMEOUT_PREPAID);
				}
				//取消-订单取消+无退款
				else if((order.getRefundedAmount()==null || order.getRefundedAmount()==0L) && !isTicketOrder(order)){
					this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND);
				}
				//取消-订单取消+有退款
				else if(order.getRefundedAmount()!=null && order.getRefundedAmount()>0L){
					boolean firstRefunded =false;
					boolean unFirstRefunded =false;
					List<PayPaymentRefundmentVo>  paymentRefundmentVos = payPaymentServiceAdapter.selectRefundListByOrderIdAndBizType(order.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name());
					for (PayPaymentRefundmentVo payPaymentRefundmentVo : paymentRefundmentVos) {
						PayPayment payPayment=	payPaymentServiceAdapter.selectByPaymentId(payPaymentRefundmentVo.getPaymentId());
						//同网关
						if(payPaymentRefundmentVo.getRefundGateway().equalsIgnoreCase(payPayment.getPaymentGateway())){
							firstRefunded = true;
						}
						//不同网关 
						else {
							unFirstRefunded = true;
						}
					}
					// （已退款）+原路退回
					if(firstRefunded){
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.CANCEL_REFUND_FIRST_BACK);
					}
					//（已退款）+非原路退回
					if(unFirstRefunded){
						this.smsSend(order.getOrderId(), OrdSmsTemplate.SEND_NODE.CANCEL_REFUND_UNFIRST_BACK);
					}
				}
			}
			//支付成功
			else if(MessageUtils.isOrderPaymentMsg(message)){
				/*PAY_PREAUTH_UNVERIFIED("支付-预授权支付成功+待审核"),
				PAY_PREAUTH_VERIFIED("支付-预授权支付完成+已审核"),
				PAY_PREPAID_VERIFIED("支付-预付成功+已审核"),*/
				List<PayAndPreVO> payAndPreVOs =  payPaymentServiceAdapter.findPaymentInfo(order.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name());
				if(payAndPreVOs.size()>0){
					PayAndPreVO lastPayAndPreVO = null;
					for (PayAndPreVO payAndPreVO : payAndPreVOs) {
						if(StringUtils.equalsIgnoreCase("SUCCESS", payAndPreVO.getStatus())){
							if(lastPayAndPreVO==null)
								lastPayAndPreVO = payAndPreVO;
							if(payAndPreVO.getCallbackTime().after(lastPayAndPreVO.getCallbackTime()))
								lastPayAndPreVO = payAndPreVO;
						}
					}
					if(lastPayAndPreVO!=null){

						//支付-预授权支付成功+待审核
						if(lastPayAndPreVO.isPrePayment()){
							//已审核
							if(order.hasInfoAndResourcePass()){
								this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.PAY_PREAUTH_VERIFIED);
							}else{//待审核
								this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.PAY_PREAUTH_UNVERIFIED);
							}
						}
						//非预授权  支付-预付成功+已审核
						else if(order.getResourceStatus().equalsIgnoreCase(OrderEnum.RESOURCE_STATUS.AMPLE.name())){
							this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.PAY_PREPAID_VERIFIED);
						}
					}
				}
				
			}
			
			/*此处直接放在JOB中//短信等待时间剩余30分钟时发送短信消息
			else if(MessageUtils.isOrderWaitPaySms(message)){
				//订单正常、支付给驴妈妈、待支付
				if(order.getOrderStatus().equalsIgnoreCase(OrderEnum.ORDER_STATUS.NORMAL.name())
						&& order.getPaymentTarget().equalsIgnoreCase(SuppGoods.PAYTARGET.PREPAID.name())
						&& order.getPaymentStatus().equalsIgnoreCase(OrderEnum.PAYMENT_STATUS.UNPAY.name())){
					this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND);
					
				}
			}
			//履行前一天短信
			else if(MessageUtils.isOrderPerformPreviousDaySms(message)){
				this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.COMMON_ARRIVAL_DAY_BEFORE_REMIND);
			}*/
		}
		//退款
		else if(message.getObjectType().equalsIgnoreCase(Constant.JMS_TYPE.ORD_REFUNDMENT.name())){
			//退款
			if(MessageUtils.isOrderRefumentMsg(message)||MessageUtils.isOrderRefumentOkMsg(message)){
				//ORDER_NORMAL_REFUND("订单正常-有退款（已退款）")
				if(order.getOrderStatus().equalsIgnoreCase(OrderEnum.ORDER_STATUS.NORMAL.name())){
					this.smsSend(order.getOrderId(),OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND);
				}
			}
		}
	}
	
	@Override
	public void process(Message message) {
		if(messageCheck(message)){
			OrdOrder order = null;
			if(MessageUtils.isOrderRefumentMsg(message)||MessageUtils.isOrderRefumentOkMsg(message)){
				OrdRefundment ref = orderRefundmentServiceAdapter.queryOrdRefundmentById(message.getObjectId());
				if(ref==null){
					logger.warn("orderRefundmentServiceAdapter.queryOrdRefundmentById OrdRefundment==null");
					return;
				}
				order = orderUpdateService.queryOrdOrderByOrderId(ref.getOrderId());
			}else{
				order = orderUpdateService.queryOrdOrderByOrderId(message.getObjectId());
			}
			logger.info("isOrderRefumentApplyMsg:"+MessageUtils.isOrderRefumentApplyMsg(message)
					+"isOrderCancelApplyMsg:"+MessageUtils.isOrderCancelApplyMsg(message)
					+",order_id:"+message.getObjectId()+",order:"+order);
			if(order != null && (ActivitiUtils.hasNotActivitiOrder(order)||MessageUtils.isOrderRefumentMsg(message)||MessageUtils.isOrderRefumentOkMsg(message)
					||MessageUtils.isOrderCancelApplyMsg(message)|| MessageUtils.isOrderRefumentApplyMsg(message)
					||MessageUtils.isStampDepositPaidMsg(message))){
				
				order = orderLocalService.queryOrdorderByOrderId(order.getOrderId());
				handle(message,order);
			}
		}
	}
	
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message){
		if(message.hasOrderMessage()){
			return true;
		}else if(message.getObjectType().equalsIgnoreCase(Constant.JMS_TYPE.ORD_REFUNDMENT.name())){
			return true;
		}
		return false;
	}
	
	// 是否是门票订单
	public boolean isTicketOrder(OrdOrder order) {
		if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
			return true;
		} else {
			return false;
		}
	}
	
	private Long smsSend(Long orderId,OrdSmsTemplate.SEND_NODE sendNode){
		Long smsId=null;
		try {
			smsId= orderSmsSendService.sendSms(orderId, sendNode);
		} catch (BusinessException e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		return smsId;
	}
	
	public void sendSms(Message msg, OrdOrder order) {
		AbstractSms sms;
		if (MessageUtils.isOrderCreateMsg(msg)) {
			// 提交
			sms = new OrderCreateSms();
		} else if (MessageUtils.isOrderResourcePassMsg(msg)) {
			// 审核完成
			if(isLocalFreedom(order) || isNeedResource(order) || OrdOrderUtils.isDestBuFrontOrder(order)||isLocalFreedomAndBusHotel(order)){
				sms = new OrderResourceSms(isPreauthSuccess(order));
			}else{
				sms = new OrderBaseSms();
			}
		} else if (MessageUtils.isOrderPaymentMsg(msg)) {
			// 支付完成
			sms = new OrderPaymentSms(isPreauthSuccess(order));
			
		} else if (MessageUtils.isOrderRefumentMsg(msg)||MessageUtils.isOrderRefumentOkMsg(msg)) {
			// 退款
			sms = new OrderRefundedSms();
		} else if (MessageUtils.isOrderCancelMsg(msg)) {
			logger.info("订单取消："+msg);
			// 取消
			sms = new OrderCancelSms();
		} else if (MessageUtils.hasOrderEleContractMessage(msg)) {
			// 电子合同更改
			sms = new OrderContractUpdateSms();
		}else if(MessageUtils.isOrderCancelApplyMsg(msg)){
			logger.info("订单取消申请："+msg);
			//订单取消申请
			sms = new OrderCancelApplySms();
		} 
		else if(MessageUtils.isOrderRefumentApplyMsg(msg)){
			logger.info("退款申请："+msg);
			//退款申请
			sms = new RefundApplySms();
		}
		else if(MessageUtils.isStampDepositPaidMsg(msg)) {// 预售券定金支付
			logger.info("stamp deposit paid：" + msg);
			sms = new StampDepositPaidSms();
		}else if(MessageUtils.isOrderCacleOfCloseHouseMsg(msg)){
			logger.info("订单取消(因为关房)"+msg);
			sms = new OrderCancleOfCloseHouseSms();
		}else if (MessageUtils.isOrderAccInsDelayRemindMsg(msg)) {
		    OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
            logger.info("意外险后置订单，过一半 等待补充游玩人时间，发送提醒补充游玩人通知"+msg);
            sms = new OrderAccInsDelayRemindSms(ordAccInsDelayInfo);
        }else if (MessageUtils.isCancelAccInsDelayMsg(msg)) {
            OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
            logger.info("意外险后置订单，过 等待补充游玩人时间 或 主动弃保意外险，发送取消意外险通知"+msg);
            sms = new OrderCancleAccInsDelaySms(ordAccInsDelayInfo);
        }else {
			// 基本短信规则
			sms = new OrderBaseSms();
		}
		//取到短信发送规则
		List<String> smsNodeList = sms.exeSmsRule(order);
		//有短信发送
		if(smsNodeList != null && smsNodeList.size() > 0){
		    logger.info("orderId=" + order.getOrderId() + "---smsNodeList=" + JSONArray.fromObject(smsNodeList));
			for(String smsNode : smsNodeList){
				orderSendSmsService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
			}
		}else {
            logger.info("orderId=" + order.getOrderId() + "---smsNodeList is null");
        }
		//机酒自由行产品，在资审通过、凭证确认且支付成功后发送防诈骗短信
		if (MessageUtils.isOrderResourcePassMsg(msg)){
			if(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
					&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())
					&&CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
					&&order.hasPayed()&&OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode().equals(order.getCertConfirmStatus())){
				orderSendSmsService.sendPreventCheatSms(order.getOrderId());
			}
		}else if(MessageUtils.isOrderPaymentMsg(msg)){
			if(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
					&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())
					&&CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
					&&order.hasResourceAmple()&&OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.getCode().equals(order.getCertConfirmStatus())){
				orderSendSmsService.sendPreventCheatSms(order.getOrderId());
			}
		}
	}
	private boolean isLocalFreedom(OrdOrder order){
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
		&&((BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
		&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
				||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue())
		&&OrdOrderUtils.isLocalBuFrontOrder(order)){
			return true;
		}
		return false;
	}
	
	private boolean isLocalFreedomAndBusHotel(OrdOrder order){
		if((BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode()))
		&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
		&&BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()==order.getSubCategoryId().longValue()){
			return true;
		}
		return false;
	}
	
	
	
	private boolean isNeedResource(OrdOrder order){
		for(OrdOrderItem orderItem:order.getOrderItemList()){
			if("true".equals(orderItem.getNeedResourceConfirm())){
				return true;
			}
		}
		return false;
	}
	
	//预授权支付成功 (预授权判断)
    private boolean isPreauthSuccess(OrdOrder order){
        List<PayAndPreVO> payAndPreVOs =  payPaymentServiceAdapter.findPaymentInfo(order.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name());
        if(payAndPreVOs!=null && payAndPreVOs.size()>0){
            for (PayAndPreVO payAndPreVO : payAndPreVOs) {
                if(payAndPreVO.isPrePayment() && StringUtils.equalsIgnoreCase("SUCCESS", payAndPreVO.getStatus())){
                    logger.info("isPreauthSuccess is true order id ="+order.getOrderId());
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * 是否可退改
	 * 1）、单酒店、自由行酒景同步商品退改，下发此条短信
	 * 条件：设置退改规则是可退改（含可退改、阶梯退改、同步商品退改），并且可退改一定设置最晚无损取消时间。
	 * @param order
	 * @return
	 */
	private boolean isCancelStrategy(OrdOrder order) {
		if (4 == order.getDistributorId() && order.getDistributionChannel() != 10000) {
			return false;
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
			if (order.getLastCancelTime() == null) {
				return false;
			}
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name().equals(order.getRealCancelStrategy()) 
					&& ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getHotelCancelStrategy())) {
				return true;
			}
			
		}
		
		return false;
	}
	/**
	 * 是否可退改
	 * 条件：酒店套餐，景酒 产品 设置退改规则是阶梯退改。
	 * @param order
	 * @return
	 */
	private boolean isHotelCancelStrategy(OrdOrder order) {
		if (4 == order.getDistributorId() && order.getDistributionChannel() != 10000) {
			return false;
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getMainOrderItem().getCancelStrategy())) {
				return true;
			}
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
			if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(order.getRealCancelStrategy())) {
				return true;
			}
			
		}
		
		return false;
	}

	/**
	 * 判断订单是否是为分销B2B渠道和API渠道的订单
	 * @param order
	 * @return
	 */
	public boolean isTntOrder(OrdOrder order) {
		boolean flag = false;
		//过滤掉非分销B2B渠道和API渠道(这些渠道都发送短信)
		if(!ArrayUtils.contains(DISTRIBUTOR_CODE_ARRAY,order.getDistributorCode())){
			return flag;
		}
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		//判断子订单内是否包含给分销发送短信标识(N表示不发短信)
		for(OrdOrderItem orderItem : orderItemList){
			Object isSendSMSDistribution = orderItem.getContentValueByKey("isSendSmsDistribution");
			if (isSendSMSDistribution != null && "N".equalsIgnoreCase(isSendSMSDistribution.toString())) {
				logger.info("orderItemId="+orderItem.getOrderItemId()+",isSendSmsDistribution="+isSendSMSDistribution);
				flag = true;
				break;
			}
		}
		return flag;
	}
}
