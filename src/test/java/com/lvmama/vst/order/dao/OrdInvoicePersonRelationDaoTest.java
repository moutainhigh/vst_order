package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdInvoicePersonRelation;

/**
 * @author chenlizhao
*/

public class OrdInvoicePersonRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdInvoicePersonRelationDao ordInvoicePersonRelationDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdInvoicePersonRelation OrdInvoicePersonRelation = new OrdInvoicePersonRelation();
		OrdInvoicePersonRelation.setOrdInvoiceId(1L);
		int i = ordInvoicePersonRelationDao.insert(OrdInvoicePersonRelation);
		Assert.assertTrue(i > 0);
		id = ordInvoicePersonRelationDao.get("selectRelId");
		Assert.assertNotNull(id);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(id);
		int i = ordInvoicePersonRelationDao.deleteByPrimaryKey(id);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdInvoicePersonRelation ordInvoicePersonRelation = ordInvoicePersonRelationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordInvoicePersonRelation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(id);
		OrdInvoicePersonRelation ordInvoicePersonRelation = new OrdInvoicePersonRelation();
		ordInvoicePersonRelation.setInvoicePersonRelationId(id);
		ordInvoicePersonRelation.setOrdInvoiceId(2L);
		int i = ordInvoicePersonRelationDao.updateByPrimaryKeySelective(ordInvoicePersonRelation);
		Assert.assertTrue(i > 0);
	}
}