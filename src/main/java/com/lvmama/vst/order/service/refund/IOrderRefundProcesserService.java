package com.lvmama.vst.order.service.refund;

import java.util.Date;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComAudit;

/**
 * 订单退款流程Service
 * @version 1.0
 */
public interface IOrderRefundProcesserService {
	/** 单酒店*/
	public static final String REFUND_SINGLE_HOTEL_PROCESS_KEY= "destbu_refund_single_hotel_pre_order";
	/** 自由行酒景*/
	public static final String REFUND_ORDER_PREPAID_MAIN_PROCESS_KEY= "destbu_refund_order_pre_main";
	
	/**
	 * startProcesser
	 * @param ordOrder
	 * @param params
	 */
	public void startProcesserByRefund(OrdOrder ordOrder, Map<String, Object> params);
	
	/**
	 * 新订单监控，取消确认操作
	 * @param order
	 * @param params
	 * @param applyDate
	 */
	public void updateOrderStatusToOrderRefund(OrdOrder order,Map<String, Object> params,Date applyDate);
	
	/**
	 * 在线退款活动
	 * @param comAudit
	 */
	public void completeTaskByOnlineRefundAudit(ComAudit comAudit);
	
	/**
	 * 供应商确认活动
	 * @param order
	 * @param supplierKey
	 */
	public void completeTaskBySupplierConfirm(OrdOrder order,String supplierKey);
	
	/**
	 * 是否开启退款流程(后台)
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
	public void updateOrderStatusToOrderRefund(OrdOrder order,Map<String, Object> params
			,Date applyDate,String operateName);
}
