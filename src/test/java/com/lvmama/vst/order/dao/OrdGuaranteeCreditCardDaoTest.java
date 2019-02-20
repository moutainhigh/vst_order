package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;

/**
 * @author chenlizhao
*/

public class OrdGuaranteeCreditCardDaoTest extends OrderTestBase {
	@Autowired
	private OrdGuaranteeCreditCardDao ordGuaranteeCreditCardDao;
	
	private Long id = 1142L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdGuaranteeCreditCard ordGuaranteeCreditCard = ordGuaranteeCreditCardDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordGuaranteeCreditCard);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdGuaranteeCreditCard ordGuaranteeCreditCard = new OrdGuaranteeCreditCard();
		ordGuaranteeCreditCard.setOrdGuaranteeCreditCardId(id);
		ordGuaranteeCreditCard.setHolderName("xyz");
		int i = ordGuaranteeCreditCardDao.updateByPrimaryKeySelective(ordGuaranteeCreditCard);
		Assert.assertTrue(i > 0);
	}
}