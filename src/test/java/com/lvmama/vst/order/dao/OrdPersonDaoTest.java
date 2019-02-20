package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPerson;

/**
 * @author chenlizhao
*/

public class OrdPersonDaoTest extends OrderTestBase {
	@Autowired
	private OrdPersonDao ordPersonDao;
	
	private Long id = 193L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdPerson ordPerson = ordPersonDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordPerson);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdPerson ordPerson = new OrdPerson();
		ordPerson.setOrdPersonId(id);
		ordPerson.setBirthPlace("sh");
		int i = ordPersonDao.updateByPrimaryKeySelective(ordPerson);
		Assert.assertTrue(i > 0);
	}
}