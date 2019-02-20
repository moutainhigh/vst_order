/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.intentionOrder.po.IntentionOrder;
import com.lvmama.vst.order.service.IOrderIntentionService;

/**
 * @author chenlizhao
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderIntentionServiceImplTest {
	@Autowired
	private IOrderIntentionService orderIntentionService;
	
	@Test
    public void testQueryIntentionsByCriteria(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(IOrderIntentionService.ORD_NO, "562");
		List<IntentionOrder> ios = orderIntentionService.queryIntentionsByCriteria(param);
		Assert.assertNotNull(ios);
		System.out.println("return list size: " + ios.size());
	}
	
	@Test
    public void testGetTotalCount(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(IOrderIntentionService.ORD_NO, "562");
		Integer cnt = orderIntentionService.getTotalCount(param);
		Assert.assertNotNull(cnt);
		System.out.println("return count: " + cnt);
	}
	
	@Test
    public void testUpdateIntention(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(IOrderIntentionService.ORD_NO, "562");
		List<IntentionOrder> ios = orderIntentionService.queryIntentionsByCriteria(param);
		Assert.assertTrue(ios != null && ios.size() == 1);
		IntentionOrder io = ios.get(0);
		io.setRemark("test");
		int cnt = orderIntentionService.updateIntention(io);
		Assert.assertTrue(cnt >= 0);
		System.out.println("update count: " + cnt);		
	}
}
