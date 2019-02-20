package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderDisneyInfo;

/**
 * @author chenlizhao
*/

public class OrdOrderDisneyInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdOrderDisneyInfoDao ordOrderDisneyInfoDao;
	
	private Long id = 22L;
	
	@Test
	public void testSelectByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Id", id);
		List<OrdOrderDisneyInfo> disneyInfoList = ordOrderDisneyInfoDao.selectByParams(params);
		Assert.assertTrue(disneyInfoList != null && disneyInfoList.size() > 0);
	}
		
	@Test
	public void testInsert() throws Exception {
		OrdOrderDisneyInfo ordOrderDisneyInfo = new OrdOrderDisneyInfo();
		ordOrderDisneyInfo.setOrderId(1L);
		ordOrderDisneyInfo.setContent("test");
		int i = ordOrderDisneyInfoDao.insert(ordOrderDisneyInfo);
		Assert.assertTrue(i > 0);
	}
}