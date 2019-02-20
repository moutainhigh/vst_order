package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdFlightTicketInfo;

/**
 * @author chenlizhao
*/

public class OrdFlightTicketInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdFlightTicketInfoDao ordFlightTicketInfoDao;
	
	private Long id;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdFlightTicketInfo ordFlightTicketInfo = ordFlightTicketInfoDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordFlightTicketInfo);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		Assert.assertNotNull(id);
		OrdFlightTicketInfo ordFlightTicketInfo = new OrdFlightTicketInfo();
		ordFlightTicketInfo.setInfoId(id);
		ordFlightTicketInfo.setPassengerName("max");
		int i = ordFlightTicketInfoDao.update(ordFlightTicketInfo);
		Assert.assertTrue(i > 0);
	}
	
	@Before
	public void testInsert() throws Exception {
		OrdFlightTicketInfo ordFlightTicketInfo = new OrdFlightTicketInfo();
		int i = ordFlightTicketInfoDao.insert(ordFlightTicketInfo);
		Assert.assertTrue(i > 0);
		id = ordFlightTicketInfoDao.get("selectInfoId");
		Assert.assertNotNull(id);
	}
}