package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.order.dao.OrdPromotionDao;
import com.lvmama.vst.order.service.OrderPromotionService;

@Service
public class OrderPromotionServiceImpl implements OrderPromotionService {

	@Autowired
	private OrdPromotionDao ordPromotionDao;
	
	@Override
	public List<OrdPromotion> selectOrdPromotionsByOrderItemId(
			Map<String,Object> params) {
		return ordPromotionDao.selectOrdPromotionsByOrderItemId(params);
	}
	@Override
    public List<OrdPromotion> selectOrdPromotionsByOrderId(Long orderId){
    	return ordPromotionDao.selectOrdPromotionsByOrderId(orderId);
    }

}
