package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPromotion;

/**
 * @author chenlizhao
*/

public class OrdPromotionDaoTest extends OrderTestBase {
	@Autowired
	private OrdPromotionDao ordPromotionDao;
	
	private Long id = 328L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPromotion ordPromotion = ordPromotionDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordPromotion);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdPromotion ordPromotion = new OrdPromotion();
		ordPromotion.setOrdPromotionId(id);
		ordPromotion.setCode("test");
		int i = ordPromotionDao.updateByPrimaryKeySelective(ordPromotion);
		Assert.assertTrue(i > 0);
	}
}