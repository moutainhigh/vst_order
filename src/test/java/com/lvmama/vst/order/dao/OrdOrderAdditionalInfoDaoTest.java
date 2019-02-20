package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderAdditionalInfo;

/**
 * @author chenlizhao
*/

public class OrdOrderAdditionalInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderAdditionalInfoDao ordOrderAdditionalInfoDao;
	
	@Test
	public void testInsert() throws Exception {
		OrdOrderAdditionalInfo ordOrderAdditionalInfo = new OrdOrderAdditionalInfo();
		ordOrderAdditionalInfo.setOrderId(1L);
		ordOrderAdditionalInfo.setOrderDestination(1L);
		int i = ordOrderAdditionalInfoDao.insert(ordOrderAdditionalInfo);
		Assert.assertTrue(i > 0);
	}
}