package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;

public interface IOrdOrderQueryInfoService {
	/**
	 * 保存订单查询信息
	 * @param orderQueryInfo
	 * @return
	 */
//	int saveOrderQueryInfo(OrdOrderQueryInfo orderQueryInfo);

	/**
	 * 修改订单查询信息
	 * @param orderQueryInfo
	 * @return
	 */
//	int updateOrderQueryInfo(OrdOrderQueryInfo orderQueryInfo);

	/**
	 * 根据订单号修改订单查询信息
	 * @param orderQueryInfo
	 * @return
	 */
//	int updateQueryInfoByOrderId(OrdOrderQueryInfo orderQueryInfo);
	
	/**
	 * 根据子订单号修改订单查询信息
	 * @param orderQueryInfo
	 * @return
	 */
//	int updateQueryInfoByOrderItemId(OrdOrderQueryInfo orderQueryInfo);
	
	/**
	 * 查询需要迁移到历史表的数据
	 * @return
	 */
//	List<Long> findOrdQueryInfoHistoryData();
	
	/**
	 * 订单查询信息的历史数据迁移
	 * @param queryInfoIds
	 * @return
	 */
//	int saveOrdQueryInfoHistoryData(List<Long> queryInfoIds);
	
	/**
	 * 根据查询条件获取满足条件的订单号列表
	 * @param orderMonitorCnd
	 * @return
	 */
//	List<Long> findOrderIdsByCondition(OrderMonitorCnd orderMonitorCnd);
	
	/**
	 * 根据查询条件获取满足条件的订单数量
	 * @param orderMonitorCnd
	 * @return
	 */
//	Long findOrderCountByCondition(OrderMonitorCnd orderMonitorCnd);
}
