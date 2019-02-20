package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenlizhao
*/

public class PreRefundCounterDaoTest extends OrderTestBase {
	@Autowired
	private PreRefundCounterDao preRefundCounterDao;
	
	private Long id = 4999734L;
	
	@Test
	public void testUpdate() throws Exception {
		int i = preRefundCounterDao.increase(id, "15928149413");
		Assert.assertTrue(i > 0);
	}
}