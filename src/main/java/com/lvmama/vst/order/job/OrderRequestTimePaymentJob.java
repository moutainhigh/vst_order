package com.lvmama.vst.order.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.Constant.PAYMENT_PRE_STATUS;
import com.lvmama.vst.comm.vo.Constant.REFUNDMENT_CHANNEL;
import com.lvmama.vst.order.processer.sms.AbstractSms;
import com.lvmama.vst.order.processer.sms.OrderUrgingPaymentSms;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.adapter.PetOrderMessageServiceAdapter;

/**
 * 小驴分期催支付的订单  
 *
 */
public class OrderRequestTimePaymentJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrderRequestTimePaymentJob.class);

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
	//注入支付业务
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			List<OrdOrder> list = orderUpdateService.queryRequestTimePaymentOrder();
			if(CollectionUtils.isNotEmpty(list)){
				for(OrdOrder order:list){
					OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
					if(ordOrder != null && ordOrder.hasCanceled()) {
						continue;
					}
					logger.info("all OrderId="+ordOrder.getOrderId());
					long diffCreateMinute = DateUtil.diffMinute(ordOrder.getCreateTime(),new Date());
					if(diffCreateMinute <= 15L){//在下单15分钟内
						logger.info("OrderRequestTimePayment possible OrderId="+ordOrder.getOrderId());
						requestTimePayment(ordOrder);
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
		
		if(order.getCategoryId()==null|| !ProductCategoryUtil.isTicket(order.getCategoryId())){
			ComAudit comAudit = orderAuditService.saveCreateOrderAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.name());
			//小驴分期生成催支付活动之后，马上进行一次分单操作，防止任务积压
			logger.info("小驴分期生成催支付活动后立即分单，活动ID:" + comAudit.getAuditId());
			distributionBusiness.makeOrderAudit(comAudit);
		}else{ 
			ComAudit audit = new ComAudit(); 
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name()); 
			audit.setObjectId(order.getOrderId()); 
			audit.setAuditType(OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.name()); 
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
	
	private boolean requestTimePayment(OrdOrder order){
		boolean flag = false;
		try{
			logger.info("orderId:"+order.getOrderId()+"==order.getBuCode():"+order.getBuCode()+"===order.getCategoryId():"+order.getCategoryId().longValue()+"==order.getSubCategoryId():"+order.getSubCategoryId());
			if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
					&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
					&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId()){
				logger.info("orderId:"+order.getOrderId()+"==enter==");
				return flag;
			}
			
            /*金融品类订单 不发送此催支付短信*/
            if (BIZ_CATEGORY_TYPE.category_finance.getCategoryId().longValue() == order.getCategoryId().longValue()) {
                logger.info("orderId=" + order.getOrderId() + "---categoryId=" + order.getCategoryId() + "---金融品类订单 不发送此催支付短信");
                return flag;
            }
			
			logger.info("orderId:"+order.getOrderId()+"make audit");
			List<PayPayment> payPaymentList = null;
			if(isPreauth(order)){
				payPaymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(order.getOrderId(), REFUNDMENT_CHANNEL.BOC_CREDIT_PRE.name(), PAYMENT_PRE_STATUS.CREATE.name());
			}else{
				payPaymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(order.getOrderId(), REFUNDMENT_CHANNEL.BOC_CREDIT_WEB.name(), PAYMENT_PRE_STATUS.CREATE.name());
			}
			if(payPaymentList != null && payPaymentList.size() > 0){
				logger.info("need requestTimePayment orderId = "+order.getOrderId());
				flag = true;
				makeAudit(order);
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
			}
		}catch(Exception ex){
			logger.error(ExceptionFormatUtil.getTrace(ex));
		}
		
		return flag;
		
	}
	
	//预订限制 (预授权)
	public boolean isPreauth(OrdOrder order){
		if(order.getPaymentType() != null && SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(order.getPaymentType())){
			return true;
		}else{
			return false;
		}
	}

}
