package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;

/**
 * 电子合同修改短信
 * @author zhaomingzhu
 *
 */
public class OrderContractUpdateSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderContractUpdateSms.class);
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderContractUpdateSms ===>>> 找到对应规则"+"orderidexeSmsRule="+order.getOrderId());	
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.电子合同修改通知
		sendList.add(OrdSmsTemplate.SEND_NODE.ELECONTRACT_UPDATE.name());
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
