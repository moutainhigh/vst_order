/**
 * 
 */
package com.lvmama.vst.order.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.supp.po.SuppGoodsLimit;

/**
 * @author chenlizhao
 *
 */
public class OrdLimitDaoTest extends OrderTestBase {
	@Autowired
	private OrdLimitDao ordLimitDao;
	
	private Long limitId;
	
	private Long goodsId = 576655L;
	
	@Before
	public void testInsert() throws Exception {
		SuppGoodsLimit limit = new SuppGoodsLimit();
		limit.setGoodId(goodsId);
		limit.setLimitNum(10L);
		int i = ordLimitDao.insert(limit);
		Assert.assertTrue(i > 0);
		limitId = ordLimitDao.get("selectLimitId");
		Assert.assertNotNull(limitId);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(limitId);
		int i = ordLimitDao.deleteByPrimaryKey(limitId);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(limitId);
		SuppGoodsLimit limit = ordLimitDao.selectByPrimaryKey(limitId);
		Assert.assertTrue(limit != null && limit.getUpdateTime() != null);
	}
	
	@Test
	public void testSelectByGoodKey() throws Exception {
		SuppGoodsLimit limit = ordLimitDao.selectByGoodKey(goodsId);
		Assert.assertTrue(limit != null && limit.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		Assert.assertNotNull(limitId);
		SuppGoodsLimit limit = new SuppGoodsLimit();
		limit.setLimitId(limitId);
		limit.setLimitNum(11L);
		int i = ordLimitDao.updateByPrimaryKey(limit);
		Assert.assertTrue(i > 0);
		limit = ordLimitDao.selectByPrimaryKey(limitId);
		Assert.assertTrue(limit != null && limit.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(limitId);
		SuppGoodsLimit limit = new SuppGoodsLimit();
		limit.setLimitId(limitId);
		limit.setLimitNum(11L);
		int i = ordLimitDao.updateByPrimaryKeySelective(limit);
		Assert.assertTrue(i > 0);
		limit = ordLimitDao.selectByPrimaryKey(limitId);
		Assert.assertTrue(limit != null && limit.getUpdateTime() != null);
	}
}
