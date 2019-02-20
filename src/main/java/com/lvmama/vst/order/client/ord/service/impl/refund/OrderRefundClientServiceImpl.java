package com.lvmama.vst.order.client.ord.service.impl.refund;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.order.service.refund.adapter.OrderRefundAmountAdapter;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundFrontAdapter;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
import com.lvmama.vst.pet.adapter.refund.IOrderRefundClientService;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;

/**
 * 在线退款服务
 * @version 1.0
 */
@Component("orderRefundClientServiceRemote")
public class OrderRefundClientServiceImpl implements IOrderRefundClientService {
	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;
	@Autowired
	private OrderRefundAmountAdapter orderRefundAmountAdapter;
	@Autowired
	private OrderRefundFrontAdapter orderRefundFrontAdapter;
	
	@Override
	public void startProcesserByRefund(Long orderId,Map<String, Object> params) {
		orderRefundProcesserAdapter.startProcesserByRefund(orderId, params);
	}
	
	@Override
	public Long getRefundAmount(Long orderId, Date applyDate){
		return orderRefundAmountAdapter.getRefundAmount(orderId, applyDate);
	}
	@Override
	public Long getOrderTotalChangeMount(Long orderId){
		 return orderRefundAmountAdapter.getOrderTotalChangeMount(orderId);
	}

	@Override
	public String getRefundStatusByOrderId(Long orderId, String systemType,
			String orderRefundStatus) {
		return orderRefundFrontAdapter.getRefundStatusByOrderId(orderId, systemType, orderRefundStatus);
	}

	@Override
	public void checkRefundOnlineByCommit(Long orderId)
			throws IllegalArgumentException {
		orderRefundFrontAdapter.checkRefundOnlineByCommit(orderId);
	}

	@Override
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(long orderId) {
		return orderRefundFrontAdapter.getOrderRefundDetailVO(orderId);
	}

}
