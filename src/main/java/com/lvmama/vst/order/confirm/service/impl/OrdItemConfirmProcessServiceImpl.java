
package com.lvmama.vst.order.confirm.service.impl;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.order.confirm.factory.ActivitiKeyFactory;
import com.lvmama.vst.order.confirm.service.IOrdConfirmProcessJobService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.po.OrdConfirmProcessJob;
import com.lvmama.vst.order.service.IOrderLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ordItemConfirmProcessService")
public class OrdItemConfirmProcessServiceImpl implements IOrdItemConfirmProcessService {
	private static final Logger LOG = LoggerFactory.getLogger(OrdItemConfirmProcessServiceImpl.class);
	@Autowired
	private ProcesserClientService processerClientService;
	@Autowired
	private IOrdConfirmProcessJobService ordConfirmProcessJobService;
	@Autowired
	private IOrderLocalService orderService;

	@Override
	public void completeAuditTaskByConfirm(OrdOrderItem orderItem, ComAudit audit) {
		ActivitiKey activitiKey =ActivitiKeyFactory.createKeyByOrderItem_confirm(orderItem, ActivitiUtils.ITEM_TYPE.approve.name());
		LOG.info(activitiKey.toString());
		processerClientService.completeTaskByAudit(activitiKey, audit);
		
		LOG.info("OrderItemId=" + orderItem.getOrderItemId() + ",auditId="
				+ audit.getAuditId() + ",type=" + audit.getAuditType());
	}
	@Override
	public void completeUserTaskByConfirm(OrdOrderItem orderItem, String taskKey,
			String operator) {
		ActivitiKey activitiKey =ActivitiKeyFactory.createKeyByOrderItem_confirm(orderItem, ActivitiUtils.ITEM_TYPE.approve.name());
		processerClientService.completeTask(activitiKey, taskKey, operator);
		
		LOG.info("OrderItemId=" + orderItem.getOrderItemId() + ",taskKey="
				+ taskKey);
	}
	@Override
	public void completeTaskByAuditHasCompensated(OrdOrderItem orderItem, ComAudit audit){
		try {
			if(orderItem ==null || audit ==null){
				return;
			}
			completeAuditTaskByConfirm(orderItem, audit);

		} catch (Exception ex) {
			LOG.error("orderItemId=" +orderItem.getOrderItemId()
					+", error:"+ ex);
			//记录补偿
			OrdConfirmProcessJob record = ordConfirmProcessJobService.selectByPrimaryKey(orderItem.getOrderItemId());
			if(record == null){
				record = new OrdConfirmProcessJob();
				record.setOrderId(orderItem.getOrderId());
				record.setOrderItemId(orderItem.getOrderItemId());
				record.setAuditId(audit.getAuditId());
				record.setTimes(0L);
				ordConfirmProcessJobService.insert(record );
			}
		}
	}
	@Override
	public void completeUserTask_O2O(OrdOrder order, String taskKey, String operator) {
		ActivitiKey activitiKey = createKeyByOrder_O2O(order);
		processerClientService.completeTask(activitiKey, taskKey, operator);
		LOG.info("OrderId=" + order.getOrderId() + ",taskKey="
				+ taskKey);
	}
	@Override
	public void completeAuditTask_O2O(OrdOrderItem orderItem, ComAudit audit) {
		ActivitiKey activitiKey = ActivitiKeyFactory.createKeyByOrderItem_O2O(orderItem, ActivitiUtils.getOrderO2oType(audit));
		processerClientService.completeTaskByAudit(activitiKey,audit);
	}
	/**
	 * createKeyByOrder_O2O
	 */
	private ActivitiKey createKeyByOrder_O2O(OrdOrder order){
		ComActivitiRelation relation = orderService.getRelation(order);
		return ActivitiKeyFactory.createKeyByOrder_O2O(relation, order);
	}

}
