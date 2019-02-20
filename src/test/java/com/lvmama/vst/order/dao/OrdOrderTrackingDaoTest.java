package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderTracking;

/**
 * @author chenlizhao
*/

public class OrdOrderTrackingDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderTrackingDao ordOrderTrackingDao;
	
	private Long id = 100299L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderTracking ordOrderTracking = ordOrderTrackingDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderTracking);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderTracking ordOrderTracking = new OrdOrderTracking();
		ordOrderTracking.setTrackingId(id);
		ordOrderTracking.setOrderStatus("PAYED");
		int i = ordOrderTrackingDao.updateByPrimaryKeySelective(ordOrderTracking);
		Assert.assertTrue(i > 0);
	}
}