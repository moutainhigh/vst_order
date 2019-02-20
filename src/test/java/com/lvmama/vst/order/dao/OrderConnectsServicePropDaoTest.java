/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;

/**
 * @author chenlizhao
 *
 */
public class OrderConnectsServicePropDaoTest extends OrderTestBase {
	@Autowired
	private OrderConnectsServicePropDao orderConnectsServicePropDao;
	
	private Long propId;
	
	@Test
	public void testFindOrderConnectsServicePropList() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", 200619825L);
		List<OrderConnectsServiceProp> props = orderConnectsServicePropDao.findOrderConnectsServicePropList(params);
		Assert.assertTrue(props != null && props.size() > 0);
		System.out.println("return prop size: " + props.size());
		for(OrderConnectsServiceProp prop : props) {
			Assert.assertNotNull(prop.getUpdateTime());
		}
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(propId);
		OrderConnectsServiceProp prop = orderConnectsServicePropDao.selectByPrimaryKey(propId);
		Assert.assertTrue(prop != null && prop.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateBySelective() throws Exception {
		Assert.assertNotNull(propId);
		OrderConnectsServiceProp prop = new OrderConnectsServiceProp();
		prop.setOrderServiceId(propId);
		prop.setPropValue("fail");
		Integer i = orderConnectsServicePropDao.updateBySelective(prop);
		Assert.assertTrue(i != null && i > 0);
		prop = orderConnectsServicePropDao.selectByPrimaryKey(propId);
		Assert.assertTrue(prop != null && prop.getUpdateTime() != null);
	}
		
	@Test
	public void testQueryOrderConnectsPropByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", 200619825L);
		List<OrderConnectsServiceProp> props = orderConnectsServicePropDao.queryOrderConnectsPropByParams(params);
		Assert.assertTrue(props != null && props.size() > 0);
		System.out.println("return prop size: " + props.size());
		for(OrderConnectsServiceProp prop : props) {
			Assert.assertNotNull(prop.getUpdateTime());
		}
	}
	
	@Before
	public void testInsert() throws Exception {
		OrderConnectsServiceProp prop = new OrderConnectsServiceProp();
		prop.setOrderId(200619825L);
		prop.setPropId(13L);
		prop.setPropValue("ok");
		Integer i = orderConnectsServicePropDao.insert(prop);
		Assert.assertTrue(i != null && i > 0);
		propId = orderConnectsServicePropDao.get("selectPropId");
		Assert.assertNotNull(propId);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(propId);
		Integer i = orderConnectsServicePropDao.deleteByPrimaryKey(propId);
		Assert.assertTrue(i != null && i > 0);
	}
}
