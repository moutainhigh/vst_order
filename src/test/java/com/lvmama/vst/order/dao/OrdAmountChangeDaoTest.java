package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAmountChange;

/**
 * @author chenlizhao
*/

public class OrdAmountChangeDaoTest extends OrderTestBase {
	@Autowired
	private OrdAmountChangeDao ordAmountChangeDao;
	
	private Long id = 226L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAmountChange ordAmountChange = ordAmountChangeDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAmountChange);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAmountChange ordAmountChange = new OrdAmountChange();
		ordAmountChange.setAmountChangeId(id);
		ordAmountChange.setReason("test");
		int i = ordAmountChangeDao.updateByPrimaryKeySelective(ordAmountChange);
		Assert.assertTrue(i > 0);
	}
}