package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrder;


/**
 * 针对vst提前退相关服务
 * @author taiqichao
 *
 */
public interface PreRefundService {	
	
	int selectPreRefundCountByOrderId(Long orderId);
	
	int selectPreRefundCountByUserId(Long userId);
	
	int selectPreRefundCountByMobie(String mobie);

	void increase(Long orderId);

	boolean isPreRefundCountLessThan3(Long orderId);

	boolean canPreRefund(Long orderId);

	boolean canPreRefund(OrdOrder order);
}
