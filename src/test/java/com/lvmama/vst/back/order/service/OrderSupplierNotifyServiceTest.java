package com.lvmama.vst.back.order.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.client.ord.service.OrderSupplierNotifyService;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated.ChildOrder;

public class OrderSupplierNotifyServiceTest extends OrderTestBase{
	private OrderSupplierNotifyService orderSupplierNotifyService;
	
	private IComplexQueryService complexQueryService;
	
	/**
	 * 根据OrderId获取整个Order对象图
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOjbectDiagramByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderAmountItemTableFlag(true);
		orderFlagParam.setOrderPackTableFlag(true);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderAddressTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderSupplierNotifyService = (OrderSupplierNotifyService) applicationContext.getBean("orderSupplierNotifyServiceRemote");
			complexQueryService = (IComplexQueryService) applicationContext.getBean("complexQueryService");
		}
	}
	
	@Test
	public void testSuit() throws Exception {
//		testAmpleResourceStatus();
//		testUnperformStatusParty();
//		testUnperformStatusAll();
//		testPerformStatusParty();
		testPerformStatusAll();
//		testCancelStatus();
	}
	
//	@Test
	public void testAmpleResourceStatus() throws Exception {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		System.out.println("errMsg=" + resultHandle.getMsg());
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getResourceStatus(), OrderEnum.RESOURCE_STATUS.AMPLE.name());
		assertEquals(order.getOrderItemList().get(0).getResourceStatus(), OrderEnum.RESOURCE_STATUS.AMPLE.name());
		for (OrdOrderStock stock : order.getOrderItemList().get(0).getOrderStockList()) {
			assertEquals(stock.getResourceStatus(), OrderEnum.RESOURCE_STATUS.AMPLE.name());
		}
	}
	
//	@Test
	public void testUnperformStatusParty() throws Exception {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		
		Date arrivalDate = CalendarUtils.getDateFormatDate("2013-12-20", "yyyy-MM-dd");
		Date departureDate = CalendarUtils.getDateFormatDate("2013-12-23", "yyyy-MM-dd");
		suppOrderRelated.setArrivalDate(arrivalDate);
		suppOrderRelated.setDepartureDate(departureDate);
		
		List<ChildOrder> childOrderList = new ArrayList<ChildOrder>();
		ChildOrder childOrder = new ChildOrder();
		childOrder.setArrivalDate(arrivalDate);
		childOrder.setDepartureDate(departureDate);
		childOrder.setStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		childOrderList.add(childOrder);
		
		ChildOrder childOrder2 = new ChildOrder();
		Date arrivalDate2 = CalendarUtils.getDateFormatDate("2013-12-25", "yyyy-MM-dd");
		Date departureDate2 = CalendarUtils.getDateFormatDate("2013-12-26", "yyyy-MM-dd");
		childOrder2.setArrivalDate(arrivalDate2);
		childOrder2.setDepartureDate(departureDate2);
		childOrder2.setStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		childOrderList.add(childOrder2);
		
		suppOrderRelated.setChildOrders(childOrderList);
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		System.out.println(resultHandle.getMsg());
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getOrderItemList().get(0).getPerformStatus(), OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
	}
	
//	@Test
	public void testUnperformStatusAll() throws Exception {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		
		Date arrivalDate = CalendarUtils.getDateFormatDate("2013-12-20", "yyyy-MM-dd");
		Date departureDate = CalendarUtils.getDateFormatDate("2013-12-26", "yyyy-MM-dd");
		suppOrderRelated.setArrivalDate(arrivalDate);
		suppOrderRelated.setDepartureDate(departureDate);
		
		List<ChildOrder> childOrderList = new ArrayList<ChildOrder>();
		ChildOrder childOrder = new ChildOrder();
		childOrder.setArrivalDate(arrivalDate);
		childOrder.setDepartureDate(departureDate);
		childOrder.setStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		childOrderList.add(childOrder);
		suppOrderRelated.setChildOrders(childOrderList);
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		System.out.println("errMsg=" + resultHandle.getMsg());
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getOrderItemList().get(0).getPerformStatus(), OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		
		for (OrdOrderHotelTimeRate timeRate : order.getOrderItemList().get(0).getOrderHotelTimeRateList()) {
			if (arrivalDate.equals(timeRate.getVisitTime())) {
				assertTrue("false".equals(timeRate.getPerformFlag()));
			}
		}
	}
	
//	@Test
	public void testPerformStatusParty() throws Exception {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		
		Date arrivalDate = CalendarUtils.getDateFormatDate("2013-12-20", "yyyy-MM-dd");
		Date departureDate = CalendarUtils.getDateFormatDate("2013-12-23", "yyyy-MM-dd");
		suppOrderRelated.setArrivalDate(arrivalDate);
		suppOrderRelated.setDepartureDate(departureDate);
		
		List<ChildOrder> childOrderList = new ArrayList<ChildOrder>();
		ChildOrder childOrder = new ChildOrder();
		childOrder.setArrivalDate(arrivalDate);
		childOrder.setDepartureDate(departureDate);
		childOrder.setStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		childOrderList.add(childOrder);
		
		ChildOrder childOrder2 = new ChildOrder();
		Date arrivalDate2 = CalendarUtils.getDateFormatDate("2013-12-25", "yyyy-MM-dd");
		Date departureDate2 = CalendarUtils.getDateFormatDate("2013-12-26", "yyyy-MM-dd");
		childOrder2.setArrivalDate(arrivalDate2);
		childOrder2.setDepartureDate(departureDate2);
		childOrder2.setStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
		childOrderList.add(childOrder2);
		
		suppOrderRelated.setChildOrders(childOrderList);
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		System.out.println(resultHandle.getMsg());
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getOrderItemList().get(0).getPerformStatus(), OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
	}
	
//	@Test
	public void testPerformStatusAll() throws Exception {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		
		Date arrivalDate = CalendarUtils.getDateFormatDate("2013-12-20", "yyyy-MM-dd");
		Date departureDate = CalendarUtils.getDateFormatDate("2013-12-26", "yyyy-MM-dd");
		suppOrderRelated.setArrivalDate(arrivalDate);
		suppOrderRelated.setDepartureDate(departureDate);
		
		List<ChildOrder> childOrderList = new ArrayList<ChildOrder>();
		ChildOrder childOrder = new ChildOrder();
		childOrder.setArrivalDate(arrivalDate);
		childOrder.setDepartureDate(departureDate);
		childOrder.setStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		childOrderList.add(childOrder);
		suppOrderRelated.setChildOrders(childOrderList);
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		System.out.println("errMsg=" + resultHandle.getMsg());
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getOrderItemList().get(0).getPerformStatus(), OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
		
		for (OrdOrderHotelTimeRate timeRate : order.getOrderItemList().get(0).getOrderHotelTimeRateList()) {
			if (arrivalDate.equals(timeRate.getVisitTime())) {
				assertTrue("true".equals(timeRate.getPerformFlag()));
			}
		}
	}
	
//	@Test
	public void testCancelStatus() {
		Long orderId = 182L;
		SuppOrderRelated suppOrderRelated = new SuppOrderRelated();
		suppOrderRelated.setOrderId(orderId);
		suppOrderRelated.setOrderItemId(196L);
		suppOrderRelated.setStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
		
		assertNotNull(orderSupplierNotifyService);
		ResultHandle resultHandle = orderSupplierNotifyService.orderNotify(suppOrderRelated);
		assertTrue(resultHandle.isSuccess());
		
		OrdOrder order = getOrderWithOjbectDiagramByOrderId(orderId);
		assertEquals(order.getOrderStatus(), OrderEnum.ORDER_STATUS.CANCEL.name());
	}
}
