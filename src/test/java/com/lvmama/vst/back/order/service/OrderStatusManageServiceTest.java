/**
 * 
 */
package com.lvmama.vst.back.order.service;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderStatusManageService;


/**
 * @author 张伟
 *
 */
public class OrderStatusManageServiceTest {

	private ApplicationContext applicationContext = null;
	
	private IOrderStatusManageService orderStatusManageService= null;
	
	private IComplexQueryService complexQueryService = null;
	
	private Long id=1L;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		applicationContext = new ClassPathXmlApplicationContext("applicationContext-vst-back-beans.xml");
		
		if (applicationContext != null) {
			orderStatusManageService = (IOrderStatusManageService) applicationContext.getBean("orderStatusManageService");
			complexQueryService = (IComplexQueryService) applicationContext.getBean("complexQueryService");
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	/*@Test
	public void testUpdateViewOrderStatus() {
		//fail("Not yet implemented");
	}
*/
	@Test
	public void testUpdateResourceStatus() {
		
		String newStatus=OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
		
		OrdOrder order=new OrdOrder();
		order.setOrderId(id);
		
//		ResultHandle result=orderStatusManageService.updateResourceStatus(order, newStatus);
		
//		assertTrue(result.isSuccess());
		
	}

	@Test
	public void testUpdateInfoStatus() {

		String newStatus=OrderEnum.INFO_STATUS.UNVERIFIED.name();
		
		OrdOrder order=new OrdOrder();
		order.setOrderId(id);
		
//		ResultHandle result=orderStatusManageService.updateInfoStatus(order, newStatus);
		
//		assertTrue(result.isSuccess());
	}

	@Test
	public void testUpdatePerformStatus() { 


		String newStatus=OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name();
		OrdOrder order=new OrdOrder();
		order.setOrderId(id);
		
//		ResultHandle result=orderStatusManageService.updatePerformStatus(order, newStatus);
//		
//		assertTrue(result.isSuccess());
		
	}

	@Test
	public void testUpdateOrderStatus() {

		
		String newStatus=OrderEnum.ORDER_STATUS.CANCEL.name();
		OrdOrder order=new OrdOrder();
		order.setOrderId(id);
		order.setCancelCode("info_not");
		order.setReason("其他原因");
//		ResultHandle result=orderStatusManageService.updateOrderStatus(order, newStatus);
//		
//		assertTrue(result.isSuccess());
		
	}
}
