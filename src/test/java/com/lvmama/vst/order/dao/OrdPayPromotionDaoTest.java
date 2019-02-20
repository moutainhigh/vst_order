package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;

/**
 * @author chenlizhao
*/

public class OrdPayPromotionDaoTest extends OrderTestBase {
	@Autowired
	private OrdPayPromotionDao ordPayPromotionDao;
	
	private Long id = 1L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPayPromotion ordPayPromotion = ordPayPromotionDao.queryOrdPayPromotionByOrderId(id);
		Assert.assertNotNull(ordPayPromotion);
	}
	
	@Before
	public void testInsert() throws Exception {
		OrdPayPromotion ordPayPromotion = new OrdPayPromotion();
		ordPayPromotion.setOrderId(id);
		ordPayPromotion.setPayPromotionId(id);
		int i = ordPayPromotionDao.insert(ordPayPromotion);
		Assert.assertTrue(i > 0);
	}
}