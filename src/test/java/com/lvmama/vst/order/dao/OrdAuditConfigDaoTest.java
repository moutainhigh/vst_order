package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAuditConfig;

/**
 * @author chenlizhao
*/

public class OrdAuditConfigDaoTest extends OrderTestBase {
	@Autowired
	private OrdAuditConfigDao ordAuditConfigDao;
	
	private Long id = 68L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAuditConfig ordAuditConfig = ordAuditConfigDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAuditConfig);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAuditConfig ordAuditConfig = new OrdAuditConfig();
		ordAuditConfig.setOrdAuditConfigId(id);
		ordAuditConfig.setOperatorName("op");
		int i = ordAuditConfigDao.updateByPrimaryKeySelective(ordAuditConfig);
		Assert.assertTrue(i > 0);
	}
}