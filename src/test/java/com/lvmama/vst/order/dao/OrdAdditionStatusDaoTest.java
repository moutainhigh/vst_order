package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;

/**
 * @author chenlizhao
*/

public class OrdAdditionStatusDaoTest extends OrderTestBase {
	@Autowired
	private OrdAdditionStatusDAO ordAdditionStatusDao;
	
	private Long id = 929L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAdditionStatus ordAdditionStatus = ordAdditionStatusDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAdditionStatus);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAdditionStatus ordAdditionStatus = new OrdAdditionStatus();
		ordAdditionStatus.setOrdAdditionStatusId(id);
		ordAdditionStatus.setStatus("ok");
		int i = ordAdditionStatusDao.updateByPrimaryKeySelective(ordAdditionStatus);
		Assert.assertTrue(i > 0);
	}
}