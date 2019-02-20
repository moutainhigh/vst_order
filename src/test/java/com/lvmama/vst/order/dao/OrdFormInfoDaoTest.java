package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdFormInfo;

/**
 * @author chenlizhao
*/

public class OrdFormInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdFormInfoDao ordFormInfoDao;
	
	private Long id = 65L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdFormInfo ordFormInfo = ordFormInfoDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordFormInfo);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdFormInfo ordFormInfo = new OrdFormInfo();
		ordFormInfo.setOrdFormInfoId(id);
		ordFormInfo.setContent("test");
		int i = ordFormInfoDao.updateByPrimaryKeySelective(ordFormInfo);
		Assert.assertTrue(i > 0);
	}
}