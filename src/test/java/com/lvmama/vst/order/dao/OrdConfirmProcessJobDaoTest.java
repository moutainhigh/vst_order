package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.po.OrdConfirmProcessJob;

/**
 * @author chenlizhao
*/

public class OrdConfirmProcessJobDaoTest extends OrderTestBase {
	@Autowired
	private OrdConfirmProcessJobDao ordConfirmProcessJobDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdConfirmProcessJob ordConfirmProcessJob = ordConfirmProcessJobDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordConfirmProcessJob);
	}
	
	@Before
	public void testInsert() throws Exception {
		OrdConfirmProcessJob ordConfirmProcessJob = new OrdConfirmProcessJob();
		ordConfirmProcessJob.setOrderItemId(id);
		ordConfirmProcessJob.setOrderId(id);
		int i = ordConfirmProcessJobDao.insert(ordConfirmProcessJob);
		Assert.assertTrue(i > 0);
	}
}