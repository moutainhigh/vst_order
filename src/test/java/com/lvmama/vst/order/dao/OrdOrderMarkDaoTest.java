package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderMark;

/**
 * @author chenlizhao
*/

public class OrdOrderMarkDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderMarkDao ordOrderMarkDao;
	
	private Long id = 1L;
	
	@Before
	public void testInsert() throws Exception {
		OrdOrderMark orderRemark = new OrdOrderMark();
		orderRemark.setMarkFlag(1);
		orderRemark.setOrderId(id);
		int i = ordOrderMarkDao.saveOrdOrderMark(orderRemark);
		Assert.assertTrue(i > 0);
	}
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderMark ordOrderMark = ordOrderMarkDao.findOrdOrderMarkByOrderId(id);
		Assert.assertNotNull(ordOrderMark);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderMark ordOrderMark = new OrdOrderMark();
		ordOrderMark.setOrderId(id);
		ordOrderMark.setMarkFlag(2);
		int i = ordOrderMarkDao.updateOrdOrderMark(ordOrderMark);
		Assert.assertTrue(i > 0);
	}
}