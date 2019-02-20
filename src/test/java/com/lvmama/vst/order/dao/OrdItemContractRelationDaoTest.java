package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdItemContractRelation;

/**
 * @author chenlizhao
*/

public class OrdItemContractRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemContractRelationDao ordItemContractRelationDao;
	
	private Long id = 1666L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdItemContractRelation ordItemContractRelation = ordItemContractRelationDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordItemContractRelation);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdItemContractRelation ordItemContractRelation = new OrdItemContractRelation();
		ordItemContractRelation.setId(id);
		ordItemContractRelation.setCancelFlag("Y");
		int i = ordItemContractRelationDao.updateByPrimaryKeySelective(ordItemContractRelation);
		Assert.assertTrue(i > 0);
	}
}