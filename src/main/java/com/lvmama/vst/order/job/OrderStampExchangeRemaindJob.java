/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.stamp.vo.StampCode;
import com.lvmama.comm.stamp.vo.StampOrderDetails;
import com.lvmama.comm.stamp.vo.StampRemindCustomerTimeSlot;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.utils.RestClient;

/**
 * 处理催预售券兑换的订单
 * @author baolm
 *
 */
public class OrderStampExchangeRemaindJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrderStampExchangeRemaindJob.class);

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
	
	@Override
    public void run() {
        if (!Constant.getInstance().isJobRunnable()) {
            return;
        }
        logger.info("OrderStampExchangeRemaindJob start...");
        List<OrdOrder> list = orderUpdateService.queryStampExchangeRemainOrder();
        if (list == null) {
            logger.info("OrderStampExchangeRemaindJob list is null");
            return;
        }
        logger.info("OrderStampExchangeRemaindJob size:{}", list.size());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (OrdOrder order : list) {

            String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/{orderId}";
            StampOrderDetails stampOrder = null;
            try {
                logger.info("get order stamp codes, orderId=" + order.getOrderId());
                stampOrder = RestClient.getClient().getForObject(url, StampOrderDetails.class,
                        String.valueOf(order.getOrderId()));
                logger.info("get order stamp codes, orderId=" + order.getOrderId());
            } catch (Throwable e) {
                logger.error("get order stamp codes error, orderId:" + order.getOrderId(), e);
            }
            
            if(stampOrder == null || CollectionUtils.isEmpty(stampOrder.getStampCodes()))
                continue;
            
            // check stamp code status
            boolean needRemind = false;
            for (StampCode code : stampOrder.getStampCodes()) {
                if (StringUtils.equals(code.getStampStatus(), "UNUSE")) {
                    needRemind = true;
                    break;
                }
            }
            
            if (!needRemind)
                continue;
            
            // check remind time slot
            logger.info("orderId:{}, remindCustomerTimeSlot:{}", order.getOrderId(), 
                    JSONUtil.bean2Json(stampOrder.getRemindCustomerTimeslot()));
            needRemind = checkRemindDate(stampOrder.getRemindCustomerTimeslot());
            if (!needRemind)
                continue;

            OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
            logger.info("OrderStampExchangeRemaindJob, orderId={},{}", ordOrder.getOrderId(), ordOrder.hasCanceled());
            if (ordOrder != null && ordOrder.hasCanceled()) {
                continue;
            }

            try {
                addAudit(ordOrder);
                // makeAudit(order);
                // String version = Constant.getInstance().getProperty("orderSms.version");
                // logger.info("orderSms.version:{}", version);
                // if("old".equals(version)){//旧
                // orderSmsSendService.sendSms(order.getOrderId(),OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND);
                // }else{//新
                // // 催支付
                // AbstractSms sms = new StmapRemindLastPaySms();
                // //取到短信发送规则
                // List<String> smsNodeList = sms.exeSmsRule(order);
                // logger.info("smsNodeList:{}", JSONUtil.bean2Json(smsNodeList));
                // //有短信发送
                // if(smsNodeList != null && smsNodeList.size() > 0){
                // for(String smsNode : smsNodeList){
                // orderSendSmsService.sendSms(order.getOrderId(), OrdSmsTemplate.SEND_NODE.valueOf(smsNode));
                // }
                // }
                // }
                // 发送jms消息给驴途使用
                // sendMessageToLVTU(order.getOrderId());
            } catch (Exception ex) {
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
		
		logger.info("add stamp remind exchange stamp audit：orderId={}", order.getOrderId());
		Date now = Calendar.getInstance().getTime();
		ComAudit audit = new ComAudit();
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setObjectId(order.getOrderId());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
		audit.setAuditSubtype(OrderEnum.AUDIT_SUB_TYPE.REMINDER_EXCHANGE_STAMP.name());
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setCreateTime(now);
		audit.setUpdateTime(now);
		orderAuditService.saveAudit(audit);

		ComMessage comMessage=new ComMessage();
		comMessage.setAuditId(audit.getAuditId());
		comMessage.setCreateTime(now);
		comMessage.setMessageContent("新增预定通知-催兑换提醒");
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
/*	
	private void makeAudit(OrdOrder order) {
		ComAudit audit = new ComAudit();
		audit.setObjectId(order.getOrderId());
		audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		audit.setCreateTime(new Date());
		audit.setAuditType(OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name());
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		orderAuditService.saveAudit(audit);
		
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
			audit.setAuditSubtype(OrderEnum.AUDIT_SUB_TYPE.REMINDER_EXCHANGE_STAMP.name());
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
*/
    private boolean checkRemindDate(StampRemindCustomerTimeSlot remindTime) {
        
        if (remindTime == null)
            return false;

        String startDate = remindTime.getStartDate();
        String endDate = remindTime.getEndDate();
        if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)
                || CollectionUtils.isEmpty(remindTime.getWeekDays()))
            return false;

        Date sdate = null;
        Date edate = null;
        try {
            sdate = DateUtil.toDate(startDate, DateUtil.PATTERN_yyyy_MM_dd);
            edate = DateUtil.toDate(endDate, DateUtil.PATTERN_yyyy_MM_dd);
        } catch (RuntimeException e) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        int weekDay = now.get(Calendar.DAY_OF_WEEK);
        if (!remindTime.getWeekDays().contains(String.valueOf(weekDay)))
            return false;

        if (now.getTime().compareTo(sdate) < 0)
            return false;

        now.add(Calendar.DATE, -1);
        if (now.getTime().compareTo(edate) > 0)
            return false;

        return true;
    }

}
