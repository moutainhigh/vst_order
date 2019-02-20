package com.lvmama.vst.order.service;

/**
 * 工作流补偿服务
 * @author xiaoyulin
 *
 */
public interface IOrdWorkflowCompensatedService {
	/**
	 * 工作流补偿服务
	 * @param orderId
	 * @param isJob
	 */
	public boolean compensatedOrdPayWorkflow(Long orderId, boolean isJob);
}
