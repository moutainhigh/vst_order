package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;

/**
 * @author chenlizhao
*/

public class OrdMulPriceRateDaoTest extends OrderTestBase {
	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDao;
	
	private Long id = 44L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdMulPriceRate ordMulPriceRate = ordMulPriceRateDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordMulPriceRate);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdMulPriceRate ordMulPriceRate = new OrdMulPriceRate();
		ordMulPriceRate.setOrdMulPriceRateId(id);
		ordMulPriceRate.setAmountType("test");
		int i = ordMulPriceRateDao.updateByPrimaryKeySelective(ordMulPriceRate);
		Assert.assertTrue(i > 0);
	}
}