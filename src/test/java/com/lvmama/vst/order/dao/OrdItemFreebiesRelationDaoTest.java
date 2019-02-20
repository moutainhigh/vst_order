package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdItemFreebiesRelation;

/**
 * @author chenlizhao
*/

public class OrdItemFreebiesRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdItemFreebieDao ordItemFreebiesRelationDao;
	
	private Long id = 164L;
	
	@Test
	public void testQueryFreebieListByItem() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("freebieId", id);
		List<OrdItemFreebiesRelation> freebieList = ordItemFreebiesRelationDao.queryFreebieListByItem(params);
		Assert.assertTrue(freebieList != null && freebieList.size() > 0);
	}
		
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdItemFreebiesRelation ordItemFreebiesRelation = new OrdItemFreebiesRelation();
		ordItemFreebiesRelation.setIfId(id);
		ordItemFreebiesRelation.setCancel(1L);
		int i = ordItemFreebiesRelationDao.updateItemFreebie(ordItemFreebiesRelation);
		Assert.assertTrue(i > 0);
	}
}