package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;
import com.lvmama.vst.back.intentionOrder.po.IntentionOrder;

/**
 * 订单意向单接口
 * @author chenpingfan
 *
 */
public interface IOrderIntentionService {

	public final static String ORD_NO = "orderNO";
	
	/**
	 * 根据相应的条件动态的查询意向单列表
	 * @param param
	 * @return
	 */
	public List<IntentionOrder> queryIntentionsByCriteria(Map<String, Object> param);
	
	
	/**
	 * 获取意向单查询数量
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param);
	
	/**
	 * 修改意向单
	 * @param intentionOrder
	 * @return
	 */
	public int updateIntention(IntentionOrder intentionOrder);
	
	
}
