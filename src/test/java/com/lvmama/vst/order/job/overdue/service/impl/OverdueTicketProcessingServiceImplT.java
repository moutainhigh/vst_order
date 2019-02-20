package com.lvmama.vst.order.job.overdue.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lvmama.comm.pet.po.pub.TaskResult;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OverdueTicketSubOrder;
import com.lvmama.vst.order.dao.OrdOrderItemDao;

import scala.actors.threadpool.Arrays;

public class OverdueTicketProcessingServiceImplT {
	static ApplicationContext context;

	static private OverdueTicketProcessingServiceImpl otps;

	static private OrdOrderItemDao dao;

	static {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		otps = (OverdueTicketProcessingServiceImpl) context.getBean("otps");
		dao = (OrdOrderItemDao) context.getBean("subOrderDao");
	}

	@Test
	public void checkExpiration_NullOrder() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		assertFalse(service.checkExpiration(null));
	}

	@Test
	public void checkExpiration_OrderWithNullVisitTime() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setVisitTime(null);
		assertFalse(service.checkExpiration(subOrder));
	}

	@Test
	public void checkExpiration_OrderWithoutCertValidDay() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setVisitTime(new Date());
		assertFalse(service.checkExpiration(subOrder));
	}

	@Test
	public void checkExpiration_OrderWithNullCertValidDay() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setVisitTime(new Date());
		subOrder.putContent(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString(), null);
		assertFalse(service.checkExpiration(subOrder));
	}

	@Test
	public void checkExpiration_OrderWithNotNumberCertValidDay() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setVisitTime(new Date());
		// subOrder.putContent(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString(),
		// "这不是一个数字");
		subOrder.setContent(
				"{\"processKey\":\"ticket\",\"ticket_spec\":\"ADULT\",\"aperiodic_flag\":\"N\",\"fax_flag\":\"N\",\"categoryCode\":\"category_single_ticket\",\"notify_type\":\"QRCODE\",\"branchCode\":\"single_ticket\",\"child_quantity\":0,\"supplierApiFlag\":\"N\",\"branchAttachFlag\":\"Y\",\"branchName\":\"单门票\",\"adult_quantity\":1,\"cert_valid_day\":\"这不是一个数字\"}");
		assertFalse(service.checkExpiration(subOrder));
	}

	@Test
	public void checkExpiration_OrderNotExpired() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setVisitTime(new GregorianCalendar().getTime());
		// subOrder.putContent(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString(),
		// 3);
		subOrder.setContent(
				"{\"processKey\":\"ticket\",\"ticket_spec\":\"ADULT\",\"aperiodic_flag\":\"N\",\"fax_flag\":\"N\",\"categoryCode\":\"category_single_ticket\",\"notify_type\":\"QRCODE\",\"branchCode\":\"single_ticket\",\"child_quantity\":0,\"supplierApiFlag\":\"N\",\"branchAttachFlag\":\"Y\",\"branchName\":\"单门票\",\"adult_quantity\":1,\"cert_valid_day\":3}");
		assertFalse(service.checkExpiration(subOrder));
	}

	@Test
	public void checkExpiration_OrderExpired() {
		OverdueTicketProcessingServiceImpl service = new OverdueTicketProcessingServiceImpl();
		// OrdOrderItem subOrder = new OrdOrderItem();
		// subOrder.setCategoryId(new Long(11));
		// subOrder.putContent("processKey", "unknown");
		// subOrder.setPrice(new Long(10));
		// subOrder.setQuantity(new Long(2));
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		Calendar calendar = new GregorianCalendar();
		calendar.set(2017, 7, 21, 13, 13);
		subOrder.setVisitTime(calendar.getTime());
		// subOrder.putContent(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString(),
		// 3);
		subOrder.setContent(
				"{\"processKey\":\"ticket\",\"ticket_spec\":\"ADULT\",\"aperiodic_flag\":\"N\",\"fax_flag\":\"N\",\"categoryCode\":\"category_single_ticket\",\"notify_type\":\"QRCODE\",\"branchCode\":\"single_ticket\",\"child_quantity\":0,\"supplierApiFlag\":\"N\",\"branchAttachFlag\":\"Y\",\"branchName\":\"单门票\",\"adult_quantity\":1,\"cert_valid_day\":3}");
		assertTrue(service.checkExpiration(subOrder));
	}

	@Test
	public void updateSubOrderMemo_Once() {
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.setContent(
				"{\"processKey\":\"ticket\",\"ticket_spec\":\"ADULT\",\"aperiodic_flag\":\"N\",\"fax_flag\":\"N\",\"categoryCode\":\"category_single_ticket\",\"notify_type\":\"QRCODE\",\"branchCode\":\"single_ticket\",\"child_quantity\":0,\"supplierApiFlag\":\"N\",\"branchAttachFlag\":\"Y\",\"branchName\":\"单门票\",\"adult_quantity\":1,\"cert_valid_day\":1}");
		subOrder.setPrice(new Long(100));
		subOrder.setQuantity(new Long(1));
		subOrder.setOrderItemId(2000022899l);
		otps.updateSubOrderMemo(subOrder);
		OrdOrderItem subOrderFromDb = dao.selectByPrimaryKey(2000022899l);
		assertTrue(subOrderFromDb.getOrderMemo().endsWith("【过期退】结算价格置为0"));
	}

	@Test
	public void getSupplierIdList() {
		List<Integer> supplierIdList = otps.getSupplierIdList();
		assertNotNull(supplierIdList);
		assertTrue(supplierIdList.size() > 0);
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeOne() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(69));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(1, result.size());
		assertTrue(result.contains(new Integer(69)));
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeTwo() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(69));
		supplierIdList.add(new Integer(269));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(2, result.size());
		assertTrue(result.contains(new Integer(69)));
		assertTrue(result.contains(new Integer(269)));
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeThree() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(619));
		supplierIdList.add(new Integer(26));
		supplierIdList.add(new Integer(2));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(3, result.size());
		assertTrue(result.contains(new Integer(619)));
		assertTrue(result.contains(new Integer(26)));
		assertTrue(result.contains(new Integer(2)));
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeFour() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(619));
		supplierIdList.add(new Integer(26));
		supplierIdList.add(new Integer(2));
		supplierIdList.add(new Integer(18));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(4, result.size());
		assertTrue(result.contains(new Integer(619)));
		assertTrue(result.contains(new Integer(26)));
		assertTrue(result.contains(new Integer(2)));
		assertTrue(result.contains(new Integer(18)));
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeFive() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(619));
		supplierIdList.add(new Integer(26));
		supplierIdList.add(new Integer(2));
		supplierIdList.add(new Integer(18));
		supplierIdList.add(new Integer(35));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(5, result.size());
		assertTrue(result.contains(new Integer(619)));
		assertTrue(result.contains(new Integer(26)));
		assertTrue(result.contains(new Integer(2)));
		assertTrue(result.contains(new Integer(18)));
		assertTrue(result.contains(new Integer(35)));
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeTen() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(619));
		supplierIdList.add(new Integer(26));
		supplierIdList.add(new Integer(2));
		supplierIdList.add(new Integer(18));
		supplierIdList.add(new Integer(35));
		supplierIdList.add(new Integer(200));
		supplierIdList.add(new Integer(199));
		supplierIdList.add(new Integer(88));
		supplierIdList.add(new Integer(66));
		supplierIdList.add(new Integer(456));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(5, result.size());
	}

	@Test
	public void extractSupplierIdToUse_ListWithSizeTwenty() {
		List<Integer> supplierIdList = new ArrayList<Integer>();
		supplierIdList.add(new Integer(619));
		supplierIdList.add(new Integer(26));
		supplierIdList.add(new Integer(2));
		supplierIdList.add(new Integer(18));
		supplierIdList.add(new Integer(35));
		supplierIdList.add(new Integer(200));
		supplierIdList.add(new Integer(199));
		supplierIdList.add(new Integer(88));
		supplierIdList.add(new Integer(66));
		supplierIdList.add(new Integer(456));
		supplierIdList.add(new Integer(19));
		supplierIdList.add(new Integer(286));
		supplierIdList.add(new Integer(27));
		supplierIdList.add(new Integer(118));
		supplierIdList.add(new Integer(5));
		supplierIdList.add(new Integer(210));
		supplierIdList.add(new Integer(19));
		supplierIdList.add(new Integer(8));
		supplierIdList.add(new Integer(36));
		supplierIdList.add(new Integer(45));
		Set<Integer> result = otps.extractSupplierIdToUse(supplierIdList);
		assertEquals(5, result.size());
	}

	@Test
	public void getOverdueTicketSubOrderListBySupplierIds() {
		Set<Integer> supplierIds = new HashSet<Integer>();
		supplierIds.add(1);
		supplierIds.add(23);
		supplierIds.add(55);
		supplierIds.add(77);
		supplierIds.add(99);
		List<OverdueTicketSubOrder> subOrders = otps.getOverdueTicketSubOrderListBySupplierIds(supplierIds);
		assertNotNull(subOrders);
		assertEquals(500, subOrders.size());
	}

	@Test
	public void getOverdueTicketSubOrderListBySupplierIds_listWithNullSupplierId() {
		Set<Integer> supplierIds = new HashSet<Integer>();
		supplierIds.add(null);
		List<OverdueTicketSubOrder> subOrders = otps.getOverdueTicketSubOrderListBySupplierIds(supplierIds);
		assertNotNull(subOrders);
		assertTrue(subOrders.size() > 0);
	}

	@Test
	public void getOverdueTicketSubOrderListBySupplierIds_listWithAssignedSupplierIdAndNullSupplierId() {
		Set<Integer> supplierIds = new HashSet<Integer>();
		supplierIds.add(1);
		supplierIds.add(null);
		List<OverdueTicketSubOrder> subOrders = otps.getOverdueTicketSubOrderListBySupplierIds(supplierIds);
		assertNotNull(subOrders);
		assertTrue(subOrders.size() > 100);
	}

	@Test
	public void checkMainOrderStatus_nullOrder() {
		assertFalse(otps.checkMainOrderStatus(null));
	}

	@Test
	public void checkMainOrderStatus_nullOrderStatus() {
		assertFalse(otps.checkMainOrderStatus(new OverdueTicketSubOrder()));
	}

	@Test
	public void checkMainOrderStatus_nullOrderResourceStatus() {
		OverdueTicketSubOrder overdueTicketSubOrder = new OverdueTicketSubOrder();
		overdueTicketSubOrder.setMainOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.toString());
		assertFalse(otps.checkMainOrderStatus(overdueTicketSubOrder));
	}

	@Test
	public void checkMainOrderStatus_allStatusNormalOrder() {
		OverdueTicketSubOrder overdueTicketSubOrder = new OverdueTicketSubOrder();
		overdueTicketSubOrder.setMainOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.toString());
		overdueTicketSubOrder.setMainOrderResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.toString());
		assertTrue(otps.checkMainOrderStatus(overdueTicketSubOrder));
	}

	@Test
	public void checkSubOrderStatus_nullOrder() {
		assertFalse(otps.checkSubOrderStatus(null));
	}

	@Test
	public void checkSubOrderStatus_nullOrderStatus() {
		assertFalse(otps.checkSubOrderStatus(new OverdueTicketSubOrder()));
	}

	@Test
	public void checkSubOrderStatus_nullOrderResourceStatus() {
		OverdueTicketSubOrder overdueTicketSubOrder = new OverdueTicketSubOrder();
		overdueTicketSubOrder.setSubOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.toString());
		assertFalse(otps.checkSubOrderStatus(overdueTicketSubOrder));
	}

	@Test
	public void checkSubOrderStatus_nullOrderPerformStatus() {
		OverdueTicketSubOrder overdueTicketSubOrder = new OverdueTicketSubOrder();
		overdueTicketSubOrder.setSubOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.toString());
		overdueTicketSubOrder.setSubOrderResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.toString());
		assertFalse(otps.checkSubOrderStatus(overdueTicketSubOrder));
	}

	@Test
	public void checkSubOrderStatus_allStatusNormalOrder() {
		OverdueTicketSubOrder overdueTicketSubOrder = new OverdueTicketSubOrder();
		overdueTicketSubOrder.setSubOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.toString());
		overdueTicketSubOrder.setSubOrderResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.toString());
		overdueTicketSubOrder.setPerformStatus(OrderEnum.PERFORM_STATUS_TYPE.UNPERFORM.toString());
		assertTrue(otps.checkSubOrderStatus(overdueTicketSubOrder));
	}

	@Test
	public void randomPickUpSubOrder_noIdxUsed_noLastSupplierId() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		Set<Integer> idxUsed = new HashSet<Integer>();
		List<OverdueTicketSubOrder> canBeFinallyProcessed = new ArrayList<OverdueTicketSubOrder>();
		Integer lastSupplierId = otps.randomPickUpSubOrder(canBeProcessed, idxUsed, null, canBeFinallyProcessed);
		assertNotNull(lastSupplierId);
		assertEquals(subOrder.getSupplierId(), lastSupplierId);
		assertTrue(canBeFinallyProcessed.size() == 1);
		assertEquals(new Long(123456l), canBeFinallyProcessed.get(0).getOrderItemId());
	}

	@Test
	public void randomPickUpSubOrder_idxUsed() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(234567l));
		subOrder2.setSupplierId(234);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		canBeProcessed.add(subOrder2);
		Set<Integer> idxUsed = new HashSet<Integer>();
		idxUsed.add(new Integer(1));
		List<OverdueTicketSubOrder> canBeFinallyProcessed = new ArrayList<OverdueTicketSubOrder>();
		Integer lastSupplierId = otps.randomPickUpSubOrder(canBeProcessed, idxUsed, null, canBeFinallyProcessed);
		assertNotNull(lastSupplierId);
		assertEquals(subOrder.getSupplierId(), lastSupplierId);
		assertTrue(canBeFinallyProcessed.size() == 1);
		assertEquals(new Long(123456l), canBeFinallyProcessed.get(0).getOrderItemId());
		assertTrue(idxUsed.contains(new Integer(0)));
	}

	@Test
	public void randomPickUpSubOrder_idxUsed_lastSupplierIdExisted() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(234567l));
		subOrder2.setSupplierId(234);
		OverdueTicketSubOrder subOrder3 = new OverdueTicketSubOrder();
		subOrder3.setOrderItemId(new Long(345678l));
		subOrder3.setSupplierId(345);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		canBeProcessed.add(subOrder2);
		canBeProcessed.add(subOrder3);
		Set<Integer> idxUsed = new HashSet<Integer>();
		idxUsed.add(new Integer(1));
		List<OverdueTicketSubOrder> canBeFinallyProcessed = new ArrayList<OverdueTicketSubOrder>();
		Integer lastSupplierId = otps.randomPickUpSubOrder(canBeProcessed, idxUsed, new Integer(345),
				canBeFinallyProcessed);
		assertNotNull(lastSupplierId);
		assertEquals(subOrder.getSupplierId(), lastSupplierId);
		assertTrue(canBeFinallyProcessed.size() == 1);
		assertEquals(new Long(123456l), canBeFinallyProcessed.get(0).getOrderItemId());
		assertTrue(idxUsed.contains(new Integer(0)));
	}

	@Test
	public void randomPickUpSubOrders_limit10_5subOrders() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(234567l));
		subOrder2.setSupplierId(234);
		OverdueTicketSubOrder subOrder3 = new OverdueTicketSubOrder();
		subOrder3.setOrderItemId(new Long(345678l));
		subOrder3.setSupplierId(345);
		OverdueTicketSubOrder subOrder4 = new OverdueTicketSubOrder();
		subOrder4.setOrderItemId(new Long(456789l));
		subOrder4.setSupplierId(456);
		OverdueTicketSubOrder subOrder5 = new OverdueTicketSubOrder();
		subOrder5.setOrderItemId(new Long(567890l));
		subOrder5.setSupplierId(567);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		canBeProcessed.add(subOrder2);
		canBeProcessed.add(subOrder3);
		canBeProcessed.add(subOrder4);
		canBeProcessed.add(subOrder5);
		List<OverdueTicketSubOrder> canBeFinallyProcessed = otps.randomPickUpSubOrders(canBeProcessed);
		assertNotNull(canBeFinallyProcessed);
		assertEquals(5, canBeFinallyProcessed.size());
		List<Long> subOrderIdList = new ArrayList<Long>();
		for (OverdueTicketSubOrder o : canBeFinallyProcessed) {
			subOrderIdList.add(o.getOrderItemId());
		}
		assertTrue(subOrderIdList.contains(new Long(123456l)));
		assertTrue(subOrderIdList.contains(new Long(234567l)));
		assertTrue(subOrderIdList.contains(new Long(345678l)));
		assertTrue(subOrderIdList.contains(new Long(456789l)));
		assertTrue(subOrderIdList.contains(new Long(567890l)));
	}

	@Test
	public void randomPickUpSubOrders_limit10_15subOrders() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(234567l));
		subOrder2.setSupplierId(234);
		OverdueTicketSubOrder subOrder3 = new OverdueTicketSubOrder();
		subOrder3.setOrderItemId(new Long(345678l));
		subOrder3.setSupplierId(345);
		OverdueTicketSubOrder subOrder4 = new OverdueTicketSubOrder();
		subOrder4.setOrderItemId(new Long(456789l));
		subOrder4.setSupplierId(456);
		OverdueTicketSubOrder subOrder5 = new OverdueTicketSubOrder();
		subOrder5.setOrderItemId(new Long(567890l));
		subOrder5.setSupplierId(567);
		OverdueTicketSubOrder subOrder6 = new OverdueTicketSubOrder();
		subOrder6.setOrderItemId(new Long(901234l));
		subOrder6.setSupplierId(123);
		OverdueTicketSubOrder subOrder7 = new OverdueTicketSubOrder();
		subOrder7.setOrderItemId(new Long(987654l));
		subOrder7.setSupplierId(234);
		OverdueTicketSubOrder subOrder8 = new OverdueTicketSubOrder();
		subOrder8.setOrderItemId(new Long(876543l));
		subOrder8.setSupplierId(345);
		OverdueTicketSubOrder subOrder9 = new OverdueTicketSubOrder();
		subOrder9.setOrderItemId(new Long(765432l));
		subOrder9.setSupplierId(456);
		OverdueTicketSubOrder subOrder10 = new OverdueTicketSubOrder();
		subOrder10.setOrderItemId(new Long(654321l));
		subOrder10.setSupplierId(567);
		OverdueTicketSubOrder subOrder11 = new OverdueTicketSubOrder();
		subOrder11.setOrderItemId(new Long(111111l));
		subOrder11.setSupplierId(123);
		OverdueTicketSubOrder subOrder12 = new OverdueTicketSubOrder();
		subOrder12.setOrderItemId(new Long(999999l));
		subOrder12.setSupplierId(234);
		OverdueTicketSubOrder subOrder13 = new OverdueTicketSubOrder();
		subOrder13.setOrderItemId(new Long(888888l));
		subOrder13.setSupplierId(345);
		OverdueTicketSubOrder subOrder14 = new OverdueTicketSubOrder();
		subOrder14.setOrderItemId(new Long(777777l));
		subOrder14.setSupplierId(456);
		OverdueTicketSubOrder subOrder15 = new OverdueTicketSubOrder();
		subOrder15.setOrderItemId(new Long(666666l));
		subOrder15.setSupplierId(567);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		canBeProcessed.add(subOrder2);
		canBeProcessed.add(subOrder3);
		canBeProcessed.add(subOrder4);
		canBeProcessed.add(subOrder5);
		canBeProcessed.add(subOrder6);
		canBeProcessed.add(subOrder7);
		canBeProcessed.add(subOrder8);
		canBeProcessed.add(subOrder9);
		canBeProcessed.add(subOrder10);
		canBeProcessed.add(subOrder11);
		canBeProcessed.add(subOrder12);
		canBeProcessed.add(subOrder13);
		canBeProcessed.add(subOrder14);
		canBeProcessed.add(subOrder15);
		List<OverdueTicketSubOrder> canBeFinallyProcessed = otps.randomPickUpSubOrders(canBeProcessed);
		assertNotNull(canBeFinallyProcessed);
		assertEquals(10, canBeFinallyProcessed.size());
		for (int i = 0; i < canBeFinallyProcessed.size() - 1; i++) {
			assertNotEquals(canBeFinallyProcessed.get(i).getSupplierId(),
					canBeFinallyProcessed.get(i + 1).getSupplierId());
		}
	}

	@Test
	public void randomPickUpSubOrders_4subOrders() {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderItemId(new Long(123456l));
		subOrder.setSupplierId(123);
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(234567l));
		subOrder2.setSupplierId(null);
		OverdueTicketSubOrder subOrder3 = new OverdueTicketSubOrder();
		subOrder3.setOrderItemId(new Long(345678l));
		subOrder3.setSupplierId(null);
		OverdueTicketSubOrder subOrder4 = new OverdueTicketSubOrder();
		subOrder4.setOrderItemId(new Long(456789l));
		subOrder4.setSupplierId(456);
		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		canBeProcessed.add(subOrder);
		canBeProcessed.add(subOrder2);
		canBeProcessed.add(subOrder3);
		canBeProcessed.add(subOrder4);
		List<OverdueTicketSubOrder> canBeFinallyProcessed = otps.randomPickUpSubOrders(canBeProcessed);
		assertNotNull(canBeFinallyProcessed);
		assertEquals(4, canBeFinallyProcessed.size());
		List<Long> subOrderIdList = new ArrayList<Long>();
		for (OverdueTicketSubOrder o : canBeFinallyProcessed) {
			subOrderIdList.add(o.getOrderItemId());
		}
		assertTrue(subOrderIdList.contains(new Long(123456l)));
		assertTrue(subOrderIdList.contains(new Long(234567l)));
		assertTrue(subOrderIdList.contains(new Long(345678l)));
		assertTrue(subOrderIdList.contains(new Long(456789l)));
	}

	@Test
	public void updateSubOrderOverdueFlagAndMemo_listNotEmpty() {
		OverdueTicketSubOrder subOrder1 = new OverdueTicketSubOrder();
		subOrder1.setOrderItemId(new Long(5));
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(6));
		OverdueTicketSubOrder subOrder3 = new OverdueTicketSubOrder();
		subOrder3.setOrderItemId(new Long(7));
		OverdueTicketSubOrder subOrder4 = new OverdueTicketSubOrder();
		subOrder4.setOrderItemId(new Long(8));
		List<OverdueTicketSubOrder> subOrderList = new ArrayList<OverdueTicketSubOrder>();
		subOrderList.add(subOrder1);
		subOrderList.add(subOrder2);
		subOrderList.add(subOrder3);
		subOrderList.add(subOrder4);
		otps.updateOverdueTicketRefundProcessedFlagAndMemoInBatch(subOrderList);
	}

	@Test
	public void updateSubOrderOverdueFlag_listEmpty() {
		List<OverdueTicketSubOrder> subOrderList = new ArrayList<OverdueTicketSubOrder>();
		otps.updateOverdueTicketRefundProcessedFlagAndMemoInBatch(subOrderList);
	}

	@Test
	public void updateOverdueTicketSubOrderStatus() {
		OverdueTicketSubOrder subOrder1 = new OverdueTicketSubOrder();
		subOrder1.setOrderItemId(new Long(2000044238));
		OverdueTicketSubOrder subOrder2 = new OverdueTicketSubOrder();
		subOrder2.setOrderItemId(new Long(2000044251));
		List<OverdueTicketSubOrder> subOrderList = new ArrayList<OverdueTicketSubOrder>();
		subOrderList.add(subOrder1);
		subOrderList.add(subOrder2);
		Boolean result = otps.updateOverdueTicketSubOrderStatus(subOrderList,
				OrderEnum.ExpiredRefundState.SUCCESS.getCode(), OrderEnum.ExpiredRefundState.SUCCESS.getDesc());
		assertTrue(result);
	}

	@Test
	public void extractParamAndSet_correctParameterAllThere() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_twoCorrectParameters() {
		otps.extractParamAndSet("{extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(5), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_twoCorrectParameters2() {
		otps.extractParamAndSet("{upperLimitOfSupplierIdToUse:10,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_noParameter() {
		otps.extractParamAndSet("");
		assertEquals(new Integer(5), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(50), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_incorrectParameter() {
		otps.extractParamAndSet("{");
		assertEquals(new Integer(5), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(50), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_incorrectParameter2() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierId:10,extractionQuantityFromEachSupplier:一千,totalExtractionToTotalFinallyPickUp:-100}");
		assertEquals(new Integer(5), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(50), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
	}

	@Test
	public void extractParamAndSet_noIdListOfSubOrdersToProcess() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertTrue(otps.getIdListOfSubOrdersToProcess().isEmpty());
	}

	@Test
	public void extractParamAndSet_malformatIdListOfSubOrdersToProcess1() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,idListOfSubOrdersToProcess:}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertTrue(otps.getIdListOfSubOrdersToProcess().isEmpty());
	}

	@Test
	public void extractParamAndSet_malformatIdListOfSubOrdersToProcess2() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,idListOfSubOrdersToProcess:[}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertTrue(otps.getIdListOfSubOrdersToProcess().isEmpty());
	}

	@Test
	public void extractParamAndSet_malformatIdListOfSubOrdersToProcess3() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,idListOfSubOrdersToProcess:[  ]}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertTrue(otps.getIdListOfSubOrdersToProcess().isEmpty());
	}

	@Test
	public void extractParamAndSet_idListOfSubOrdersToProcess() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,idListOfSubOrdersToProcess:[1, 2, three,  4  ]}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
	}

	@Test
	public void extractParamAndSet_idListOfSubOrdersToProcess2() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
	}

	@Test
	public void extractParamAndSet_runningInSerial1() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,runningInSerial:false}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertFalse(otps.isRunningInSerial());
	}

	@Test
	public void extractParamAndSet_runningInSerial2() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,runningInSerial:faalse}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertTrue(otps.isRunningInSerial());
	}

	@Test
	public void extractParamAndSet_runningInSerial3() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertTrue(otps.isRunningInSerial());
	}
	
	@Test
	public void extractParamAndSet_noSubOrderIdNum() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertTrue(otps.isRunningInSerial());
		assertEquals(new Integer(50), OverdueTicketProcessingServiceImpl.getSubOrderIdNum());
	}	
	
	@Test
	public void extractParamAndSet_withSubOrderIdNum() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,subOrderIdNum:60}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertTrue(otps.isRunningInSerial());
		assertEquals(new Integer(60), OverdueTicketProcessingServiceImpl.getSubOrderIdNum());
	}
	
	@Test
	public void extractParamAndSet_withUpperLimitExcceededSubOrderIdNum() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,idListOfSubOrdersToProcess:[1, 2, three,  4  ],extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,subOrderIdNum:6000}");
		assertEquals(new Integer(10), OverdueTicketProcessingServiceImpl.getUpperLimitOfSupplierIdToUse());
		assertEquals(new Integer(1000), OverdueTicketProcessingServiceImpl.getExtractionQuantityFromEachSupplier());
		assertEquals(new Integer(100), OverdueTicketProcessingServiceImpl.getTotalExtractionToTotalFinallyPickUp());
		assertEquals(3, otps.getIdListOfSubOrdersToProcess().size());
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(1)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(2)));
		assertTrue(otps.getIdListOfSubOrdersToProcess().contains(new Long(4)));
		assertFalse(otps.getIdListOfSubOrdersToProcess().contains("three"));
		assertTrue(otps.isRunningInSerial());
		assertEquals(new Integer(50), OverdueTicketProcessingServiceImpl.getSubOrderIdNum());
	}	

	@Test
	public void getOverdueTicketSubOrderListBySpecifiedIds_nullList() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100}");
		assertNull(otps.getOverdueTicketSubOrderListBySpecifiedIds());
	}

	@Test
	public void getOverdueTicketSubOrderListBySpecifiedIds_listNotNull() {
		otps.extractParamAndSet(
				"{upperLimitOfSupplierIdToUse:10,extractionQuantityFromEachSupplier:1000,totalExtractionToTotalFinallyPickUp:100,idListOfSubOrdersToProcess:[2000022923, 2000022919, 2000022952, 2000022905]}");
		List<OverdueTicketSubOrder> subOrders = otps.getOverdueTicketSubOrderListBySpecifiedIds();
		assertEquals(4, subOrders.size());
		List<Long> subOrderIds = new ArrayList<Long>();
		for (OverdueTicketSubOrder subOrder : subOrders) {
			subOrderIds.add(subOrder.getOrderItemId());
		}
		assertTrue(subOrderIds.contains(new Long(2000022923)));
		assertTrue(subOrderIds.contains(new Long(2000022919)));
		assertTrue(subOrderIds.contains(new Long(2000022952)));
		assertTrue(subOrderIds.contains(new Long(2000022905)));
	}

	@Test
	public void singleton() {
		OverdueTicketProcessingServiceImpl service1 = (OverdueTicketProcessingServiceImpl) context.getBean("otps");
		OverdueTicketProcessingServiceImpl service2 = (OverdueTicketProcessingServiceImpl) context.getBean("otps");
		assertEquals(service1, service2);
	}

	@Test
	public void multipleThreaded() throws InterruptedException {
		List<TaskResult> resL = new ArrayList<TaskResult>();
		TestThread r1 = new TestThread(otps, resL);
		TestThread r2 = new TestThread(otps, resL);
		TestThread r3 = new TestThread(otps, resL);
		TestThread r4 = new TestThread(otps, resL);
		TestThread r5 = new TestThread(otps, resL);
		TestThread r6 = new TestThread(otps, resL);
		TestThread r7 = new TestThread(otps, resL);
		TestThread r8 = new TestThread(otps, resL);
		TestThread r9 = new TestThread(otps, resL);
		TestThread r10 = new TestThread(otps, resL);
		TestThread r11 = new TestThread(otps, resL);
		TestThread r12 = new TestThread(otps, resL);
		TestThread r13 = new TestThread(otps, resL);
		TestThread r14 = new TestThread(otps, resL);
		TestThread r15 = new TestThread(otps, resL);
		TestThread r16 = new TestThread(otps, resL);
		TestThread r17 = new TestThread(otps, resL);
		TestThread r18 = new TestThread(otps, resL);
		TestThread r19 = new TestThread(otps, resL);
		TestThread r20 = new TestThread(otps, resL);
		TestThread r21 = new TestThread(otps, resL);
		TestThread r22 = new TestThread(otps, resL);
		TestThread r23 = new TestThread(otps, resL);
		TestThread r24 = new TestThread(otps, resL);
		TestThread r25 = new TestThread(otps, resL);
		TestThread r26 = new TestThread(otps, resL);
		TestThread r27 = new TestThread(otps, resL);
		TestThread r28 = new TestThread(otps, resL);
		TestThread r29 = new TestThread(otps, resL);
		TestThread r30 = new TestThread(otps, resL);
		TestThread r31 = new TestThread(otps, resL);
		TestThread r32 = new TestThread(otps, resL);
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);
		Thread t3 = new Thread(r3);
		Thread t4 = new Thread(r4);
		Thread t5 = new Thread(r5);
		Thread t6 = new Thread(r6);
		Thread t7 = new Thread(r7);
		Thread t8 = new Thread(r8);
		Thread t9 = new Thread(r9);
		Thread t10 = new Thread(r10);
		Thread t11 = new Thread(r11);
		Thread t12 = new Thread(r12);
		Thread t13 = new Thread(r13);
		Thread t14 = new Thread(r14);
		Thread t15 = new Thread(r15);
		Thread t16 = new Thread(r16);
		Thread t17 = new Thread(r17);
		Thread t18 = new Thread(r18);
		Thread t19 = new Thread(r19);
		Thread t20 = new Thread(r20);
		Thread t21 = new Thread(r21);
		Thread t22 = new Thread(r22);
		Thread t23 = new Thread(r23);
		Thread t24 = new Thread(r24);
		Thread t25 = new Thread(r25);
		Thread t26 = new Thread(r26);
		Thread t27 = new Thread(r27);
		Thread t28 = new Thread(r28);
		Thread t29 = new Thread(r29);
		Thread t30 = new Thread(r30);
		Thread t31 = new Thread(r31);
		Thread t32 = new Thread(r32);
		List<Thread> threads = new ArrayList<Thread>();
		threads.add(t1);
		threads.add(t2);
		threads.add(t3);
		threads.add(t4);
		threads.add(t5);
		threads.add(t6);
		threads.add(t7);
		threads.add(t8);
		threads.add(t9);
		threads.add(t10);
		threads.add(t11);
		threads.add(t12);
		threads.add(t13);
		threads.add(t14);
		threads.add(t15);
		threads.add(t16);
		threads.add(t17);
		threads.add(t18);
		threads.add(t19);
		threads.add(t20);
		threads.add(t21);
		threads.add(t22);
		threads.add(t23);
		threads.add(t24);
		threads.add(t25);
		threads.add(t26);
		threads.add(t27);
		threads.add(t28);
		threads.add(t29);
		threads.add(t30);
		threads.add(t31);
		threads.add(t32);
		for (Thread t : threads) {
			t.start();
		}
		Thread.sleep(45000l);
		assertFalse(resL.isEmpty());
		assertEquals(32, resL.size());
		TestThread r33 = new TestThread(otps, resL);
		Thread t33 = new Thread(r33);
		t33.start();
		Thread.sleep(5000l);
		assertEquals(33, resL.size());
		List<TaskResult> processed = new ArrayList<TaskResult>();
		for (TaskResult res : resL) {
			if (res.getResult().equals("can't get overdue ticket by supplier id"))
				processed.add(res);
		}
		assertTrue(processed.size() > 1);
		assertEquals(2, processed.size());
	}

	private class TestThread implements Runnable {
		private OverdueTicketProcessingServiceImpl otps;
		private List<TaskResult> resL;

		public TestThread(OverdueTicketProcessingServiceImpl otps, List<TaskResult> resL) {
			this.otps = otps;
			this.resL = resL;
		}

		@Override
		public void run() {
			try {
				resL.add(otps.execute(null, "idListOfSubOrdersToProcess:[666666]"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Test
	public void updateOverdueTicketSubOrderInOneShot() {
		assertFalse(otps.updateOverdueTicketSubOrderInOneShot(new Long(2000022919)));
	}

	@Test
	public void getIdAndStatusOfNotFullyProcessed_noParam() {
		assertNotNull(otps.getOverdueTicketOrderDao());
		assertEquals(50, otps.getOverdueTicketOrderDao().getIdAndStatusOfNotFullyProcessed(null).size());
	}

	@Test
	public void getIdAndStatusOfNotFullyProcessed_withParamRowNum() {
		assertNotNull(otps.getOverdueTicketOrderDao());
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rowNum", 100);
		assertEquals(100, otps.getOverdueTicketOrderDao().getIdAndStatusOfNotFullyProcessed(param).size());
	}
	
	@Test
	public void getIdAndStatusOfNotFullyProcessed_withParamIdList() {
		assertNotNull(otps.getOverdueTicketOrderDao());
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("idList", Arrays.asList(new Long[] { 2000044264l, 2000044424l }));
		param.put("rowNum", 100);
		List<OrdExpiredRefund> list = otps.getOverdueTicketOrderDao().getIdAndStatusOfNotFullyProcessed(param);
		assertEquals(2, list.size());
		Map<Long, Integer> idAndStatus = new HashMap<Long, Integer>();
		for (OrdExpiredRefund item : list) {
			idAndStatus.put(item.getOrderItemId(), item.getProcessStatus());
		}
		assertTrue(idAndStatus.containsKey(2000044264l));
		assertTrue(idAndStatus.containsKey(2000044424l));
		assertTrue(idAndStatus.containsValue(0) || idAndStatus.containsValue(7) || idAndStatus.containsValue(8));
	}

	@Test
	public void getSubOrderByIdForOverdueRefundProcessing() {
		List<OverdueTicketSubOrder> subOrderList = otps.getSubOrderDao().getSubOrderByIdForOverdueRefundProcessing(
				Arrays.asList(new Long[] { 2000022899l, 2000022923l, 2000022927l, 2000022939l, 2000022917l, 2000022944l,
						2000022946l, 2000022907l, 2000022919l, 2000022945l }));
		assertNotNull(subOrderList);
		assertEquals(10, subOrderList.size());
		List<Long> subOrderIdList = new ArrayList<Long>();
		for (OverdueTicketSubOrder subOrder : subOrderList) {
			subOrderIdList.add(subOrder.getOrderItemId());
		}
		assertTrue(subOrderIdList.contains(new Long(2000022899l)));
		assertTrue(subOrderIdList.contains(new Long(2000022923l)));
		assertTrue(subOrderIdList.contains(new Long(2000022927l)));
		assertTrue(subOrderIdList.contains(new Long(2000022939l)));
		assertTrue(subOrderIdList.contains(new Long(2000022917l)));
		assertTrue(subOrderIdList.contains(new Long(2000022944l)));
		assertTrue(subOrderIdList.contains(new Long(2000022946l)));
		assertTrue(subOrderIdList.contains(new Long(2000022907l)));
		assertTrue(subOrderIdList.contains(new Long(2000022919l)));
		assertTrue(subOrderIdList.contains(new Long(2000022945l)));
	}
	
	@Test
	public void canPassIntoInTime() {
		OverdueTicketSubOrder ticket = new OverdueTicketSubOrder();
		ticket.setNotInTimeFlag("N");
		assertTrue(otps.canPassIntoInTime(ticket));
	}
	
	@Test
	public void checkInfoStatus_false() {
		assertFalse(otps.checkInfoStatus(null));
		OverdueTicketSubOrder order = new OverdueTicketSubOrder();
		order.setMainOrderInfoStatus("INFOPASS");
		order.setSubOrderInfoStatus("UNVERIFIED");
		assertFalse(otps.checkInfoStatus(order));
	}
	
	@Test
	public void checkInfoStatus_true() {
		OverdueTicketSubOrder order = new OverdueTicketSubOrder();
		order.setMainOrderInfoStatus("INFOPASS");
		order.setSubOrderInfoStatus("INFOPASS");
		assertTrue(otps.checkInfoStatus(order));
	}
	
	@Test
	public void updateOverdueTicketRefundProcessedFlagAndMemoInBatch() {
		otps.getSubOrderDao().updateOverdueTicketRefundProcessedFlagAndMemoInBatch(
				Arrays.asList(new Long[] { 2024900145l }));
	}	
}
