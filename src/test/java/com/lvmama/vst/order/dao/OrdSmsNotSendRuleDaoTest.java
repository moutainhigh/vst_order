package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;

/**
 * @author chenlizhao
*/

public class OrdSmsNotSendRuleDaoTest extends OrderTestBase {
	@Autowired
	private OrdSmsNotSendRuleDao ordSmsNotSendRuleDao;
	
	private Long id;
	
	@Before
	public void testInsert() throws Exception {
		OrdSmsNotSendRule ordSmsNotSendRule = new OrdSmsNotSendRule();
		ordSmsNotSendRule.setRuleName("test");
		int i = ordSmsNotSendRuleDao.insertSelective(ordSmsNotSendRule);
		Assert.assertTrue(i > 0);
		id = ordSmsNotSendRuleDao.get("selectRuleId");
		Assert.assertNotNull(id);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdSmsNotSendRule ordSmsNotSendRule = ordSmsNotSendRuleDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordSmsNotSendRule);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdSmsNotSendRule ordSmsNotSendRule = new OrdSmsNotSendRule();
		ordSmsNotSendRule.setRuleId(id);
		ordSmsNotSendRule.setRuleName("haha");
		int i = ordSmsNotSendRuleDao.updateByPrimaryKeySelective(ordSmsNotSendRule);
		Assert.assertTrue(i > 0);
	}
}