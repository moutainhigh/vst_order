package com.lvmama.vst.order.service;

import com.lvmama.comm.bee.po.ord.OrdRefundmentLog;

/**
 * 
 * 执行订单退款单日志记录
 *
 */
public interface IOrdRefundmentLogService {

	
	public int addOrdRefundmentLog(OrdRefundmentLog ordRefundmentLog);
	
	
	
	
}
