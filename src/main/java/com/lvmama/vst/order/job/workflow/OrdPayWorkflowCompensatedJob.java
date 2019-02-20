package com.lvmama.vst.order.job.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdPayProcessJobService;
import com.lvmama.vst.order.service.IOrdWorkflowCompensatedService;
import com.lvmama.vst.order.service.IOrderLocalService;

public class OrdPayWorkflowCompensatedJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OrdPayWorkflowCompensatedJob.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrdPayProcessJobService ordPayProcessJobService;
	
	@Autowired
	private IOrdWorkflowCompensatedService ordWorkflowCompensatedService;
	
	@Override
	public void run() {
		logger.info("OrdPayWorkflowCompensatedJob start...");
		if(isJobRunnable()){
			logger.info("OrdPayWorkflowCompensatedJob start...");
			List<Long> orderIds = ordPayProcessJobService.selectValidOrderIdList();
			
			if(CollectionUtils.isEmpty(orderIds)) {
				return;
			}
			
			for(Long orderId : orderIds) {
				doCompensatedOrdPayWorkflow(orderId);
			}
			
			logger.info("OrdPayWorkflowCompensatedJob end...");
		}
	}
	
	public void doCompensatedOrdPayWorkflow(final Long orderId){
		Thread task = new Thread() {
			@Override
			public void run() {
				logger.info("do OrdPayWorkflowCompensatedJob orderId:" + orderId);
				ordWorkflowCompensatedService.compensatedOrdPayWorkflow(orderId, true);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.info("OrdPayWorkflowCompensatedJob throw InterruptedException"); 
				}
			}
		};
		PayWorkflowThreadPoolExcutor.execute(task);
	}
	
	/**
	 * JOB是否运行
	 *
	 * @return
	 */
	public boolean isJobRunnable() {
		String jobEnabled = Constant.getInstance().getProperty("OrdPayWorkflowCompensated.enabled");
		if (logger.isDebugEnabled()) {
			logger.debug("job is runnable: " + jobEnabled + " => "
					+ ("true".equals(jobEnabled)));
		}
		if (jobEnabled != null) {
			return Boolean.valueOf(jobEnabled);
		} else {
			return true;
		}
	}
}
