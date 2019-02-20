package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;

/**
 * 退款申请
 * @author xujibai
 *
 */
public class RefundApplySms implements AbstractSms {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(RefundApplySms.class);

	// 支付对象(预付)
	public boolean isPrepaid(OrdOrder order) {
		if (order.hasNeedPrepaid()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("RefundApplySms isPrepaid(order)=" + isPrepaid(order)+"orderidexeSmsRule="+order.getOrderId());
		// 发送规则列表
		List<String> sendList = new ArrayList<String>();
		// 不发送规则列表
		List<String> noneSendList = new ArrayList<String>();

		logger.info("退款申请"+order);
		if(isPrepaid(order)){	
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_REFUND_APPLY.name());
		}
		if (noneSendList.size() > 0) {
			for (String noneSend : noneSendList) {
				if (sendList.contains(noneSend)) {
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
