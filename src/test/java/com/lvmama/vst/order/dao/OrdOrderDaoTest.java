package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * @author chenlizhao
*/

public class OrdOrderDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	private Long id = 46L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrder ordOrder = ordOrderDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrder);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrder ordOrder = new OrdOrder();
		ordOrder.setOrderId(id);
		ordOrder.setReason("test");
		int i = ordOrderDao.updateByPrimaryKeySelective(ordOrder);
		Assert.assertTrue(i > 0);
	}
}