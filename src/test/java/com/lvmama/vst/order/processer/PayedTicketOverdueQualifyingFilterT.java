package com.lvmama.vst.order.processer;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;

public class PayedTicketOverdueQualifyingFilterT {
	
	@Test
	public void checkEligibilityOfNullSubOrder() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(null));
	}
	
	@Test
	public void checkEligibilityOfSubOrderNotTreatable() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy(null);
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
		subOrder.setCancelStrategy("UNRETREATANDCHANGE");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
		subOrder.setCancelStrategy("MANUALCHANGE");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
		subOrder.setCancelStrategy("PARTRETREATANDCHANGE");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkEligibilityOfSubOrderWithoutRefundRules() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy("RETREATANDCHANGE");
		subOrder.setRefundRules(null);
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkEligibilityOfSubOrderWithEmptyRefundRuleList() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy("RETREATANDCHANGE");
		subOrder.setRefundRules("[]");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkEligibilityOfSubOrderWithIncorrectRefundRule() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy("RETREATANDCHANGE");
		subOrder.setRefundRules("[{\"cancelStrategy\":\"UNRETREATANDCHANGE\",\"deductType\":\"\",\"deductValue\":0,\"deductValueYuan\":0,\"goodsId\":1269877,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4939}]");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkEligibilityOfSubOrderWithRefundRuleHavingDeductValue() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy("RETREATANDCHANGE");
		subOrder.setRefundRules("[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"\",\"deductValue\":5,\"deductValueYuan\":5,\"goodsId\":1269877,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4939}]");
		assertFalse(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkEligibilityOfSubOrderWithCorrectRefundRule() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		subOrder.setCancelStrategy("RETREATANDCHANGE");
		subOrder.setRefundRules("[{\"cancelStrategy\":\"RETREATANDCHANGE\",\"deductType\":\"\",\"deductValue\":0,\"deductValueYuan\":0,\"goodsId\":1269877,\"lastTime\":\"当天的0点0分\",\"latestCancelTime\":0,\"refundId\":4939}]");
		assertTrue(filter.checkEligibilityAccording2RefundPolicy(subOrder));
	}
	
	@Test
	public void checkOverdueTicketCancellationSupportedFlag_NullOrder() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		assertFalse(filter.checkOverdueTicketCancellationSupportedFlag(null));
	}
	
	@Test
	public void checkOverdueTicketCancellationSupportedFlag_OrderWithoutTheFlag() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		assertFalse(filter.checkOverdueTicketCancellationSupportedFlag(subOrder));
	}	
	
	@Test
	public void checkOverdueTicketCancellationSupportedFlag_OrderWithTheFlagOfValueN() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.putContent(OrderEnum.ORDER_COMMON_TYPE.expiredRefundFlag.name(), "N");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		assertFalse(filter.checkOverdueTicketCancellationSupportedFlag(subOrder));
	}	
	
	@Test
	public void checkOverdueTicketCancellationSupportedFlag_OrderWithTheFlagOfValueY() {
		PayedTicketOverdueQualifyingFilter filter = new PayedTicketOverdueQualifyingFilter();
		OrdOrderItem subOrder = new OrdOrderItem();
		subOrder.setCategoryId(new Long(11));
		subOrder.putContent("processKey", "unknown");
		subOrder.putContent(OrderEnum.ORDER_COMMON_TYPE.expiredRefundFlag.name(), "Y");
		subOrder.setPrice(new Long(10));
		subOrder.setQuantity(new Long(2));
		assertTrue(filter.checkOverdueTicketCancellationSupportedFlag(subOrder));
	}		
}
