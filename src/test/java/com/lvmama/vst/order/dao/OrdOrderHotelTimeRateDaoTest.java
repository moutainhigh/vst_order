package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;

/**
 * @author chenlizhao
*/

public class OrdOrderHotelTimeRateDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderHotelTimeRateDao ordOrderHotelTimeRateDao;
	
	private Long id = 89L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderHotelTimeRate ordOrderHotelTimeRate = ordOrderHotelTimeRateDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderHotelTimeRate);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderHotelTimeRate ordOrderHotelTimeRate = new OrdOrderHotelTimeRate();
		ordOrderHotelTimeRate.setHotelTimeRateId(id);
		ordOrderHotelTimeRate.setPerformFlag("N");
		int i = ordOrderHotelTimeRateDao.updateByPrimaryKeySelective(ordOrderHotelTimeRate);
		Assert.assertTrue(i > 0);
	}
}