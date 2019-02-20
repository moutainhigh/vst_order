package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;

/**
 * @author chenlizhao
*/

public class OrdOrderQueryInfoHistoryDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderQueryInfoHistoryDao ordOrderQueryInfoDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdOrderQueryInfo ordOrderQueryInfo = new OrdOrderQueryInfo();
		int i = ordOrderQueryInfoDao.insert(ordOrderQueryInfo);
		Assert.assertTrue(i > 0);
		id = ordOrderQueryInfoDao.get("selectHistId");
		Assert.assertNotNull(id);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdOrderQueryInfo ordOrderQueryInfo = new OrdOrderQueryInfo();
		ordOrderQueryInfo.setQueryInfoId(id);
		ordOrderQueryInfo.setBookerName("clz");
		int i = ordOrderQueryInfoDao.updateByPrimaryKey(ordOrderQueryInfo);
		Assert.assertTrue(i > 0);
	}
}