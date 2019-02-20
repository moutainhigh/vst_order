package com.lvmama.vst.back.order.service;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.IOrderAuditService;
/**
 * 订单审核业务单元测试
 * 
 * @author wenzhengtao
 *
 */
public class OrderAuditServiceTest extends OrderTestBase{
	private IOrderAuditService orderAuditService;
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderAuditService = (IOrderAuditService) applicationContext.getBean("orderAuditService");
		}
	}
	@Test
	public void testSaveAudit() {
		try {
			ComAudit audit = new ComAudit();
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			audit.setObjectId(10L);
			audit.setAuditType(OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name());
			audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
			audit.setOperatorName("lv123456789");
			audit.setCreateTime(Calendar.getInstance().getTime());
			int auditId = orderAuditService.saveAudit(audit);
			Assert.assertNotNull(auditId);
		} catch (Exception e) {
			Assert.fail("服务器发生内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testQueryAuditById() {
		
	}

	@Test
	public void testQueryAuditListByCondition() {
		
	}

	@Test
	public void testCountAuditByCondition() {
		
	}

	@Test
	public void testQueryComAuditListByPool() {
		
	}

	@Test
	public void testUpdateComAuditByPool() {
		
	}

	@Test
	public void testUpdateComAuditByUnProcessed() {
		
	}
	
	@Test
	public void testUpdateComAuditToProcessed() {
		try {
			Long orderId = 155L;
			String auditType = "SALE_AUDIT";
			String operatorName = "wenzhengtao";
			int num = orderAuditService.updateComAuditToProcessed(orderId, auditType, operatorName);
			Assert.assertTrue(num != -1);
		} catch (Exception e) {
			Assert.fail("服务器异常");
			e.printStackTrace();
		}
	}

}
