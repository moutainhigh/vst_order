package com.lvmama.vst.order.service.refund.adapter.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.service.refund.IOrderRefundAmountService;
import com.lvmama.vst.order.service.refund.OrderRefundComService;
import com.lvmama.vst.order.service.refund.OrderRefundComService.ORDER_REFUND_SERVICE_TYPE_KEY;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundAmountAdapter;
/**
 * 退款金额适配
 * @version 1.0
 */
@Service("orderRefundAmountAdapter")
public class OrderRefundAmountAdapterImpl implements OrderRefundAmountAdapter{
	private static final Log LOG = LogFactory.getLog(OrderRefundAmountAdapterImpl.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderRefundComService orderRefundComService;
	
	@Override
	public Long getOrderTotalChangeMount(Long orderId) {
		OrdOrder ordOrder =orderService.querySimpleOrder(orderId);
		if (ordOrder == null) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null");
        	return null;
        }
		return newInstall(ordOrder).getOrderTotalChangeMount(orderId);
	}
	
	@Override
	public Long getRefundAmount(Long orderId, Date applyDate) {
		//默认值
        long refundAmount = 0L;
		OrdOrder ordOrder = orderService.queryOrdorderByOrderId(orderId);
        if (ordOrder == null) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null");
        	return refundAmount;
        }
		return getRefundAmount(ordOrder, applyDate);
	}

	@Override
	public Long getRefundAmount(OrdOrder ordOrder, Date applyDate) {
		//默认值
		long refundAmount = 0L;
		if (ordOrder == null) {
			LOG.info("orderId=" +ordOrder +",ordOrder is not null");
			return refundAmount;
		}
		return newInstall(ordOrder).getRefundAmount(ordOrder, applyDate);
	}

	/**
	 * newInstall
	 */
	private IOrderRefundAmountService newInstall(OrdOrder ordOrder){
		return (IOrderRefundAmountService)orderRefundComService.newInstall(ordOrder,ORDER_REFUND_SERVICE_TYPE_KEY.AMOUNT);
	}
}
