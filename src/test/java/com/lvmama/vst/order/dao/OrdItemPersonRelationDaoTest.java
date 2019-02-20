package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;

/**
 * @author chenlizhao
*/

public class OrdItemPersonRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemPersonRelationDao ordItemPersonRelationDao;
	
	private Long id = 43L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdItemPersonRelation ordItemPersonRelation = ordItemPersonRelationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordItemPersonRelation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdItemPersonRelation ordItemPersonRelation = new OrdItemPersonRelation();
		ordItemPersonRelation.setItemPersionRelationId(id);
		ordItemPersonRelation.setOptionContent("test");
		int i = ordItemPersonRelationDao.updateByPrimaryKeySelective(ordItemPersonRelation);
		Assert.assertTrue(i > 0);
	}
}