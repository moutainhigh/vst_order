package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdSmsSend;

/**
 * @author chenlizhao
*/

public class OrdSmsSendDaoTest extends OrderTestBase {
	@Autowired
	private OrdSmsSendDao ordSmsSendDao;
	
	private Long id = 80L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdSmsSend ordSmsSend = ordSmsSendDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordSmsSend);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdSmsSend ordSmsSend = new OrdSmsSend();
		ordSmsSend.setSmsId(id);
		ordSmsSend.setOperate("admin");
		int i = ordSmsSendDao.updateByPrimaryKeySelective(ordSmsSend);
		Assert.assertTrue(i > 0);
	}
}