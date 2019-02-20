package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrdTicketPerformDetail;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;

/**
 * 综合查询业务接口
 * 
 * @author wenzhengtao
 * 
 */
public interface IComplexQueryService {
	/**
	 * 根据各种条件查询订单集合
	 * 
	 * @param condition
	 * @return
	 */
	@ReadOnlyDataSource
	List<OrdOrder> queryOrderListByCondition(OrderMonitorCnd orderMonitorCnd, ComplexQuerySQLCondition condition);
	/**
	 * 从读库中根据各种条件统计订单数量
	 * 
	 * @param condition
	 * @return
	 */
	@ReadOnlyDataSource
	Long queryOrderCountByCondition(OrderMonitorCnd orderMonitorCnd);
	/**
	 * 根据各种条件查询订单集合
	 * 
	 * @param condition
	 * @return
	 */
	List<OrdOrder> queryOrderListByCondition(final ComplexQuerySQLCondition condition);

	/**
	 * 根据各种条件统计订单数量
	 * 
	 * @param condition
	 * @return
	 */
	@ReadOnlyDataSource
	Long checkOrderCountFromReadDB(final ComplexQuerySQLCondition condition);
	
	/**
	 * 从读库中根据各种条件查询订单集合
	 * 
	 * @param condition
	 * @return
	 */
	@ReadOnlyDataSource
	List<OrdOrder> checkOrderListFromReadDB(final ComplexQuerySQLCondition condition);


	/**
	 * 从读库中根据各种条件查询订单集合
	 *只查询订单不组合其他数据
	 * @param condition
	 * @return
	 */
	@ReadOnlyDataSource
	List<OrdOrder> checkOnlyOrderListFromReadDB(final ComplexQuerySQLCondition condition);

	/**
	 * 从读库中根据各种条件统计订单数量
	 * 
	 * @param condition
	 * @return
	 */
	Long queryOrderCountByCondition(final ComplexQuerySQLCondition condition);
	
	/**
	 * 
	 * @param orderId
	 * @return
	 */
	OrdOrder queryOrderByOrderId(final Long orderId);
	
	/**
	 * 
	 * @param orderId
	 * @return
	 */
	List<OrdTicketPerform> findOrdTicketPerformList(Long orderId);
	
	/**
	 * 
	 * @param orderItemId
	 * @return
	 */
	List<OrdTicketPerform> selectByOrderItem(Long orderItemId);
	
	/**
	 * 根据子订单ID查询履行记录
	 * @param orderItemIds
	 * @return
	 */
	@ReadOnlyDataSource
	List<OrdTicketPerform> selectByOrderItems(List<Long> orderItemIds);
	
	/**
	 * 
	 * @param orderItemId
	 * @return
	 */
	List<OrdTicketPerformDetail> selectPerformDetailByOrderItem(Long orderItemId);
	/**
	 * 查询传入sql语句
	 * */
	public List<Map<String, Object>> findAllObjectsBySql(Map<String, Object> params);

	/**
	 * 查询传入sql语句（从只读库查询）
	 * */
	public List<Map<String, Object>> findAllObjectsBySqlFromReadDB(Map<String, Object> params);



	/**
	 * 查询需要生成工作流的订单
	 * @return
	 */
	public List<Long> findNeedGenWorkflowOrders();
	
	/**
	 * 查询需要调用创建供应商订单的订单ID
	 * @return
	 */
	public List<Long> findNeedCreateSupplierOrders();
	
	/**
	 * 查询需要调用取消供应商订单的订单ID
	 * @return
	 */
	public List<Long> findNeedCancelSupplierOrders();
	
	/**
	 * 查询需要触发补偿支付工作流的订单IDs
	 * @return
	 */
	public List<Long> findNeedTiggerPayProcOrders() ;
	
	/**
	 * 更新是否触发支付工作流状态
	 * 
	 * @param paramsMap
	 * @return
	 */
	public int updatePayProcTriggeredByOrderID(Map<String, Object> paramsMap);

	

	
	/**
	 * 查询主订单的设备号
	 * 
	 * @param Long (orderId)
	 * @return
	 */
	public String findMobileId(Long orderId);


	List<Map<String, Object>> selectOrdOrderByOrderIds(Map<String, Object> params);

}
