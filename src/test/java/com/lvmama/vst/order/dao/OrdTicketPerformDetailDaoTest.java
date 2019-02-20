package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdTicketPerformDetail;

/**
 * @author chenlizhao
*/

public class OrdTicketPerformDetailDaoTest extends OrderTestBase {
	@Autowired
	private OrdTicketPerformDetailDao ordTicketPerformDetailDao;
	
	private Long id = 238L;
	
	@Test
	public void testSelectByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", 30000319098L);
		List<OrdTicketPerformDetail> ordTicketPerformDetailList = ordTicketPerformDetailDao.findOrdTicketPerformDetailList(params);
		Assert.assertTrue(ordTicketPerformDetailList != null && ordTicketPerformDetailList.size() > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdTicketPerformDetail ordTicketPerformDetail = ordTicketPerformDetailDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordTicketPerformDetail);
	}
	
}