/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComJobConfig;

/**
 * @author chenlizhao
 *
 */
public class ComJobConfigDAOTest extends OrderTestBase {

	@Autowired
	private ComJobConfigDAO comJobConfigDAO;
	
	private Long configId;
	
	@Before
	public void testInsert() throws Exception {
		ComJobConfig config = new ComJobConfig();
		config.setPlanTime(new Date());
		config.setJobType("ORDER_SETTLEMENT");
		config.setObjectId(200619886L);
		config.setObjectType("ORDER_PAYMENT_MSG");
		int i = comJobConfigDAO.insert(config);
		Assert.assertTrue(i > 0);
		configId = comJobConfigDAO.get("selectConfigId");
		Assert.assertNotNull(configId);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(configId);
		int i = comJobConfigDAO.deleteByPrimaryKey(configId);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(configId);
		ComJobConfig config = new ComJobConfig();
		config.setComJobConfigId(configId);
		config.setPlanTime(new Date());
		int i = comJobConfigDAO.updateByPrimaryKeySelective(config);
		Assert.assertTrue(i > 0);
		config = comJobConfigDAO.selectByPrimaryKey(configId);
		Assert.assertTrue(config != null && config.getUpdateTime() != null);
	}
}
