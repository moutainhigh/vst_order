package com.lvmama.vst.order.service.refund.adapter;

import com.lvmama.vst.back.order.po.OrdOrder;

import java.util.Date;

public interface OrderRefundAmountAdapter {
  /**
   * 获取订单被修改金额总和
   * @param orderId 主订单号
   * @return
   */
  public Long getOrderTotalChangeMount(Long orderId);
  
  /**
   * 目的地订单获取退款金额(前台)
   * @param orderId 主订单ID
   * @param applyDate 申请时间
   */
  public Long getRefundAmount(Long orderId, Date applyDate);


  /**
   * 目的地订单获取退款金额（在已经查询订单后调用）
   * @param ordOrder
   * @param applyDate
   * @return
   */
  public Long getRefundAmount(OrdOrder ordOrder, Date applyDate);
  
}
