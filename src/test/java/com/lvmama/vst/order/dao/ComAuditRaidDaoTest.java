/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComAuditRaid;

/**
 * @author chenlizhao
 *
 */
public class ComAuditRaidDaoTest extends OrderTestBase {
	@Autowired
	private ComAuditRaidDao comAuditRaidDao;

	private Long raidId;
	
	@Before
	public void testInsert() throws Exception {
		ComAuditRaid raid = new ComAuditRaid();
		raid.setAuditId(8867L);
		raid.setBuCode("OB");
		raid.setContactName("test");
		raid.setStockFlag("Y");
		raid.setSupplierId(3411L);
		int i = comAuditRaidDao.insert(raid);
		Assert.assertTrue(i > 0);
		raidId = comAuditRaidDao.get("selectRaidId");
		Assert.assertNotNull(raidId);
	}

	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(raidId);
		ComAuditRaid raid = comAuditRaidDao.selectByPrimaryKey(raidId);
		Assert.assertTrue(raid != null && raid.getUpdateTime() != null);
	}
	
	@Test
	public void testQueryAuditListByCondition() throws Exception {
		Assert.assertNotNull(raidId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("auditRaidId", raidId);
		List<ComAuditRaid> raids = comAuditRaidDao.queryAuditListByCondition(params);
		Assert.assertTrue(raids != null && raids.size() > 0);
		System.out.println("return raid size: " + raids.size());
		for(ComAuditRaid raid : raids) {
			Assert.assertNotNull(raid.getUpdateTime());
		}
	}
}
