package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;

/**
 * @author chenlizhao
*/

public class OrdAuditUserStatusDaoTest extends OrderTestBase {
	@Autowired
	private OrdAuditUserStatusDAO ordAuditUserStatusDao;
	
	private String name = "cs5562";
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAuditUserStatus ordAuditUserStatus = ordAuditUserStatusDao.selectByPrimaryKey(name);
		Assert.assertNotNull(ordAuditUserStatus);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAuditUserStatus ordAuditUserStatus = new OrdAuditUserStatus();
		ordAuditUserStatus.setOperatorName(name);
		ordAuditUserStatus.setUserStatus("ok");
		int i = ordAuditUserStatusDao.updateByPrimaryKeySelective(ordAuditUserStatus);
		Assert.assertTrue(i > 0);
	}
}