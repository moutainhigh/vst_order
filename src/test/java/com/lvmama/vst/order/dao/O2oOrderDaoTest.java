package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.O2oOrder;

/**
 * @author chenlizhao
*/

public class O2oOrderDaoTest extends OrderTestBase {
	@Autowired
	private O2oOrderDao o2oOrderDao;
	
	private Long id = 200607188L;
	
	@Test
	public void testFindO2oOrderList() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", id);
		List<O2oOrder> o2oOrderList = o2oOrderDao.findO2oOrderList(params);
		Assert.assertTrue(o2oOrderList != null && o2oOrderList.size() > 0);
	}
	
	@Test
	public void testInsertSelective() throws Exception {
		O2oOrder o2oOrder = new O2oOrder();
		o2oOrder.setOrderId(id);
		int i = o2oOrderDao.insertSelective(o2oOrder);
		Assert.assertTrue(i > 0);
	}
}