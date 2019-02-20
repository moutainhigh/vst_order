package com.lvmama.vst.order.service.refund;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrderRefundBatch;


/**
 * 订单退款批次Service
 * @version 1.0
 */
public interface OrderRefundBatchService {

	
	
	/**
	 * 根据订单ID查询退款明细(按退款申请时间倒序)
	 * @param orderId
	 * @return
	 */
	public List<OrderRefundBatch> getRefundBatchAndSuppGoods(Long orderId);
	
	void insertOrderRefundBatch(OrderRefundBatch batch);

	void updateStatus(Map<String, Object> map);

	void updateOrderRefundBatch(Map<String, Object> map);


	List<OrderRefundBatch> getOrderRefundBatch(Map<String, Object> map);

}
