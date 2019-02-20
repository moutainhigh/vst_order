package com.lvmama.vst.back.order.processer;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;

public class SupplierOrderProcesserTest extends OrderTestBase{
	private IComplexQueryService complexQueryService;
	
	private IOrderLocalService orderLocalService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			complexQueryService = (IComplexQueryService) applicationContext.getBean("complexQueryService");
			orderLocalService = (IOrderLocalService) applicationContext.getBean("orderServiceRemote");
		}
	}
	
	@Test
	public void testSuit() {
		testOrderInfoPassMsg();
//		testCancelOrderMsg();
	}
	
//	@Test
	public void testOrderInfoPassMsg() {
		assertNotNull(orderLocalService);
		Long orderId = 223L;
		OrdOrder oldOrder = complexQueryService.queryOrderByOrderId(orderId);
		orderLocalService.executeUpdateInfoStatus(oldOrder, INFO_STATUS.INFOPASS.name(), null, null);
	}
	
//	@Test
	public void testCancelOrderMsg() {
		assertNotNull(orderLocalService);
		Long orderId = 180L;
		orderLocalService.cancelOrder(orderId, "JunitCode", "Junit test", "JunitUser", null);
	}
}
