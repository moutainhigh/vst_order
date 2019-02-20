package com.lvmama.vst.order.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;

/**
 * 
 * @author lancey
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class SupplierOrderOperatorImplTest {
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;

	@Test
	public void testCreateSupplierOrder() {
		Long orderId=29641L;
		OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(orderId);
		Assert.assertNotNull(result);
	}

}
