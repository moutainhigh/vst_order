package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdApplyInvoiceInfo;

/**
 * @author chenlizhao
*/

public class OrdApplyInvoiceInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdApplyInvoiceInfoDao ordApplyInvoiceInfoDao;
	
	private Long id = 1015L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordApplyInvoiceInfo);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdApplyInvoiceInfo ordApplyInvoiceInfo = new OrdApplyInvoiceInfo();
		ordApplyInvoiceInfo.setId(id);
		ordApplyInvoiceInfo.setContent("test");
		int i = ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
		Assert.assertTrue(i > 0);
	}
}