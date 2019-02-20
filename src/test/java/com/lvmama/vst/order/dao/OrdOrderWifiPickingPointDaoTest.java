package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;

/**
 * @author chenlizhao
*/

public class OrdOrderWifiPickingPointDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderWifiPickingPointDao ordOrderWifiPickingPointDao;
	
	private Long id = 84L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderWifiPickingPoint ordOrderWifiPickingPoint = ordOrderWifiPickingPointDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderWifiPickingPoint);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderWifiPickingPoint ordOrderWifiPickingPoint = new OrdOrderWifiPickingPoint();
		ordOrderWifiPickingPoint.setOrdPickingPointId(id);
		ordOrderWifiPickingPoint.setDistrictId(13L);
		int i = ordOrderWifiPickingPointDao.updateByPrimaryKeySelective(ordOrderWifiPickingPoint);
		Assert.assertTrue(i > 0);
	}
}