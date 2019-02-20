package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdSmsReSend;

/**
 * @author chenlizhao
*/

public class OrdSmsReSendDaoTest extends OrderTestBase {
	@Autowired
	private OrdSmsReSendDao ordSmsReSendDao;
	
	private Long id = 1L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdSmsReSend ordSmsReSend = ordSmsReSendDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordSmsReSend);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdSmsReSend ordSmsReSend = new OrdSmsReSend();
		ordSmsReSend.setSmsId(id);
		ordSmsReSend.setOperate("admin");
		int i = ordSmsReSendDao.updateByPrimaryKeySelective(ordSmsReSend);
		Assert.assertTrue(i > 0);
	}
}