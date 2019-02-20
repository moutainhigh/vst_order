package com.lvmama.vst.order.client.ord.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ticket.vo.PagedTicketOrderInfo;
import com.lvmama.vst.ticket.vo.TicketOrderInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext-vst-order-beans.xml"})
public class TestCase4OrderCancelByPhoneCallProjectOfOrdOrderClientServiceImpl {
	
	@Autowired
	OrderServiceMocker service;
	
	@Test
	public void getOrderListWithNullMoblie() {
		ResultHandleT<PagedTicketOrderInfo> result = service.getPagedTicketOrderInfoByMobile(null, 10, 1);
		assertEquals("missing mobile", result.getMsg());
	}
	
	
	@Test
	public void getOrderListWithEmptyMoblie() {
		ResultHandleT<PagedTicketOrderInfo> result = service.getPagedTicketOrderInfoByMobile("   ", 10, 1);
		assertEquals("missing mobile", result.getMsg());
	}
	
	@Test
	public void noOrderRelated2Mobile() {
		ResultHandleT<PagedTicketOrderInfo> result = service.getPagedTicketOrderInfoByMobile("13999999999", 10, 1);
		assertEquals("No order is related to mobile[13999999999]!", result.getMsg());
	}
	
	@Test
	public void getOrderListWithSpecifiedMobile() {
		ResultHandleT<PagedTicketOrderInfo> result = service.getPagedTicketOrderInfoByMobile("15806862219", 10, 1);
		assertNotEquals(null, result.getReturnContent());
	}
	
	@Test
	public void getSingleTicketOrder() {
		TicketOrderInfo toi = service.getSingleTicketOrder("20024045");
		assertNotNull(toi);
		assertEquals("20024045", toi.getOrderId());
	}
}
