package com.lvmama.vst.back.order.processer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.processer.OrderEcontractProcesser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:applicationContext-vst-order-beans.xml")
public class OrderEcontractProcesserTest {
	@Autowired
	private OrderEcontractProcesser orderEcontractProcesser;
	
	@Test
	public void testSuit() {
		testCancelOrderMsg();
	}
	
	public void testCancelOrderMsg() {
		
		Long orderId=9581L;
		orderEcontractProcesser.process(MessageFactory.newOrderCancelMessage(orderId,null));
	}

}
