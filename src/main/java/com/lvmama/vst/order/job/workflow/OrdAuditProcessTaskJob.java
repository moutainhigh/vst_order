package com.lvmama.vst.order.job.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.processer.workflow.WorkflowThreadPoolExcutor;
import com.lvmama.vst.order.service.IOrdAuditProcessTaskService;
import com.lvmama.vst.order.service.IOrderLocalService;

/**
 * 订单审核流程补偿
 * @author xiaoyulin
 *
 */
public class OrdAuditProcessTaskJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrdAuditProcessTaskJob.class);
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrdAuditProcessTaskService ordAuditProcessTaskService;
	
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			logger.info("OrdAuditProcessTaskJob start...");
			List<Long> orderIds = ordAuditProcessTaskService.selectValidOrderIdList();
			
			if(CollectionUtils.isEmpty(orderIds)) {
				return;
			}
			
			for(Long orderId : orderIds) {
				doOrdAuditProcessTask(orderId);
			}
			
			logger.info("OrdAuditProcessTaskJob end...");
		}
	}
	
	public void doOrdAuditProcessTask(final Long orderId){
		Thread task = new Thread() {
			@Override
			public void run() {
				logger.info("do OrdAuditProcessTaskJob orderId:" + orderId);
				OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
				boolean flag = orderLocalService.handelAuditReceiveTask(order);
				if(flag){
					logger.info("OrdAuditProcessTaskJob success!"); 
				}else{
					logger.error("OrdAuditProcessTaskJob failure!"); 
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("OrdAuditProcessTaskJob throw InterruptedException"); 
				}
			}
		};
		WorkflowThreadPoolExcutor.execute(task);
	}
}
