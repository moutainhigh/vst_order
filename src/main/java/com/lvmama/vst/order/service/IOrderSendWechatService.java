package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdWechatTemplate;
import com.lvmama.vst.comm.web.BusinessException;

/**
 * 微信发送
 * @author zhaomingzhu
 *
 */
public interface IOrderSendWechatService{
	
	/**
	 * 发送微信
	 * @param orderId
	 * @param sendNode
	 * @throws Exception 
	 */
	Long sendSms(Long orderId, String mobile, OrdWechatTemplate.SendNode sendNode) throws Exception;
	
	/**
	 * 微信重发
	 * @param smsId
	 * @throws BusinessException
	 */
	void reSendSms(Long id)throws BusinessException;
	
}