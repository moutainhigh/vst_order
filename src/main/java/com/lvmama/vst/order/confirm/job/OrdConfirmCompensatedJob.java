package com.lvmama.vst.order.confirm.job;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.utils.ConfirmUtils;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.order.confirm.service.IOrdConfirmProcessJobService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.po.OrdConfirmProcessJob;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 子订单确认流程补偿
 *
 */
public class OrdConfirmCompensatedJob implements Runnable{
	private static final Logger LOG = LoggerFactory.getLogger(OrdConfirmCompensatedJob.class);
	
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private IOrdConfirmProcessJobService ordConfirmProcessJobService;
	@Autowired
	private IOrdItemConfirmProcessService ordItemConfirmProcessService;
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Override
	public void run() {
		boolean isRun = ConfirmUtils.isNewWorkflowStart();
		LOG.info("OrdConfirmCompensatedJob start...,isRun=" +isRun);
		
		if(isRun){
			List<OrdConfirmProcessJob> list = ordConfirmProcessJobService.selectValidOrdConfirmProcessJobList();
			if(CollectionUtils.isEmpty(list)) {
				return;
			}
			for(OrdConfirmProcessJob record : list) {
				LOG.info("orderItemId orderItemId:" + record.getOrderItemId()
						+",auditId=" +record.getAuditId());
				//加载参数
				OrdOrderItem orderItem= orderLocalService.getOrderItem(record.getOrderItemId());
				//补偿
				ComAudit audit =orderAuditService.queryAuditById(record.getAuditId());
				completeTask(orderItem, audit);
			}
			LOG.info("OrdConfirmCompensatedJob end...");
		}
	}

	/**
	 * 确认流程补偿
	 * @param orderItem
	 * @param audit
	 */
	private void completeTask(OrdOrderItem orderItem, ComAudit audit) {
		try {
			ordItemConfirmProcessService.completeAuditTaskByConfirm(orderItem, audit);
			
			ordConfirmProcessJobService.makeValid(orderItem.getOrderItemId());
		} catch (Exception ex) {
			LOG.error("OrderItemId=" +  orderItem.getOrderItemId()
					+ ",Exception:" +ExceptionFormatUtil.getTrace(ex));
			
			ordConfirmProcessJobService.addTimes(orderItem.getOrderItemId());
		}
	}
}
