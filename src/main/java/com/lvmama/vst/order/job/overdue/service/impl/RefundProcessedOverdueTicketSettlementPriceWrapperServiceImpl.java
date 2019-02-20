package com.lvmama.vst.order.job.overdue.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.fin.SettlementPriceChange;
import com.lvmama.comm.utils.PriceUtil;
import com.lvmama.vst.back.client.ord.service.OrderItemSettlementPriceChangeClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OverdueTicketSubOrder;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.job.overdue.service.RefundProcessedOverdueTicketSettlementPriceWrapperService;
import com.lvmama.vst.order.service.IOrdOrderItemService;

import scala.actors.threadpool.Arrays;

@Service
public class RefundProcessedOverdueTicketSettlementPriceWrapperServiceImpl
		implements RefundProcessedOverdueTicketSettlementPriceWrapperService {

	final static private Logger LOGGER = LoggerFactory
			.getLogger(RefundProcessedOverdueTicketSettlementPriceWrapperServiceImpl.class);

	@Autowired
	private OrderItemSettlementPriceChangeClientService orderItemSettlementPriceChangeServiceRemote;

	@Override
	public ResultHandleT<String> setSettlementPriceToZero(OverdueTicketSubOrder subOrder) {
		ResultHandleT<String> result = new ResultHandleT<String>();

		if (subOrder == null) {
			result.setMsg("");
			result.setErrorCode(ERROR_CODE_PARAM_ERR);
			return result;
		}

		try {
			return generateNewSettlementPriceAndUpdate(convert(subOrder));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			result.setMsg("");
			result.setErrorCode(ERROR_CODE_SETTLEMENT_PRICE_UPDATE_FAILED);
		}
		
		return result;
	}

	@Override
	public Boolean setSettlementPriceToZeroInBatch(List<OverdueTicketSubOrder> subOrderList) {
		Boolean result = new Boolean(false);

		if (subOrderList == null || subOrderList.isEmpty())
			return result;

		try {
			List<OrdOrderItem> completeStructedSubOrderList = new ArrayList<OrdOrderItem>();			
			for (OverdueTicketSubOrder subOrder : subOrderList) {
				completeStructedSubOrderList.add(convert(subOrder));
			}

			if (completeStructedSubOrderList == null || completeStructedSubOrderList.isEmpty())
				return result;

			int succeededInTotal = 0;
			int failedInTotal = 0;
			for (OrdOrderItem completeStructedSubOrder : completeStructedSubOrderList) {
				ResultHandleT<String> singleResult = generateNewSettlementPriceAndUpdate(completeStructedSubOrder);
				if (singleResult != null && singleResult.isSuccess())
					succeededInTotal++;
				else
					failedInTotal++;
			}
			LOGGER.info("succeeded: " + succeededInTotal + ", failed: " + failedInTotal);
			if (succeededInTotal > 0)
				result = new Boolean(true);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		return result;
	}
	
	private OrdOrderItem convert(OverdueTicketSubOrder from) {
		OrdOrderItem to = null;
		if (from != null) {
			to = new OrdOrderItem();
			to.setOrderId(from.getOrderId());
			to.setOrderItemId(from.getOrderItemId());
			to.setBuyoutFlag(from.getBuyoutFlag());
			to.setBuyoutTotalPrice(from.getBuyoutTotalPrice());
			to.setBuyoutPrice(from.getBuyoutPrice());
			to.setBuyoutQuantity(from.getBuyoutQuantity());
			to.setTotalSettlementPrice(from.getTotalSettlementPrice());
			to.setActualSettlementPrice(from.getActualSettlementPrice());
			to.setQuantity(from.getQuantity());
			to.setVisitTime(from.getVisitTime());
			to.setSupplierId(from.getSupplierId() != null ? from.getSupplierId().longValue() : null);
			to.setSuppGoodsId(from.getSuppGoodsId());
		}
		return to;
	}

	private ResultHandleT<String> generateNewSettlementPriceAndUpdate(OrdOrderItem subOrder) {
		ResultHandleT<String> result = new ResultHandleT<String>();

		if (subOrder == null || subOrder.getOrderId() == null || subOrder.getOrderItemId() == null) {
			result.setMsg("");
			result.setErrorCode(ERROR_CODE_PARAM_ERR);
			return result;
		}

		if (orderItemSettlementPriceChangeServiceRemote == null) {
			result.setMsg("");
			result.setErrorCode(ERROR_CODE_DEPENDED_SRV_NULL);
			return result;
		}

		try {
			SettlementPriceChange settlementPriceChange = new SettlementPriceChange();
			settlementPriceChange.setOrderId(subOrder.getOrderId());
			settlementPriceChange.setOrderItemId(subOrder.getOrderItemId());
			settlementPriceChange.setReason("退款修改结算价");
			settlementPriceChange.setRemark("退款修改结算价");
			settlementPriceChange.setNewTotalSettlementPriceStr(PriceUtil.convertToYuanStr(0l));
			LOGGER.info("try to update settlement price of subOrder[" + subOrder.getOrderId() + ":"
					+ subOrder.getOrderItemId() + "] to zero");
			result = orderItemSettlementPriceChangeServiceRemote.updateSettlementChange(settlementPriceChange, subOrder,
					0l);
			LOGGER.info("settlement price of subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
					+ "] updated to zero successfully");
		} catch (Exception e) {
			LOGGER.warn("fail to update settlement price of subOrder[" + subOrder.getOrderId() + ":"
					+ subOrder.getOrderItemId() + "] due to error below");
			LOGGER.error(e.getMessage(), e);
			result.setMsg("");
			result.setErrorCode(ERROR_CODE_SETTLEMENT_PRICE_UPDATE_FAILED);
		}

		return result;
	}

	@Override
	public void setSettlementPriceToZero(OverdueTicketSubOrder subOrder, Integer processStatus, Boolean isEbkSubOrder) {
		try {
			LOGGER.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId() + "], processStatus["
					+ processStatus + "]");

			if (subOrder == null || processStatus == null)
				return;
			
			generateNewSettlementPriceAndUpdate(convert(subOrder), processStatus, isEbkSubOrder);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	private void generateNewSettlementPriceAndUpdate(OrdOrderItem subOrder, Integer processStatus, Boolean isEbkSubOrder) {
		try {
			if (subOrder == null || subOrder.getOrderId() == null || subOrder.getOrderItemId() == null
					|| processStatus == null)
				return;

			if (orderItemSettlementPriceChangeServiceRemote == null)
				return;

			SettlementPriceChange settlementPriceChange = new SettlementPriceChange();
			settlementPriceChange.setOrderId(subOrder.getOrderId());
			settlementPriceChange.setOrderItemId(subOrder.getOrderItemId());
			settlementPriceChange.setReason("退款修改结算价");
			settlementPriceChange.setRemark("退款修改结算价");
			settlementPriceChange.setNewTotalSettlementPriceStr(PriceUtil.convertToYuanStr(0l));
			LOGGER.info("try to update settlement price of subOrder[" + subOrder.getOrderId() + ":"
					+ subOrder.getOrderItemId() + "] to zero");
			orderItemSettlementPriceChangeServiceRemote.updateSettlementChange(settlementPriceChange, subOrder, 0l, processStatus, isEbkSubOrder);
			LOGGER.info("settlement price of subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
					+ "] updated to zero successfully");
		} catch (Exception e) {
			LOGGER.warn("fail to update settlement price of subOrder[" + subOrder.getOrderId() + ":"
					+ subOrder.getOrderItemId() + "] due to error below");
			LOGGER.error(e.getMessage(), e);
		}
	}	
}
