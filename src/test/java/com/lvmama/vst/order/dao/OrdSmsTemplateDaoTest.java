package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;

/**
 * @author chenlizhao
*/

public class OrdSmsTemplateDaoTest extends OrderTestBase {
	@Autowired
	private OrdSmsTemplateDao ordSmsTemplateDao;
	
	private Long id = 465L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdSmsTemplate ordSmsTemplate = ordSmsTemplateDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordSmsTemplate);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdSmsTemplate ordSmsTemplate = new OrdSmsTemplate();
		ordSmsTemplate.setTemplateId(id);
		ordSmsTemplate.setValid("N");
		int i = ordSmsTemplateDao.updateByPrimaryKeySelective(ordSmsTemplate);
		Assert.assertTrue(i > 0);
	}
}