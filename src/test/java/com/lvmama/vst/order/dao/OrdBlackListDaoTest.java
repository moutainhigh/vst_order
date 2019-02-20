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
import com.lvmama.vst.back.supp.po.SuppGoodsBlackList;

/**
 * @author chenlizhao
 *
 */
public class OrdBlackListDaoTest extends OrderTestBase {
	@Autowired
	private OrdBlackListDao ordBlackListDao;
	
	private Long listId;
	
	private Long goodId = 569309L;
	
	@Before
	public void testInsert() throws Exception {
		SuppGoodsBlackList list = new SuppGoodsBlackList();
		list.setBlacklistNum("18817683516");
		list.setBlacklistType("PHONE");
		list.setGoodId(goodId);
		int i = ordBlackListDao.insert(list);
		Assert.assertTrue(i > 0);
		listId = ordBlackListDao.get("selectBlacklistId");
		Assert.assertNotNull(listId);
	}
	
	@After
	public void testDelete() throws Exception {
		Assert.assertNotNull(listId);
		int i = ordBlackListDao.deleteByPrimaryKey(listId);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		Assert.assertNotNull(listId);
		SuppGoodsBlackList list = ordBlackListDao.selectByPrimaryKey(listId);
		Assert.assertTrue(list != null && list.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateByPrimaryKey() throws Exception {
		Assert.assertNotNull(listId);
		SuppGoodsBlackList list = new SuppGoodsBlackList();
		list.setBlacklistId(listId);
		int i = ordBlackListDao.updateByPrimaryKey(list);
		Assert.assertTrue(i > 0);
		list = ordBlackListDao.selectByPrimaryKey(listId);
		Assert.assertTrue(list != null && list.getUpdateTime() != null);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		Assert.assertNotNull(listId);
		SuppGoodsBlackList list = new SuppGoodsBlackList();
		list.setBlacklistId(listId);
		int i = ordBlackListDao.updateByPrimaryKeySelective(list);
		Assert.assertTrue(i > 0);
		list = ordBlackListDao.selectByPrimaryKey(listId);
		Assert.assertTrue(list != null && list.getUpdateTime() != null);
	}
	
	@Test
	public void testSelectByParams() throws Exception {
		Assert.assertNotNull(listId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("blacklistType", "PHONE");
		params.put("goodId", goodId);
		List<SuppGoodsBlackList> lists = ordBlackListDao.selectByParams(params);
		Assert.assertTrue(lists != null && lists.size() > 0);
		System.out.println("return blacklist size: " + lists.size());
		for(SuppGoodsBlackList list : lists) {
			Assert.assertNotNull(list.getUpdateTime());
		}
	}
}
