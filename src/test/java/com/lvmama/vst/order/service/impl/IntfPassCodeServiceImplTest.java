/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.passport.po.PassCode;
import com.lvmama.vst.order.service.IntfPassCodeService;

/**
 * @author chenlizhao
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class IntfPassCodeServiceImplTest {
	@Autowired
	private IntfPassCodeService intfPassCodeService;
	
	@Test
    public void testGetOrderIdByPassCodeId() {
		Long orderId = intfPassCodeService.getOrderIdByPassCodeId(2086L);
		Assert.assertNotNull(orderId);
		System.out.println("return order id: " + orderId);
	}
}
