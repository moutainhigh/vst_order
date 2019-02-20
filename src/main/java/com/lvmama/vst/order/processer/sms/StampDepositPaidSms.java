package com.lvmama.vst.order.processer.sms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;

/**
 * 預售券完成定金支付短信
 * @author baolm
 *
 */
public class StampDepositPaidSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(StampDepositPaidSms.class);
	
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("StampDownPaidSms ===>>> orderid="+order.getOrderId());
		//发送规则列表
		List<String> sendList = Lists.newArrayList();
		//不发送规则列表
//		List<String> noneSendList = Lists.newArrayList();
		
		//1.常规-邻近最晚支付等待时间提醒 半小时+预付
		sendList.add(OrdSmsTemplate.SEND_NODE.STAMP_DEPOSIT_PAID.name());
//		if(CollectionUtils.isNotEmpty(noneSendList)){
//			for(String noneSend : noneSendList){
//				if(sendList.contains(noneSend)){
//					sendList.remove(noneSend);
//				}
//			}
//		}
		return sendList;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}
}
