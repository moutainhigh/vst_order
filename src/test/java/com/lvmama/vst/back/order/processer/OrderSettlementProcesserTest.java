package com.lvmama.vst.back.order.processer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.processer.OrderSettlementProcesser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderSettlementProcesserTest{

	@Autowired
	private OrderSettlementProcesser orderSettlementProcesser;
	
	
	@Test
	public void testSuit() {
		testOrderRefumentMsg();
//		testCancelOrderMsg();
	}
	
	@Test
	public void testOrderPaymentMsg() {
		Long orderId = 20022957L;
		OrdOrder order=new OrdOrder();
		order.setOrderId(orderId);
		orderSettlementProcesser.process(MessageFactory.newOrderPaymentMessage(orderId, ""));
		
	}
	
	public void testOrderRefumentMsg() {
		
		Long refundmentId=185117L;
		orderSettlementProcesser.process(MessageFactory.newOrderRefundedSuccessMessage(refundmentId));
		
		
	}

	

}
