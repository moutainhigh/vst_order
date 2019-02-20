package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdTravAdditionConf;

/**
 * @author chenlizhao
*/

public class OrdTravAdditionConfDaoTest extends OrderTestBase {
	@Autowired
	private OrdTravAdditionConfDAO ordTravAdditionConfDao;
	
	private Long id = 1011L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdTravAdditionConf ordTravAdditionConf = ordTravAdditionConfDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordTravAdditionConf);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		OrdTravAdditionConf ordTravAdditionConf = new OrdTravAdditionConf();
		ordTravAdditionConf.setTravAdditionConfId(id);
		ordTravAdditionConf.setUserName("Y");
		int i = ordTravAdditionConfDao.updateByPrimaryKey(ordTravAdditionConf);
		Assert.assertTrue(i > 0);
	}
}