package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderDownpay;

/**
 * @author chenlizhao
*/

public class OrdOrderDownpayDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderDownpayDao ordOrderDownpayDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdOrderDownpay downpay = new OrdOrderDownpay();
		downpay.setOrderId(1L);
		int i = ordOrderDownpayDao.insert(downpay);
		Assert.assertTrue(i > 0);
		id = ordOrderDownpayDao.get("selectPayId");
		Assert.assertNotNull(id);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdOrderDownpay ordOrderDownpay = ordOrderDownpayDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordOrderDownpay);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(id);
		OrdOrderDownpay ordOrderDownpay = new OrdOrderDownpay();
		ordOrderDownpay.setOrderDownpayId(id);
		ordOrderDownpay.setPayStatus("ok");
		int i = ordOrderDownpayDao.updateByPrimaryKeySelective(ordOrderDownpay);
		Assert.assertTrue(i > 0);
	}
}