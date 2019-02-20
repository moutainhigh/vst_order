package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdPromotion;

public interface OrderPromotionService {
	
	/**
	 * 查询主订单的促销信息
	 * @param orderItemIdList
	 * @return
	 */
	public List<OrdPromotion> selectOrdPromotionsByOrderItemId(Map<String,Object> params);
	/**
	 * 根据订单id查询所有促销记录
	 * @param orderId
	 * @return
	 */
	 public List<OrdPromotion> selectOrdPromotionsByOrderId(Long orderId);
	
}
