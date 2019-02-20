package com.lvmama.vst.back.order.service;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
/**
 * 订单分单单元测试
 * 
 * @author wenzhengtao
 *
 */
public class OrderDistributionBusinessTest extends OrderTestBase{
	
	private IOrderDistributionBusiness orderDistributionBusiness;  
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderDistributionBusiness = (IOrderDistributionBusiness) applicationContext.getBean("orderDistributionBusiness");
		}
	}
	
	@Test
	public void testMakeOrderAuditForInfoAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForInfoAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForResourceAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForResourceAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForCertificateAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForCertificateAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForPaymentAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForPaymentAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForSaleAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForSaleAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForCancelAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForCancelAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForBookingAudit() {
		try {
			ComAudit audit1 = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForBookingAudit(audit1);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeOrderAuditForManualAuditSingle() {
		/*try {
			Long auditId = null;
			String auditStatus = null;
			String assignor = null;
			String operator = null;
			ComAudit audit = orderDistributionBusiness.makeOrderAuditForManualAudit(auditId, auditStatus, assignor, operator);
			System.out.println(audit);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}*/
	}
	
	@Test
	public void testMakeOrderAuditForManualAuditMultiple() {
		/*try {
			List<String> auditIdStatusList = new ArrayList<String>();
			String assignor = null;
			List<String> operators = new ArrayList<String>();
			Map<String, Object> message = orderDistributionBusiness.makeOrderAuditForManualAudit(auditIdStatusList, assignor, operators);
			System.out.println(message);
		} catch (Exception e) {
			fail("服务器内部异常");
			e.printStackTrace();
		}*/
	}

	
}
