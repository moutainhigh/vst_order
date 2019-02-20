package com.lvmama.vst.order.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderEcontractGeneratorServiceTest {

	@Autowired
	private OrderEcontractGeneratorService econtractGeneratorService;
	
	@Test
	public void testGenerateEcontract() {
		Long orderId111=20015320L;
		econtractGeneratorService.generateEcontract(orderId111, "system");
	}

}
