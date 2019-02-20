package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdInvoiceRelation;

/**
 * @author chenlizhao
*/

public class OrdInvoiceRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdInvoiceRelationDao ordInvoiceRelationDao;
	
	private Long id = 22L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdInvoiceRelation ordInvoiceRelation = ordInvoiceRelationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordInvoiceRelation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdInvoiceRelation ordInvoiceRelation = new OrdInvoiceRelation();
		ordInvoiceRelation.setInvoiceRelationId(id);
		ordInvoiceRelation.setOrdInvoiceId(2L);
		int i = ordInvoiceRelationDao.updateByPrimaryKeySelective(ordInvoiceRelation);
		Assert.assertTrue(i > 0);
	}
}