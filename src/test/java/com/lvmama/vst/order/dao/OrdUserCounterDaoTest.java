package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;

/**
 * @author chenlizhao
*/

public class OrdUserCounterDaoTest extends OrderTestBase {
	@Autowired
	private OrdUserCounterDao ordUserCounterDao;
	
	@Test
	public void testUpdate() throws Exception {
		int i = ordUserCounterDao.increase("lv5789", "ORDER_ITEM");
		Assert.assertTrue(i > 0);
	}
}