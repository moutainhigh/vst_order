package com.lvmama.vst.order.service.refund.adapter;

import java.util.List;

import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;


/**
 * 前台提交验证服务
 * @version 1.0
 */
public interface OrderRefundFrontAdapter {
	//订单信息展示(产品,订单状态,价格)
	
	//按钮漏出规则
	public String getRefundStatusByOrderId(Long orderId, String systemType, String orderRefundStatus);
	
	/**
	 * 提交按钮校验退改规则
	 * @param orderId
	 * @throws IllegalArgumentException 校验失败
	 */
	public void checkRefundOnlineByCommit(Long orderId) throws IllegalArgumentException;
	
	/**
	 * 获取订单退款申请的信息
	 * @param orderId
	 * @return
	 */
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(long orderId);
	
}
