package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.VstSearchSaleMuilt;

/**
 * @author chenlizhao
*/

public class VstSearchSaleMuiltDaoTest extends OrderTestBase {
	@Autowired
	private VstSearchSaleMuiltDao vstSearchSaleMuiltDao;
	
	private Long id = 1L;
	
	@Test
	public void testInsertSelective() throws Exception {
		VstSearchSaleMuilt vstSearchSaleMuilt = new VstSearchSaleMuilt();
		vstSearchSaleMuilt.setProductId(id);
		vstSearchSaleMuilt.setQuantitySale(1L);
		vstSearchSaleMuilt.setSalePer(0.1);
		vstSearchSaleMuilt.setStartDistrictId(1L);
		int i = vstSearchSaleMuiltDao.insertSingleSelective(vstSearchSaleMuilt);
		Assert.assertTrue(i > 0);
	}
}