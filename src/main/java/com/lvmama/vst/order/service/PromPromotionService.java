/**
 * 
 */
package com.lvmama.vst.order.service;

/**
 * 促销接口服务
 * @author lancey
 *
 */
public interface PromPromotionService {

	/**
	 * 增加已使用金额
	 * @param amount
	 * @param promPromotionId
	 * @return
	 */
	 public int addPromAmount(Long amount,Long promPromotionId);
	 /**
	  * 减少已使用金额
	  * @param amount
	  * @param promPromotionId
	  * @return
	  */
	 public int subtractPromAmount(Long amount,Long promPromotionId);
}
