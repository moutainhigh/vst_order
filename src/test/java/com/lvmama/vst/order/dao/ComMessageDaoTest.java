package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComMessage;

/**
 * @author chenlizhao
*/

public class ComMessageDaoTest extends OrderTestBase {
	@Autowired
	private ComMessageDao comMessageDao;
	
	private Long id = 1601L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		ComMessage comMessage = comMessageDao.selectByPrimaryKey(id);
		Assert.assertNotNull(comMessage);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		ComMessage comMessage = new ComMessage();
		comMessage.setMessageId(id);
		comMessage.setMessageContent("test");
		int i = comMessageDao.updateByPrimaryKeySelective(comMessage);
		Assert.assertTrue(i > 0);
	}
}