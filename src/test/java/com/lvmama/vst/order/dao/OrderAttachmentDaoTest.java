package com.lvmama.vst.order.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderAttachment;

/**
 * @author chenlizhao
*/

public class OrderAttachmentDaoTest extends OrderTestBase {
	@Autowired
	private OrderAttachmentDao orderAttachmentDao;
	
	private Long id = 445L;
	
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrderAttachment orderAttachment = orderAttachmentDao.selectByPrimaryKey(id);
		Assert.assertNotNull(orderAttachment);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrderAttachment orderAttachment = new OrderAttachment();
		orderAttachment.setOrdAttachmentId(id);
		orderAttachment.setAttachmentName("test");
		int i = orderAttachmentDao.updateByPrimaryKeySelective(orderAttachment);
		Assert.assertTrue(i > 0);
	}
}