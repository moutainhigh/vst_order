package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderStock;

/**
 * @author chenlizhao
*/

public class OrdOrderStockDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderStockDao ordOrderStockDao;
	
	private Long id = 89L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderStock ordOrderStock = ordOrderStockDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderStock);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderStock ordOrderStock = new OrdOrderStock();
		ordOrderStock.setOrderStockId(id);
		ordOrderStock.setResourceStatus("OK");
		int i = ordOrderStockDao.updateByPrimaryKeySelective(ordOrderStock);
		Assert.assertTrue(i > 0);
	}
}