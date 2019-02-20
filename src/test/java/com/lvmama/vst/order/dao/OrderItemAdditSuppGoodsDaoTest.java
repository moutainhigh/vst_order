package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.back.order.vo.OrderItemAdditSuppGoodsVo;

/**
 * @author chenlizhao
*/

public class OrderItemAdditSuppGoodsDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemAdditSuppGoodsDao orderItemAdditSuppGoodsDao;
	
	private Long id = 5555555L;
	
	@Test
	public void testGetOrderItemAdditSuppGoodsList() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", id);
		List<OrderItemAdditSuppGoodsVo> orderItemAdditSuppGoods = orderItemAdditSuppGoodsDao.getOrderItemAdditSuppGoodsList(params);
		Assert.assertTrue(orderItemAdditSuppGoods != null && orderItemAdditSuppGoods.size() > 0);
	}
	
	@Test
	public void testInsertOrdItemAdditSuppGoods() throws Exception {
		OrderItemAdditSuppGoods orderItemAdditSuppGoods = new OrderItemAdditSuppGoods();
		orderItemAdditSuppGoods.setOrderItemId(id);
		int i = orderItemAdditSuppGoodsDao.insertOrdItemAdditSuppGoods(orderItemAdditSuppGoods);
		Assert.assertTrue(i > 0);
	}
}