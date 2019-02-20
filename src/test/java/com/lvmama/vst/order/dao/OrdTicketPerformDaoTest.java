package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdTicketPerform;

/**
 * @author chenlizhao
*/

public class OrdTicketPerformDaoTest extends OrderTestBase {
	@Autowired
	private OrdTicketPerformDao ordTicketPerformDao;
	
	private Long id = 25L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdTicketPerform ordTicketPerform = ordTicketPerformDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordTicketPerform);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdTicketPerform ordTicketPerform = new OrdTicketPerform();
		ordTicketPerform.setTicketPerformId(id);
		ordTicketPerform.setMemo("test");
		int i = ordTicketPerformDao.updateByPrimaryKeySelective(ordTicketPerform);
		Assert.assertTrue(i > 0);
	}
}