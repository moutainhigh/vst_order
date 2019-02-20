package com.lvmama.vst.order.dao;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdResponsible;

/**
 * @author chenlizhao
*/

public class OrdResponsibleDaoTest extends OrderTestBase {
	@Autowired
	private OrdResponsibleDao ordResponsibleDao;
	
	private Long id = 61L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdResponsible ordResponsible = ordResponsibleDao.getResponsibleByObject(20006062L, "ORDER");
		Assert.assertNotNull(ordResponsible);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		OrdResponsible ordResponsible = new OrdResponsible();
		ordResponsible.setResponsibleId(id);
		ordResponsible.setOperatorName("clz");
		ordResponsible.setObjectId(20006062L);
		ordResponsible.setOrgId(336L);
		ordResponsible.setObjectType("ORDER");
		int i = ordResponsibleDao.updateByPrimaryKey(ordResponsible);
		Assert.assertTrue(i > 0);
	}
}