package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPartRefundItem;

/**
 * @author chenlizhao
*/

public class OrdPartRefundItemDaoTest extends OrderTestBase {
	@Autowired
	private OrdPartRefundItemDAO ordPartRefundItemDao;
	
	private Long id = 30000344054L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPartRefundItem ordPartRefundItem = ordPartRefundItemDao.getOrdPartRefundItemByOrderItemId(id);
		Assert.assertNotNull(ordPartRefundItem);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdPartRefundItem ordPartRefundItem = new OrdPartRefundItem();
		ordPartRefundItem.setOrderItemId(id);
		ordPartRefundItem.setRefundPerson("test");
		int i = ordPartRefundItemDao.updateOrdPartRefundItem(ordPartRefundItem);
		Assert.assertTrue(i > 0);
	}
}