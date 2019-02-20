package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdItemReschedule;

/**
 * @author chenlizhao
*/

public class OrdItemRescheduleDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemRescheduleDao ordItemRescheduleDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdItemReschedule sched = new OrdItemReschedule();
		sched.setOrderItemId(1L);
		int i = ordItemRescheduleDao.insert(sched);
		Assert.assertTrue(i > 0);
		id = ordItemRescheduleDao.get("selectSchedId");
		Assert.assertNotNull(id);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(id);
		int i = ordItemRescheduleDao.deleteByPrimaryKey(id);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdItemReschedule ordItemReschedule = ordItemRescheduleDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordItemReschedule);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(id);
		OrdItemReschedule ordItemReschedule = new OrdItemReschedule();
		ordItemReschedule.setOrdItemRescheduleId(id);
		ordItemReschedule.setMemo("test");
		int i = ordItemRescheduleDao.updateByPrimaryKeySelective(ordItemReschedule);
		Assert.assertTrue(i > 0);
	}
}