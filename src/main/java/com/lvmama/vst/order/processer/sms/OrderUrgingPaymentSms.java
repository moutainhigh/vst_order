package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.order.utils.OrderUtils;

/**
 * 催支付
 * @author zhaomingzhu
 *
 */
public class OrderUrgingPaymentSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderUrgingPaymentSms.class);
	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderUrgingPaymentSms ===>>> isPrepaid(order)=" + isPrepaid(order)
				+"orderidexeSmsRule="+order.getOrderId());
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.常规-邻近最晚支付等待时间提醒 半小时+预付
		if(isPrepaid(order)){
			if(order!=null) {
				logger.info("OrderUrgingPaymentSms hasOutAndFreed: orderId" + order.getOrderId() + "," + order.getBuCode() + "," + order.getCategoryId());
			}
			if (OrderUtils.hasOutAndFreed(order)) {
				sendList.add(OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND_OUTBOUND_FREED.name());
			}else {
				sendList.add(OrdSmsTemplate.SEND_NODE.COMMON_PAY_WAIT_TIME_REMIND.name());
			}
		}
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		return sendList;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}
}
