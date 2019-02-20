/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
public interface OrderPromotionBussiness {

	/**
	 * 初始化促销信息
	 * @param order
	 * @param key
	 * @param promotionIds
	 * @return
	 */
	List<OrdPromotion> initPromotion(OrdOrderDTO order,String key,List<Long> promotionIds);
	
	/**
	 * 促销赋值
	 * @param obj
	 * @param promotion
	 * @return
	 */
	IPromFavorable fillFavorableData(Object obj, PromPromotion promotion);
}
