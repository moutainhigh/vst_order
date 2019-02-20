package com.lvmama.vst.order.processer.sms;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * 基本短信
 * @author zhaomingzhu
 *
 */
public class OrderBaseSms implements AbstractSms {
	private final static Log logger=LogFactory.getLog(OrderBaseSms.class);
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderBaseSms ===>>> 无任何规则可选"+"orderidexeSmsRule="+order.getOrderId());
		return null;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}

}
