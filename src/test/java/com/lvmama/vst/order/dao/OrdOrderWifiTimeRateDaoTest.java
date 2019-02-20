package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;

/**
 * @author chenlizhao
*/

public class OrdOrderWifiTimeRateDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderWifiTimeRateDao ordOrderWifiTimeRateDao;
	
	private Long id = 110L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderWifiTimeRate ordOrderWifiTimeRate = ordOrderWifiTimeRateDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderWifiTimeRate);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderWifiTimeRate ordOrderWifiTimeRate = new OrdOrderWifiTimeRate();
		ordOrderWifiTimeRate.setWifiTimeRateId(id);
		ordOrderWifiTimeRate.setPerformFlag("test");
		int i = ordOrderWifiTimeRateDao.updateByPrimaryKeySelective(ordOrderWifiTimeRate);
		Assert.assertTrue(i > 0);
	}
}