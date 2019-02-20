package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdRemarkLog;

/**
 * @author chenlizhao
*/

public class OrdRemarkLogDaoTest extends OrderTestBase {
	@Autowired
	private OrdRemarkLogDao ordRemarkLogDao;
	
	private Long id = 1059L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdRemarkLog ordRemarkLog = ordRemarkLogDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordRemarkLog);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdRemarkLog ordRemarkLog = new OrdRemarkLog();
		ordRemarkLog.setLogId(id);
		ordRemarkLog.setCreatedUser("op");
		int i = ordRemarkLogDao.updateByPrimaryKeySelective(ordRemarkLog);
		Assert.assertTrue(i > 0);
	}
}