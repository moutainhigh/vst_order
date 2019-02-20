package com.lvmama.vst.order.client.ord.service.impl;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderWorkflowService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.IOrderUpdateService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderWorkflowServiceImplTest {
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private OrderWorkflowService orderWorkflowService;

	@Test
	public void testAddTicketPerformInfo() {
		long orderItemId=28081L;
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		Assert.assertNotNull(orderItem);
		orderWorkflowService.addTicketPerformInfo(orderItemId);
	}

}
