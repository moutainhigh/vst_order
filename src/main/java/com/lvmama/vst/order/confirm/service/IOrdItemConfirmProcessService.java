package com.lvmama.vst.order.confirm.service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;

public interface IOrdItemConfirmProcessService {
	/**
	 * 触发流程节点-确认子单
	 * @param orderItem
	 * @param audit
	 */
	public void completeAuditTaskByConfirm(OrdOrderItem orderItem, ComAudit audit);
	/**
	 * 触发流程节点(任务key)-确认子单
	 * @param orderItem
	 * @param taskKey
	 * @param operator
	 */
	public void completeUserTaskByConfirm(OrdOrderItem orderItem, String taskKey, String operator);
	/**
	 * 触发流程节点-确认子单(异常补偿)
	 * @param orderItem
	 * @param audit
	 */
	public void completeTaskByAuditHasCompensated(OrdOrderItem orderItem, ComAudit audit);
	/**
	 * 触发流程节点-o2o主单
	 * @param order
	 * @param taskKey
	 * @param operator
	 */
	public void completeUserTask_O2O(OrdOrder order, String taskKey, String operator);
	/**
	 * 触发流程节点-o2o子单
	 * @param orderItem
	 * @param audit
	 */
	public void completeAuditTask_O2O(OrdOrderItem orderItem, ComAudit audit);

}
