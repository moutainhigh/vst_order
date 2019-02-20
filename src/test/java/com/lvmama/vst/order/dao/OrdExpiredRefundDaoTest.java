package com.lvmama.vst.order.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundCnd;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundRst;

public class OrdExpiredRefundDaoTest extends OrderTestBase {

	@Autowired
	private OrdExpiredRefundDao ordExpiredRefundDao;
	
	@Test
	public void testInsert() throws Exception {
		OrdExpiredRefund ordExpiredRefund = new OrdExpiredRefund();
		ordExpiredRefund.setOrderId(1L);
		ordExpiredRefund.setOrderItemId(11L);
		ordExpiredRefund.setSupplierId(1000L);
		ordExpiredRefund.setProcessStatus(0);
		ordExpiredRefund.setProcessDesc("未处理");
		int i = ordExpiredRefundDao.insert(ordExpiredRefund);
		Assert.assertTrue(i == 1);
	}
	
	@Test
	public void testUpdate1() throws Exception {
		OrdExpiredRefund ordExpiredRefund = new OrdExpiredRefund();
		ordExpiredRefund.setId(5L);
		ordExpiredRefund.setProcessStatus(1);
		ordExpiredRefund.setProcessDesc("废码处理中");
		ordExpiredRefund.setProcessNum(1);
		int i = ordExpiredRefundDao.update(ordExpiredRefund);
		Assert.assertTrue(i == 1);
	}
	
	@Test
	public void testUpdate2() throws Exception {
		OrdExpiredRefund ordExpiredRefund = new OrdExpiredRefund();
		ordExpiredRefund.setOrderId(1L);
		ordExpiredRefund.setOrderItemId(11L);
		ordExpiredRefund.setProcessStatus(3);
		ordExpiredRefund.setProcessDesc("废码成功");
		ordExpiredRefund.setProcessNum(1);
		int i = ordExpiredRefundDao.update(ordExpiredRefund);
		Assert.assertTrue(i == 1);
	}
	
	@Test
	public void testSelectByOrderId() throws Exception {
		List<OrdExpiredRefund> oerlist = ordExpiredRefundDao.selectByOrderId(1L);
		Assert.assertEquals(2, oerlist.size());
	}
	
	@Test
	public void testSelectByOrderItemId() throws Exception {
		OrdExpiredRefund oer = ordExpiredRefundDao.selectByOrderItemId(10L);
		Assert.assertNotNull(oer);
	}
	
	@Test
	public void testDeleteByPrimaryKey() {
		int i = ordExpiredRefundDao.deleteByPrimaryKey(4L);
		Assert.assertEquals(1, i);
	}
	
	@Test
	public void testDeleteByOrderItemId() {
		int i = ordExpiredRefundDao.deleteByOrderItemId(10L);
		Assert.assertEquals(1, i);
	}
	
	@Test
	public void testDeleteByOrderId() {
		int i = ordExpiredRefundDao.deleteByOrderId(1L);
		Assert.assertEquals(1, i);
	}
	
	@Test
	public void testExpiredRefundStateEnum() {
		Assert.assertEquals(0, OrderEnum.ExpiredRefundState.UNPROCESS.getCode());
		Assert.assertNotEquals(OrderEnum.ExpiredRefundState.UNPROCESS.getDesc(), OrderEnum.ExpiredRefundState.UNNEEDED.getDesc());
	}
	
	@Test
	public void testBatchInsert() throws Exception {
		List<OrdExpiredRefund> list = new ArrayList<OrdExpiredRefund>();
		
		for (int i = 0; i < 10; i++) {
		    OrdExpiredRefund ordExpiredRefund = new OrdExpiredRefund();
		    ordExpiredRefund.setOrderId(124L);
		    ordExpiredRefund.setOrderItemId(131L + i);
		    ordExpiredRefund.setSupplierId(1000L);
		    ordExpiredRefund.setProcessStatus(0);
		    ordExpiredRefund.setProcessDesc("未处理");
		    ordExpiredRefund.setProcessNum(0);
		    
		    list.add(ordExpiredRefund);
		}
		
		int num = ordExpiredRefundDao.batchInsert(list);
		Assert.assertTrue(num == 10);
	}
	
	@Test
	public void testQueryListForPage() {
		OrdExpiredRefundCnd erCnd = new OrdExpiredRefundCnd();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", erCnd.getOrderId());
		params.put("productId", erCnd.getProductId());
		params.put("productName", erCnd.getProductName());
		params.put("suppGoodsId", erCnd.getSuppGoodsId());
		params.put("distChnlId", erCnd.getDistChnlId());
		params.put("supplierName", erCnd.getSupplierName());
		params.put("orderItemStatus", erCnd.getOrderItemStatus());
		params.put("processStatus", erCnd.getProcessStatus());
		params.put("_start", 1);
		params.put("_end", 10);
		params.put("_ROWNUM", Constant.ROWNUM_MAX);
		
		try {
			List<OrdExpiredRefundRst> result = ordExpiredRefundDao.queryListForPage(params);
			Assert.assertTrue(10 == result.size());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
