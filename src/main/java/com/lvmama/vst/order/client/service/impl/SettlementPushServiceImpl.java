/**
 * 
 */
package com.lvmama.vst.order.client.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.vo.Constant.EVENT_TYPE;
import com.lvmama.vst.order.processer.OrderSettlementProcesser;
import com.lvmama.vst.order.service.SettlementPushService;

/**
 * @author fengyonggang
 *
 */
@Service("settlementPushService")
public class SettlementPushServiceImpl implements SettlementPushService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettlementPushServiceImpl.class);
	@Autowired
	private OrderSettlementProcesser settlementProcessor;

	private static final String EMPTY = "";

	@Override
	public void pushSettlement(EVENT_TYPE eventType, List<Long> objectIds) {
		if (eventType == null || objectIds.isEmpty())
			return;

		for (Long objectId : objectIds) {
			pushSettlement(eventType, objectId);
		}

	}
	
	private void pushSettlement(EVENT_TYPE eventType, Long objectId) {
		Message message = createMessage(eventType, objectId);
		if (message != null) {
			settlementProcessor.process(message);
		}
	}

	private Message createMessage(EVENT_TYPE eventType, Long objectId) {
		switch (eventType) {
		case ORDER_PAYMENT_MSG:
			return MessageFactory.newOrderPaymentMessage(objectId, OrderEnum.PAYMENT_STATUS.PAYED.getCode());
		case ORDER_RESOURCE_MSG:
			return MessageFactory.newOrderResourceStatusMessage(objectId, OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
		case ORDER_INFOPASS_MSG:
			return MessageFactory.newOrderInformationStatusMessage(objectId, OrderEnum.INFO_STATUS.INFOPASS.name());
		case ORDER_APPORTION_SUCCESS_MSG:
			return MessageFactory.newOrderApportionSuccessMessage(objectId);
		case ORDER_CANCEL_MSG:
			return MessageFactory.newOrderCancelMessage(objectId, EMPTY);
		case ORDER_REFUNDED_MSG:
			return MessageFactory.newOrderRefundedSuccessMessage(objectId);
		case ORDER_MODIFY_SETTLEMENT_PRICE_MSG:
			return MessageFactory.newOrdSettlementPriceChangeMessage(objectId, objectId + "|system");
		case ORDER_ITEM_SETTLE_MSG:
			return MessageFactory.newOrderItemSettleMessage(objectId, EMPTY);
		case PASSCODE_APPLY_NOTIFY:
			return MessageFactory.newPasscodeApplyNotifyMessage(objectId);
		case ITEM_PERFROM_SETTLE_MSG:
			return MessageFactory.newItemPerformSettle(objectId, EMPTY);
		case ORDITEM_PRICE_STATUS_CHANGE_MSG:
			return MessageFactory.newOrdItemPriceConfirmChangeMessage(objectId, EMPTY);
		default:
			LOGGER.warn("not support for event type: {}", eventType);
			return null;
		}
	}
}
