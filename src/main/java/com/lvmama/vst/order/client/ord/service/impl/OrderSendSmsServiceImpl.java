package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.sms.SmsMMS;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.vo.order.OrderWechatAppVo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSendWechatService;
import com.lvmama.vst.order.service.OrdWechatAppService;

@Component("orderSmsSendServiceRemote")
public class OrderSendSmsServiceImpl implements com.lvmama.vst.back.client.ord.service.OrderSendSMSService{

	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	@Autowired
	IOrderLocalService orderLocalService;
	
	@Autowired
	private OrdWechatAppService ordWechatAppService;
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
    IOrderSendWechatService orderSendWechatService;
	
	@Override
	public String getTicketCertContent(Long orderId, List<Long> orderItemList, Map<String, Object> map) {
		return orderSendSmsService.getTicketCertContent(orderId, orderItemList, map);
	}
	
	@Override
	public String getMessageContent(Long orderId) {
		return orderSendSmsService.getMessageContent(orderId);
	}		

	@Override
	public void sendSMS(String content, String mobile, Long orderId) {
		/* update by xiexun 此处传入业务类型标识调用方
		 * orderSendSmsService.sendSMS(content, mobile, orderId);*/
		orderSendSmsService.sendSMS(content, mobile, "OTHER", orderId);
	}

	@Override
	public void sendSMS(SmsMMS sms, String mobile) {
		orderSendSmsService.sendSMS(sms, mobile);
	}

	@Override
	public Long sendSms(Long orderId, SEND_NODE sendNode, String operate)
			throws BusinessException {
		return orderSendSmsService.sendSms(orderId, sendNode, operate);
	}
	
	/**
	 * 订单凭证短信接口
	 * @param orderId
	 */
	public void sendSms(Long orderId){
		orderSendSmsService.sendSms(orderId);
	}
	
	/**
	 * 门票订单是否需要发送凭证短信接口
	 * @param orderId
	 * @return
	 */
	public boolean isShouldSendCertOfTicket(Long orderId){
		return orderSendSmsService.isShouldSendCertOfTicket(orderId);
	}

	@Override
	public void sendSms(Long orderId, String mobile, String sendNode,Map<String, Object> params) {
		orderSendSmsService.sendSms(orderId, mobile, sendNode, params);
		
	}

	//VST申请退款短信消息
	@Override
	public void sendOrderRefundApplyMessage(Long orderId) {
		orderMessageProducer.sendMsg(MessageFactory.newOrderRefundApplyMessage(orderId));
	}

	//订单取消申请发送短信
	@Override
	public void sendCancelOrderApplyMessage(Long orderId) {
		orderMessageProducer.sendMsg(MessageFactory.newCancelOrderApplyMessage(orderId));
	}


	@Override
	public String getExpressContentAndSend(Long orderId, Map<String, Object> map) {
		return orderSendSmsService.getExpressContentAndSend(orderId,map);
	}

	@Override
	public void insertWechatApp(OrderWechatAppVo orderWechatApp) {
		ordWechatAppService.insert(orderWechatApp);
	}
}
