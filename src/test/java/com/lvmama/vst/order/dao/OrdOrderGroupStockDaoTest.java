package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;

/**
 * @author chenlizhao
*/

public class OrdOrderGroupStockDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderGroupStockDao ordOrderGroupStockDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderGroupStock ordOrderGroupStock = ordOrderGroupStockDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderGroupStock);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderGroupStock ordOrderGroupStock = new OrdOrderGroupStock();
		ordOrderGroupStock.setOrderGroupStockId(id);
		ordOrderGroupStock.setQuantity(2L);
		int i = ordOrderGroupStockDao.updateByPrimaryKeySelective(ordOrderGroupStock);
		Assert.assertTrue(i > 0);
	}
}