/**
 * 
 */
package com.lvmama.vst.order.service.book;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
public interface OrderInitBussiness {

	/**
	 * 初始化订单子项相关信息
	 * @param orderItem
	 * @param order
	 * @return
	 */
	boolean initOrderItem(final OrdOrderItem orderItem,final OrdOrderDTO order);
}
