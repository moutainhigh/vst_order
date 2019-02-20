package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderRefundBatch;

/**
 * @author chenlizhao
*/

public class OrderRefundBatchDaoTest extends OrderTestBase {
	@Autowired
	private OrderRefundBatchDao orderRefundBatchDao;
	
	private Long id = 30000343892L;
	
	@Test
	public void testSelectByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", id);
		List<OrderRefundBatch> orderRefundBatchList = orderRefundBatchDao.getOrderRefundBatch(params);
		Assert.assertTrue(orderRefundBatchList != null && orderRefundBatchList.size() > 0);
	}
		
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", id);
		params.put("refundApplyId", 48829L);
		params.put("auditStatus", "OK");
		int i = orderRefundBatchDao.update(params);
		Assert.assertTrue(i > 0);
	}
}