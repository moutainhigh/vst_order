package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.order.po.OrdAuditProcessTask;

/**
 * 支付流程补偿job Service
 * 
 * @author xiaoyulin
 *
 */
public interface IOrdAuditProcessTaskService {
	/**
	 * 插入需要补偿的单子
	 * @param record
	 * @return
	 */
	int insert(OrdAuditProcessTask record);
	
	/**
	 * 查询需要补偿的订单号
	 * @return
	 */
	List<Long> selectValidOrderIdList();
	
	/**
	 * 补偿次数累加
	 * @param orderId
	 * @return
	 */
	int addTimes(Long orderId);

	OrdAuditProcessTask selectByPrimaryKey(Long orderId);
	
	/**
	 * 标记成功
	 * @param orderId
	 * @return
	 */
	public int makeSuccess(Long orderId);
	
	/**
	 * 标记结束
	 * @param orderId
	 * @return
	 */
	public int makeValid(Long orderId);
}
