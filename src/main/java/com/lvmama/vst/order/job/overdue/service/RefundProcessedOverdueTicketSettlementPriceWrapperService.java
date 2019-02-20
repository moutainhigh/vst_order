package com.lvmama.vst.order.job.overdue.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OverdueTicketSubOrder;
import com.lvmama.vst.comm.vo.ResultHandleT;

public interface RefundProcessedOverdueTicketSettlementPriceWrapperService {
	final static public String ERROR_CODE_PARAM_ERR = "PARAM_ERR";
	final static public String ERROR_CODE_DEPENDED_SRV_NULL = "DEPENDED_SRV_NULL";
	final static public String ERROR_CODE_NO_ORDER_FOUND = "NO_ORDER_FOUND";
	final static public String ERROR_CODE_SETTLEMENT_PRICE_UPDATE_FAILED = "SETTLEMENT_PRICE_UPDATE_FAILED";
	final static public String ERROR_CODE_SETTLEMENT_PRICE_UPDATE_ALL_FAILED = "SETTLEMENT_PRICE_UPDATE_ALL_FAILED";

	public ResultHandleT<String> setSettlementPriceToZero(OverdueTicketSubOrder subOrder);

	public Boolean setSettlementPriceToZeroInBatch(List<OverdueTicketSubOrder> subOrder);
	
	public void setSettlementPriceToZero(OverdueTicketSubOrder subOrder, Integer processStatus, Boolean isEbkSubOrder);
}
