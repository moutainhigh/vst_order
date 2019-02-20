package com.lvmama.vst.back.order.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.order.service.IOrderSmsSendService;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderSmsSendTest {
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Test
	public void testSuit() {
		Long orderId = 110L;
		try {
			
			orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId, ""));
			//orderSmsSendService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.PREPAID_RESOURCE_AUDIT,"SYSTEM");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
