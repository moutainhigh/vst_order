package com.lvmama.vst.order.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderRefundBatchDetail;

/**
 * @author chenlizhao
*/

public class OrderRefundBatchDetailDaoTest extends OrderTestBase {
	@Autowired
	private OrderRefundBatchDetailDao orderRefundBatchDetailDao;
	
	private Long id = 30000343814L;
	
	@Test
	public void testSelectByParams() throws Exception {
		OrderRefundBatchDetail orderRefundBatchDetail = new OrderRefundBatchDetail();
		orderRefundBatchDetail.setOrderItemId(id);
		List<OrderRefundBatchDetail> orderRefundBatchDetailList = orderRefundBatchDetailDao.getOrderRefundBatchDetails(orderRefundBatchDetail);
		Assert.assertTrue(orderRefundBatchDetailList != null && orderRefundBatchDetailList.size() > 0);
	}
		
}