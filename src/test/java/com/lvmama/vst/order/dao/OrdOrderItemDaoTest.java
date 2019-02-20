package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * @author chenlizhao
*/

public class OrdOrderItemDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	private Long id = 41L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderItem ordOrderItem = ordOrderItemDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderItem);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderItem ordOrderItem = new OrdOrderItem();
		ordOrderItem.setOrderItemId(id);
		ordOrderItem.setBuCode("OB");
		int i = ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItem);
		Assert.assertTrue(i > 0);
	}
}