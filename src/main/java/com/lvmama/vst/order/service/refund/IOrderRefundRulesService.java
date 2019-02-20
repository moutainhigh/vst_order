package com.lvmama.vst.order.service.refund;

import java.util.Date;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * 订单退款规则Service
 * @version 1.0
 */
public interface IOrderRefundRulesService {

	/**
	 * 获取线路订单的退改规则
	 * 主订单详情页显示 (子订单集合)
	 * @param order
	 * @return
	 */
	public String getRouteOrderRefundRules(OrdOrder order);
	
	/**
	 * 获取退改中最晚日期
	 * @param orderItem
	 * @return
	 */
	public Date getLastTime(OrdOrderItem orderItem) throws Exception;

}
