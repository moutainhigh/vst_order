package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdFunction;

/**
 * @author chenlizhao
*/

public class OrdFunctionDaoTest extends OrderTestBase {
	@Autowired
	private OrdFunctionDao ordFunctionDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdFunction ordFunction = ordFunctionDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordFunction);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdFunction ordFunction = new OrdFunction();
		ordFunction.setOrdFunctionId(id);
		ordFunction.setFunctionCode("test");
		int i = ordFunctionDao.updateByPrimaryKeySelective(ordFunction);
		Assert.assertTrue(i > 0);
	}
}