package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.O2oOrdInvoiceRelation;

/**
 * @author chenlizhao
*/

public class O2oOrdInvoiceRelationDaoTest extends OrderTestBase {
	@Autowired
	private O2oOrdInvoiceRelationDao o2oOrdInvoiceRelationDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		O2oOrdInvoiceRelation o2oOrdInvoiceRelation = o2oOrdInvoiceRelationDao.selectByInvoiceId(id);
		Assert.assertNotNull(o2oOrdInvoiceRelation);
	}
	
	@Before
	public void testInsertSelective() throws Exception {
		O2oOrdInvoiceRelation o2oOrdInvoiceRelation = new O2oOrdInvoiceRelation();
		o2oOrdInvoiceRelation.setInvoiceId(id);
		int i = o2oOrdInvoiceRelationDao.insertSelective(o2oOrdInvoiceRelation);
		Assert.assertTrue(i > 0);
	}
}