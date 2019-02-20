package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;

/**
 * @author chenlizhao
*/

public class OrdAccInsDelayInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdAccInsDelayInfoDao ordAccInsDelayInfoDao;
	
	private Long id = 1003L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAccInsDelayInfo);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		OrdAccInsDelayInfo ordAccInsDelayInfo = new OrdAccInsDelayInfo();
		ordAccInsDelayInfo.setOrdAccInsDelayInfoId(id);
		ordAccInsDelayInfo.setTravDelayStatus("ok");
		int i = ordAccInsDelayInfoDao.updateByPrimaryKey(ordAccInsDelayInfo);
		Assert.assertTrue(i > 0);
	}
}