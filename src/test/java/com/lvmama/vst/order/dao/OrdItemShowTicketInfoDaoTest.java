package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;

/**
 * @author chenlizhao
*/

public class OrdItemShowTicketInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemShowTicketInfoDAO ordItemShowTicketInfoVODao;
	
	private Long id = 30000344093L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdItemShowTicketInfoVO ordItemShowTicketInfoVO = ordItemShowTicketInfoVODao.queryByOrdItemId(id);
		Assert.assertNotNull(ordItemShowTicketInfoVO);
	}
	
	@Test
	public void testInsert() throws Exception {
		OrdItemShowTicketInfoVO ordItemShowTicketInfoVO = new OrdItemShowTicketInfoVO();
		ordItemShowTicketInfoVO.setOrderItemId(1L);
		ordItemShowTicketInfoVO.setPriceId(1L);
		ordItemShowTicketInfoVO.setPrice(1L);
		ordItemShowTicketInfoVO.setPriceType(1L);
		int i = ordItemShowTicketInfoVODao.insert(ordItemShowTicketInfoVO);
		Assert.assertTrue(i > 0);
	}
}