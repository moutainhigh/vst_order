package com.lvmama.vst.back.order.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;

public class OrderUpdateServiceTest  extends OrderTestBase{
	private IOrderUpdateService orderUpdateService = null;
	
	private IComplexQueryService complexQueryService = null;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderUpdateService = (IOrderUpdateService) applicationContext.getBean("ordOrderUpdateService");
			complexQueryService = (IComplexQueryService) applicationContext.getBean("complexQueryService");
		}
	}
	@Test
	public void testQuery(){
		orderUpdateService.updateGuaranteeCC(20000000L);
		List<Long> list = orderUpdateService.queryGuaranteeOrderIds();
		Assert.assertNull(list);
	}
	
	//@Test
	public void testSuit() {
//		testCancelOrder();
		testCancelFreeSaleOrder();
	}
	
//	@Test
	public void testCancelOrder() {
		orderUpdateService.updateCancelOrder(48L, "cancelCode", "reason", "operatorId001", null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderStock queryOrderStock = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(48L);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		
		//订单验证
		assertEquals(OrderEnum.ORDER_STATUS.CANCEL.name(), queryOrder.getOrderStatus());
		
		for (int i = 0; i < queryOrder.getOrderItemList().size(); i++) {
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			//验证订单本地库存项
			for (int k = 0; k < queryOrderItem.getOrderStockList().size(); k++) {
				queryOrderStock = queryOrderItem.getOrderStockList().get(k);
				
				assertEquals(OrderEnum.INVENTORY_STATUS.RESTOCK.name(), queryOrderStock.getInventory());
			}
		}
	}
	
//	@Test
	public void testCancelFreeSaleOrder() {
		orderUpdateService.updateCancelOrder(245L, "cancelCode", "reason", "operatorId001", null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderStock queryOrderStock = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(245L);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		
		//订单验证
		assertEquals(OrderEnum.ORDER_STATUS.CANCEL.name(), queryOrder.getOrderStatus());
		
		for (int i = 0; i < queryOrder.getOrderItemList().size(); i++) {
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			//验证订单本地库存项
			for (int k = 0; k < queryOrderItem.getOrderStockList().size(); k++) {
				queryOrderStock = queryOrderItem.getOrderStockList().get(k);
				
				assertEquals(OrderEnum.INVENTORY_STATUS.FREESALE.name(), queryOrderStock.getInventory());
			}
		}
	}
}
