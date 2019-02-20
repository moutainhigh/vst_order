/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IBusinessRuleService;

/**
 * @author chenlizhao
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class BusinessRuleServiceImplTest {
	@Autowired
	private  IBusinessRuleService businessRuleService;
	
	@Test
    public void testFindBusinessRuleByAllValid(){
		ResultHandleT<List<BusinessRule>> result = businessRuleService.findBusinessRuleByAllValid();
		Assert.assertNotNull(result);
		List<BusinessRule> rules = result.getReturnContent();
		Assert.assertNotNull(rules);
		System.out.println("return list size: " + rules.size());
	}
}
