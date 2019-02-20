package com.lvmama.vst.order.service;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IOrdOrderItemService {
	
	public List<OrdOrderItem> selectOrderItemsByIds(List<Long> ids);
	
	/**
	 * 根据订单ID查询订单子项集合
	 * @param orderId
	 * @return
	 */
	public List<OrdOrderItem> selectByOrderId(Long orderId);
	
	
	public int updateOrdOrderItem(OrdOrderItem ordOrderItem);
	/**
	 * 根据下单时间和产品统计orderItem数量
	 * @param params
	 * @return
	 */
	public Integer countOrderItemByCreateTimeAndProductId(Map<String, Object> params);

	/**
	 * 选择更新子订单对象
	 * @param ordOrderItem
	 * @return
	 */
	public Integer updateByPrimaryKeySelective(OrdOrderItem ordOrderItem);

	/**
	 * 根据订单id查询
	 * @param orders
	 * @return
	 */
	public List<OrdOrderItem> selectOrderItemsByorderIds(List<Long> orders);
	
	
	/**
	 * 根据产品ID和门店ID查询指定时间内的有效订单的数量 
	 * @param params
	 * @return
	 */
	public Integer countOrderMainItemForO2oTicketByProductId(Map<String, Object> params);
	
	/**
	 * 根据子订单号查询子订单
	 * */
	public OrdOrderItem selectOrderItemByOrderItemId(Long orderItemId);

	/**
	 * 查询一段时间内的子单
	 * @param paramMap
	 * @return
	 */
	public List<OrdOrderItem> queryOrderItemListByCreateTimeBetween(Map<String, Object> paramMap);

	/**
	 * 传入时间之后新增的门票子订单数量
	 * @param userId
	 * @param createTime
	 * @return
	 */
	
	public Long getTicketItemCountByUserIdAndCreateTime(String userId, Date createTime);
	
	/**
	 * 用户门票子订单列表
	 * @param userId
	 * @return
	 */
	public List<OrdOrderItem> getTicketItemListByUserId(String userId);

	
	public List<OrdOrderItem> selectByParams(Map<String, Object> params);
	
	/**
	 * 更新子订单,快递发货状态
	 * @param orderItemId
	 * @param courierStatus
	 * @return
	 */
	public String updateCourierStatus(Long orderItemId, String courierStatus);
	
	@ReadOnlyDataSource
	public List<OrdOrderItem> selectSubOrderItemsByIds(List<Long> ids);
	
	@ReadOnlyDataSource
	public List<OrdOrderItem> queryTicketOrderItem(Map<String,Object> params);
	
	/**<!-- 获取该商品当天子订单列表【除去废单下的子单】-->
	 * @param params
	 * @return
	 */
	@ReadOnlyDataSource
	public List<OrdOrderItem> queryOrderItemListByParams(Map<String,Object> params);



	@ReadOnlyDataSource
	public List<OrdOrderItem> getPageTicketItemListByUserId(Map<String,Object> paramsMap);

	@ReadOnlyDataSource
	public Integer getTicketItemCountByUserId(String userId);
	
	@ReadOnlyDataSource
	public List<VstOrdOrderItemDateVo> queryTicketOrderItemByVisitDate(
			Map<String, Object> params);
	
	public List<Long> getOrderItemIdListByOrderId(Long orderId) ;
	
	public Long getOrderIdByOrderItemId(Long orderItemId);
	
	public OrdPassCode getOrdPassCodeByOrderItemId(final Long orderItemId);
	/**
	 * 根据订单的userId、orderId查询出所有子订单中属于门票的子订单
	 * @param params
	 * @return
	 */
	public List<OrdOrderItem> queryTicketOrderItemByOrderId(Map<String, Object> params);
	
	public Integer queryOrderItemCountByVisitDate(Map<String,Object> params);
	
	/**
	 * 单表查询，查询特殊供应商的组织下的子订单数
	 * 
	 * @param params
	 * @return
	 */
	public Integer getTotalCount(Map<String,Object> params);
	/**
	 * 获取用户门票子订单
	 * @param params
	 * @return
	 */
	public List<OrdETicketOrderItem> selectUserTicketByParams(Map<String, Object> params);

	/**
	 * 获取用户子订单信息
	 * @param params
	 * @return
	 */
	public List<OrdOrderItem> queryUserOrderItemList(Map<String, Object> params);

	/**
	 * 修改子订单价格确认状态(订单金额转义过程中用到)
	 * @param orderId 订单状态
	 * @param priceConfirmStatus 子订单价格确认状态
	 * @return
	 */
	public int updatePriceConfirmStatusByOrderId(Long orderId,String priceConfirmStatus);

	/**
	 * 查询所有符合条件的子订单集合
	 *
	 * @param params Map<String, Object>
	 * @return List<OrdOrderItemView>
	 */
	public List<OrdOrderItemView> selectListByParams(Map<String, Object> params);

	/**
	 * 查询子订单附加表ord_order_item_extend信息
	 * @param
	 * @return OrdOrderItemExtend
	 */
	public OrdOrderItemExtend selectOrdOrderItemExtendByOrderItemId(Long orderItemId);



}
