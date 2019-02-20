package com.lvmama.vst.order.service.refund;

import java.util.Date;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * 退款金额服务
 * @version 1.0
 */
public interface IOrderRefundAmountService {
	/**
     * 获取订单被修改金额总和
     * @param orderId
     * @return
     */
    public Long getOrderTotalChangeMount(Long orderId);
    
    /**
     * 目的地订单获取退款金额(前台)
     * @param ordOrder 主订单
     * @param applyDate 申请时间
     */
    public Long getRefundAmount(OrdOrder ordOrder, Date applyDate);
}
