package com.lvmama.vst.order.service;

public interface IntfPassCodeService {
	
	/**
	 * 根据申码成功消息objectId的codeId 查主订单号
	 */
	public Long getOrderIdByPassCodeId(Long passCodeId);
	
}
