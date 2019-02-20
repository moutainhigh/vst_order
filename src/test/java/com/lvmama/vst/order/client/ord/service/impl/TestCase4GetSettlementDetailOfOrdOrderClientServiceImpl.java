package com.lvmama.vst.order.client.ord.service.impl;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.vo.SettlementDetailAcquisitionReq;
import com.lvmama.vst.back.client.ord.vo.TicketSettlementDetail;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.order.service.IOrdOrderItemServiceMocker;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration({ "classpath*:applicationContext-only-used-by-TestCase4GetSettlementDetailOfOrdOrderClientServiceImpl.xml" })
public class TestCase4GetSettlementDetailOfOrdOrderClientServiceImpl {
	OrdOrderClientServiceImpl orderClientService = new OrdOrderClientServiceImpl();

	@Test
	public void nullReq() {
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(null);
		assertNull(detail);
	}

	@Test
	public void orderCanNotBeFound() {
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(0), new Long(0), new Long(0), new Long(0), new Integer(0), null, null));
		assertNull(detail);
	}

	@Test
	public void orderWithUncountableCategory() {
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(-1l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborderService.selectOrderItemByOrderItemId(null).getCategoryId(), suborder.getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), new Long(2), new Long(1), new Long(1), new Integer(1), new Date(), null));
		assertNull(detail);
	}

	@Test
	public void buyoutFullRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 10000l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_YES);
		suborder.setBuyoutQuantity(1l);
		suborder.setActualAmount(actualAmount);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(1), new Date(), null));
		assertNotNull(detail);
		assertEquals(new Long(0l), detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void buyoutPartRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 9000l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_YES);
		suborder.setBuyoutQuantity(1l);
		suborder.setActualAmount(actualAmount);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(1), new Date(), null));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void totalSettlementPriceChangedFullRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 10000l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(0l);
		suborder.setQuantity(2l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(2), new Date(), null));
		assertNotNull(detail);
		assertEquals(new Long(0l), detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void totalSettlementPriceChangedPartRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 4500l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(2750l);
		suborder.setQuantity(2l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(500), new Integer(2), new Date(), null));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void refundAmountChangedFullRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 9000l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(5000l);
		suborder.setQuantity(2l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount + 1000, new Long(0), new Integer(2), new Date(), null));
		assertNotNull(detail);
		assertEquals(new Long(0l), detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void parseNullRefundRule() {
		List<SuppGoodsRefundVO> rules = null;
		boolean parsedWell = true;
		try {
			rules = JSONUtil.jsonArray2Bean(null, SuppGoodsRefundVO.class);
		} catch (Exception e) {
			parsedWell = false;
		}
		assertTrue(parsedWell);
		assertEquals(0, rules.size());
	}

	@Test
	public void noRefundRuleOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 0l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(5000l);
		suborder.setQuantity(2l);
		suborder.setRefundRules(null);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(0), new Date(), null));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void parseUnresolvalbeRefundRule() {
		List<SuppGoodsRefundVO> rules = null;
		boolean parsedWell = true;
		try {
			rules = JSONUtil.jsonArray2Bean("{abcdefg", SuppGoodsRefundVO.class);
		} catch (Exception e) {
			parsedWell = false;
		}
		assertTrue(parsedWell);
		assertNull(rules);
	}

	@Test
	public void orderWithUnresolvableRefundRule() {
		Double actualAmount = 100d;
		Long refundAmount = 5000l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(5000l);
		suborder.setQuantity(2l);
		suborder.setRefundRules("{abcdefg");
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(1), new Integer(0), new Date(), null));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void parseManualRefundRule() {
		List<SuppGoodsRefundVO> rules = null;
		boolean parsedWell = true;
		try {
			rules = JSONUtil.jsonArray2Bean(
					"[{\"cancelStrategy\":\"MANUALCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":0,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]",
					SuppGoodsRefundVO.class);
		} catch (Exception e) {
			parsedWell = false;
		}
		assertTrue(parsedWell);
		assertNotNull(rules);
		assertEquals(1, rules.size());
		assertEquals("MANUALCHANGE", rules.get(0).getCancelStrategy());
	}

	@Test
	public void manualRefundedOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 200l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(100l);
		suborder.setQuantity(100l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(2), new Date(),
				"[{\"cancelStrategy\":\"MANUALCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":0,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void nonrefundableOrder() {
		Double actualAmount = 100d;
		Long refundAmount = 0l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(5000l);
		suborder.setQuantity(2l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(0), new Date(),
				"[{\"cancelStrategy\":\"UNRETREATANDCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":0,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void orderWithDeductedAmountByPercentage() {
		Double actualAmount = 100d;
		Long refundAmount = 0l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(5000l);
		suborder.setSettlementPrice(5000l);
		suborder.setQuantity(2l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(0), new Integer(1), new Date(),
				"[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"PERCENT\",\"deductValue\":1000,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(new Long(5500l), detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void orderWithFixedDeductedAmount() {
		Double actualAmount = 100d;
		Long refundAmount = 0l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(1000l);
		suborder.setSettlementPrice(1000l);
		suborder.setQuantity(10l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(500), new Integer(5), new Date(),
				"[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":500,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(new Long(7500l), detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}

	@Test
	public void orderWithFixedDeductedAmountLargerThanSettlementPrice() {
		Double actualAmount = 100d;
		Long refundAmount = 0l;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(1000l);
		suborder.setSettlementPrice(1000l);
		suborder.setQuantity(10l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(5000), new Integer(5), new Date(),
				"[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":500,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}
	
	@Test
	public void orderWithNullRefundAmount() {
		Double actualAmount = 100d;
		Long refundAmount = null;
		OrdOrderItem suborder = new OrdOrderItem();
		suborder.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId());
		suborder.setBuyoutFlag(OrdOrderClientServiceImpl.BUYOUT_FLAG_NO);
		suborder.setBuyoutQuantity(0l);
		suborder.setActualAmount(actualAmount);
		suborder.setTotalSettlementPrice(10000l);
		suborder.setActualSettlementPrice(1000l);
		suborder.setSettlementPrice(1000l);
		suborder.setQuantity(10l);
		IOrdOrderItemServiceMocker suborderService = new IOrdOrderItemServiceMocker();
		suborderService.setSuborder(suborder);
		assertEquals(suborder.getCategoryId(), suborderService.selectOrderItemByOrderItemId(null).getCategoryId());
		Field iOrdOrderItemService = ReflectionUtils.findField(OrdOrderClientServiceImpl.class, "iOrdOrderItemService");
		ReflectionUtils.makeAccessible(iOrdOrderItemService);
		ReflectionUtils.setField(iOrdOrderItemService, orderClientService, suborderService);
		TicketSettlementDetail detail = orderClientService.getTicketSettlementDetail(new SettlementDetailAcquisitionReq(
				new Long(1), refundAmount, refundAmount, new Long(5000), new Integer(5), new Date(),
				"[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"AMOUNT\",\"deductValue\":500,\"deductValueYuan\":0,\"goodsId\":567726,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4797}]"));
		assertNotNull(detail);
		assertEquals(null, detail.getSettlementPrice());
		assertEquals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString(), detail.getPriceConfirmStatus());
	}
}
