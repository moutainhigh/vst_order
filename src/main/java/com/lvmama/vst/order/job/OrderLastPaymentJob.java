/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * 处理尾款未支付的订单
 * @author baolm
 *
 */
public class OrderLastPaymentJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrderLastPaymentJob.class);

//	@Autowired
//	private IOrderSendSmsService orderSendSmsService;
//	@Autowired
//	private IOrderSmsSendService orderSmsSendService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
//	@Autowired
//	private PetOrderMessageServiceAdapter petOrderMessageService;
	@Autowired
	protected IComplexQueryService complexQueryService;
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private IOrder2RouteService order2RouteService;
	
	@Override
	public void run() {
		
		boolean msgAndJobSwitch= false; //msg and job 总开关
		msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
		if(msgAndJobSwitch){
		    return;
		}
		
		if(!Constant.getInstance().isJobRunnable()){
			return;
		}
		logger.info("OrderLastPaymentJob start...");
		List<OrdOrder> list = orderUpdateService.queryLastPaymentOrder();
		if(CollectionUtils.isEmpty(list)){
			return;
		}
//		logger.info("REMINDER_LASH_PAY stamp ordersize:{}", list.size());
		for(OrdOrder order:list){
			OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
			if(ordOrder != null && ordOrder.hasCanceled()) {
				continue;
			}
			try{
				addAudit(ordOrder);
//				makeAudit(order);
//				String version = Constant.getInstance().getProperty("orderSms.version");
//				logger.info("orderSms.version:{}", version);
//				if("old".equals(version)){//旧
//					orderSmsSendService.sendSms(order.getOrderId(),OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND);
//				}else{//新
//					// 催支付
//					AbstractSms sms = new OrderUrgingPaymentSms();
//					//取到短信发送规则
//					List<String> smsNodeList = sms.exeSmsRule(order);
//					logger.info("smsNodeList:{}", JSONUtil.bean2Json(smsNodeList));
//					//有短信发送
//					if(CollectionUtils.isNotEmpty(smsNodeList)){
//						for(String smsNode : smsNodeList){
//							orderSendSmsService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
//						}
//					}
//				}
				//发送jms消息给驴途使用
//				sendMessageToLVTU(order.getOrderId());
			}catch(Exception ex){
				logger.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
	}
	
//	private void sendMessageToLVTU(Long orderId){
//		logger.info("send message to lvtu begin");
//		OrdOrder complexOrder = complexQueryService.queryOrderByOrderId(orderId);
//		String addition=complexOrder.getProductId()+","+complexOrder.getUserNo();
//		petOrderMessageService.sendOrderRemindPayMessage(orderId, addition);
//		logger.info("send message to lvtu end");
//	}
	
	private void addAudit(OrdOrder order) {
		
		logger.info("add stamp remind last pay audit：orderId={}", order.getOrderId());
		Date now = Calendar.getInstance().getTime();
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setObjectId(order.getOrderId());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(OrderEnum.AUDIT_SUB_TYPE.REMINDER_LAST_PAY.name());
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(now);
		audit.setUpdateTime(now);
		orderAuditService.saveAudit(audit);

		ComMessage comMessage=new ComMessage();
		comMessage.setAuditId(audit.getAuditId());
		comMessage.setCreateTime(now);
		comMessage.setMessageContent("新增预定通知-催尾款支付");
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
//		if(order.getCategoryId()==null||!(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())
//				||BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
//				||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId()))
//				 ){
//			ComAudit comAudit = orderAuditService.saveCreateOrderAudit(order.getOrderId(), OrderEnum.AUDIT_TYPE.REMINDER_LASH_PAY.name());
//			//生成催支付活动之后，马上进行一次分单操作，防止任务积压
//			logger.info("生成催尾款支付活动后立即分单，活动ID:" + comAudit.getAuditId());
//			distributionBusiness.makeOrderAudit(comAudit);
//		}else{ 
			ComAudit audit = new ComAudit(); 
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name()); 
			audit.setObjectId(order.getOrderId()); 
			audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name()); 
			audit.setAuditSubtype(OrderEnum.AUDIT_SUB_TYPE.REMINDER_LAST_PAY.name());
//			audit.setOperatorName("SYSTEM"); 
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()); 
			audit.setCreateTime(Calendar.getInstance().getTime()); 
			audit.setCompleteTime(Calendar.getInstance().getTime()); 
			audit.setUpdateTime(Calendar.getInstance().getTime()); 
//			if("SYSTEM".equals(audit.getOperatorName()))// 标记为系统自动过
//				audit.setAuditFlag("SYSTEM");
			orderAuditService.saveAudit(audit);
			logger.info("insert audit:{}", JSONUtil.bean2Json(audit));
			// 分到人
			distributionBusiness.makeOrderAudit(audit);
			// 自动过
//			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name()); 
			orderAuditService.updateByPrimaryKey(audit);
			logger.info("make audit end.{}", JSONUtil.bean2Json(audit));
//		}
	}

}
