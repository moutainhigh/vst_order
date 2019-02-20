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
import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;

/**
 * @author chenlizhao
 *
 */
public class BizOrderConnectsPropDaoTest extends OrderTestBase {

	@Autowired
	private BizOrderConnectsPropDao bizOrderConnectsPropDao;
	
	private Long propId;
	
	@Before
	public void testInsert() throws Exception {
		BizOrderConnectsProp prop = new BizOrderConnectsProp();
		prop.setBranchId(50L);
		prop.setPropCode("ok");
		prop.setPropName("test");
		prop.setMaxLength(10L);
		prop.setRequire("Y");
		prop.setSeq(1L);
		prop.setTextType("TEXT");
		long l = bizOrderConnectsPropDao.insert(prop);
		Assert.assertTrue(l > 0);
		propId = bizOrderConnectsPropDao.get("selectPropId");
		Assert.assertNotNull(propId);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(propId);
		int i = bizOrderConnectsPropDao.deleteByPrimaryKey(propId);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(propId);
		BizOrderConnectsProp prop = bizOrderConnectsPropDao.selectByPrimaryKey(propId);
		Assert.assertTrue(prop != null && prop.getUpdateTime() != null);
	}
	
	@Test
	public void testSelectAllByParams() throws Exception {
		Assert.assertNotNull(propId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("propId", propId);
		List<BizOrderConnectsProp> props = bizOrderConnectsPropDao.selectAllByParams(params);
		Assert.assertTrue(props != null && props.size() > 0);
		System.out.println("return prop size: " + props.size());
		for(BizOrderConnectsProp prop : props) {
			Assert.assertNotNull(prop.getUpdateTime());
		}
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		Assert.assertNotNull(propId);
		BizOrderConnectsProp prop = new BizOrderConnectsProp();
		prop.setPropId(propId);
		int i = bizOrderConnectsPropDao.updateByPrimaryKey(prop);
		Assert.assertTrue(i > 0);
		prop = bizOrderConnectsPropDao.selectByPrimaryKey(propId);
		Assert.assertTrue(prop != null && prop.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(propId);
		BizOrderConnectsProp prop = new BizOrderConnectsProp();
		prop.setPropId(propId);
		int i = bizOrderConnectsPropDao.updateByPrimaryKeySelective(prop);
		Assert.assertTrue(i > 0);
		prop = bizOrderConnectsPropDao.selectByPrimaryKey(propId);
		Assert.assertTrue(prop != null && prop.getUpdateTime() != null);
	}
}
