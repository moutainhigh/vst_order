package com.lvmama.vst.order.dao;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OverdueTicketSubOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdETicketOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderItemView;
import com.lvmama.vst.back.order.po.VstOrdOrderItemDateVo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderItemDao extends MyBatisDao {

	public OrdOrderItemDao() {
		super("ORD_ORDER_ITEM");
	}

	public int deleteByPrimaryKey(Long orderItemId) {
		return super.delete("deleteByPrimaryKey", orderItemId);
	}

	public int insert(OrdOrderItem ordOrderItem) {
		setOrderDefaultPriceConfirmStatus(ordOrderItem);
		return super.insert("insert", ordOrderItem);
	}
	
	public int insertForPreLockSeat(OrdOrderItem ordOrderItem) {
		setOrderDefaultPriceConfirmStatus(ordOrderItem);
		return super.insert("insertForPreLockSeat", ordOrderItem);
	}


	public int insertSelective(OrdOrderItem ordOrderItem) {
		setOrderDefaultPriceConfirmStatus(ordOrderItem);
		return super.insert("insertSelective", ordOrderItem);
	}


	/**
	 * 	当创建子订单时，默认子订单价格确认状态为 价格已确认
	 *
	 */
	private void setOrderDefaultPriceConfirmStatus(OrdOrderItem ordOrderItem){
		if(StringUtils.isEmpty(ordOrderItem.getPriceConfirmStatus())){
			ordOrderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());
		}
	}


	public OrdOrderItem selectByPrimaryKey(Long orderItemId) {
		return super.get("selectByPrimaryKey", orderItemId);
	}
	
	public List<OrdOrderItem> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	/**
	 * 为OrderSettlement页面查询结果
	 * @param map
	 * @return list
	 */
	public List<OrdOrderItem> selectByParamsForOrdSettlement(Map<String, Object> params) {
		return super.queryForList("selectByParamsForOrdSettlement", params);
	}
	

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}
	/**
	 * 为OrderSettlement页面查询总数
	 * @param map
	 * @return integer
	 */
	public Integer getToalCountForOrdSettlement(Map<String, Object> params) {
		return super.get("getToalCountForOrdSettlement", params);
	}

	public List<OrdOrderItem> selectByParamsForSettlement(Map<String, Object> params) {
		return super.queryForList("selectByParamsForSettlement", params);
	}
	

	public Integer getTotalCountForSettlement(Map<String, Object> params) {
		return super.get("getTotalCountForSettlement", params);
	}
	
	public int updateByPrimaryKeySelective(OrdOrderItem ordOrderItem) {
		ordOrderItem.setOrderUpdateTime(new Date());
		return super.update("updateByPrimaryKeySelective", ordOrderItem);
	}

	public int updateByPrimaryKey(OrdOrderItem ordOrderItem) {
		ordOrderItem.setOrderUpdateTime(new Date());
		return super.update("updateByPrimaryKey", ordOrderItem);
	}

	public int updateContentById(OrdOrderItem ordOrderItem) {
		ordOrderItem.setOrderUpdateTime(new Date());
		return super.update("updateContentById", ordOrderItem);
	}
	public int updateByOrderItemIdSelective(OrdOrderItem ordOrderItem) {
		ordOrderItem.setOrderUpdateTime(new Date());
		return super.update("updateByOrderItemIdSelective", ordOrderItem);
	}
	
	public int updateByOrderIdSelective(OrdOrderItem ordOrderItem) {
		ordOrderItem.setOrderUpdateTime(new Date());
		return super.update("updateByOrderIdSelective", ordOrderItem);
	}
	
	/**
	 * 根据订单ID查询订单子项集合
	 * @param orderId
	 * @return
	 */
	public List<OrdOrderItem> selectByOrderId(Long orderId) {
		return super.queryForList("selectByOrderId", orderId);
	}
	
	/**
	 * 查询同商品的订单子项集合
	 * @param suppGoodsIds
	 * @return
	 */
	public List<OrdOrderItem> selectOrderIdByOrderId(Long orderId) {
		return super.queryForList("selectOrderIdByOrderId", orderId);
	}
	
	public List<OrdOrderItem> selectCategoryIdByOrderId(Long orderId) {
		return super.queryForList("selectCategoryIdByOrderId", orderId);
	}
	
	/**
	 * 根据id集合查询订单子项集合
	 * @param ids
	 * @return
	 */
	public List<OrdOrderItem> selectOrderItemsByIds(List<Long> ids) {
		return super.queryForList("selectOrderItemsByIds", ids);
	}
	
	
	/**
	 * 查询已经履行的，根据id集合查询订单子项集合
	 */
	public List<OrdOrderItem> selectSubOrderItemsByIds(List<Long> ids){
		return super.queryForList("selectSubOrderItemsByIds",ids);
	}
	
	/**
	 * 根据orderId查询信息
	 * @param ids
	 * @return
	 */
	public List<OrdOrderItem> selectOrderItemsByorderIds(List<Long> orderIds) {
		return super.queryForList("selectOrderItemsByorderIds", orderIds);
	}
	
	/**
	 * 根据下单时间和产品统计orderItem数量
	 * @param params
	 * @return
	 */
	public Integer countOrderItemByCreateTimeAndProductId(Map<String, Object> params) {
		return super.get("countOrderItemByCreateTimeAndProductId", params);
	}
	
	/**
	 * 根据产品ID和门店ID查询指定时间内的有效订单的数量 
	 * @param params
	 * @return
	 */
	public Integer countOrderMainItemForO2oTicketByProductId(Map<String, Object> params) {
		if (params.get("productId")==null
			||params.get("beginTime")==null
			||params.get("endTime")==null){
			return 0;
		}
		return super.get("countOrderMainItemForO2oTicketByProductId", params);
	}
	
	/**
	 * 获取订单状态为NORMAL状态且非对接酒店的orderItem记录 
	 * @param params
	 * @return
	 */
	public List<OrdOrderItem> selectOrderItems() {
		return super.queryForList("selectOrderItems","");
	}

	/**
	 * 根据条件查询订单
	 * @param paramMap 查询条件map集合
	 * @return 订单集合
	 * @author Zhang.Wei
	 */
	public List<OrdOrderItem> listOrderItemByConditions(Map<String, Object> paramMap) {
		return super.queryForList("listOrderItemByConditions", paramMap);
	}
	
	public Long listOrderItemByConditionsCount(Map<String, Object> paramMap) {
		return super.get("listOrderItemByConditionsCount", paramMap);
	}


	public Long getTicketItemCountByUserIdAndCreateTime(String userId, Date createTime){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("userId", userId);
		paramsMap.put("createTime", createTime);
		return super.get("getTicketItemCountByUserIdAndCreateTime", paramsMap);
	}

	public List<OrdOrderItem> getTicketItemListByUserId(String userId){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("userId", userId);
		return super.queryForList("getTicketItemListByUserId", paramsMap);
	}
	public List<OrdOrderItem> getPageTicketItemListByUserId(Map<String, Object> paramsMap){
		return super.queryForList("getPageTicketItemListByUserId", paramsMap);
	}
	
	public Integer getTicketItemCountByUserId(String userId){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("userId", userId);
		return super.get("getTicketItemCountByUserId", paramsMap);
	}
	
	public int updateInvokeInterfacePfStatus(Map<String,Object> map){
		return super.update("updateInvokeInterfacePfStatus", map);
	}
	public Integer updateCourierStatus(Long orderItemId, String courierStatus){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("orderItemId", orderItemId);
		paramsMap.put("courierStatus", courierStatus);
		return super.update("updateCourierStatus", paramsMap);
	}
	
	public Integer getOrderOrderItemSuppGoodsTotalCount(Map<String, Object> params) {
		return super.get("getOrderOrderItemSuppGoodsTotalCount", params);
	}
	
	public List<OrdOrderItem> getOrderOrderItemSuppGoodsByParam(Map<String, Object> params) {
		return super.queryForList("getOrderOrderItemSuppGoodsByParam", params);
	}
	
	public List<OrdOrderItem> queryTicketOrderItem(Map<String,Object> params){
		return super.queryForList("queryTicketOrderItem",params);
	}
	
	public List<OrdOrderItem> queryOrderItemListByParams(Map<String,Object> params){
		return super.queryForList("queryOrderItemListByParams",params);
	}

	public List<OrdOrderItem> queryOrderItemListByCreateTimeBetween(Map<String, Object> paramMap){
		return super.queryForListForReport("queryOrderItemListByCreateTimeBetween", paramMap);
//		return super.queryForList("queryOrderItemListByCreateTimeBetween", paramMap);
	}
	
	public int updateNotInTimeFlag(Map<String,Object> params) {
		return super.update("updateNotInTimeFlag", params);
	}
	 /**
     * 根据入园日期查询订单信息（提供给驴途）
     * @param paramMap
     * @return
     */
    public List<VstOrdOrderItemDateVo> queryTicketOrderItemByVisitDate(Map<String, Object> paramMap) {
        return super.queryForList("queryTicketOrderItemByVisitDate", paramMap);
    }
    
	public Integer getTotalCountForOrdOrderItem(Map<String, Object> params) {
		return super.get("getTotalCountForOrdOrderItem", params);
	}
    
    public Long getOrderIdByOrderItemId(Long orderItemId) {
		return super.get("getOrderIdByOrderItemId", orderItemId);
	}
	public List<Long> getOrderItemIdListByOrderId(Long orderId) {
		return super.queryForList("getOrderItemIdListByOrderId", orderId);
	}
	/**
	 * 更新子订单退款信息
	 * @param orderItemList
	 */
	public int updateOrderItemRefundQutityAndPrice(OrdOrderItem orderItem) {
		return super.update("updateOrderItemRefundQutityAndPrice", orderItem);
	}
	
	  public List<OrdOrderItem> queryTicketOrderItemByOrderId(Map<String, Object> paramMap) {
	        return super.queryForList("queryTicketOrderItemByOrderId", paramMap);
	    }

	public List<OrdETicketOrderItem> selectUserTicketByParams(
			Map<String, Object> params) {
		return super.queryForList("selectUserTicketByParams", params);
	}

	public List<Long> getOrderIdsForSendMail(List<Long> orderIds) {
		return super.queryForList("getOrderIdsForSendMail", orderIds);
	}
	
	public List<OrdOrderItem> getOrderItemsForSendMail(List<Long> orderIds) {
		return super.queryForList("getOrderItemsForSendMail", orderIds);
	}
	
	public List<OrdOrderItem> queryUserOrderItemList(
			Map<String, Object> params) {
		return super.queryForList("queryUserOrderItemList", params);
	}

	public List<OrdOrderItem> getOrderItemsForFlightTicket(List<Long> orderIds) {
		return super.queryForList("getOrderItemsForFlightTicket", orderIds);
	}
	
	public int updateOrderItemActSettlement(Map<String,Object> map){
		return super.update("updateOrderItemActSettlement", map);
	}
	
	public int updateSubTypeByOrderId(Map<String, Object> paramsMap) {
		return super.update("updateSubTypeByOrderId", paramsMap);
	}

	public Long findOrderItemNoCodeCount(Map<String, Object> paramMap) {
		return super.get("findOrderItemNoCodeCount", paramMap);
	}	

	/**
	 * 修改子订单价格确认状态(订单金额转义过程中用到)
	 * @param paramMap 包含这两个字段（Long orderId,String priceConfirmStatus）
	 * @return
	 */
	public int updatePriceConfirmStatusByOrderId(Map<String, Object> paramMap) {
		
		return super.update("updatePriceConfirmStatusByOrderId", paramMap);
	}

	public Long getOrderItemIdForPreLockSeat() {
		return super.get("getOrderItemIdForPreLockSeat");
	}
	
	public List<OverdueTicketSubOrder> getOverdueTicketSubOrderListBySupplierIds (Map<String, Object> paramMap) {
		return super.getList("getOverdueTicketSubOrderListBySupplierIds", paramMap);
	}

	public int updateOverdueTicketRefundProcessedFlagAndMemoInBatch(List<Long> subOrderIdList) {
		return super.update("updateOverdueTicketRefundProcessedFlagAndMemoInBatch", subOrderIdList);
	}
	
	public int updateOverdueTicketRefundProcessedFlag(Map<String, Object> param) {
		return super.update("updateOverdueTicketRefundProcessedFlag", param);
	}	
	
	public List<OrdOrderItemView> selectListByParams(Map<String, Object> params) {
		return super.queryForList("selectListByParams", params);
	}
	
	public List<OverdueTicketSubOrder> getOverdueTicketSubOrderListBySpecifiedIds(List<Long> subOrderIdList) {
		return super.getList("getOverdueTicketSubOrderListBySpecifiedIds", subOrderIdList);
	}
	
	public List<OverdueTicketSubOrder> getSubOrderByIdForOverdueRefundProcessing(List<Long> subOrderIdList) {
		return super.getList("getSubOrderByIdForOverdueRefundProcessing", subOrderIdList);
	}
	/**
	 * 根据OrderId修改所有orderItem的结算总价为0
	 * @param orderId
	 * @return
	 */
	public int updateTotalSettlementPriceByOrderId(Map<String, Object> paramMap){
		return super.update("updateTotalSettlementPriceByOrderId", paramMap);
	}
	
}