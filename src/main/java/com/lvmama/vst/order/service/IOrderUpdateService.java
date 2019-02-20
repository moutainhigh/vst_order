/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderInsurance;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderSharedStock;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.OrdOrderPersonVO;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;

/**
 * 订单操作使用
 * @author lancey
 *
 */
public interface IOrderUpdateService {

	
	/**
	 * 查找person
	 * @param order
	 */
	OrdPerson findOrderPersonById(final Long id);
	/**
	 * 更新人person
	 * @param order
	 */
	void updateOrderPerson(final OrdPerson ordPerson);
	
	
	
	/**
	 * 查询一个订单，该操作只操作订单表
	 * @param orderId
	 * @return
	 */
	OrdOrder queryOrdOrderByOrderId(final Long orderId);
	
	int  updateOrderAndChangeOrderItemPayment(final OrdOrder order);

    public Pair<Date, List<Long>> updateOrderItemVisitTime(Long orderId, Long orderItemId, Date changeVisitDate);

    int updateByPrimaryKeySelective(final OrdOrder order);
	
	int updateOrderItemByIdSelective(OrdOrderItem ordOrderItem);

	int updateContentById(final OrdOrder order);

	int updateContentById(OrdOrderItem ordOrderItem);
	
	void updateOrderPerformStatus(Long orderId, String performStatus);
	/**
	 * 进行取消订单操作，为了支持事务操作，此接口可能抛出运行时异常，支持事务回滚，调用者可以进行捕获。
	 * 
	 * @param orderId
	 * @param cancelCode
	 * @param reason
	 * @param operatorId
	 * @return
	 */
	ResultHandle updateCancelOrder(final Long orderId,String cancelCode,String reason,String operatorId,String memo);

	void updateViewStatus(final Map<String,Object> map);
	
//	int updateCancelStatus(final Map<String,Object> map);
	
	int updateIsTestOrder(Map<String, Object> map);
	
	/**
	 * 修改调用对接平台的状态
	 * @param orderId
	 * @param invokeInterfacePfStatus
	 * @return
	 */
	int updateInvokeInterfacePfStatus(Long orderId, String invokeInterfacePfStatus);
	
	/**
	 * 修改调用对接平台的状态
	 * @param orderItemIds
	 * @param invokeInterfacePfStatus
	 * @return
	 */
	int updateItemInvokeInterfacePfStatus(String orderItemIds, String invokeInterfacePfStatus);
	
	/**
	 * 查询需要催支付的订单列表
	 * 查询订单未支付并且已经做了资源审核通过的订单列表
	 * 时间定在30分钟到期的订单
	 * @return
	 */
	@ReadOnlyDataSource
	List<OrdOrder> queryRequestPaymentOrder();
	
	/**
	 * 查询需要催尾款支付的订单列表
	 * 查询订单未完成支付，券订单，定金支付并完成
	 * 尾款等待支付时间定在24小时到期的订单
	 * @return
	 */
	List<OrdOrder> queryLastPaymentOrder();
	
	/**
	 * 查询需要催兑换的订单列表
	 * 查询订单完成支付，券订单
	 * 催兑换时间到的订单
	 * @return
	 */
	List<OrdOrder> queryStampExchangeRemainOrder();
	
	/**
	 * 查询需要小驴分期催支付的订单列表
	 * 查询订单未支付并且已经做了资源审核通过的订单列表
	 * 时间定在下单以后90分之内的订单
	 * @return
	 */
	List<OrdOrder> queryRequestTimePaymentOrder();
	
	/**
	 * 根据订单ID查询订单子项集合
	 * @param orderId
	 * @return
	 */
	List<OrdOrderItem> queryOrderItemByOrderId(final Long orderId);
	
	List<OrdOrderItem> queryOrderItemByParams(Map<String, Object> params);
	/**
	 * 为OrderSettlement页面查询结果
	 * @param map
	 * @return list
	 */
	List<OrdOrderItem> queryOrderItemByParamsForOrdSettlement(Map<String, Object> params);

	/**
	 * 获取结算的订单项
	 * @param params
	 * @return
	 */
	List<OrdOrderItem> querySettlementOrderItemByParams(Map<String, Object> params);
	
	public List<OrdOrderItem> selectCategoryIdByOrderId(Long orderId);
	
	public Integer getOrderItemTotalCount(Map<String, Object> params);

	/**
	 * 为OrderSettlement页面查询结果总数
	 * @param map
	 * @return integer
	 */
	public Integer getOrderItemTotalCountForOrdSettlement(Map<String, Object> params);

	
	/**
	 * 获取结算的订单项数
	 * @param params
	 * @return
	 */
	public Integer getSettlementOrderItemTotalCount(Map<String, Object> params);
	
	/**
	 * 查询同商品的订单子项集合,但不包括条件本身
	 * @param orderId
	 * @return
	 */
	List<OrdOrderItem> queryOrderIdByOrderId(Long orderId);
	
	/**
	 * 查询一个子项
	 * @param orderItemId
	 * @return
	 */
	OrdOrderItem getOrderItem(final Long orderItemId);
	
	/**
	 * 获取订单状态为NORMAL状态且支付等待时间小于当前时间的订单ID列表
	 * 
	 * @param currentDate
	 * @return
	 */
	public List<Long> getPaymentTimeoutOrderIds(Date currentDate);
	
	public List<Long> getPendingCancelTestOrderIdList(Map<String, Object> paramsMap);
	
	/**
	 * 履行前一天所有有效的订单
	 * @return
	 */
	List<OrdOrder> queryPerformPreviousDayOrderIdList(Date orderCreateTime);
	
	/**
	 * 更新订单退款金额
	 * @param orderId
	 * @param refundmentId
	 * @param amount
	 */
	boolean updateRefundedAmount(final Long orderId, final Long refundmentId,long amount);
	
	/**
	 * 根据订单Id列表查询订单(订单包含子项)
	 * 
	 * @param orderIdList
	 * @return
	 */
	public List<OrdOrder> queryOrdorderByOrderIdList(List<Long> orderIdList);
	
	/**
	 * 更新信用卡信息
	 * @param orderId
	 */
	void updateGuaranteeCC(final Long orderId);
	
	/**
	 * 清除订单流程数据
	 * @param orderId
	 */
	void updateClearOrderProcess(final Long orderId);
	
	/**
	 * 更新信用卡信息
	 * @param orderId
	 */
	List<Long> queryGuaranteeOrderIds();
	
	/**
	 * 对订单金额转移到指定的订单，更改订单的支付状态
	 * @param paymentList
	 * @return 返回被更改支付状态后的订单,当前订单对象只有OrdOrder相关的内容
	 */
	OrdOrder updateTransferOrder(List<PayPayment> paymentList);
	
	void saveTicketPerform(final OrdOrderItem orderItem);
	
	/**
	 * 设置订单支付等待时间
	 * 
	 * @param ordOrder
	 * @return
	 */
	public boolean setOrderWatiPaymentTime(OrdOrder ordOrder, Date baseDate, boolean checkFlag);
	
	/**
	 * 订单优惠劵使用后 依据订单优惠策略算出优惠金额，操作订单相关操作
	 * @param orderVst
	 * @param couponCode
	 */
	public void updateOrderUsedFavor(OrdOrder orderVst,final String couponCode) ;
	
	public String updateOrderForBuyInfo(OrdOrder orderVst,BuyInfo buyInfo);
	
	public String updateOrderForBuyInfo(OrdOrder orderVst,DestBuBuyInfo buyInfo);
	
	/**
	 * 根据用户ID查询第一笔订单
	 * @param userId
	 * @return
	 */
	public Long queryUserFirstOrder(Long userId);
	
	ResultHandleT<String> updateOrderPerson(final OrdOrder order,final OrdOrderPersonVO vo);
	
	/**
	 * 获取订单状态为NORMAL状态且非对接酒店的orderItem记录
	 * @return
	 */
	List<OrdOrderItem> queryOrderItems();
	
	/**
	 * 更新非对接酒店的履行状态
	 * @param updateItem
	 * @return
	 */
	int updateOrderItemPerformStatus(OrdOrderItem updateItem);

	
	/**
	 * 修改主订单.
	 * 
	 * @param ordOrder 主订单实例
	 * @return int value.
	 */
	int updateOrdOrder(OrdOrder ordOrder);
	
	/**
	 * 将订单置为无效
	 * @param orderId
	 * @return
	 */
	int invalidOrder(Long orderId);

    void markRemindSmsNoSend(List<Long> orderIdList);

    /**
	 * 更新游玩前一天的提醒短信的发送状态为已发送
	 * @param orderIdList
	 */
	void markRemindSmsSent(List<Long> orderIdList);
	
	/**
	 * 增加实付金额
	 * @param orderId
	 * @param actualAmount
	 * @return
	 */
	int addOrderActualAmount(Long orderId, Long actualAmount);
	
	

    /**
     * 增加支付立减金额
     * @param orderId
     * @param payPromotionAmount
     * @return
     */
	int addPayPromotionAmount(Long orderId, Long payPromotionAmount);
	
	
	/**
	 * 增加实付金额(改为update* 有事务控制)
	 * @param orderId
	 * @param actualAmount
	 * @return
	 */
	int updateAddOrderActualAmount(Long orderId, Long actualAmount);
	
	/**
	 * 设置取消订单的job对某订单的执行次数.
	 * @param orderIdList
	 */
	void markCancelTimes(List<Long> orderIdList);
	
	public Integer getOrderOrderItemSuppGoodsTotalCount(Map<String, Object> params);

	List<OrdOrderItem> getOrderOrderItemSuppGoodsByParam(
			Map<String, Object> params);
	
	public int updateNotInTimeFlag(Map<String,Object> params);
	public int updatePreRefundStatus(Map<String, Object> params);
	
	/**
	 * 更新订单支付信息
	 */
	int updateOrderAndItemPaymentInfo(Long orderId, Long bonusAmount, String paymentStatus, Date paymentTime,
			String paymentType);


    /**
     * 根据订单id查询保险订单信息
     * @param orderIdList
     * @return
     */
    public List<OrdOrderInsurance> getInsuranceOrderByOrderIdList(List<Long> orderIdList);
    /**
	 * 更新子订单退款信息
	 * @param orderItemList
	 */
	public void updateOrderItemRefundQutityAndPrice(List<OrdOrderItem> orderItemList);
	/**
	 * 检查是否需要主订单信息审核和子订单资源信息审核等活动并行
	 * @param ordOrder
	 * @return
	 */
    boolean checkWorkflowOrderInfoParallelProcess(OrdOrder ordOrder);
    
    public List<OrdOrderSharedStock> getOrderShareStockByParams(Map<String, Object> param);
    
    int updateOrdOrderStock(OrdOrderStock ordOrderStock);

    /**
     * 更新订单支付成功信息
     * @param order
     * @param payment
     * @return
     */
    OrdOrder updatePaymentSuccessInfo(PayPayment payment);
    
    int updateOrderItemActSettlement(Map<String, Object> params);

    ResultHandleT<String> updateOrdTravellers(OrdOrder order, OrdOrderPersonVO ordOrderPersonVO);

    ResultHandleT<String> updateOrdInsurers(OrdOrder order, OrdOrderPersonVO ordOrderPersonVO);
}
