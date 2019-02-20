package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.po.OrdPayProcessJob;

/**
 * @author chenlizhao
*/

public class OrdPayProcessJobDaoTest extends OrderTestBase {
	@Autowired
	private OrdPayProcessJobDao ordPayProcessJobDao;
	
	private Long id = 200616190L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPayProcessJob ordPayProcessJob = ordPayProcessJobDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordPayProcessJob);
	}
	
	@Test
	public void testUpdate() throws Exception {
		int i = ordPayProcessJobDao.addTimes(id);
		Assert.assertTrue(i > 0);
	}
}