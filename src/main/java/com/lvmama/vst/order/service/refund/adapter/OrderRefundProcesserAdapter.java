package com.lvmama.vst.order.service.refund.adapter;

import java.util.Date;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComAudit;


public interface OrderRefundProcesserAdapter {
	/**
	 * startProcesser
	 * @param orderId
	 * @param params
	 */
	public void startProcesserByRefund(Long orderId, Map<String, Object> params);
  
	/**
	 * completeTaskBySupplierConfirm
	 * @param order
	 * @param supplierKey
	 */
	public void completeTaskBySupplierConfirm(OrdOrder order, String supplierKey);
	
	/**
	 * 在线退款活动
	 * @param order
	 * @param comAudit
	 */
	public void completeTaskByOnlineRefundAudit(OrdOrder order, ComAudit comAudit);
	
	/**
	 * isStartProcessByRefund
	 * @param order
	 * @param operateName
	 * @return
	 */
	public boolean isStartProcessByRefund(OrdOrder order,String operateName);

	/**
	 * 新订单监控，取消确认操作
	 * @param order
	 * @param params
	 * @param applyDate
	 * @param operateName
	 */
	public void updateOrderStatusToOrderRefund(OrdOrder order, Map<String, Object> params
			, Date applyDate, String operateName);
	
}
