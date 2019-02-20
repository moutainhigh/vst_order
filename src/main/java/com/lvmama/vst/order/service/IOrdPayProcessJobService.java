package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.order.po.OrdPayProcessJob;

/**
 * 支付流程补偿job Service
 * 
 * @author xiaoyulin
 *
 */
public interface IOrdPayProcessJobService {
	/**
	 * 插入需要补偿的单子
	 * @param record
	 * @return
	 */
	int insert(OrdPayProcessJob record);
	
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

	OrdPayProcessJob selectByPrimaryKey(Long orderId);
	
	/**
	 * 补偿成功
	 * @param orderId
	 * @return
	 */
	public int makeValid(Long orderId);
}
