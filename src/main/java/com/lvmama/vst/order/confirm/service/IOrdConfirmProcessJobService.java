package com.lvmama.vst.order.confirm.service;

import com.lvmama.vst.order.po.OrdConfirmProcessJob;

import java.util.List;

/**
 * 订单确认流程补偿job
 */
public interface IOrdConfirmProcessJobService {
	/**
	 * 插入需要补偿的单子
	 * @param record
	 * @return
	 */
	int insert(OrdConfirmProcessJob record);
	
	/**
	 * 查询需要补偿的订单集合
	 * @return
	 */
	List<OrdConfirmProcessJob> selectValidOrdConfirmProcessJobList();
	
	/**
	 * 补偿次数累加
	 * @param orderItemId
	 * @return
	 */
	int addTimes(Long orderItemId);

	OrdConfirmProcessJob selectByPrimaryKey(Long orderItemId);
	
	/**
	 * 补偿成功
	 * @param orderItemId
	 * @return
	 */
	public int makeValid(Long orderItemId);
}
