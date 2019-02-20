package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdWechatTemplate;

/**
 * @author chenlizhao
*/

public class OrdWechatTemplateDaoTest extends OrderTestBase {
	@Autowired
	private OrdWechatTemplateDao ordWechatTemplateDao;
	
	private Long id = 65L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdWechatTemplate ordWechatTemplate = ordWechatTemplateDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordWechatTemplate);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdWechatTemplate ordWechatTemplate = new OrdWechatTemplate();
		ordWechatTemplate.setId(id);
		ordWechatTemplate.setUpdatedUser("system");
		int i = ordWechatTemplateDao.updateByPrimaryKeySelective(ordWechatTemplate);
		Assert.assertTrue(i > 0);
	}
}