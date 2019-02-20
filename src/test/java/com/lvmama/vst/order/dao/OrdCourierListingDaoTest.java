package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdCourierListing;

/**
 * @author chenlizhao
*/

public class OrdCourierListingDaoTest extends OrderTestBase {
	@Autowired
	private OrdCourierListingDao ordCourierListingDao;
	
	private Long id = 301L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdCourierListing ordCourierListing = ordCourierListingDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordCourierListing);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdCourierListing ordCourierListing = new OrdCourierListing();
		ordCourierListing.setCourierListingId(id);
		ordCourierListing.setExpressNumber("1");
		int i = ordCourierListingDao.updateByPrimaryKeySelective(ordCourierListing);
		Assert.assertTrue(i > 0);
	}
}