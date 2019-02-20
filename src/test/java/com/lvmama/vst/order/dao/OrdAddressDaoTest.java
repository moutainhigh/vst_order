package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdAddress;

/**
 * @author chenlizhao
*/

public class OrdAddressDaoTest extends OrderTestBase {
	@Autowired
	private OrdAddressDao ordAddressDao;
	
	private Long id = 201L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdAddress ordAddress = ordAddressDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordAddress);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAddress ordAddress = new OrdAddress();
		ordAddress.setOrdAddressId(id);
		ordAddress.setStreet("xian");
		int i = ordAddressDao.updateByPrimaryKeySelective(ordAddress);
		Assert.assertTrue(i > 0);
	}
}