package com.lvmama.vst.order.confirm.service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * 子订单确认邮件相关接口
 * Created by dongningbo on 2017/3/28.
 */
public interface IOrdItemConfirmEmailService {

	/**
	 *
	 * @param confirmStatus
	 * @param ordOrderItem
	 * @param operateName
	 * @param orderMemo
	 */
	public void notifyManager(String confirmStatus, OrdOrderItem ordOrderItem, String operateName, String orderMemo);
	
	
	public void notifyManagerByEmailAddress(OrdOrder order,String confirmStatus, OrdOrderItem ordOrderItem, String operateName, String orderMemo);
}
