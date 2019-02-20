package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdStatusGroup;

/**
 * @author chenlizhao
*/

public class OrdStatusGroupDaoTest extends OrderTestBase {
	@Autowired
	private OrdStatusGroupDao ordStatusGroupDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdStatusGroup ordStatusGroup = ordStatusGroupDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordStatusGroup);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdStatusGroup ordStatusGroup = new OrdStatusGroup();
		ordStatusGroup.setStatusGroupId(id);
		ordStatusGroup.setFileds("20008011,20008012");
		int i = ordStatusGroupDao.updateByPrimaryKeySelective(ordStatusGroup);
		Assert.assertTrue(i > 0);
	}
}