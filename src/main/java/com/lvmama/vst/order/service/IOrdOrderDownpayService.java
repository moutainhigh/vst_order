package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderDownpay;

public interface IOrdOrderDownpayService {
	
	/**
	 * 根据订单id更新支付状态
	 * @param orderId
	 * @param payStatus
	 * @return
	 */
	public int updatePayStatusByOrderId(Long orderId,String payStatus);

	/**
	 * 根据订单id查询定金信息
	 * @param orderId
	 * @return
	 */
	public List<OrdOrderDownpay> selectByOrderId(Long orderId);

}
