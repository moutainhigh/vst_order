package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.order.dao.OrdOrderDownpayDao;
import com.lvmama.vst.order.dao.OrdPayPromotionDao;
import com.lvmama.vst.order.service.OrdPayPromotionService;

/**
 * 
 * @author JasonYu
 *
 */
@Service
public class OrdPayPromotionServiceImpl implements OrdPayPromotionService{
	@Autowired
	private OrdPayPromotionDao dao;
	
	@Autowired
	private OrdOrderDownpayDao ordOrderDownpayDao;

	@Override
	public int savePayPromotion(OrdPayPromotion ordPayPromotion) {
		
		return dao.insert(ordPayPromotion);
	}

	@Override
	public OrdPayPromotion queryOrdPayPromotionById(Long id) {

		return dao.queryOrdPayPromotionById(id);
	}

	@Override
	public OrdPayPromotion queryOrdPayPromotionByOrderId(Long orderId) {
		return dao.queryOrdPayPromotionByOrderId(orderId);
	}

	@Override
	public List<OrdOrderDownpay> queryOrderDownpayByOrderId(Long id) {
		
		return ordOrderDownpayDao.selectByOrderId(id);
	}

	@Override
	public int saveOrderDownpay(OrdOrderDownpay ordOrderDownpay) {
		
		return ordOrderDownpayDao.insert(ordOrderDownpay);
	}

	@Override
	public int updateByPrimaryKeyOrderId(OrdOrderDownpay ordOrderDownpay) {
		
		return ordOrderDownpayDao.updateByPrimaryKeyOrderId(ordOrderDownpay);
	}

}
