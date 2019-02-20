package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdInvoice;

/**
 * @author chenlizhao
*/

public class OrdInvoiceDaoTest extends OrderTestBase {
	@Autowired
	private OrdInvoiceDao ordInvoiceDao;
	
	private Long id = 22L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdInvoice ordInvoice = ordInvoiceDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordInvoice);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdInvoice ordInvoice = new OrdInvoice();
		ordInvoice.setOrdInvoiceId(id);
		ordInvoice.setBuyerAddress("shanghai");
		int i = ordInvoiceDao.updateByPrimaryKeySelective(ordInvoice);
		Assert.assertTrue(i > 0);
	}
}