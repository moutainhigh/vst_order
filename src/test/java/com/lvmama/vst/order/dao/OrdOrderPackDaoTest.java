package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderPack;

/**
 * @author chenlizhao
*/

public class OrdOrderPackDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	
	private Long id = 42L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderPack ordOrderPack = ordOrderPackDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderPack);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderPack ordOrderPack = new OrdOrderPack();
		ordOrderPack.setOrderPackId(id);
		ordOrderPack.setBuCode("OB");
		int i = ordOrderPackDao.updateByPrimaryKeySelective(ordOrderPack);
		Assert.assertTrue(i > 0);
	}
}