package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPassCode;

/**
 * @author chenlizhao
*/

public class OrdPassCodeDaoTest extends OrderTestBase {
	@Autowired
	private OrdPassCodeDao ordPassCodeDao;
	
	private Long id = 2000008988L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPassCode ordPassCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(id);
		Assert.assertNotNull(ordPassCode);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdPassCode ordPassCode = new OrdPassCode();
		ordPassCode.setPassCodeId(21L);
		ordPassCode.setAddCode("348427");
		int i = ordPassCodeDao.update(ordPassCode);
		Assert.assertTrue(i > 0);
	}
}