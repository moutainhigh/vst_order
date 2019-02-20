package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;

/**
 * @author chenlizhao
*/

public class OrdFlightTicketStatusDaoTest extends OrderTestBase {
	@Autowired
	private OrdFlightTicketStatusDao ordFlightTicketStatusDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdFlightTicketStatus ordFlightTicketStatus = ordFlightTicketStatusDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordFlightTicketStatus);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
		ordFlightTicketStatus.setStatusId(id);
		ordFlightTicketStatus.setStatusCode("ok");
		int i = ordFlightTicketStatusDao.update(ordFlightTicketStatus);
		Assert.assertTrue(i > 0);
	}
}