package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.O2oOrder;
import com.lvmama.vst.back.order.po.OrdOrderSharedStock;

/**
 * @author chenlizhao
*/

public class OrdOrderSharedStockDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderSharedStockDao ordOrderSharedStockDao;
	
	private Long id = 121L;
	
	@Test
	public void testSelectByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", 2000022407L);
		List<OrdOrderSharedStock> stockList = ordOrderSharedStockDao.selectByParams(params);
		Assert.assertTrue(stockList != null && stockList.size() > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderSharedStock ordOrderSharedStock = ordOrderSharedStockDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderSharedStock);
	}
	
}