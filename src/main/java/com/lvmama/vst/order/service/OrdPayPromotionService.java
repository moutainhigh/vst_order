package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
/**
 * 查询支付立减
 * @author JasonYu
 *
 */
public interface OrdPayPromotionService {
	
	public int savePayPromotion(OrdPayPromotion ordPayPromotion);
	/**
	 * 根据id 查询
	 * @param id
	 * @return
	 */
	public OrdPayPromotion queryOrdPayPromotionById(Long id); 
	/**
	 * 根据orderid查询
	 * @param orderId
	 * @return
	 */
	public OrdPayPromotion queryOrdPayPromotionByOrderId(Long orderId);
	
	/**
	 * 根据订单ID查询
	 * @param id
	 * @return
	 */
	public List<OrdOrderDownpay> queryOrderDownpayByOrderId(Long id);
	
	/**
	 * 添加定金支付
	 * @param ordOrderDownpay
	 * @return
	 */
	public int saveOrderDownpay(OrdOrderDownpay ordOrderDownpay);
	
	/**
	 * 修改定金支付
	 * @return
	 */
	public int updateByPrimaryKeyOrderId(OrdOrderDownpay ordOrderDownpay);
	
}
