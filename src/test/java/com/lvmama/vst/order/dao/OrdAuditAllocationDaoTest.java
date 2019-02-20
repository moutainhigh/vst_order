package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAuditAllocation;

/**
 * @author chenlizhao
*/

public class OrdAuditAllocationDaoTest extends OrderTestBase {
	@Autowired
	private OrdAuditAllocationDao ordAuditAllocationDao;
	
	private Long id = 103L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAuditAllocation ordAuditAllocation = ordAuditAllocationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAuditAllocation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAuditAllocation ordAuditAllocation = new OrdAuditAllocation();
		ordAuditAllocation.setOrdAllocationId(id);
		ordAuditAllocation.setDistributionChannel("test");
		int i = ordAuditAllocationDao.updateByPrimaryKeySelective(ordAuditAllocation);
		Assert.assertTrue(i > 0);
	}
}