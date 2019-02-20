package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderPromotionClientService;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.order.service.OrderPromotionService;

@Component("orderPromotionServiceRemote")
public class OrderPromotionClientServiceImpl implements OrderPromotionClientService {
	@Autowired
	private OrderPromotionService promotionService;

	@Override
	public List<OrdPromotion> selectOrdPromotionsByOrderItemId(Map<String, Object> params) {
		return promotionService.selectOrdPromotionsByOrderItemId(params);
	}

	@Override
	public List<OrdPromotion> selectOrdPromotionsByOrderId(Long orderId) {
		return promotionService.selectOrdPromotionsByOrderId(orderId);
	}

}
