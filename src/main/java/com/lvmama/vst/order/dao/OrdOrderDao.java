package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.control.vo.ResPrecontrolOrderVo;
import com.lvmama.vst.back.ebooking.vo.ProdOrdRoute;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderInsurance;
import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.ebooking.vo.DepartureNoticeVo;
import com.lvmama.vst.order.dao.datamodel.RawTicketOrderInfo;
import com.lvmama.vst.ticket.vo.TicketOrderInfo;

@Repository
public class OrdOrderDao extends MyBatisDao {
	
	public OrdOrderDao() {
		super("ORD_ORDER");
	}

	public int deleteByPrimaryKey(Long orderId) {
		return super.delete("deleteByPrimaryKey", orderId);
	}

	public int insert(OrdOrder order) {
		order.setOrderUpdateTime(new Date());
		return super.insert("insert", order);
	}

	public int insertSelective(OrdOrder order) {
		order.setOrderUpdateTime(new Date());
		return super.insert("insertSelective", order);

	}
	
	public int insertSelectiveForPreLockSeat(OrdOrder order) {
		return super.insert("insertSelectiveForPreLockSeat", order);
	}

	public OrdOrder selectByPrimaryKey(Long orderId) {
		return super.get("selectByPrimaryKey", orderId);
	}
	
	//查询邮寄门票订单
	public List<OrdOrder> selectMailOrderInfoByParams(Map<String, Object> param){
		return super.queryForList("selectMailOrderInfoByParams",param);
	}
	
	public int updateByPrimaryKeySelective(OrdOrder order) {
		//同步更新订单查询信息
//		syncOrderQueryInfo(order);
//		order.setOrderUpdateTime(new Date());
		return super.update("updateByPrimaryKeySelective", order);
	}

	public int updateContentById(OrdOrder order) {
		return super.update("updateContentById", order);
	}

	public int updateIsTestOrder(Map<String,Object> map){
		return super.update("updateIsTestOrder", map);
	}
	
	public int updateViewStatus(Map<String,Object> map){
		return super.update("updateViewStatus", map);
	}
	
	public int updateTagAndWaitPaymentTime(OrdOrder order){
		return super.update("updateTagAndWaitPaymentTime", order);
	}
	
	public int updateValidStatus(Map<String,Object> map){
		return super.update("updateValidStatus", map);
	}

	public int updateInvokeInterfacePfStatus(Map<String,Object> map){
		return super.update("updateInvokeInterfacePfStatus", map);
	}


	public int updateByPrimaryKey(OrdOrder order) {
		//同步更新订单查询信息
//		syncOrderQueryInfo(order);
		//order.setOrderUpdateTime(new Date());
		return super.update("updateByPrimaryKey", order);
	}

	public Long saveOrder(OrdOrder order) {
		insertSelective(order);
		return order.getOrderId();
	}
	
	public Long saveOrderForPreLockSeat(OrdOrder order) {
		insertSelectiveForPreLockSeat(order);
		return order.getOrderId();
	}

	public int updateOrder(OrdOrder order) {
		return updateByPrimaryKeySelective(order);
	}
	
	public Long queryUserFirstOrder(Long userId){
		return super.get("queryUserFirstOrder", userId);
	}
	
	/**
	 * 获取订单状态为NORMAL状态且支付等待时间小于当前时间的订单ID
	 * 
	 * @param currDate
	 * @return
	 */
	public List<Long> getPaymentTimeoutOrderIdList(Date currDate) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("orderStatus", OrderEnum.ORDER_STATUS.NORMAL.name());
		paramsMap.put("currentTime", currDate);
		return super.queryForList("getPaymentTimeoutOrderIdList", paramsMap);
	}
	
	public List<Long> getPendingCancelTestOrderIdList(Map<String, Object> paramsMap) {
		return super.queryForList("getPendingCancelTestOrderIdList", paramsMap);
	}
	
	
	public List<OrdOrder> queryRequestPaymentOrder(final long minute){
		Map<String,Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("waitMinute", minute);
		return super.queryForList("queryRequestPaymentOrder",paramsMap);
	}
	
	public List<OrdOrder> queryLastPaymentOrder(){
		Map<String,Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("waitHour", 24);
		return super.queryForList("queryLastPaymentOrder", paramsMap);
	}
	
	public List<OrdOrder> queryStampExchangeRemainOrder(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("waitDay", 2); // 兼容老数据
		return super.queryForList("queryExchangeStampOrder", params);
	}
	
	public List<OrdOrder> queryRequestTimePaymentOrder(final long minute){
		Map<String,Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("waitMinute", minute);
		return super.queryForList("queryRequestTimePaymentOrder",paramsMap);
	}
	
	public List<OrdOrder> queryPerformPreviousDayOrderIdList(Date orderCreateTime){
		Map<String, Object> params = new HashMap<String, Object>();
		if(orderCreateTime != null) {
			params.put("orderCreateTime", orderCreateTime);
		}
		return super.getList("queryPerformPreviousDayOrderIdList", params);
	}
	
	public List<OrdOrder> selectByPrimaryKeyList(List<Long> orderIdList) {
		return super.queryForList("selectByPrimaryKeyList", orderIdList);
	}

    public List<OrdOrderInsurance> getInsuranceOrderByOrderIdList(List<Long> orderIdList) {
        return super.queryForList("getInsuranceOrderByOrderIdList", orderIdList);
    }
	
	public List<OrdOrder> sortSelectByPrimaryKeyList(List<Long> orderIdList) {
		return super.queryForList("sortSelectByPrimaryKeyList", orderIdList);
	}
	
	public int markRemindSmsSent(List<Long> orderIdList) {
		return super.update("markRemindSmsSent", orderIdList);
	}

    public int markRemindSmsNoSend(List<Long> orderIdList) {
        return super.update("markRemindSmsNoSend", orderIdList);
    }

	public int updateRefundedAmount(final Long orderId,long amount){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId",orderId);
		map.put("amount", amount);
		return super.update("updateRefundedAmount", map);
	}
	
	public void updateClearOrderProcess(final Map<String,Object> map){
		super.update("updateClearOrderProcessKey", map);
	}
	
	public Long getDepartureNoticeCount(Map<String, Object> params) {
		return super.get("getDepartureNoticeCount", params);
	}

	public Long queryUserOrderCountByParams(Map<String, Object> params){
		return super.get("queryUserOrderCountByParams", params);
	}
	
	public List<DepartureNoticeVo> selectDepartureNoticeList(Map<String, Object> paramsMap) {
		return super.queryForList("selectDepartureNoticeList", paramsMap);
	}
	
	public List<DepartureNoticeVo> selectAllDepartureNoticeList(Map<String, Object> paramsMap) {
		return super.queryForList("selectAllDepartureNoticeList", paramsMap);
	}
	
	public List<OrdOrder> getordOrderList(Map<String, Object> params){
		return super.queryForList("getordOrderList", params);
	}
	
	public List<Long> findNeedGenWorkflowOrders() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		return super.queryForList("findNeedGenWorkflowOrders", paramsMap);
	}
	
	public List<Long> findNeedCreateSupplierOrders() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		return super.queryForList("findNeedCreateSupplierOrders", paramsMap);
	}
	
	public List<Long> findNeedCancelSupplierOrders() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		return super.queryForList("findNeedCancelSupplierOrders", paramsMap);
	}
	
	public int addOrderActualAmount(final Map<String,Object> map){
		return super.update("addOrderActualAmount", map);
	}
	
	public int markCancelTimes(List<Long> orderIdList) {
		return super.update("markCancelTimes", orderIdList);
	}
	
	 /**
     * 同步更新订单查询信息
     */
	
    @SuppressWarnings("unused")
	private void syncOrderQueryInfo(OrdOrder order) {
		OrdOrderQueryInfo orderQueryInfo = new OrdOrderQueryInfo();
		orderQueryInfo.setOrderId(order.getOrderId());
		orderQueryInfo.setOrderStatus(order.getOrderStatus());
		orderQueryInfo.setResourceStatus(order.getResourceStatus());
		orderQueryInfo.setInfoStatus(order.getInfoStatus());
		orderQueryInfo.setCertConfirmStatus(order.getCertConfirmStatus());
		orderQueryInfo.setPaymentStatus(order.getPaymentStatus());
		orderQueryInfo.setPaymentTime(order.getPaymentTime());
//		if (order.getOrderStatus() != null || order.getResourceStatus() != null
//				|| order.getInfoStatus() != null
//				|| order.getCertConfirmStatus() != null
//				|| order.getPaymentStatus() != null
//				|| order.getPaymentTime() != null) {
//			orderQueryInfoDao.updateByOrderId(orderQueryInfo);
//		}
    }
    
    /**
	 * 更新产品权限
	 * @param ordOrder
	 * @return
	 */
	public int updateManagerIdPerm(OrdOrder ordOrder){
		return super.update("updateManagerIdPerm", ordOrder);
	}

    /**
     * 得到买断订单列表
     * @param paramsMap
     * @return
     */
    public List<ResPrecontrolOrderVo> findPercontrolGoodsOrderList(Map paramsMap){
        return super.getList("findPercontrolGoodsOrderList",paramsMap);
    }
    
    /**
     * 得到买断订单列表
     * @param paramsMap
     * @return
     */
    public List<ResPrecontrolOrderVo> findPercontrolHotelGoodsOrderList(Map paramsMap){
        return super.getList("findPercontrolHotelGoodsOrderList",paramsMap);
    }

    @SuppressWarnings("rawtypes")
	public Long countPercontrolGoodsOrderList(Map paramsMap){
        return super.get("countPercontrolGoodsOrderList", paramsMap);
    }
    
    /**
     * 根据资源信息查询订单状态为正常的订单
     * @param paramsMap
     * @return
     * @return List<OrdOrder>
     * @date 2016-2-16
     * @author yangruochen
     *
     */
    public List<OrdOrder> queryResourceStatusOrderList(Map params){
		return super.getList("queryResourceStatusOrderList", params);
    }

	public List<OrdOrder> queryInfoStatusOrderList(Map<String, Object> params) {
		return super.getList("queryInfoStatusOrderList", params);
	}
    
	public List<Long> findNeedTiggerPayProcOrders() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		return super.queryForList("findNeedTiggerPayProcOrders", paramsMap);
	}
	
	public int updatePayProcTriggeredByOrderID(Map<String, Object> paramsMap) {
		return super.update("updatePayProcTriggeredByOrderID", paramsMap);
	}

	public List<OrdOrder> findDestBuOrderUpdateViewStatus(Map<String, Object> params){
		return super.getList("findDestBuOrderUpdateViewStatus", params);	
	}
	
	public List<OrdOrder> findDestBuWaitViewStatus(Map<String, Object> params){
		return super.getList("findDestBuWaitViewStatus", params);
	}
	
	
	 /**
     * 查询手机设备号根据订单id
     * @param Long 
     * @return
     * @return List<String>
     *
     */
	public String findMobileId(Long orderId){
		return super.get("findMobileId", orderId);
	}

	/**
	 *锁定订单游玩人
	 * */
	public int lockOrderTraveller(Long orderId){
		return super.update("lockOrderTraveller", orderId);
	}
	
	public int updatePreRefundStatus(Map<String,Object> params) {
		return super.update("updatePreRefundStatus", params);
	}

	public int updateOrderPaymentInfo(Long orderId, Long bonusAmount, String paymentStatus, Date paymentTime,
			String paymentType) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId",orderId);
		map.put("bonusAmount", bonusAmount);
		map.put("paymentStatus", paymentStatus);
		map.put("paymentTime", paymentTime);
		map.put("paymentType", paymentType);
		map.put("orderUpdateTime", new Date());
		return super.update("updateOrderPaymentInfo", map);
	}
	
	/**
	 * 批量修改订单展示状态
	 * @param orderList
	 * @return
	 */
	public int batchUpdateViewStatusByList(List<OrdOrder> orderList){
		return super.update("batchUpdateViewStatusByList", orderList);	
	}
	/**
	 *增加支付立减金额
	 * @param params
	 * @return
	 */
	public int addPayPromotionAmount(Map<String, Object> params) {
		return super.update("addPayPromotionAmount", params);
	}
	

	 public Long countPercontrolHotelGoodsOrderList(Map paramsMap){
	        return super.get("countPercontrolHotelGoodsOrderList", paramsMap);
	  }

	public List<Long> queryOutBookTimeOrder(Map<String, Object> params){
		return super.queryForList("queryOutBookTimeOrder", params);
	}
	
	/**
	 * 根据游玩时间和品类查询订单
	 * @param paramMap 参数
	 * @return
	 */
	public List<OrdOrder> getOrderInfoByVisitTimeAndCat(Map<String, Object> paramMap) {
		return super.queryForListForReport("getOrderInfoByVisitDateAndCat", paramMap);
	}

	public List<Long> queryStampOrderIds(int startBeforeDays) {
	    return super.queryForList("queryStampOrderIds", startBeforeDays);
    }

	public Long countPercontrolGoodsHisOrder(Map<String, Object> paramsMap) {
		return super.get("countPercontrolGoodsHisOrder",paramsMap);
	}
	
	public List<ResPrecontrolOrderVo> findPercontrolGoodsHisOrderList(Map<String, Object> paramsMap) {
		return super.queryForList("findPercontrolGoodsHisOrderList",paramsMap);
	}

	public List<OrdOrder> getOrderIdsForSendMail(Map<String, Object> paramsMap) {
		return super.queryForList("getOrderIdsForSendMail",paramsMap);
	}

	public List<OrdOrder> findHotelOrderListByParams(Map<String, Object> params){
		return super.queryForList("findHotelOrderListByParams",params);
	}

    public Long countRouteProductList(Map<String, Object> params) {
		return super.get("countRouteProductList", params);
    }

	public List<ProdOrdRoute> findRouteProductList(Map<String, Object> params) {
		return super.queryForList("findRouteProductList", params);
	}

	public List<ProdOrdRoute> findRouteProductListForReport(Map<String, Object> params) {
		return super.queryForListForReport("findRouteProductList", params);
	}
	
	public List<Long> getOrderIdsForFlightTicket(Map<String, Object> paramsMap) {
		return super.queryForList("getOrderIdsForFlightTicket",paramsMap);
	}
	
	public Integer getTicketOrderTotalQuantityByMobile(String mobile) {
		return super.get("getTicketOrderTotalQuantityByMobile", mobile);
	}
	
	public List<RawTicketOrderInfo> getPagedRawTicketOrderInfoByMobile(Map<String, Object> paramsMap) {
		return super.queryForList("getPagedRawTicketOrderInfoByMobile", paramsMap);
	}

	public List<TicketOrderInfo> getTicketOrderInfoById(List<Long> orderIdL) {
		return super.queryForList("getTicketOrderInfoById", orderIdL);
	}
	
	public List<TicketOrderInfo> getSingleTicketOrder(String orderId) {
		return super.queryForList("getSingleTicketOrder", orderId);
	}

	public List<Long> getStampByUserId(Map<String, Object> paramsMap) {
		return super.queryForList("getStampByUserId", paramsMap);
	}
	
	public int updateSubTypeByOrderId(Map<String, Object> paramsMap) {
		return super.update("updateSubTypeByOrderId", paramsMap);
	}
	
	public Long countStampByUserId(String userId) {
		return super.get("countStampByUserId", userId);
	}
	
	public int updateWaitPaymentTimeByOrderId(Long orderId) {
		return super.update("updateWaitPaymentTimeByOrderId", orderId);
	}

	public List<ResPrecontrolOrderVo> selectByVisitTimeAndGoodsId(Map<String, Object> paramMap) {
		return super.queryForList("selectByVisitTimeAndGoodsId", paramMap);
	}

	public int insertPushOrderBatch(Map<String, Object> params) {
		return super.insert("insertPushOrderBatch", params);
	}

	public int insertPushOrderBatchHotel(Map<String, Object> params) {
		return super.insert("insertPushOrderBatchHotel", params);
	}


	public List<Long> queryOrderIdsByParams(Map<String, Object> params) {
		return super.queryListForDelActivitiData("queryOrderIdsByParams", params);
	}


	public Long getOrderIdForPreLockSeat() {
		return super.get("getOrderIdForPreLockSeat");
	}

	
	public List<OrdOrder> getNoUploadNoticeRegiment(Map<String, Object> params){
		return super.queryForList("getNoUploadNoticeRegiment", params);
	}

	public List<OrdOrder> selectHotelOrderList() {
		return super.getList("selectHotelOrderList");
	}

}