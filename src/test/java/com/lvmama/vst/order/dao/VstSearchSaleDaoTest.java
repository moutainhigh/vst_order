package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.VstSearchSale;

/**
 * @author chenlizhao
*/

public class VstSearchSaleDaoTest extends OrderTestBase {
	@Autowired
	private VstSearchSaleDao vstSearchSaleDao;
	
	private Long id = 1L;
			
	@Test
	public void testInsertSelective() throws Exception {
		VstSearchSale vstSearchSale = new VstSearchSale();
		vstSearchSale.setProductId(id);
		vstSearchSale.setQuantitySale(1L);
		vstSearchSale.setWeekSale(1L);
		vstSearchSale.setSalePer(0.1);
		int i = vstSearchSaleDao.insertSelective(vstSearchSale);
		Assert.assertTrue(i > 0);
	}
}