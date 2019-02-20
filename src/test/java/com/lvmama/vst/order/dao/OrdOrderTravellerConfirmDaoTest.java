package com.lvmama.vst.order.dao;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;

/**
 * @author chenlizhao
*/

public class OrdOrderTravellerConfirmDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderTravellerConfirmDao ordOrderTravellerConfirmDao;
	
	private Long id = 200610937L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdOrderTravellerConfirm ordOrderTravellerConfirm = ordOrderTravellerConfirmDao.selectSingleByOrderId(id);
		Assert.assertNotNull(ordOrderTravellerConfirm);
	}
	
	@Test
	public void testUpdate() throws Exception {
		OrdOrderTravellerConfirm ordOrderTravellerConfirm = new OrdOrderTravellerConfirm();
		ordOrderTravellerConfirm.setOrderId(id);
		ordOrderTravellerConfirm.setCreateTime(new Date());
		int i = ordOrderTravellerConfirmDao.merge(ordOrderTravellerConfirm);
		Assert.assertTrue(i > 0);
	}
}