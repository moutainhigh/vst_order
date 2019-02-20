package com.lvmama.vst.order.service.audit;

import com.lvmama.vst.back.pub.po.ComAudit;

/**
 * 订单活动数据冗余服务
 * @author xiaoyulin
 *
 */
public interface ComAuditQueryConditionRaidService {
	/***主订单类型***/
	static final String OBJECT_ORDER = "ORDER";
	/***子订单类型***/
	static final String OBJECT_ORD_ITEM = "ORDER_ITEM";
	/**
	 * 保存活动查询条件数据
	 * @param comAudit
	 * @return
	 */
	void saveQueryConditionRaidData(ComAudit comAudit);
}
