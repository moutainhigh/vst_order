package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;

/**
 * @author chenlizhao
*/

public class OrdOrderAmountItemDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderAmountItemDao ordOrderAmountItemDao;
	
	private Long id = 129L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderAmountItem ordOrderAmountItem = ordOrderAmountItemDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderAmountItem);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
		ordOrderAmountItem.setOrderAmountItemId(id);
		ordOrderAmountItem.setOrderAmountType("ORIGINAL_SETTLEPRICE");
		int i = ordOrderAmountItemDao.updateByPrimaryKeySelective(ordOrderAmountItem);
		Assert.assertTrue(i > 0);
	}
}