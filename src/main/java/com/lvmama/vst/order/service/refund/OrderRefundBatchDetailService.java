package com.lvmama.vst.order.service.refund;

import java.util.List;

import com.lvmama.vst.back.order.po.OrderRefundBatchDetail;


/**
 * 订单退款明细日志
 * @version 1.0
 */
public interface OrderRefundBatchDetailService {

	/**
	 * 根据批次号和子订单号获取订单退款明细日志
	 * @param detail
	 * @return
	 */
	public List<OrderRefundBatchDetail> getOrderRefundBatchDetails(OrderRefundBatchDetail detail);
	
	void insertRefundBatchDetail(OrderRefundBatchDetail detail);

}
