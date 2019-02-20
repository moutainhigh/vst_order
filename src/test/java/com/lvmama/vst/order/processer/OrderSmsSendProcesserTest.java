package com.lvmama.vst.order.processer;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderSendSmsService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderSmsSendProcesserTest {

	@Autowired
	private OrderSmsSendProcesser orderSmsSendProcesser;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	@Test
	public void testProcess() {
		Long orderId=20047190L;
		OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
//		orderSmsSendProcesser.handle(MessageFactory.newOrderCreateMessage(orderId),order);
//		orderSmsSendProcesser.handle(MessageFactory.newOrderResourceStatusMessage(orderId, "AMPLE"), order);
		orderSmsSendProcesser.handle(MessageFactory.newOrderPaymentMessage(orderId, "PAYED"), order);
	}

	@Test
	public void testSendSms(){
		Long orderId=20047190L;
		orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.VERIFIED_PAYED_PREPAID, "admin");
		//orderSendSmsService.sendSms(orderId);
	}
	
	public static void main(String[] args) {
		Long category = 181L;
		Long subCategory = 181L;
		System.out.println(category == subCategory);
	}
}
