package com.lvmama.vst.back.order.processer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.processer.OrderSmsSendProcesser;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:applicationContext-vst-order-beans.xml"})
public class OrderSmsSendProcesserTest{
	@Autowired
	private OrderSmsSendProcesser orderSmsSendProcesser;
	
	@Before
	public void before() {
		
	}
	@Test
	public void test() {
		testOrderCreateMsg(); 
//		testOrderResourceMsg();
//		testCancelOrderMsg();
//		testCancelOrderMsg();
	}
	
	public void testOrderCreateMsg() {
		
		Long orderId=20022290L;
		orderSmsSendProcesser.process(MessageFactory.newOrderCreateMessage(orderId));
		
		
	}
	public void testOrderResourceMsg() {
		
		Long orderId=429L;
		orderSmsSendProcesser.process(MessageFactory.newOrderResourceStatusMessage(orderId,OrderEnum.RESOURCE_STATUS.AMPLE.getCode()));
		
		
	}
	public void testCancelOrderMsg() {
		
		Long orderId=544L;
		orderSmsSendProcesser.process(MessageFactory.newOrderCancelMessage(orderId,null));
		
		
	}
	public void testOrderPaymentMsg() {
		
		Long orderId=467L;
		orderSmsSendProcesser.process(MessageFactory.newOrderPaymentMessage(orderId,null));
		
		
	}
	

	

}