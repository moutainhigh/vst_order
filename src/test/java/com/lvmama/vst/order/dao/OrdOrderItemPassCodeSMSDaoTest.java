package com.lvmama.vst.order.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;

/**
 * @author chenlizhao
*/

public class OrdOrderItemPassCodeSMSDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderItemPassCodeSMSDao ordOrderItemPassCodeSMSDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdOrderItemPassCodeSMS sms = new OrdOrderItemPassCodeSMS();
		sms.setOrderId(1L);
		sms.setPassCodeId(1L);
		sms.setStatus("Y");
		int i = ordOrderItemPassCodeSMSDao.insert(sms);
		Assert.assertTrue(i > 0);
		id = ordOrderItemPassCodeSMSDao.get("selectSmsId");
		Assert.assertNotNull(id); 
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		List<OrdOrderItemPassCodeSMS> ordOrderItemPassCodeSMS = ordOrderItemPassCodeSMSDao.queryByStatus("Y");
		Assert.assertTrue(ordOrderItemPassCodeSMS != null && ordOrderItemPassCodeSMS.size() > 0);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		int i = ordOrderItemPassCodeSMSDao.updateStatus(id);
		Assert.assertTrue(i > 0);
	}
}