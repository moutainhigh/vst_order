package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;


/**
 * 出团通知
 * @author zhaomingzhu
 *
 */
public class OrderGroupNotifySms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderGroupNotifySms.class);
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderGroupNotifySms ===>>> 找到出团通知对应规则"+"orderidexeSmsRule="+order.getOrderId());
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.出团通知触发的短信----出团通知书模块，发送出团通知后，自动发送该短信
		sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_GROUP_NOTICE_SENT.name());
		
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
