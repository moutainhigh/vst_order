package com.lvmama.vst.order.service.refund;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;

/**
 * 我的工作台服务
 * @author chenhao
 *
 */
public interface IOrderRefundFrontService {
	//订单信息展示(产品,订单状态,价格)
	
	/**
	 * 前台按钮露出状态码
	 * @param order
	 * @param refundApplyStatus
	 * @return
	 */
	public String getOrderRefundApplyStatus(OrdOrder order,String refundApplyStatus);
	
	/**
	 * 提交按钮校验退改规则
	 * @param ordOrder
	 * @throws IllegalArgumentException 校验失败
	 */
	public void checkRefundOnlineByCommit(OrdOrder ordOrder) throws IllegalArgumentException;
	
	/**
	 * 获取订单退款申请的信息
	 * @param ordOrder
	 * @return
	 */
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(OrdOrder ordOrder);
}

