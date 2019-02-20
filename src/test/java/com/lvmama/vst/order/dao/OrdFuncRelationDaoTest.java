package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdFuncRelation;

/**
 * @author chenlizhao
*/

public class OrdFuncRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdFuncRelationDao ordFuncRelationDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdFuncRelation ordFuncRelation = new OrdFuncRelation();
		ordFuncRelation.setOrdFunctionId(1L);
		int i = ordFuncRelationDao.insert(ordFuncRelation);
		Assert.assertTrue(i > 0);
		id = ordFuncRelationDao.get("selectRelId");
		Assert.assertNotNull(id);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(id);
		int i = ordFuncRelationDao.deleteByPrimaryKey(id);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdFuncRelation ordFuncRelation = ordFuncRelationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordFuncRelation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdFuncRelation ordFuncRelation = new OrdFuncRelation();
		ordFuncRelation.setOrdFunctionRelationId(id);
		ordFuncRelation.setCategoryId(1L);
		int i = ordFuncRelationDao.updateByPrimaryKeySelective(ordFuncRelation);
		Assert.assertTrue(i > 0);
	}
}