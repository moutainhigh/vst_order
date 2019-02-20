package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdItemAdditionStatus;

/**
 * @author chenlizhao
*/

public class OrdItemAdditionStatusDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemAdditionStatusDAO ordItemAdditionStatusDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdItemAdditionStatus OrdItemAdditionStatus = new OrdItemAdditionStatus();
		OrdItemAdditionStatus.setOrderItemId(1L);
		int i = ordItemAdditionStatusDao.insert(OrdItemAdditionStatus);
		Assert.assertTrue(i > 0);
		id = ordItemAdditionStatusDao.get("selectStatusId");
		Assert.assertNotNull(id);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(id);
		int i = ordItemAdditionStatusDao.deleteByPrimaryKey(id);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdItemAdditionStatus ordItemAdditionStatus = ordItemAdditionStatusDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordItemAdditionStatus);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(id);
		OrdItemAdditionStatus ordItemAdditionStatus = new OrdItemAdditionStatus();
		ordItemAdditionStatus.setOrdItemAdditionStatusId(id);
		ordItemAdditionStatus.setStatus("ok");
		int i = ordItemAdditionStatusDao.updateByPrimaryKeySelective(ordItemAdditionStatus);
		Assert.assertTrue(i > 0);
	}
}