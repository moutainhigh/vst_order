package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.O2oOrder;
import com.lvmama.vst.back.order.po.OrdFuncRelation;
import com.lvmama.vst.back.order.po.OrdOrderNotice;

/**
 * @author chenlizhao
*/

public class OrdOrderNoticeDaoTest extends OrderTestBase {
	@Autowired
	private OrdNoticeDao ordOrderNoticeDao;
	
	private Long id;
		
	@Test
	public void testFindOrdNoticeList() throws Exception {
		Assert.assertNotNull(id);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ordNoticeId", id);
		List<OrdOrderNotice> noticeList = ordOrderNoticeDao.findOrdNoticeList(params);
		Assert.assertTrue(noticeList != null && noticeList.size() > 0);
	}
		
	@Before
	public void testInsert() throws Exception {
		OrdOrderNotice ordOrderNotice = new OrdOrderNotice();
		ordOrderNotice.setNoticeId(1L);
		ordOrderNotice.setOrdOrderId(1L);
		int i = ordOrderNoticeDao.insert(ordOrderNotice);
		Assert.assertTrue(i > 0);
		id = ordOrderNoticeDao.get("selectNoticeId");
		Assert.assertNotNull(id);
	}
}