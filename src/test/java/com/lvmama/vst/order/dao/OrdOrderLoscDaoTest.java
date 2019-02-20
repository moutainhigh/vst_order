package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderLosc;

/**
 * @author chenlizhao
*/

public class OrdOrderLoscDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderLoscDao ordOrderLoscDao;
	
	private Long id = 344L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderLosc ordOrderLosc = ordOrderLoscDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderLosc);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdOrderLosc ordOrderLosc = new OrdOrderLosc();
		ordOrderLosc.setOrderLoscId(id);
		ordOrderLosc.setLoscId("111111");
		int i = ordOrderLoscDao.updateByPrimaryKeySelective(ordOrderLosc);
		Assert.assertTrue(i > 0);
	}
}