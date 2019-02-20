/**
 * 
 */
package com.lvmama.vst.order.service.book;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 保存orderItem的附加信息，
 * 目前主要有价格类型、酒店的天数、库存等
 * 
 * @author lancey
 *
 */
public interface OrderItemSaveBussiness {

	/**
	 * 保存订单子项的附加信息
	 * @param order
	 * @param orderItem
	 */
	void saveAddition(final OrdOrderDTO order,final OrdOrderItem orderItem);
	
	/**
	 * 保存订单子项人员关系
	 * @param orderItem
	 */
	void saveOrderItemPersonRelation(final OrdOrderItem orderItem);
}
