package com.lvmama.vst.order.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.IOrderAuditService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderDistributionBusinessTest {

	@Autowired
	private OrderDistributionBusiness orderDistributionBusiness;
	
	//订单活动审核业务
	@Autowired
	private IOrderAuditService orderAuditService; 
	
	@Test
	public void testMakeOrderAudit() {
		ComAudit audit = orderAuditService.queryAuditById(73759L);
		orderDistributionBusiness.makeOrderAudit(audit);
	}

}
