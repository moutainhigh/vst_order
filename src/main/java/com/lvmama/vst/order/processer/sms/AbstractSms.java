package com.lvmama.vst.order.processer.sms;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * 抽象短信
 * @author zhaomingzhu
 *
 */
public interface AbstractSms {

	/* 执行短信规则  */
	public abstract List<String> exeSmsRule (OrdOrder order);
	
	/* 填充短信内容  */
	public abstract String fillSms(String content,OrdOrder order);
	
}
