/**
 *
 */
package com.lvmama.vst.order.service.impl;

import com.google.common.collect.Lists;
import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.comm.vst.vo.CardInfo;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.service.api.ticket.IApiOrderRescheduleDateService;
import com.lvmama.order.vo.ticket.OrderItemRescheduleDateVo;
import com.lvmama.ship.api.ship.order.client.ShipOrderTimePriceApiClientService;
import com.lvmama.visa.api.base.VisaResultHandleT;
import com.lvmama.visa.api.service.VisaApprovalClientService;
import com.lvmama.visa.api.vo.approval.VisaApprovalVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.passport.service.PassportService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.*;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice.STOCKTYPE;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.goods.vo.SuppGoodsRescheduleVO;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombProdAdditionService;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_TYPE;
import com.lvmama.vst.back.passport.po.PassProvider;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.mybatis.annotation.ForceRead;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.OrdOrderPersonVO;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.order.dao.*;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.IOrderDistinguishByBuBussiness;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.PaymentWaitRule;
import com.lvmama.vst.pet.adapter.BonusPayServiceAdapter;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.goods.PetProdGoodsAdapter;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.ticket.utils.DisneyUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * 订单简单操作业务
 *
 * @author lancey
 *
 * @author wenzhengtao
 *
 */
@Service("ordOrderUpdateService")
public class OrdOrderUpdateServiceImpl extends AbstractBookService implements IOrderUpdateService{
	private static final Log LOG = LogFactory.getLog(OrdOrderUpdateServiceImpl.class);

	public static final int CANCEL_REASON_MAX_LEN = 200;


	private static final String[] categorys = {
			"category_route_freedom_FOREIGNLINE" ,//自由行 出境/港澳台
			"category_route_local_FOREIGNLINE",//当地游 出境/港澳台
			"category_route_hotelcomb_FOREIGNLINE"//酒店套餐 出境/港澳台
	};
	/**
	 * 下单以后20分钟的订单
	 */
	private final long AFTER_CREATE_MINUTE=20L;

	@Autowired
	private OrdOrderDao orderDao;

	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	@Autowired
	private OrdOrderItemExtendDao ordOrderItemExtendDao;

	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;

	@Autowired
	private OrdOrderSharedStockDao ordOrderSharedStockDao;

	@Autowired
	private OrdOrderStockDao ordOrderStockDao;

	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	protected SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

	@Resource(name="goodsOraTimePriceStockService")
	protected IGoodsTimePriceStockService goodsTimePriceStockService;

	@Resource(name="goodsOraLineTimePriceStockService")
	protected IGoodsTimePriceStockService goodsLineTimePriceStockService;

	@Autowired
	private PetProdGoodsAdapter petProdGoodsAdapter;

	@Autowired
	private OrdPersonDao ordPersonDao;

	@Autowired
	private OrdGuaranteeCreditCardDao ordGuaranteeCreditCardDao;

	@Autowired
	private CategoryClientService categoryClientService;
	//推送服务接口
	@Autowired
	private ComPushClientService comPushClientService;

	@Autowired
	private OrdOrderAmountItemDao ordOrderAmountItemDao;

	@Autowired
	private FavorServiceAdapter favorServiceAdapter;

	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;

	@Autowired
	private OrdTicketPerformDao ticketPerformDao;

	@Autowired
	private OrdItemPersonRelationDao ordItemPersonRelationDao;

	@Autowired
	private ProdProductClientService productClientService;

	@Autowired
	private IOrderDistinguishByBuBussiness iOrderDistinguishByBuBussiness;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;


	@Resource(name="wait_payment_time_minute")
	private Map<String,Integer> paymentMinuteMap;


	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;//调用vstpet获取现金和奖金余额接口

	@Autowired
	private VisaApprovalClientService visaApprovalClientService;

	@Autowired
	private BonusPayServiceAdapter bonusPayService;

	@Autowired
	private IOrdItemAdditionStatusService ordItemAdditionStatusService;

	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;

	@Autowired
	private IOrderSendSmsService orderSendSmsService;

	@Autowired
	private IHotelTradeApiService hotelTradeApiService;

	@Autowired
	private INewHotelCombTimePriceService newHotelCombTimePriceClientRemote;
	@Autowired
	private INewHotelCombProdAdditionService newHotelCombProdAdditionClientRemote;
	@Autowired
	private OrderTicketNoTimePriceServiceImpl orderTicketNoTimePriceServiceImpl;
	@Autowired
	private  ShipOrderTimePriceApiClientService shipOrderTimePriceApiClientService;

	@Autowired
	private IOrdPaymentInfoService ordPaymentInfoService;

	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	 @Resource
	 IHotelCombOrderService hotelCombOrderService;

	@Autowired
	private PassportService passportService;

	@Autowired
	private IApiOrderRescheduleDateService apiOrderRescheduleDateService;

	/**
	 * 查找person
	 * @param id
	 */
	public OrdPerson findOrderPersonById(final Long id){


		return ordPersonDao.selectByPrimaryKey(id);
	}
	/**
	 * 更新人person
	 * @param ordPerson
	 */
	public void updateOrderPerson(OrdPerson ordPerson)
	{

		ordPersonDao.updateByPrimaryKeySelective(ordPerson);


	}
	@Override
	public OrdOrder queryOrdOrderByOrderId(Long orderId) {
		return orderDao.selectByPrimaryKey(orderId);
	}

	@Override
	public int updateOrderAndChangeOrderItemPayment(OrdOrder order) {
		int result = orderDao.updateByPrimaryKey(order);
		if(result==1&&order.hasFullPayment()){
			updateOrderItemPaymentStatus(order);
		}
		return result;
	}
	public void updateOrderItemPaymentStatus(OrdOrder order) {
		List<OrdOrderItem> list = ordOrderItemDao.selectByOrderId(order.getOrderId());
		for(OrdOrderItem orderItem:list){
			if(OrderEnum.ORDER_STATUS.NORMAL.name().equalsIgnoreCase(orderItem.getOrderStatus())){
				orderItem.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
				ordOrderItemDao.updateByPrimaryKey(orderItem);
			}
		}
	}

	@Override
	public Pair<Date, List<Long>> updateOrderItemVisitTime(Long orderId, Long orderItemId, Date changeVisitDate) {
		Pair<Date, List<Long>> resultHandle = new Pair<Date, List<Long>>();
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
		if (orderItem == null) {
			resultHandle.setMsg("没有子订单");
			return resultHandle;
		}


		List<SuppOrderResult> resultList = supplierOrderOtherService.ticketExchange(orderId, changeVisitDate, orderItemId);

		if (CollectionUtils.isEmpty(resultList)) {
			resultHandle.setMsg("改期失败");
		} else {
			String errMsg = null;
			List<Long> resultItemIdList = new ArrayList<Long>();
			for (SuppOrderResult suppOrderResult : resultList) {
				if (suppOrderResult.isSuccess()) {
					resultItemIdList.add(suppOrderResult.getOrderItemId());
				} else {
					errMsg = suppOrderResult.getErrMsg();
					break;
				}
			}
			if (errMsg != null) {
				resultHandle.setMsg(errMsg);
			} else {
				Date oldDate = orderItem.getVisitTime();
				this.saveOrderItemRescheduleDate(orderId, orderItemId, oldDate, changeVisitDate);
				for (Long resultItemId : resultItemIdList) {
					this.updateOrderItemVisitTime(resultItemId, changeVisitDate);
				}
				this.updateOrderVisitTime(orderId, changeVisitDate);
				this.markRemindSmsNoSend(orderId);
				this.addOrderItemAdditionStatus(resultItemIdList);

				if( DisneyUtils.isDisneyTicket(orderItem)){
					orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.DISNEY_REVISE_DATE);
				}else if(isShiyuanhuiTicket(orderItem)){
					orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.SHIYUANHUI_REVISE_DATE);
				}else if(isFangTeTicket(orderItem)){
					orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.FANGTE_REVISE_DATE);
				}else {
					orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.WANDA_REVISE_DATE);
				}
				resultHandle.setFirst(oldDate);
				resultHandle.setSecond(resultItemIdList);
			}
		}
		return resultHandle;
	}

	/**
	 * 判断是否为世园会门票
	 * @param orderItem
	 * @return
	 */
	private boolean isShiyuanhuiTicket(OrdOrderItem orderItem){
		String specialTicketType = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name());
		if(SuppGoods.SPECIAL_TICKET_TYPE.SHIYUANHUI_TICKET.name().equalsIgnoreCase(specialTicketType)){
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为方特门票
	 * @param orderItem
	 * @return
	 */
	private boolean isFangTeTicket(OrdOrderItem orderItem){
		if (StringUtils.equals(orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()),
				SuppGoods.NOTICETYPE.QRCODE.name())) {
			PassProvider passProvider = passportService.getPassProvide(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
			if(passProvider != null){
				LOG.info("isOrderItemChangable OrderItemId:" + orderItem.getOrderItemId() + ", ProviderName:" + passProvider.getProviderName());
				if ("方特新对接".equalsIgnoreCase(passProvider.getProviderName())) {
					return true;
				}
			}
		}
		return false;
	}

	private void markRemindSmsNoSend(Long orderId) {
		List<Long> orderIdList = new ArrayList<Long>();
		orderIdList.add(orderId);
		this.markRemindSmsNoSend(orderIdList);
	}

	private void addOrderItemAdditionStatus(List<Long> resultItemIdList) {
		for (Long itemId : resultItemIdList) {
			OrdItemAdditionStatus ordItemAdditionStatus = new OrdItemAdditionStatus();
			Map<String, Object> params =new HashMap<String, Object>();
			params.put("orderItemId", itemId);
			params.put("statusType", OrderEnum.ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
			params.put("status", OrderEnum.ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());

			List<OrdItemAdditionStatus> list = ordItemAdditionStatusService.findOrdItemAdditionStatusList(params);
			if(CollectionUtils.isNotEmpty(list)){
				ordItemAdditionStatus=list.get(0);
				ordItemAdditionStatus.setExchangeCount((ordItemAdditionStatus.getExchangeCount()==null? 2 :(ordItemAdditionStatus.getExchangeCount()+1)));
				ordItemAdditionStatusService.updateByPrimaryKeySelective(ordItemAdditionStatus);
			}else{
				ordItemAdditionStatus.setOrderItemId(itemId);
				ordItemAdditionStatus.setStatus(OrderEnum.ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());
				ordItemAdditionStatus.setStatusType(OrderEnum.ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
				ordItemAdditionStatus.setExchangeCount(1L);
				ordItemAdditionStatusService.addOrdItemAdditionStatus(ordItemAdditionStatus);
			}

		}
	}

	private void updateOrderItemVisitTime(Long orderItemId, Date changeVisitDate) {
		OrdOrderItem updateItem = new OrdOrderItem();
		updateItem.setOrderItemId(orderItemId);
		updateItem.setVisitTime(changeVisitDate);
		//更新子订单
		ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
	}

	private void saveOrderItemRescheduleDate(Long orderId, Long orderItemId, Date oldVisitDate, Date changeVisitDate) {
		//添加改期记录
		com.lvmama.order.api.base.vo.RequestBody<OrderItemRescheduleDateVo> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		OrderItemRescheduleDateVo vo = new OrderItemRescheduleDateVo();
		vo.setOrderId(orderId);
		vo.setOrderItemId(orderItemId);
		vo.setUserNo("");
		vo.setRescheduleType("");
		vo.setPreviousDate(oldVisitDate);
		vo.setRescheduleDate(changeVisitDate);
		requestBody.setT(vo);
		ResponseBody<Integer> responseBody = apiOrderRescheduleDateService.saveOrdRescheduleDate(requestBody);
		LOG.info(orderId+"___"+ orderItemId+ "gaiqi改期reps="+ responseBody);

	}

	private void updateOrderVisitTime(Long orderId, Date changeVisitDate) {
		List<OrdOrderItem> itemList = ordOrderItemDao.selectByOrderId(orderId);
		boolean updateOrderFlag = true;
		for (OrdOrderItem ordOrderItem : itemList) {
			if (! changeVisitDate.equals(ordOrderItem.getVisitTime())) {
				updateOrderFlag = false;
			}
		}
		if (updateOrderFlag) {
			//如果所有子订单都可以改期，则修改订单的游玩日期
			OrdOrder updateOrder = new OrdOrder();
			updateOrder.setVisitTime(changeVisitDate);
			updateOrder.setOrderId(orderId);
			orderDao.updateByPrimaryKeySelective(updateOrder);
		}
	}

	public  int updateByPrimaryKeySelective(final OrdOrder order){

		return orderDao.updateByPrimaryKeySelective(order);
	}
	public int updateOrderItemByIdSelective(OrdOrderItem ordOrderItem){

		return ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItem);

	}

	public int updateContentById(final OrdOrder order){

		return orderDao.updateContentById(order);
	}

	public int updateContentById(OrdOrderItem ordOrderItem){

		return ordOrderItemDao.updateContentById(ordOrderItem);
	}

	public void updateOrderPerformStatus(Long orderId, String performStatus){
		//更行订单
		OrdOrder updateOrder = new OrdOrder();
		updateOrder.setOrderId(orderId);
		updateOrder.setPerformStatus(performStatus);
		orderDao.updateByPrimaryKeySelective(updateOrder);
	};
	@Override
	public ResultHandle updateCancelOrder(Long orderId, String cancelCode,
										  String reason, String operatorId,String memo) {
		ResultHandle handle = new ResultHandle();
		updateOrderForCancel(orderId, cancelCode, reason, operatorId,memo);
		return handle;
	}

	/**
	 * 根据OrderID取消订单，更新订单状态，库存返回
	 *
	 * @param orderId
	 * @param cancelCode
	 * @param reason
	 * @param operatorId
	 */
	private void updateOrderForCancel(Long orderId, String cancelCode,
									  String reason, String operatorId,String memo) {
		Map<String,Map<Date, List<OrdOrderStock>>> hotelDataOrderStockMap = new HashMap<String, Map<Date,List<OrdOrderStock>>>();
		Map<Date, List<OrdOrderStock>> dateStockMap = null;
		List<OrdOrder> orderList = getOrderForCancelById(orderId);

		if (orderList != null && orderList.size() == 1 && orderList.get(0) != null) {
			OrdOrder order = orderList.get(0);
			/*if ("10.15.1.94".equals(order.getClientIpAddress())) {
				throw new RuntimeException("测试抛出异常，只针对10.15.1.94");
			}*/
			if (order != null && isCancelable(order)) {
				Date cancleTime = new Date();
				OrdOrder updateOrder = new OrdOrder();
				updateOrder.setOrderId(orderId);
				updateOrder.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
//				updateOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.CANCEL.name());
				updateOrder.setCancelCode(cancelCode);
				if (reason != null && reason.length() > OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN) {
					LOG.info("OrdOrderUpdateServiceImpl.updateOrderForCancel:reason.length > " + OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN + ",reason=" + reason);
					reason = reason.substring(0, OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN);
				}
				updateOrder.setReason(reason);
				updateOrder.setCancelTime(cancleTime);
				updateOrder.setOrderMemo(memo);
				//记录该订单被取消的次数
				//updateOrder.setCancelFailCount(order.getCancelFailCount() == null ? 1L : order.getCancelFailCount() + 1L);
				/**
				 * 取消人记录在Order日志中
				 updateOrder.setBackUserId(operatorId);
				 **/

				/**
				 * 在JMS接受消息中修改
				 updateOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.CANCEL.name());
				 **/
				//更行订单
				orderDao.updateByPrimaryKeySelective(updateOrder);

				if (order.getOrderItemList() != null) {
					  List<HotelOrdOrderVo> hotelOrdOrderVoList = Lists.newArrayList();
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(order.getCategoryId())){
						HotelOrdOrderVo hotelOrdOrderVo = new HotelOrdOrderVo();
						hotelOrdOrderVo.setOrderId(order.getOrderId());						
						for (OrdOrderItem orderItem : order.getOrderItemList()) {
							if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()
									.equals(orderItem.getCategoryId())) {
								hotelOrdOrderVo.setOrdOrderItemId(orderItem.getOrderItemId());
							}
						}
						hotelOrdOrderVoList.add(hotelOrdOrderVo);
						hotelCombOrderService.returnHotelCombOrderStock(new RequestBody<List<HotelOrdOrderVo>>()
								.setTFlowStyle(hotelOrdOrderVoList, NewOrderConstant.VST_ORDER_TOKEN));
			    	 }else{
						for (OrdOrderItem orderItem : order.getOrderItemList()) {
							if (orderItem != null)
							{
								//根据日期，统计订单本地库存对象
								dateStockMap = computeOrderStockByDate(orderItem.getOrderStockList());
								boolean isEnable = BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())
										&& DynamicRouterUtils.getInstance().isGrayGoodsId(orderItem.getSuppGoodsId());
								LOG.info("cancel order orderItemId ="+orderItem.getOrderItemId()+" isEnable="+isEnable);
								ResultHandleT<SuppGoods> suppGoodsT=suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId());
								SuppGoods suppGoods =suppGoodsT.getReturnContent();
								//酒店路由开关
								if(!isEnable)
								{
									updateStockByDate(orderId, orderItem, dateStockMap,orderItem.hasSupplierApi());
								}else{
									hotelDataOrderStockMap.put(orderItem.getOrderItemId()+"_"+orderItem.getSuppGoodsId(), dateStockMap);
								}
							}
						}
						//酒店库存取消
						if(hotelDataOrderStockMap.size()>0){
							//TODO 调用酒店tradeHotel接口
							hotelTradeApiService.updateRevertStock(hotelDataOrderStockMap);
						}
					}
				}
				LOG.info("在更新订单处，处理买断资源start");
				boolean ret = orderLocalService.updateResBackToPrecontrol(orderId);
				LOG.info("在更新订单处，处理买断资源end" + ret );
				if(ret == false){
					//throw new RuntimeException("更新预控库存失败");
				}

			}
			//向中间表推送订单状态
		} else {
			throw new RuntimeException("订单不存在。");
		}
	}

	/**
	 * 判断订单是否可以取消
	 *
	 * @param order
	 * @param currDate
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isCancelable(OrdOrder order, Date currDate) {
		boolean isCancel = false;

		if (order != null) {
			if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())) {
				throw new RuntimeException("此订单已经是取消状态。");
			}

			if (order.getLastCancelTime() == null || order.getLastCancelTime().after(currDate)) {
				throw new RuntimeException("此订单已经过了取消时间。");
			}

			isCancel = true;
		}

		return isCancel;
	}

	/**
	 *
	 * @param order
	 * @return
	 */
	private boolean isCancelable(OrdOrder order) {
		boolean isCancel = false;

		if (order != null) {
			if (OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())) {
				isCancel = true;
			} else {
				throw new RuntimeException("此订单状态是" + order.getOrderStatus() + "已经无法取消。");
			}


		}

		return isCancel;
	}

	/**
	 * 根据日期，统计订单本地库存对象
	 *
	 * @param orderStockList
	 * @return
	 */
	private Map<Date, List<OrdOrderStock>> computeOrderStockByDate(List<OrdOrderStock> orderStockList) {
		HashMap<Date, List<OrdOrderStock>> dateStockMap = null;

		if (orderStockList != null) {
			dateStockMap = new HashMap<Date, List<OrdOrderStock>>();
			Boolean isSharedStockFlg=false;
			for (OrdOrderStock orderStock : orderStockList) {
				if (orderStock != null) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderItemId", orderStock.getOrderItemId());
					params.put("visitTime", orderStock.getVisitTime());
					//只要ord_order_shared_stock表中同一个子订单,同一天的所有记录
					List<OrdOrderSharedStock> ordOrderSharedStockList = ordOrderSharedStockDao.selectByParams(params);
					isSharedStockFlg =false;
					if(ordOrderSharedStockList!=null&&ordOrderSharedStockList.size()>0){
						for(OrdOrderSharedStock sharedStock:ordOrderSharedStockList){
							//group_id不为空就是共享库存
							if(sharedStock.getGroupId()!=null && !"".equals(sharedStock.getGroupId())){
								isSharedStockFlg =true;
								break;
							}
						}
					}

					if (((orderStock.getShareTotalStockId() == null || orderStock.getShareTotalStockId() <= 0)
							&& OrderEnum.INVENTORY_STATUS.INVENTORY.name().equals(orderStock.getInventory()))
							|| OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name().equals(orderStock.getInventory())
							|| isSharedStockFlg)
					{
						List<OrdOrderStock> sameDateStockList = dateStockMap.get(orderStock.getVisitTime());
						if (sameDateStockList == null) {
							sameDateStockList = new ArrayList<OrdOrderStock>();
							dateStockMap.put(orderStock.getVisitTime(), sameDateStockList);
						}

						sameDateStockList.add(orderStock);
					}
				}
			}
		}

		return dateStockMap;
	}




	/**
	 * 根据日期统计的订单库存对象，进行范库存更新操作。
	 *
	 * @param orderId
	 * @param orderItem
	 */
	private void updateStockByDate(Long orderId, OrdOrderItem orderItem, Map<Date, List<OrdOrderStock>> dateOrderStockMap,boolean supplierApi) {
		if (dateOrderStockMap != null) {
			Long suppGoodsId = orderItem.getSuppGoodsId();
			Long categoryId = orderItem.getCategoryId();
			//取得商品上的组ID
			ResultHandleT<SuppGoods> suppgoods=suppGoodsClientService.findSuppGoodsById(suppGoodsId);

			Long suppGoupId =0L;

			if(suppgoods.getReturnContent() != null && suppgoods.getReturnContent().getGroupId() != null) {
				suppGoupId = suppgoods.getReturnContent().getGroupId();
			}

			if (categoryId != null) {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);

				Set<Entry<Date, List<OrdOrderStock>>>  entrySet = dateOrderStockMap.entrySet();
				Date visitDate = null;
				for (Entry<Date, List<OrdOrderStock>> entry : entrySet) {
					if (entry != null) {
						visitDate = entry.getKey();
						Long updateStockTotal = countOrderStockTotal(entry.getValue());
						if (updateStockTotal != null && updateStockTotal > 0) {
							if (!supplierApi) {
								dataMap.put("isUpdateSuperStock", new Boolean(true));
							} else {
								dataMap.put("isUpdateSuperStock", new Boolean(false));
							}
							dataMap.put("orderItemId", entry.getValue().get(0).getOrderItemId());
							dataMap.put("suppGoodsId", suppGoodsId);
							dataMap.put("shareTotalStockId", entry.getValue().get(0).getShareTotalStockId());
							dataMap.put("shareDayLimitId", entry.getValue().get(0).getShareDayLimitId());
							dataMap.put("orderStockId", entry.getValue().get(0).getOrderStockId());
							dataMap.put("visitDate", visitDate);
							dataMap.put("beginDate", visitDate);//和visitDate一样
							dataMap.put("orderItem", orderItem);
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("orderItemId", entry.getValue().get(0).getOrderItemId());
							params.put("visitTime", visitDate);
							//取得共享组中的库存记录
							List<OrdOrderSharedStock> ordOrderSharedStockList = ordOrderSharedStockDao.selectByParams(params);
							Long returnGroupId=0L;
							Date returnDate =null;
							Boolean isSharedStockFlg=false;
							Boolean isReturnFlg=false;
							if(ordOrderSharedStockList!=null&&ordOrderSharedStockList.size()>0){
								returnGroupId =ordOrderSharedStockList.get(0).getGroupId();
								if(returnGroupId!=null&&!"".equals(returnGroupId)){
									isSharedStockFlg =true;
								}else{
									returnGroupId=0L;
								}
							}
							boolean isHotelCombOrder = false;
							boolean onlineFlag = DynamicRouterUtils.getInstance().isHotelSystemOnlineEnabled();
							Long hasShareStock =null;
							//酒店
							if(1L==categoryId){
								hasShareStock = goodsTimePriceStockService.getShareStock(suppGoupId, visitDate);
								//酒店套餐
							}else if(17L==categoryId){
								hasShareStock = goodsLineTimePriceStockService.getShareStock(suppGoupId, visitDate);
								if(onlineFlag){
									hasShareStock = hotelTradeApiService.getHotelShareStock(suppGoupId, visitDate);
									isHotelCombOrder = true;
								}
								LOG.info("updateStockByDate onlineFlag="+onlineFlag+" _isHotelCombOrder="+isHotelCombOrder+" _hasShareStock="+hasShareStock+" _orderItemId"+entry.getValue().get(0).getOrderItemId());
							}
							//下单前不在共享组，取消时也不再共享组
							if((hasShareStock==null&&isSharedStockFlg==false)){
								isReturnFlg =true;
								//下单前在共享组，取消时也在共享组
							}else if (hasShareStock!=null&&isSharedStockFlg==true){
								//商品所在的组相同
								if(suppGoupId.equals(returnGroupId)){
									isReturnFlg =true;
								}

							}
							LOG.info("返还判断***hasShareStock="+hasShareStock +"返还判断***isSharedStockFlg="+isSharedStockFlg+"***isReturnFlg="+isReturnFlg);
							//下单前后商品所在的组相同
							if(isReturnFlg){
								//设置了共享组
								if(isSharedStockFlg){
									for(OrdOrderSharedStock  shareStock : ordOrderSharedStockList){

										if(shareStock.getVisitTime()!=null){
											//returnGroupId =shareStock.getGroupId();
											returnDate =shareStock.getVisitTime();
										}
										//日期相同
										if(visitDate.equals(returnDate))
										{
											orderTimePriceService.updateRevertStock(suppGoodsId, visitDate, updateStockTotal, dataMap);
											//更新订单本地库存为：库存已返还
											OrdOrderStock updateOrderStock = new OrdOrderStock();
											updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.RESTOCK.name());
											for (OrdOrderStock orderStock : entry.getValue()) {
												if (orderStock != null) {
													updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
													ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
												}
											}
										}
									}
								}else{
									//如果是邮轮的库存回滚就走邮轮子系统
									if(OrderUtils.isShipOrderItem(categoryId)){
										shipOrderTimePriceApiClientService.updateRevertStock(orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name())+"", suppGoodsId, visitDate, updateStockTotal, dataMap);
									}else{
										LOG.info("categoryId is " + categoryId + ". and suppGoodsId is " + suppGoodsId);
										// 当返还库存为景点门票，其它票,组合套餐票商品时
										if(categoryId.intValue()== BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().intValue()
												||categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().intValue()
												||categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().intValue()){
											dataMap.put("categoryId",categoryId);
											if(null != suppgoods.getReturnContent()){
												dataMap.put("aperiodicFlag",suppgoods.getReturnContent().getAperiodicFlag());
											}

										}

										orderTimePriceService.updateRevertStock(suppGoodsId, visitDate, updateStockTotal, dataMap);
									}
									//更新订单本地库存为：库存已返还
									OrdOrderStock updateOrderStock = new OrdOrderStock();
									updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.RESTOCK.name());
									for (OrdOrderStock orderStock : entry.getValue()) {
										if (orderStock != null) {
											updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
											ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
										}
									}
								}

							}else{
								//更新订单本地库存为：订单取消前后商品共享组不一致
								OrdOrderStock updateOrderStock = new OrdOrderStock();
								updateOrderStock.setInventory(OrderEnum.INVENTORY_STATUS.UNRESTOCK.name());
								for (OrdOrderStock orderStock : entry.getValue()) {
									if (orderStock != null) {
										updateOrderStock.setOrderStockId(orderStock.getOrderStockId());
										ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
									}
								}
							}
						}
					}
				}

			} else {
				throw new RuntimeException("OrdOrderItem(ID=" + orderItem.getOrderItemId() + ")的CategoryId不存在。");
			}
		}
	}

	/**
	 * 计算库存列表的总库存
	 *
	 * @param orderStockList
	 * @return
	 */
	private Long countOrderStockTotal(List<OrdOrderStock> orderStockList) {
		Long total = 0L;
		if (orderStockList != null) {
			for (OrdOrderStock orderStock : orderStockList) {
				if (orderStock != null) {
					total = total + orderStock.getQuantity();
				}
			}
		}

		return total;
	}

	/**
	 * 根据订单ID查询订单，只关联订单子项、订单库存表
	 *
	 * @param orderId
	 * @return
	 */
	private List<OrdOrder> getOrderForCancelById(Long orderId) {
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);

		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);

		return complexQueryService.queryOrderListByCondition(condition);
	}

	@Override
	public void updateViewStatus(Map<String, Object> map) {
		orderDao.updateViewStatus(map);
	}

	@Override
	public int updateIsTestOrder(Map<String, Object> map) {
		return orderDao.updateIsTestOrder(map);
	}

	@Override
	public int updateInvokeInterfacePfStatus(Long orderId, String invokeInterfacePfStatus) {
		LOG.info("orderId:" + orderId + ", invokeInterfacePfStatus:" + invokeInterfacePfStatus);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("invokeInterfacePfStatus", invokeInterfacePfStatus);
		return orderDao.updateInvokeInterfacePfStatus(params);
	}

	@Override
	public int updateItemInvokeInterfacePfStatus(String orderItemIds, String invokeInterfacePfStatus) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemIds", orderItemIds);
		params.put("invokeInterfacePfStatus", invokeInterfacePfStatus);
		return ordOrderItemDao.updateInvokeInterfacePfStatus(params);
	}

	@Override
	public List<OrdOrderItem> queryOrderItemByOrderId(Long orderId) {
		return ordOrderItemDao.selectByOrderId(orderId);
	}

	public Integer getOrderItemTotalCount(Map<String, Object> params){
		return ordOrderItemDao.getTotalCount(params);
	}
	/**
	 * 为OrderSettlement页面查询总数
	 * @param map
	 * @return integer
	 */
	@Override
	public Integer getOrderItemTotalCountForOrdSettlement(Map<String, Object> params){
		if(null != params && null != params.get("supplierName")){
			String supplierName = (String) params.get("supplierName");
			ResultHandleT<List<SuppSupplier>> resultHandle = suppSupplierClientService.findSuppSupplierByName(supplierName);
			if(resultHandle.isSuccess()){
				List<SuppSupplier> supplierList = resultHandle.getReturnContent();
				if(CollectionUtils.isNotEmpty(supplierList)){
					List<Long> supplierIdList = new ArrayList<>();
					for (SuppSupplier suppSupplier : supplierList) {
						supplierIdList.add(suppSupplier.getSupplierId());
					}
					params.put("supplierIdList", supplierIdList);
				}
			}
		}
		return ordOrderItemDao.getToalCountForOrdSettlement(params);
	}
	@Override
	public List<OrdOrderItem> queryOrderIdByOrderId(Long orderId) {
		return ordOrderItemDao.selectOrderIdByOrderId(orderId);
	}

	@Override
	@ReadOnlyDataSource
	public List<OrdOrder> queryRequestPaymentOrder() {
		return orderDao.queryRequestPaymentOrder(WAIT_MINUTE);
	}

	@Override
	@ReadOnlyDataSource
	@ForceRead
	public List<OrdOrder> queryLastPaymentOrder() {
		return orderDao.queryLastPaymentOrder();
	}

	@Override
	public List<OrdOrder> queryStampExchangeRemainOrder() {
		return orderDao.queryStampExchangeRemainOrder();
	}

	@Override
	public List<OrdOrder> queryRequestTimePaymentOrder() {
		return orderDao.queryRequestTimePaymentOrder(AFTER_CREATE_MINUTE);
	}

	@Override
	@ReadOnlyDataSource
	@ForceRead
	public List<OrdOrder> queryPerformPreviousDayOrderIdList(Date orderCreateTime) {
		List<OrdOrder> orderList = orderDao.queryPerformPreviousDayOrderIdList(orderCreateTime);
		if (orderList != null && orderList.size() > 0) {
			List<OrdOrderItem> itemList = null;
			for (OrdOrder order : orderList) {
				if (order != null) {
					itemList = ordOrderItemDao.selectByOrderId(order
							.getOrderId());
					order.setOrderItemList(itemList);
				}
			}
		}

		return orderList;
	}

	/**
	 * 支付时间只差30分钟的订单
	 */
	private final long WAIT_MINUTE=30L;

	@Override
	public List<Long> getPaymentTimeoutOrderIds(Date currentDate) {
		return orderDao.getPaymentTimeoutOrderIdList(currentDate);
	}

	@Override
	public List<Long> getPendingCancelTestOrderIdList(Map<String, Object> paramsMap) {
		return orderDao.getPendingCancelTestOrderIdList(paramsMap);
	}

	@Override
	public OrdOrderItem getOrderItem(Long orderItemId) {
		OrdOrderItem ordOrderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
		if (ordOrderItem != null) {
			OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendDao.selectByPrimaryKey(orderItemId);
			ordOrderItem.setOrdOrderItemExtend(ordOrderItemExtend);
		}
		return ordOrderItem;
	}
	@Override
	public boolean updateRefundedAmount(Long orderId, Long refundmentId,
										long amount) {
		//只更新订单上的该值
		return orderDao.updateRefundedAmount(orderId,amount)>0;
	}

	@Override
	public List<OrdOrder> queryOrdorderByOrderIdList(List<Long> orderIdList) {
		List<OrdOrder> orderList = null;
		if (orderIdList != null && orderIdList.size() > 0) {
			orderList = orderDao.selectByPrimaryKeyList(orderIdList);
			if (orderList != null && orderList.size() > 0) {
				List<OrdOrderItem> itemList = null;
				for (OrdOrder order : orderList) {
					if (order != null) {
						itemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
						order.setOrderItemList(itemList);
					}
				}
			}
		}

		return orderList;
	}
	@Override
	public void updateGuaranteeCC(Long orderId) {
		OrdGuaranteeCreditCard gcc = ordGuaranteeCreditCardDao.getByOrderId(orderId);
		if(gcc!=null){
			gcc.setCardNo(null);
			gcc.setCvv(null);
			gcc.setExpirationMonth(null);
			gcc.setExpirationYear(null);
			gcc.setHolderName(null);
			gcc.setIdNo(null);
			gcc.setIdType(null);
			ordGuaranteeCreditCardDao.updateByPrimaryKey(gcc);
		}
	}
	@Override
	public List<Long> queryGuaranteeOrderIds() {
		return ordGuaranteeCreditCardDao.selectOrderIds();
	}
	@Override
	public void updateClearOrderProcess(Long orderId) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId", orderId);
		orderDao.updateClearOrderProcess(map);
	}

	@Override
	public boolean setOrderWatiPaymentTime(OrdOrder ordOrder, Date baseDate, boolean checkFlag) {
		boolean isSuccess = false;
		if (ordOrder != null && baseDate != null) {
			if (checkFlag) {
				LOG.info("ordOrder.hasResourceAmple() is :" + ordOrder.hasResourceAmple());
				if (ordOrder.hasResourceAmple()) {
					int waitMinute = getOrderDefaultWatiPaymentTimeMinute(ordOrder);
					if (waitMinute > 0) {
						OrderUtils.setOrderBusinessWaitPaymentTime(DateUtils.addMinutes(baseDate, waitMinute), ordOrder);
						isSuccess = true;
					}
				}
			} else {
				int waitMinute = getOrderDefaultWatiPaymentTimeMinute(ordOrder);
				ordOrder.setWaitPaymentTime(DateUtils.addMinutes(baseDate, waitMinute));
				isSuccess = true;
			}
		}
		//工作流控制变量:人工预审和子订单资源信息预审并行
//		setWorkflowInfoOrderParallelProcess(ordOrder);
		return isSuccess;
	}

	/**
	 *
	 * waitMinute酒店的 2*60 分钟   油轮的24 * 60分钟
	 * @param ordOrder
	 * @return
	 * Modified by ZhuDongQuan @ 201501 支付默认等待时长需要根据不同的BU来设置初始值
	 */
	private int getOrderDefaultWatiPaymentTimeMinute(OrdOrder ordOrder) {
		Integer waitMinute = 2*60;
		Integer ticketWaitMinute = Integer.MAX_VALUE;



		/**
		 * 1.过滤ordOrder 为空
		 * */
		if(ordOrder == null){
			return waitMinute.intValue();
		}
		/**
		 * 如果是券兑换的支付等待时间默认两小时
		 */
		if(ordOrder.getCategoryId()!=null&&ordOrder.getCategoryId().equals(99L)){
			return waitMinute;
		}
		/**
		 * 2.获取是否打包
		 * */
		OrdOrderPack ordOrderPack = ordOrder.getOrdOrderPack();
		if(ordOrderPack == null && ordOrder.getOrderId() !=null){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", ordOrder.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			if (ordOrderPackList != null && !ordOrderPackList.isEmpty()) {
				ordOrderPack = ordOrderPackList.get(0);
			}
		}
		/**
		 * 3.初始化子订单 商品与时间价格信息
		 * orderItemList 子订单信息
		 * 注：现只有线路涉及到有时间价格的判断，所以只获取线路的时间价格。
		 * */
		List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
		if(CollectionUtils.isEmpty(orderItemList) && ordOrder.getOrderId() != null){
			orderItemList = queryOrderItemByOrderId(ordOrder.getOrderId());
		}
		if(CollectionUtils.isEmpty(orderItemList)){
			return waitMinute.intValue();
		}

		//门票BU & 门票品类，设置支付等待时间为30分钟
		if (CommEnumSet.BU_NAME.TICKET_BU.name().equals(ordOrder.getBuCode())
				&& OrderUtils.isTicketByCategoryId(ordOrder.getCategoryId())) {
			for(OrdOrderItem orderItem : orderItemList) {
				ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientService.getBaseTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
				//取得出游日期对应的时间价格数据
				SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
				if(timePrice != null) {
					if (timePrice instanceof SuppGoodsAddTimePrice) {
						SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice) timePrice;
						LOG.info("StockFlag=" + addTimePrice.getStockFlag() + ", orderId:" + ordOrder.getOrderId());
						//限制日库存
						if ("Y".equalsIgnoreCase(addTimePrice.getStockFlag())) {
							ticketWaitMinute = 30;
							break;
						}
					} else if (timePrice instanceof SuppGoodsNotimeTimePrice) {
						SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice) timePrice;
						LOG.info("StockFlag=" + notimeTimePrice.getStockFlag() + ", orderId:" + ordOrder.getOrderId());
						//限制日库存
						if ("Y".equalsIgnoreCase(notimeTimePrice.getStockFlag())) {
							ticketWaitMinute = 30;
							break;
						}
					}
				}
			}
		}

		/**
		 * 4.打包的情况
		 * 国内BU依然可以按照后续非打包的进行处理。
		 * */
		String buType = "";//打包后所属BU
		boolean isLocalBuRule = false; //是否符合国内BU的判断条件
		Integer basicTime = waitMinute; //BU下的基准规则时间
		if(ordOrderPack != null){
			String categoryCode = (String) ordOrderPack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			if(StringUtil.isEmptyString(categoryCode)){
				return Math.min(waitMinute, ticketWaitMinute); //打包的没有categoryCode，返回默认时长
			}
			buType = iOrderDistinguishByBuBussiness.getBuType(categoryCode, ordOrderPack.getProductId(),ordOrder);
			if(iOrderDistinguishByBuBussiness.isConformBuRule(buType,CommEnumSet.BU_NAME.LOCAL_BU.getCode(), ordOrder)){//国内BU的判断规则
				LOG.info("getOrderDefaultWatiPaymentTimeMinute isConformBuRule,orderId = " + ordOrder.getOrderId());
				isLocalBuRule = true;
				fillContentByBuType(CommEnumSet.BU_NAME.LOCAL_BU.getCode(),ordOrder); //初始化信息
				basicTime = iOrderDistinguishByBuBussiness.getOrderDefaultWaitPaymentTimeMinuteByBu(basicTime, buType); //计算基本的时间
			}else{
				//线路品类
				String key = categoryCode.toLowerCase()+"_"+ordOrderPack.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name());
				// 自由行+当地游+酒店套餐 供应商打包
				if (StringUtils.startsWithAny(key, categorys) //自由行+当地游+酒店套餐 <出境>
						&& ProdProduct.PACKAGETYPE.SUPPLIER.getCode().equals(ordOrderPack.getOwnPack())) {

					waitMinute = PaymentWaitRule.TWELVE;
					for(OrdOrderItem orderItem : orderItemList) {
						ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = suppGoodsTimePriceClientService.getBaseTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
						//取得出游日期对应的时间价格数据
						SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
						if(timePrice != null && timePrice instanceof SuppGoodsLineTimePrice) {
							SuppGoodsLineTimePrice lineTimePrice = (SuppGoodsLineTimePrice) timePrice;
							//切位、不可超卖、有库存
							if(STOCKTYPE.CONTROL.name().equals(lineTimePrice.getStockType())//切位
									&& "N".equals(lineTimePrice.getOversellFlag()) //不可超买
									&& lineTimePrice.getStock() != null){//有库存
								waitMinute = PaymentWaitRule.TWO;
								break;
							}
						}
					}
				} else if (categoryCode.startsWith(BizEnum.BIZ_CATEGORY_TYPE.category_route.name())) {
					waitMinute = PaymentWaitRule.getPaymentMinute(key, ordOrder.getCreateTime());
				} else {
					waitMinute = PaymentWaitRule.getPaymentMinute(categoryCode.toLowerCase(), ordOrder.getCreateTime());
				}
				if (waitMinute == null) {
					waitMinute = 2 * 60;
				}
				return Math.min(waitMinute, ticketWaitMinute);
			}
		}
		//判断供应商打包的是否是国内BU
		if(StringUtil.isEmptyString(buType)){
			String categoryCode = "";
			Long productId = 0L;
			for(OrdOrderItem tmp : orderItemList){
				if("true".equalsIgnoreCase(tmp.getMainItem())){
					categoryCode = tmp.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					productId = tmp.getProductId();
				}
			}
			buType = iOrderDistinguishByBuBussiness.getBuType(categoryCode, productId,ordOrder);
			if(iOrderDistinguishByBuBussiness.isConformBuRule(buType,CommEnumSet.BU_NAME.LOCAL_BU.getCode(), ordOrder)){//国内BU的判断规则
				isLocalBuRule = true;
				fillContentByBuType(CommEnumSet.BU_NAME.LOCAL_BU.getCode(),ordOrder); //初始化信息
				basicTime = iOrderDistinguishByBuBussiness.getOrderDefaultWaitPaymentTimeMinuteByBu(basicTime, buType); //计算基本的时间
				LOG.info("getOrderDefaultWatiPaymentTimeMinute isConformBuRule,orderId = " + ordOrder.getOrderId());
			}
		}
		List<Integer> minutes = new ArrayList<Integer>();
		Integer tempWaitMinute = 0;
		for (OrdOrderItem ordOrderItem : orderItemList) {
			String categoryCode = ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			//线路品类
			if(categoryCode.startsWith(BizEnum.BIZ_CATEGORY_TYPE.category_route.name())){
//				tempWaitMinute = paymentMinuteMap.get(categoryCode.toLowerCase()+"_"+ordOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()));
				String key = categoryCode.toLowerCase()+"_"+ ordOrderItem.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name());
				tempWaitMinute = PaymentWaitRule.getPaymentMinute(key, ordOrder.getCreateTime());
			}else{
				tempWaitMinute = PaymentWaitRule.getPaymentMinute(categoryCode.toLowerCase(), ordOrder.getCreateTime());
			}
			//防错处理
			if(tempWaitMinute == null){
				tempWaitMinute = 2*60;
			}
			if(isLocalBuRule){
				if(categoryCode.startsWith(BizEnum.BIZ_CATEGORY_TYPE.category_other.name())){
					tempWaitMinute = 24*60;
				}
				if(categoryCode.startsWith(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name())){
					tempWaitMinute = iOrderDistinguishByBuBussiness.getHotelDefaultWaitPaymentTimeMinuteByBu(tempWaitMinute, ordOrderItem);//线路下的酒店.
				}
			}
			//防错处理
			if(tempWaitMinute == null){
				tempWaitMinute = 2*60;
			}
			minutes.add(tempWaitMinute);
		}
		if(isLocalBuRule){
			minutes.add(basicTime);
		}
		waitMinute = Collections.min(minutes);
		return Math.min(waitMinute, ticketWaitMinute);
	}

	private void fillContentByBuType(String buType, OrdOrder ordOrder) {
		if(StringUtil.isEmptyString(buType) || ordOrder == null){
			return;
		}
		if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(buType)){
			if(CollectionUtils.isEmpty(ordOrder.getOrderItemList())){
				return;
			}
			OrderTimePriceService orderTimePriceService = null;
			for(OrdOrderItem itm : ordOrder.getOrderItemList()){
				String categoryCode = itm.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if(categoryCode.startsWith(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name()) //获得单酒店的时间价格
						&& itm.getSuppGoodsTimePrice() == null){
					orderTimePriceService = orderOrderFactory.createTimePrice(itm);
					ResultHandleT<SuppGoodsBaseTimePrice> baseTimePrice = orderTimePriceService.getTimePrice(itm.getSuppGoodsId(), itm.getVisitTime(), false);
					if(baseTimePrice.isSuccess()){
						itm.setSuppGoodsTimePrice(baseTimePrice.getReturnContent());
					}
				}
			}
		}
	}

	private void orgnizeOrderItemList(OrdOrder ordOrder) {
		List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
		ordOrder.setOrderItemList(orderItemList);
	}

	public boolean checkWorkflowOrderInfoParallelProcess(OrdOrder ordOrder){

		if(CollectionUtils.isEmpty(ordOrder.getOrderItemList())){
			LOG.info("ordOrder.getOrderItemList is empty");
			return false;
		}
		OrdOrderItem mainOrdOrderItem = ordOrder.getMainOrderItem();
		if (null == ordOrder.getMainOrderItem()){
			LOG.info("orgnizeOrderItemList to get mainOrderItem");
			orgnizeOrderItemList(ordOrder);
			mainOrdOrderItem = ordOrder.getMainOrderItem();
		}

		//BU：出境 品类：跟团游 全部产品产生订单后，客服信息预审和订单凭证确认同步进行
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(mainOrdOrderItem.getBuCode()) &&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(mainOrdOrderItem.getCategoryId())){
			return true;
		}
		//如果不是 出境BU 或者不是产品类别:自由行 那么不走并行流程（并行流程：客服信息预审和订单凭证确认同步进行）
		if(!CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(mainOrdOrderItem.getBuCode()) ||
				!BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(mainOrdOrderItem.getCategoryId())){
			return false;
		}

		OrderTimePriceService orderTimePriceService = null;
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = null;
		if(mainOrdOrderItem.getSuppGoodsTimePrice() == null) {
			orderTimePriceService = orderOrderFactory.createTimePrice(mainOrdOrderItem);
			ResultHandleT<SuppGoodsBaseTimePrice> baseTimePrice =
					orderTimePriceService.getTimePrice(mainOrdOrderItem.getSuppGoodsId(),
							mainOrdOrderItem.getVisitTime(), false);
			if (baseTimePrice.isSuccess()) {
				suppGoodsBaseTimePrice = baseTimePrice.getReturnContent();
			}
		}
		if (null == suppGoodsBaseTimePrice){
			LOG.info("failed to get getTimePrice, set non parallel process");
			return false;
		}
		//出境BU: 产品类别:自由行 商品库存属性为切位且不可超卖 走信息自动审核
		LOG.info("buCode:" + mainOrdOrderItem.getBuCode() + " categoryId:"+mainOrdOrderItem.getCategoryId()
				+ " suppGoodsBaseTimePrice.getOversellFlag:" + suppGoodsBaseTimePrice.getOversellFlag() );
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(mainOrdOrderItem.getBuCode()) &&
				BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(mainOrdOrderItem.getCategoryId())){
			if ( "N".equals(suppGoodsBaseTimePrice.getOversellFlag())) {
				if( SuppGoodsLineTimePrice.class.isInstance(suppGoodsBaseTimePrice)
						&& SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equals(
						((SuppGoodsLineTimePrice)suppGoodsBaseTimePrice).getStockType())){
					return true;
				}
				if( SuppGoodsMultiTimePrice.class.isInstance(suppGoodsBaseTimePrice)
						&& SuppGoodsMultiTimePrice.STOCKTYPE.CONTROLLABLE.name().equals(
						((SuppGoodsMultiTimePrice)suppGoodsBaseTimePrice).getStockType())){
					return true;
				}
				if( SuppGoodsSingleTimePrice.class.isInstance(suppGoodsBaseTimePrice)
						&& SuppGoodsSingleTimePrice.STOCKTYPE.CONTROLLABLE.name().equals(
						((SuppGoodsSingleTimePrice)suppGoodsBaseTimePrice).getStockType())){
					return true;
				}
			}
		}
		LOG.info("failed to get getTimePrice, set non parallel process");
		return false;
	}

	public void updateOrderUsedFavor(OrdOrder orderVst, String couponCode) {
		//计算优惠系统优惠结果 返回 需要优惠的商品id：orderItemIdFavor 和 优惠金额：discountAmount
		Pair<FavorStrategyInfo, Object> pair = favorServiceAdapter.calculateFavor(orderVst, couponCode, orderVst.getUserNo());

		if(pair.isSuccess()){
			Long orderId=orderVst.getOrderId();
			Object orderPackItemFavor=pair.getSecond();
			final Long discountAmount = pair.getFirst().getDiscountAmount();

			if (discountAmount<=0) {
				LOG.info("优惠金额小于等于0，discountAmount："+discountAmount);
				return;
			}
			// 更新订单应付金额
			Long oughtAmount = orderVst.getOughtAmount() - discountAmount;
			if (oughtAmount<=0) {
				oughtAmount=0L;
			}
			orderVst.setOughtAmount(oughtAmount);
			orderDao.updateByPrimaryKey(orderVst);

			// 保存扣记录
			OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
			ordOrderAmountItem.setItemAmount(-discountAmount);
			ordOrderAmountItem.setOrderId(orderId);
			ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.name());
			ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.COUPON_PRICE.name());
			ordOrderAmountItemDao.insert(ordOrderAmountItem);

			//记录优惠日志
			orderVst.setOughtAmount(orderVst.getOughtAmount() + discountAmount);
			ResultHandle handle = favorServiceAdapter.updateMarkCoupon(orderVst,orderPackItemFavor,
					couponCode, orderVst.getUserNo(), orderVst.getClientIpAddress(), orderVst.getOrderDfp());
			if (handle.isFail()) {
				throw new RuntimeException("mark coupon error:"+handle.getMsg());
			}
			orderVst.setOughtAmount(orderVst.getOughtAmount() - discountAmount);
		}
	}

	private int insertAmountItem(Long orderId,Long discountAmount,String amountName,String amountType){
		// 保存扣记录
		OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
		ordOrderAmountItem.setItemAmount(-discountAmount);
		ordOrderAmountItem.setOrderId(orderId);
		ordOrderAmountItem.setItemName(amountName);
		ordOrderAmountItem.setOrderAmountType(amountType);
		int rs = ordOrderAmountItemDao.insert(ordOrderAmountItem);
		return rs;
	}
	public String updateOrderForBuyInfo(OrdOrder orderVst, BuyInfo buyInfo) {
		LOG.info("OrdOrderUpdateServiceImpl#updateOrderForBuyInfo  OrdOrder==" + 
				com.alibaba.fastjson.JSONObject.toJSONString(orderVst) + "BuyInfo==" + 
				com.alibaba.fastjson.JSONObject.toJSONString(buyInfo));
		long cashPayAmt=0L;//现金账户支付
		long bonusPayAmt=0L;//奖金账户支付
		long couponPayAmt=0L;//优惠券优惠金额
		long giftCardPayAmt=0L;//礼品卡支付金额
		long storePayAmt=0L;//储值卡支付金额
		Long bonusActualAmt = null;
		Long commissionAmount=buyInfo.getCommissionAmountHidden();//店员专享优惠


		if (commissionAmount!=null&&commissionAmount>0) {
			insertAmountItem(orderVst.getOrderId(),commissionAmount,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_O2OCHANEL.name(),OrderEnum.ORDER_AMOUNT_TYPE.O2OCHANEL_AMOUNT.name());
		}else{
			commissionAmount=0l;
		}
		int size = buyInfo.getUserCouponVoList().size();
		LOG.info("==========cm============updateOrderForBuyInfo UserCouponVoList size:"+size);
		String erroFlag="";
		if (CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())) {
			List<String> usedCouponList=new ArrayList<String>();
			for(UserCouponVO c:buyInfo.getUserCouponVoList()){
				if("B".equals(c.getCouponType())){
					usedCouponList.add(c.getCouponCode());
				}
				if (c.getDiscountAmount()!=null) {
					couponPayAmt+=c.getDiscountAmount();
				} else {
					c.setDiscountAmount(0F);
				}
			}
			if (couponPayAmt>0) {
				insertAmountItem(orderVst.getOrderId(),couponPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_COUPON.name(),OrderEnum.ORDER_AMOUNT_TYPE.COUPON_AMOUNT.name());
				if(usedCouponList.size()>0){
					try {
						int usedResult=favorServiceAdapter.updateMarkCouponCodeUsed(usedCouponList, true);
						if (!(usedResult>0)) {
							erroFlag="优惠券扣减失败";
							return erroFlag;
						}
					} catch (Exception e) {
						erroFlag="优惠券扣减失败";
						return erroFlag;
					}

				}
				orderVst.setCouponAmount(couponPayAmt);
			}
			LOG.info("---------------------------------------------优惠券集合 UserCouponVoList:"+JSONArray.fromObject(buyInfo.getUserCouponVoList()));
			LOG.info("---------------------------------------------订单ID，orderId"+	orderVst.getOrderId());
			favorServiceAdapter.insertMarkCouponUsages(buyInfo.getUserCouponVoList(), orderVst.getOrderId(),"VST_ORDER",
                    buyInfo.getIp(), buyInfo.getOrderDfp());

		}
		LOG.info("==========cm============updateOrderForBuyInfo couponPayAmt :"+couponPayAmt);
		// 更新订单应付金额    为避免和支付系统回调金额交叉记录，将更新应付金额提前
		Long oughtAmount = orderVst.getOughtAmount() -couponPayAmt-commissionAmount;
		if (oughtAmount<=0) {
			oughtAmount=0L;
		}
		orderVst.setOughtAmount(oughtAmount);//避免零元支付时不是已收款
		orderDao.updateOrder(orderVst);//为避免覆盖更新回调时的收款信息此处使用updateByPrimaryKeySelective方式

		//根据用户NO获取奖金账户和现金账户信息
		LOG.info("vstPayFromMoneyAccount start buyInfo:"+buyInfo);
		if( buyInfo.getCashAmountHidden()!=null && buyInfo.getCashAmountHidden().intValue()>0){
			try {
				//boolean monyResult=ordUserOrderServiceAdapter.vstPayFromMoneyAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserNo(), orderVst.getOrderId(), buyInfo.getCashAmountHidden());
				boolean monyResult=ordUserOrderServiceAdapter.vstPayFromMoneyAccountNew(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserNo(), orderVst.getOrderId(), buyInfo.getCashAmountHidden());
				LOG.info("monyResult:"+monyResult);
				if (!monyResult) {
					erroFlag="现金账户扣减失败";
					return erroFlag;
				}
				cashPayAmt+=buyInfo.getCashAmountHidden();
				insertAmountItem(orderVst.getOrderId(),cashPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_CASH.name(),OrderEnum.ORDER_AMOUNT_TYPE.CASH_AMOUNT.name());
			} catch (Exception e) {
				LOG.info("vstPayFromMoneyAccountNew:现金账户扣减失败");
				erroFlag="现金账户扣减失败";
				return erroFlag;
			}
		}

		//奖金支付（老接口不能用暂时判断改版前是否有使用，避免重复扣）
		if(orderVst.getBonusAmount()>0){
			try {
				boolean bonusResult=false;
				LOG.info("------------------------------奖金扣款传参,userId:"+buyInfo.getUserId()+"OrderId"+orderVst.getOrderId()+"BonusAmount"+orderVst.getBonusAmount());
				String reasult=bonusPayService.payFromBonusForVstOrder(buyInfo.getUserId(), orderVst.getOrderId(), orderVst.getBonusAmount());
				LOG.info("------------------------------奖金接口返回,reasult:"+reasult);
				//继续使用老接口
				JSONObject o = JSONObject.fromObject(reasult);
				bonusResult="true".equals(o.getString("success"));
				//boolean bonusResult=ordUserOrderServiceAdapter.vstPayFromBonusAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(),orderVst.getOrderId(), buyInfo.getUserNo(),orderVst.getBonusAmount());
				if (!bonusResult) {
					erroFlag="奖金账户扣减失败";
					return erroFlag;
				}
				bonusPayAmt+=orderVst.getBonusAmount();
				insertAmountItem(orderVst.getOrderId(),bonusPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_BONUS.name(),OrderEnum.ORDER_AMOUNT_TYPE.BONUS_AMOUNT.name());
			} catch (Exception e) {
				erroFlag="奖金账户扣减失败";
				return erroFlag;
			}
		}
		//千万不要和上面代码交换顺序 因为此方法中有设置  				orderVst.setBonusAmount(bonusPayAmt);  
		if(buyInfo.getBonusAmountHidden()!=null&& buyInfo.getBonusAmountHidden().intValue()>0){
			try {
				boolean bonusResult=false;
				LOG.info("------------------------------奖金扣款传参,userId:"+buyInfo.getUserId()+"OrderId"+orderVst.getOrderId()+"BonusAmount"+buyInfo.getBonusAmountHidden());
				String reasult=bonusPayService.payFromBonusForVstOrder(buyInfo.getUserId(), orderVst.getOrderId(),buyInfo.getBonusAmountHidden());
				//继续使用老接口
				LOG.info("------------------------------奖金接口返回,reasult:"+reasult);
				JSONObject o = JSONObject.fromObject(reasult);
				bonusResult="true".equals(o.getString("success"));
				//boolean bonusResult=ordUserOrderServiceAdapter.vstPayFromBonusAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(),orderVst.getOrderId(), buyInfo.getUserNo(),buyInfo.getBonusAmountHidden());
				if (!bonusResult) {
					erroFlag="奖金账户扣减失败";
					return erroFlag;
				}
				bonusPayAmt+=buyInfo.getBonusAmountHidden();
				insertAmountItem(orderVst.getOrderId(),bonusPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_BONUS.name(),OrderEnum.ORDER_AMOUNT_TYPE.BONUS_AMOUNT.name());
				orderVst.setBonusAmount(bonusPayAmt);
				bonusActualAmt = bonusPayAmt;
			} catch (Exception e) {
				erroFlag="奖金账户扣减失败";
				return erroFlag;
			}
		}

		if(CollectionUtils.isNotEmpty(buyInfo.getGiftCardList())){
			LOG.info("####使用礼品卡支付buyInfo start####");
			LOG.info("####礼品卡信息####" + com.alibaba.fastjson.JSONObject.toJSONString(buyInfo.getGiftCardList()));
			Map<String, Long> map =new HashMap<String, Long>();
			for(CardInfo c:buyInfo.getGiftCardList()){
				map.put(c.getCardNo(),c.getUseAmt());
				giftCardPayAmt+=c.getUseAmt();
			}
			try {
				LOG.info("####PARAM GiftCard Info####" + com.alibaba.fastjson.JSONObject.toJSONString(map));
				LOG.info("####OrderId####" + orderVst.getOrderId());
				LOG.info("####IP####" + buyInfo.getIp());
				LOG.info("####OrderDfp####" + buyInfo.getOrderDfp());
				LOG.info("####giftCardPayAmt####" + giftCardPayAmt);
				String orderDfp = buyInfo.getOrderDfp() == null ? "" : buyInfo.getOrderDfp();
				boolean giftCardReasult=payPaymentServiceAdapter.reduceStoreCardsAmt(map, "1", orderVst.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserId(),
						buyInfo.getIp(), orderDfp);
				LOG.info("orderId###" + orderVst.getOrderId() + "###invoke reduceStoreCardsAmt,return giftCardReasult===" + giftCardReasult);
				if (!giftCardReasult) {
					erroFlag="礼品卡扣减失败";
					return erroFlag;
				}
				if(giftCardPayAmt >0){
					insertAmountItem(orderVst.getOrderId(),giftCardPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_GIFTCARD.name(),OrderEnum.ORDER_AMOUNT_TYPE.GIFTCARD_AMOUNT.name());
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				erroFlag="礼品卡扣减失败";
				return erroFlag;

			}
		}

		if(CollectionUtils.isNotEmpty(buyInfo.getStoreCardList())){
			LOG.info("####使用储值卡支付buyInfo start####");
			LOG.info("####储值卡信息####" + com.alibaba.fastjson.JSONObject.toJSONString(buyInfo.getStoreCardList()));
			Map<String, Long> map =new HashMap<String, Long>();
			for(CardInfo c:buyInfo.getStoreCardList()){
				map.put(c.getCardNo(),c.getUseAmt());
				storePayAmt+=c.getUseAmt();
			}
			try {
				LOG.info("####PARAM StoreCard Info####" + com.alibaba.fastjson.JSONObject.toJSONString(map));
				LOG.info("####OrderId####" + orderVst.getOrderId());
				LOG.info("####IP####" + buyInfo.getIp());
				LOG.info("####OrderDfp####" + buyInfo.getOrderDfp());
				LOG.info("####storePayAmt####" + storePayAmt);
				String orderDfp = buyInfo.getOrderDfp() == null ? "" : buyInfo.getOrderDfp();
				boolean StoreReasult=payPaymentServiceAdapter.reduceStoreCardsAmt(map, "0", orderVst.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserId(),
						buyInfo.getIp(), orderDfp);
				LOG.info("orderId###" + orderVst.getOrderId() + "###invoke reduceStoreCardsAmt,return StoreReasult===" + StoreReasult);
				if (!StoreReasult) {
					erroFlag="储值卡扣减失败";
					return erroFlag;
				}
				if(storePayAmt >0 ){
					insertAmountItem(orderVst.getOrderId(),storePayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_STORECARD.name(),OrderEnum.ORDER_AMOUNT_TYPE.STORECARD_AMOUNT.name());
				}


			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				erroFlag="储值卡扣减失败";
				return erroFlag;

			}
		}

		OrdOrder order=new OrdOrder();
		order.setOrderId(orderVst.getOrderId());
		if(bonusActualAmt!=null ){
			//Long amount = orderVst.getBonusAmount();
			order.setBonusAmount(bonusActualAmt);
		}
		order.setOughtAmount(oughtAmount);
		orderDao.updateOrder(order);//为避免覆盖更新回调时的收款信息此处使用updateByPrimaryKeySelective方式
		return erroFlag;
	}


	@Override
	public OrdOrder updateTransferOrder(List<PayPayment> paymentList) {
		if(CollectionUtils.isEmpty(paymentList)){
			return null;
		}

		OrdOrder order = orderDao.selectByPrimaryKey(paymentList.get(0).getObjectId());
		if(order==null){
			return null;
		}

		long amount = 0;
		for(PayPayment payment:paymentList){
			amount+=payment.getAmount();
		}
		if(order.getActualAmount()!=null){
			amount+=order.getActualAmount();
		}
		order.setActualAmount(amount);
		if(order.getActualAmount()>=order.getOughtAmount()){
			order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
		}else{
			order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PART_PAY.name());
		}
		OrdOrder oriOrder = orderDao.selectByPrimaryKey(order.getOriOrderId());
		if(oriOrder==null){
			ReflectionUtils.rethrowRuntimeException(new RuntimeException("ori order not found"));
		}
		oriOrder.setPaymentStatus(OrderEnum.PAYMENT_STATUS.TRANSFERRED.name());
		oriOrder.setPaymentTime(new Date());
		oriOrder.setOrderUpdateTime(new Date());
		orderDao.updateByPrimaryKey(oriOrder);


		order.setPaymentTime(new Date());
		order.setOrderUpdateTime(new Date());
		int len = orderDao.updateByPrimaryKey(order);
		if(len!=1){
			ReflectionUtils.rethrowRuntimeException(new RuntimeException("order update error: update length="+len));
		}
		updateOrderItemPaymentStatus(order);
		return order;
	}
	@Override
	public List<OrdOrderItem> selectCategoryIdByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return ordOrderItemDao.selectCategoryIdByOrderId(orderId);
	}
	@Override
	public List<OrdOrderItem> queryOrderItemByParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordOrderItemDao.selectByParams(params);
	}

	/**
	 * 为OrderSettlement页面查询结果
	 * @param map
	 * @return list
	 */
	@Override
	public List<OrdOrderItem> queryOrderItemByParamsForOrdSettlement(Map<String, Object> params) {
		// TODO Auto-generated method stub
		if(null != params && null != params.get("supplierName")){
			String supplierName = (String) params.get("supplierName");
			ResultHandleT<List<SuppSupplier>> resultHandle = suppSupplierClientService.findSuppSupplierByName(supplierName);
			if(resultHandle.isSuccess()){
				List<SuppSupplier> supplierList = resultHandle.getReturnContent();
				if(CollectionUtils.isNotEmpty(supplierList)){
					List<Long> supplierIdList = new ArrayList<>();
					for (SuppSupplier suppSupplier : supplierList) {
						supplierIdList.add(suppSupplier.getSupplierId());
					}
					params.put("supplierIdList", supplierIdList);
				}
			}
		}
		//查询子单是否有外币结算记录
		List<OrdOrderItem> ordOrderItems = ordOrderItemDao.selectByParamsForOrdSettlement(params);
		if (CollectionUtils.isNotEmpty(ordOrderItems)) {
			for (OrdOrderItem ordOrderItem : ordOrderItems) {
				OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendDao.selectByPrimaryKey(ordOrderItem.getOrderItemId());
				ordOrderItem.setOrdOrderItemExtend(ordOrderItemExtend);
			}
		}
		return ordOrderItems;
	}
	@Override
	public void saveTicketPerform(OrdOrderItem orderItem) {
		if(ticketPerformDao.selectCountByOrderItem(orderItem.getOrderItemId())==0){
//			long  adultQuantityCount = 0L;
//			long  childQuantityCount = 0L;
//			if(orderItem.getQuantity()>0){
//				adultQuantityCount = orderItem.getAdultQuantity()*orderItem.getQuantity();
//				childQuantityCount = orderItem.getChildQuantity()*orderItem.getQuantity();
//			}

			OrdTicketPerform perform = new OrdTicketPerform();
			perform.setOrderId(orderItem.getOrderId());
			perform.setOrderItemId(orderItem.getOrderItemId());
			perform.setVisitTime(orderItem.getVisitTime());
			perform.setAdultQuantity(orderItem.getAdultQuantity());//成人数
			perform.setChildQuantity(orderItem.getChildQuantity());//儿童数
			perform.setCreateTime(new Date());
			ticketPerformDao.insert(perform);
		}
	}


	@Override
	public Long queryUserFirstOrder(Long userId){
		return orderDao.queryUserFirstOrder(userId);
	}
	@Override
	public ResultHandleT<String> updateOrderPerson(OrdOrder order, OrdOrderPersonVO vo) {
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(vo != null){
			List<OrdPerson> travellers = vo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(OrdPerson person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		ResultHandleT<String> handle = new ResultHandleT<String>();
		StringBuffer sb =new StringBuffer();
		List<OrdPerson> travellerList = order.getOrdTravellerList();
		Map<Long,OrdPerson> personMap = new HashMap<Long, OrdPerson>();
		for(OrdPerson op:travellerList){
			personMap.put(op.getOrdPersonId(), op);
		}

		List<OrdPerson> updatePersonList = new ArrayList<OrdPerson>();
		boolean flag=false;
		StringBuffer travellerPerson = new StringBuffer();
		for(OrdPerson op:vo.getTravellers()){
			if(!personMap.containsKey(op.getOrdPersonId())){
				throw new IllegalArgumentException("不存在对应的游玩人");
			}
			OrdPerson oldPerson = personMap.get(op.getOrdPersonId());
			if(comparePerson(oldPerson, op)){
				flag=true;
				updatePersonList.add(oldPerson);
				travellerPerson.append("_");
				travellerPerson.append(oldPerson.getOrdPersonId());

			}
			if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
				OrderUtils.resetPersonInfo(oldPerson);
			}
		}
		if(flag){
			sb.append(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
			sb.append(travellerPerson.toString());
			sb.append(",");
			flag=false;
		}

		if(order.getContactPerson()!=null){
			if(comparePerson(order.getContactPerson(), vo.getContact())){
				sb.append(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
				sb.append(",");
				updatePersonList.add(order.getContactPerson());
			}
		}
		if(vo.getEmergencyPerson()!=null){
			OrdPerson op = getEmergencyPerson(order.getOrdPersonList());
			if(op==null){
				op=new OrdPerson();
				op.setObjectId(order.getOrderId());
				op.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
				op.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
			}

			if(comparePerson(op, vo.getEmergencyPerson())){
				sb.append(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
				sb.append(",");
				//order.getContactPerson()
				updatePersonList.add(op);
			}
		}


		List<OrdItemPersonRelation> addRelationList = new ArrayList<OrdItemPersonRelation>();
		List<Long> deleteKeys = new ArrayList<Long>();
		StringBuffer ordPersonRelation=new StringBuffer();
		if(MapUtils.isNotEmpty(vo.getPersonRelationMap())){
			Map<String,Object> params = new HashMap<String, Object>();
			for(String key:vo.getPersonRelationMap().keySet()){
				Long orderItemId = org.apache.commons.lang3.math.NumberUtils.toLong(key.replace("ORDERITEM_",""));
				if(orderItemId<1){
					throw new IllegalArgumentException("商品关联信息错误");
				}
				params.put("orderItemId", orderItemId);
				List<OrdItemPersonRelation> relationList = ordItemPersonRelationDao.findOrdItemPersonRelationList(params);
				if(relationList.isEmpty()){
					throw new IllegalArgumentException("商品关联信息不存在");
				}
				OrdOrderPersonVO.ItemPersonRelation ipr = vo.getPersonRelationMap().get(key);
				if(ipr.getOrdItemPersonRelationList()==null||ipr.getOrdItemPersonRelationList().isEmpty()){
					throw new IllegalArgumentException("商品关联人员信息为空");
				}
				if(relationList.size()!=ipr.getOrdItemPersonRelationList().size()){
					throw new IllegalArgumentException("商品关联人员信息数量错误");
				}

				Map<Long,Long> oldItemMap = new HashMap<Long, Long>();
				Set<Long> newPersonIds = new HashSet<Long>();
				for(OrdItemPersonRelation r:relationList){
					oldItemMap.put(r.getOrdPersonId(), r.getItemPersionRelationId());
				}
				boolean f=false;
				for(OrdItemPersonRelation pr:ipr.getOrdItemPersonRelationList()){
					if(!oldItemMap.containsKey(pr.getOrdPersonId())){
						pr.setOrderItemId(orderItemId);
						addRelationList.add(pr);
						f=true;
					}
					newPersonIds.add(pr.getOrdPersonId());
				}
				if(f){
					ordPersonRelation.append("_");
					ordPersonRelation.append(orderItemId);
				}
//				if(newPersonIds)

				for(OrdItemPersonRelation pr:relationList){
					if(!newPersonIds.contains(pr.getOrdPersonId())){
						deleteKeys.add(pr.getItemPersionRelationId());
					}
				}
			}

		}


		for(OrdPerson op:updatePersonList){
			if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
				if(!OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name().equals(op.getIdType())
						&&!OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name().equals(op.getIdType())){
					op.setIssued("");
					op.setExpDate(null);
				}
			}

			if (op.getOrdPersonId()!=null) {
				ordPersonDao.updateByPrimaryKey(op);
			}else {
				ordPersonDao.insertSelective(op);
			}
		}

		//为游玩人时，更新签证审核中的游客名称
		try {
			for(OrdPerson op:updatePersonList){
				if(ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(op.getPersonType())){
					if(order.getCategoryId()!=null && OrderUtil.showVisaApprovalFlag(order)){
						updateVisaApprovalTraveller(order, op);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("orderId:"+order.getOrderId()+"updateVisaApprovalTraveller error:"+e);
		}


		for(Long id:deleteKeys){
			ordItemPersonRelationDao.deleteByPrimaryKey(id);
		}

		if(!addRelationList.isEmpty()){
			for(OrdItemPersonRelation opr:addRelationList){
				ordItemPersonRelationDao.insert(opr);
			}
		}
		if(ordPersonRelation.length()>0){
			sb.append("ORDERITEM");
			sb.append(ordPersonRelation.toString());
			sb.append(",");
		}
		if(sb.length()>0){
			sb.setLength(sb.length()-1);
			handle.setReturnContent(sb.toString());
		}
		return handle;
	}


	/**
	 * 更新签证审核
	 * @param opEbkCertifAuditServiceImpl
	 */
	private void updateVisaApprovalTraveller(OrdOrder order, OrdPerson op) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", order.getOrderId());
		params.put("personId", op.getOrdPersonId());
		VisaResultHandleT<ArrayList<VisaApprovalVo>> resultHandleT = visaApprovalClientService.findVisaApprovalList(params);
		if(resultHandleT.isSuccess()) {
			List<VisaApprovalVo> visaApprovalList = resultHandleT.getReturnContent();
			if(CollectionUtils.isNotEmpty(visaApprovalList)) {
				for(VisaApprovalVo va : visaApprovalList) {
					va.setName(op.getFullName());
					visaApprovalClientService.updateVisaApproval(va);
				}
			}
		}
	}

	private OrdPerson getEmergencyPerson(List<OrdPerson> personList){
		for(OrdPerson op:personList){
			if(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equals(op.getPersonType())){
				return op;
			}
		}
		return null;
	}


	private boolean comparePerson(OrdPerson oldPerson,OrdPerson newPerson){
		boolean flag=false;
		for(String f:person_fields){
			if(checkAndSet(oldPerson,newPerson,f)){
				flag=true;
			}
		}
		return flag;
	}

	private static final String[] person_fields={
			"firstName","lastName","fullName", "gender",
			"mobile","phone","outboundPhone","fax",
			"email","idType","idNo","nationality",
			"birthday","birthPlace", "issued", "expDate"
	};

	private static boolean checkAndSet(OrdPerson op,OrdPerson np,String fieldName){
		boolean flag=false;
		try {
			Method m = PropertyUtils.getPropertyDescriptor(op, fieldName).getReadMethod();
			if(!m.getReturnType().equals(Date.class)){
				String oldValue = (String)m.invoke(op);
				String newValue = (String)m.invoke(np);
				if(StringUtils.equals(oldValue, newValue)){
					return false;
				}
				m = PropertyUtils.getPropertyDescriptor(op, fieldName).getWriteMethod();
				m.invoke(op, newValue);
			}else{
				Date oldValue = (Date)m.invoke(op);
				Date newValue = (Date)m.invoke(np);

				if(oldValue==null&&newValue!=null||oldValue!=null&&newValue==null||oldValue!=null&& newValue!=null){
					if(oldValue!=null&&!oldValue.equals(newValue)||newValue!=null&&!newValue.equals(oldValue)){
						m = PropertyUtils.getPropertyDescriptor(op, fieldName).getWriteMethod();
						m.invoke(op, newValue);
					}
				}else{
					return false;
				}
			}

			flag=true;
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return flag;
	}

	@Override
	public List<OrdOrderItem> queryOrderItems() {
		return ordOrderItemDao.selectOrderItems();
	}


	@Override
	public int updateOrderItemPerformStatus(OrdOrderItem updateItem) {
		return ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
	}


	@Override
	public List<OrdOrderItem> querySettlementOrderItemByParams(
			Map<String, Object> params) {
		return ordOrderItemDao.selectByParamsForSettlement(params);
	}

	@Override
	public Integer getSettlementOrderItemTotalCount(Map<String, Object> params) {
		return ordOrderItemDao.getTotalCountForSettlement(params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateOrdOrder(OrdOrder ordOrder) {
		return orderDao.updateOrder(ordOrder);
	}
	@Override
	public int invalidOrder(Long orderId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("validStatus", OrderEnum.VALID_STATUS.INVALID);
		return orderDao.updateValidStatus(params);
	}

	@Override
	public void markRemindSmsNoSend(List<Long> orderIdList) {
		if(orderIdList == null || orderIdList.size() <= 1000) {
			orderDao.markRemindSmsNoSend(orderIdList);
			return;
		}

		int fromIndex = 0;
		int toIndex = 0;
		while(fromIndex < orderIdList.size()) {
			if(fromIndex + 1000 >= orderIdList.size()) {
				toIndex = orderIdList.size();
			} else {
				toIndex = fromIndex + 1000;
			}
			orderDao.markRemindSmsNoSend(orderIdList.subList(fromIndex, toIndex));

			fromIndex = toIndex;
		}
	}

	@Override
	public void markRemindSmsSent(List<Long> orderIdList) {
		if(orderIdList == null || orderIdList.size() <= 1000) {
			orderDao.markRemindSmsSent(orderIdList);
			return;
		}

		int fromIndex = 0;
		int toIndex = 0;
		while(fromIndex < orderIdList.size()) {
			if(fromIndex + 1000 >= orderIdList.size()) {
				toIndex = orderIdList.size();
			} else {
				toIndex = fromIndex + 1000;
			}
			orderDao.markRemindSmsSent(orderIdList.subList(fromIndex, toIndex));

			fromIndex = toIndex;
		}
	}
	@Override
	public int addOrderActualAmount(Long orderId, Long actualAmount) {
		if(actualAmount == null) {
			return 0;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("actualAmount", actualAmount);

		return orderDao.addOrderActualAmount(params);
	}

	@Override
	public int updateAddOrderActualAmount(Long orderId, Long actualAmount) {
		if(actualAmount == null) {
			return 0;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("actualAmount", actualAmount);

		return orderDao.addOrderActualAmount(params);
	}

	@Override
	public void markCancelTimes(List<Long> orderIdList) {
		if(orderIdList == null || orderIdList.size() <= 1000) {
			orderDao.markCancelTimes(orderIdList);
			return;
		}

		int fromIndex = 0;
		int toIndex = 0;
		while(fromIndex < orderIdList.size()) {
			if(fromIndex + 1000 >= orderIdList.size()) {
				toIndex = orderIdList.size();
			} else {
				toIndex = fromIndex + 1000;
			}
			orderDao.markCancelTimes(orderIdList.subList(fromIndex, toIndex));

			fromIndex = toIndex;
		}
	}
//	@Override
//	public int updateCancelStatus(Map<String, Object> map) {
//		return orderDao.updateCancelStatus(map);
//	}

	@Override
	public Integer getOrderOrderItemSuppGoodsTotalCount(Map<String, Object> params) {
		// TODO Auto-generated method stub

		return ordOrderItemDao.getOrderOrderItemSuppGoodsTotalCount(params);
	}
	@Override
	public List<OrdOrderItem> getOrderOrderItemSuppGoodsByParam(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordOrderItemDao.getOrderOrderItemSuppGoodsByParam(params);
	}
	@Override
	public int updateNotInTimeFlag(Map<String, Object> params) {
		return ordOrderItemDao.updateNotInTimeFlag(params);
	}

	@Override
	public int updatePreRefundStatus(Map<String, Object> params) {
		return orderDao.updatePreRefundStatus(params);
	}
	@Override
	public int updateOrderAndItemPaymentInfo(Long orderId, Long bonusAmount, String paymentStatus, Date paymentTime,
											 String paymentType) {
		LOG.info("updateOrderAndItemPaymentInfo#orderId:"+orderId+",bonusAmount:"+bonusAmount+",paymentStatus:"+paymentStatus+",paymentTime:"+paymentTime);
		if (StringUtils.equals(OrderEnum.PAYMENT_STATUS.PAYED.name(), paymentStatus)) {
			updateOrderItemPaymentStatus(orderId);
		}
		LOG.info("updateOrderAndItemPaymentInfo#updateOrderItem end:"+orderId);
		int result = orderDao.updateOrderPaymentInfo(orderId, bonusAmount, paymentStatus, paymentTime,
				paymentType);
		return result;
	}

	public void updateOrderItemPaymentStatus(Long orderId) {
		List<OrdOrderItem> list = ordOrderItemDao.selectByOrderId(orderId);
		for(OrdOrderItem orderItem:list){
			if(OrderEnum.ORDER_STATUS.NORMAL.name().equalsIgnoreCase(orderItem.getOrderStatus())){
				OrdOrderItem item = new OrdOrderItem();
				item.setOrderItemId(orderItem.getOrderItemId());
				item.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
				ordOrderItemDao.updateByOrderItemIdSelective(item);
			}
		}
	}

	@Override
	public List<OrdOrderInsurance> getInsuranceOrderByOrderIdList(List<Long> orderIdList) {
		List<OrdOrderInsurance> orderList = null;
		if (orderIdList != null && orderIdList.size() > 0) {
			orderList = orderDao.getInsuranceOrderByOrderIdList(orderIdList);
		}

		return orderList;
	}
	/**
	 * 更新子订单退款信息
	 * @param orderItemList
	 */
	@Override
	public void updateOrderItemRefundQutityAndPrice(List<OrdOrderItem> orderItemList) {
		for(OrdOrderItem orderItem : orderItemList){
			if(orderItem.getOrderItemId()!=null ){
				LOG.info("orderItemId="+orderItem.getOrderItemId()+",refundPrice="+orderItem.getRefundPrice()+",refundQuantity="+orderItem.getRefundQuantity());
				ordOrderItemDao.updateOrderItemRefundQutityAndPrice(orderItem);
			}
		}
	}
	@Override
	public int addPayPromotionAmount(Long orderId, Long payPromotionAmount) {
		if(payPromotionAmount == null) {
			return 0;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("payPromotionAmount", payPromotionAmount);

		return orderDao.addPayPromotionAmount(params);
	}


	@Override
	public String updateOrderForBuyInfo(OrdOrder orderVst, DestBuBuyInfo buyInfo) {
		LOG.info("OrdOrderUpdateServiceImpl#updateOrderForBuyInfo  OrdOrder==" + 
				com.alibaba.fastjson.JSONObject.toJSONString(orderVst) + "DestBuBuyInfo==" + 
				com.alibaba.fastjson.JSONObject.toJSONString(buyInfo));


		long cashPayAmt=0L;//现金账户支付
		long bonusPayAmt=0L;//奖金账户支付
		long couponPayAmt=0L;//优惠券优惠金额
		long giftCardPayAmt=0L;//礼品卡支付金额
		long storePayAmt=0L;//储值卡支付金额
		Long bonusActualAmt = null;
//		Long commissionAmount=buyInfo.getCommissionAmountHidden();//店员专享优惠


		//if (commissionAmount!=null&&commissionAmount>0) {
		//		insertAmountItem(orderVst.getOrderId(),commissionAmount,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_O2OCHANEL.name(),OrderEnum.ORDER_AMOUNT_TYPE.O2OCHANEL_AMOUNT.name());
		//	}else{
		//		commissionAmount=0l;
		//	}
		int size = buyInfo.getUserCouponVoList().size();
		LOG.info("==========cm============updateOrderForBuyInfo UserCouponVoList size:"+size);
		String erroFlag="";
		if (CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())) {
			List<String> usedCouponList=new ArrayList<String>();
			for(UserCouponVO c:buyInfo.getUserCouponVoList()){
				if("B".equals(c.getCouponType())){
					usedCouponList.add(c.getCouponCode());
				}

				couponPayAmt+=c.getDiscountAmount();
			}
			if (couponPayAmt>0) {
				insertAmountItem(orderVst.getOrderId(),couponPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_COUPON.name(),OrderEnum.ORDER_AMOUNT_TYPE.COUPON_AMOUNT.name());
				if(usedCouponList.size()>0){
					try {
						int usedResult=favorServiceAdapter.updateMarkCouponCodeUsed(usedCouponList, true);
						if (!(usedResult>0)) {
							erroFlag="优惠券扣减失败";
							return erroFlag;
						}
					} catch (Exception e) {
						erroFlag="优惠券扣减失败";
						return erroFlag;
					}

				}
				orderVst.setCouponAmount(couponPayAmt);
			}
			LOG.info("---------------------------------------------优惠券集合 UserCouponVoList:"+JSONArray.fromObject(buyInfo.getUserCouponVoList()));
			LOG.info("---------------------------------------------订单ID，orderId"+	orderVst.getOrderId());
			favorServiceAdapter.insertMarkCouponUsages(buyInfo.getUserCouponVoList(), orderVst.getOrderId(),"VST_ORDER",
                    buyInfo.getIp(), orderVst.getOrderDfp());

		}
		LOG.info("==========cm============updateOrderForBuyInfo couponPayAmt :"+couponPayAmt);
		// 更新订单应付金额    为避免和支付系统回调金额交叉记录，将更新应付金额提前
		Long oughtAmount = orderVst.getOughtAmount() -couponPayAmt;
		if (oughtAmount<=0) {
			oughtAmount=0L;
		}
		orderVst.setOughtAmount(oughtAmount);//避免零元支付时不是已收款
		orderDao.updateOrder(orderVst);//为避免覆盖更新回调时的收款信息此处使用updateByPrimaryKeySelective方式

		//根据用户NO获取奖金账户和现金账户信息
		LOG.info("vstPayFromMoneyAccount start buyInfo:"+buyInfo);
		if( buyInfo.getCashAmountHidden()!=null && buyInfo.getCashAmountHidden().intValue()>0){
			try {
				//boolean monyResult=ordUserOrderServiceAdapter.vstPayFromMoneyAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserNo(), orderVst.getOrderId(), buyInfo.getCashAmountHidden());
				boolean monyResult=ordUserOrderServiceAdapter.vstPayFromMoneyAccountNew(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserNo(), orderVst.getOrderId(), buyInfo.getCashAmountHidden());
				LOG.info("monyResult:"+monyResult);
				if (!monyResult) {
					erroFlag="现金账户扣减失败";
					return erroFlag;
				}
				cashPayAmt+=buyInfo.getCashAmountHidden();
				insertAmountItem(orderVst.getOrderId(),cashPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_CASH.name(),OrderEnum.ORDER_AMOUNT_TYPE.CASH_AMOUNT.name());
			} catch (Exception e) {
				LOG.info("vstPayFromMoneyAccountNew:现金账户扣减失败");
				erroFlag="现金账户扣减失败";
				return erroFlag;
			}
		}

		//奖金支付（老接口不能用暂时判断改版前是否有使用，避免重复扣）
		if(orderVst.getBonusAmount()>0){
			try {
				boolean bonusResult=false;
				LOG.info("------------------------------奖金扣款传参,userId:"+buyInfo.getUserId()+"OrderId"+orderVst.getOrderId()+"BonusAmount"+orderVst.getBonusAmount());
				String reasult=bonusPayService.payFromBonusForVstOrder(buyInfo.getUserId(), orderVst.getOrderId(), orderVst.getBonusAmount());
				LOG.info("------------------------------奖金接口返回,reasult:"+reasult);
				//继续使用老接口
				JSONObject o = JSONObject.fromObject(reasult);
				bonusResult="true".equals(o.getString("success"));
				//boolean bonusResult=ordUserOrderServiceAdapter.vstPayFromBonusAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(),orderVst.getOrderId(), buyInfo.getUserNo(),orderVst.getBonusAmount());
				if (!bonusResult) {
					erroFlag="奖金账户扣减失败";
					return erroFlag;
				}
				bonusPayAmt+=orderVst.getBonusAmount();
				insertAmountItem(orderVst.getOrderId(),bonusPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_BONUS.name(),OrderEnum.ORDER_AMOUNT_TYPE.BONUS_AMOUNT.name());
			} catch (Exception e) {
				erroFlag="奖金账户扣减失败";
				return erroFlag;
			}
		}
		//千万不要和上面代码交换顺序 因为此方法中有设置  				orderVst.setBonusAmount(bonusPayAmt);  
		if(buyInfo.getBonusAmountHidden()!=null&& buyInfo.getBonusAmountHidden().intValue()>0){
			try {
				boolean bonusResult=false;
				LOG.info("------------------------------奖金扣款传参,userId:"+buyInfo.getUserId()+"OrderId"+orderVst.getOrderId()+"BonusAmount"+buyInfo.getBonusAmountHidden());
				String reasult=bonusPayService.payFromBonusForVstOrder(buyInfo.getUserId(), orderVst.getOrderId(),buyInfo.getBonusAmountHidden());
				//继续使用老接口
				LOG.info("------------------------------奖金接口返回,reasult:"+reasult);
				JSONObject o = JSONObject.fromObject(reasult);
				bonusResult="true".equals(o.getString("success"));
				//boolean bonusResult=ordUserOrderServiceAdapter.vstPayFromBonusAccount(OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(),orderVst.getOrderId(), buyInfo.getUserNo(),buyInfo.getBonusAmountHidden());
				if (!bonusResult) {
					erroFlag="奖金账户扣减失败";
					return erroFlag;
				}
				bonusPayAmt+=buyInfo.getBonusAmountHidden();
				insertAmountItem(orderVst.getOrderId(),bonusPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_BONUS.name(),OrderEnum.ORDER_AMOUNT_TYPE.BONUS_AMOUNT.name());
				orderVst.setBonusAmount(bonusPayAmt);
				bonusActualAmt = bonusPayAmt;
			} catch (Exception e) {
				erroFlag="奖金账户扣减失败";
				return erroFlag;
			}
		}

		if(CollectionUtils.isNotEmpty(buyInfo.getGiftCardList())){
			LOG.info("####使用礼品卡支付DestBuBuyInfo start####");
			LOG.info("####礼品卡信息####" + com.alibaba.fastjson.JSONObject.toJSONString(buyInfo.getGiftCardList()));
			Map<String, Long> map =new HashMap<String, Long>();
			for(CardInfo c:buyInfo.getGiftCardList()){
				map.put(c.getCardNo(),c.getUseAmt());
				giftCardPayAmt+=c.getUseAmt();
			}
			try {
				LOG.info("####PARAM GiftCard Info####" + com.alibaba.fastjson.JSONObject.toJSONString(map));
				LOG.info("####IP####" + buyInfo.getIp());
				LOG.info("####OrderId####" + orderVst.getOrderId());
				LOG.info("####OrderDfp####" + orderVst.getOrderDfp());
				LOG.info("####giftCardPayAmt####" + giftCardPayAmt);
				String orderDfp = orderVst.getOrderDfp() == null ? "" : orderVst.getOrderDfp();
				boolean giftCardReasult=payPaymentServiceAdapter.reduceStoreCardsAmt(map, "1", orderVst.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserId(),
						buyInfo.getIp(), orderDfp);
				LOG.info("orderId###" + orderVst.getOrderId() + "###invoke reduceStoreCardsAmt,return giftCardReasult===" + giftCardReasult);
				if (!giftCardReasult) {
					erroFlag="礼品卡扣减失败";
					return erroFlag;
				}
				if(giftCardPayAmt >0){
					insertAmountItem(orderVst.getOrderId(),giftCardPayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_GIFTCARD.name(),OrderEnum.ORDER_AMOUNT_TYPE.GIFTCARD_AMOUNT.name());
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				erroFlag="礼品卡扣减失败";
				return erroFlag;

			}
		}

		if(CollectionUtils.isNotEmpty(buyInfo.getStoreCardList())){
			LOG.info("####使用储值卡支付buyInfo start####");
			LOG.info("####储值卡信息####" + com.alibaba.fastjson.JSONObject.toJSONString(buyInfo.getStoreCardList()));
			Map<String, Long> map =new HashMap<String, Long>();
			for(CardInfo c:buyInfo.getStoreCardList()){
				map.put(c.getCardNo(),c.getUseAmt());
				storePayAmt+=c.getUseAmt();
			}
			try {
				LOG.info("####PARAM StoreCard Info####" + com.alibaba.fastjson.JSONObject.toJSONString(map));
				LOG.info("####IP####" + buyInfo.getIp());
				LOG.info("####OrderId####" + orderVst.getOrderId());
				LOG.info("####OrderDfp####" + orderVst.getOrderDfp());
				LOG.info("####storePayAmt####" + storePayAmt);
				String orderDfp = orderVst.getOrderDfp() == null ? "" : orderVst.getOrderDfp();
				boolean StoreReasult=payPaymentServiceAdapter.reduceStoreCardsAmt(map, "0", orderVst.getOrderId(), OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name(), buyInfo.getUserId(),
						buyInfo.getIp(), orderDfp);
				LOG.info("orderId###" + orderVst.getOrderId() + "###invoke reduceStoreCardsAmt,return StoreReasult===" + StoreReasult);
				if (!StoreReasult) {
					erroFlag="储值卡扣减失败";
					return erroFlag;
				}
				if(storePayAmt >0 ){
					insertAmountItem(orderVst.getOrderId(),storePayAmt,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_STORECARD.name(),OrderEnum.ORDER_AMOUNT_TYPE.STORECARD_AMOUNT.name());
				}


			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				erroFlag="储值卡扣减失败";
				return erroFlag;

			}
		}

		//注释此处，已收款金额应由支付系统扣款后进行回调 故注释此处代码  by 李志强   2015-10-21
	/*	if((cashPayAmt+giftCardPayAmt+storePayAmt)>0){
			orderVst.setActualAmount(cashPayAmt+giftCardPayAmt+storePayAmt);
		}*/
	/*	if(orderVst.hasFullPayment()){
			orderVst.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
				orderVst.setPaymentTime(new Date());
		}else if(orderVst.getActualAmount()>0){
			orderVst.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PART_PAY.name());
				orderVst.setPaymentTime(new Date());
		}*/

		OrdOrder order=new OrdOrder();
		order.setOrderId(orderVst.getOrderId());
		if(bonusActualAmt!=null ){
			//Long amount = orderVst.getBonusAmount();
			order.setBonusAmount(bonusActualAmt);
		}
		order.setOughtAmount(oughtAmount);
		orderDao.updateOrder(order);//为避免覆盖更新回调时的收款信息此处使用updateByPrimaryKeySelective方式
		return erroFlag;

	}

	@Override
	public List<OrdOrderSharedStock> getOrderShareStockByParams(
			Map<String, Object> param) {
		return ordOrderSharedStockDao.selectByParams(param);
	}

	@Override
	public int updateOrdOrderStock(OrdOrderStock ordOrderStock) {

		return ordOrderStockDao.updateByPrimaryKeySelective(ordOrderStock);
	}

	@Override
	public OrdOrder updatePaymentSuccessInfo(PayPayment payment){
		//记录支付信息
		OrdPaymentInfo ordPaymentInfo = new OrdPaymentInfo();
		ordPaymentInfo.setOrderId(payment.getObjectId());
		ordPaymentInfo.setPaymentId(payment.getPaymentId());
		int result = ordPaymentInfoService.addOrdPaymentInfo(ordPaymentInfo);
		LOG.info("ordPaymentInfoService.addOrdPaymentInfo__" + "orderId=" + payment.getObjectId() + "result =" + result);
		if (result == 1) {
			OrdOrder order = this.queryOrdOrderByOrderId(payment.getObjectId());
			if(order == null){
				return null;
			}
			int updateCount=this.addOrderActualAmount(payment.getObjectId(), payment.getAmount());
			if(payment.getPromotionAmount()!=null){
				LOG.info("add payPromotion amount  orderId"+ payment.getObjectId());
				int updatePayCount = this.addPayPromotionAmount(payment.getObjectId(), payment.getPromotionAmount());

			}
			LOG.info("orderUpdateService.addOrderActualAmount__" + "orderId=" + payment.getObjectId() + "updateCount =" + updateCount);
			order = this.queryOrdOrderByOrderId(payment.getObjectId());
			LOG.info("orderUpdateService.queryOrdOrderByOrderId__" + "orderId=" + order.getOrderId() + "OrderActualAmount =" + order.getActualAmount() + "orderSubType=" + order.getOrderSubType());
			if(Constant.PAYMENT_GATEWAY.CASH_BONUS.name().equals(payment.getPaymentGateway())
					&&(order.getBonusAmount()==null||order.getBonusAmount() == 0)){
				order.setBonusAmount(payment.getAmount());
			}
			if(order.getPayPromotionAmount()!=null){
				if(order.hasPayPromtionFullPayment()){
					order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
				/*	order.setViewOrderStatus(viewOrderStatus)*/
				}else if(order.getActualAmount()>0){
					order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PART_PAY.name());
				}
			}else{
				if(order.hasFullPayment()){
					order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
				}else if(order.getActualAmount()>0){
					order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PART_PAY.name());
				}
			}
			order.setPaymentTime(new Date());

			// 目的地BU前台订单,重设支付类型
			if(OrdOrderUtils.isDestBuFrontOrder(order)){
				// OrderEnum.PAYMENT_TYPE.PRE_PAY.name() --> SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name()
				if(OrderEnum.PAYMENT_TYPE.PRE_PAY.name().equals(payment.getPaymentType())){
					order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
				}
				// OrderEnum.PAYMENT_TYPE.PAY.name() --> SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name()
				else{
					order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name());
				}
			}
			String lockKey = "updateOrderAndItemPaymentInfo"+order.getOrderId();
			boolean isLock = MemcachedUtil.getInstance().tryLock(lockKey,3,10);
			if (isLock) {
				LOG.info("orderUpdateService.updateOrderAndChangeOrderItemPayment__Start" + "orderId=" + order.getOrderId()
						+ "OrderActualAmount =" + order.getActualAmount()+"setPaymentTime"+order.getPaymentTime()
						+"setPaymentState"+order.getPaymentStatus()+"setPaymentType"+order.getPaymentType());
				int resUpdate = this.updateOrderAndItemPaymentInfo(order.getOrderId(),
						order.getBonusAmount(), order.getPaymentStatus(), order.getPaymentTime(),
						order.getPaymentType());
				LOG.info("orderUpdateService.updateOrderAndChangeOrderItemPayment__EDN:resUpdateNum" +resUpdate+ "orderId=" + order.getOrderId() + "OrderActualAmount ="
						+ order.getActualAmount()+"setPaymentTime"+order.getPaymentTime()+"setPaymentState"+order.getPaymentStatus()+"setPaymentType"+order.getPaymentType());
			}
			MemcachedUtil.getInstance().remove(lockKey);
			return order;
		}
		return null;
	}

	@Override
	public int updateOrderItemActSettlement(Map<String, Object> params) {
		return ordOrderItemDao.updateOrderItemActSettlement(params);
	}

    @Override
    public ResultHandleT<String> updateOrdTravellers(OrdOrder order, OrdOrderPersonVO vo) {
        //身份证输入规范中的字母为大写，所以在此统一转为大写
        if(vo != null){
            List<OrdPerson> travellers = vo.getTravellers();
            if(travellers != null && travellers.size() > 0){
                for(OrdPerson person : travellers){
                    if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
                        person.setIdNo(person.getIdNo().toUpperCase());
                    }
                }
            }
        }
        ResultHandleT<String> handle = new ResultHandleT<String>();
        StringBuffer sb =new StringBuffer();
        List<OrdPerson> travellerList = order.getOrdTravellerList();
        Map<Long,OrdPerson> personMap = new HashMap<Long, OrdPerson>();
        for(OrdPerson op:travellerList){
            personMap.put(op.getOrdPersonId(), op);
        }

        List<OrdPerson> updatePersonList = new ArrayList<OrdPerson>();
        boolean flag=false;
        StringBuffer travellerPerson = new StringBuffer();
        for(OrdPerson op:vo.getTravellers()){
            if(!personMap.containsKey(op.getOrdPersonId())){
                throw new IllegalArgumentException("不存在对应的游玩人");
            }
            OrdPerson oldPerson = personMap.get(op.getOrdPersonId());
            if(comparePerson(oldPerson, op)){
                flag=true;
                updatePersonList.add(oldPerson);
                travellerPerson.append("_");
                travellerPerson.append(oldPerson.getOrdPersonId());

            }
            if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
                OrderUtils.resetPersonInfo(oldPerson);
            }
        }
        if(flag){
            sb.append(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
            sb.append(travellerPerson.toString());
            sb.append(",");
            flag=false;
        }

        if(order.getContactPerson()!=null){
            if(comparePerson(order.getContactPerson(), vo.getContact())){
                sb.append(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
                sb.append(",");
                updatePersonList.add(order.getContactPerson());
            }
        }
        if(vo.getEmergencyPerson()!=null){
            OrdPerson op = getEmergencyPerson(order.getOrdPersonList());
            if(op==null){
                op=new OrdPerson();
                op.setObjectId(order.getOrderId());
                op.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
                op.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
            }

            if(comparePerson(op, vo.getEmergencyPerson())){
                sb.append(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
                sb.append(",");
                //order.getContactPerson()
                updatePersonList.add(op);
            }
        }
        for(OrdPerson op:updatePersonList){
            if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
                if(!OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name().equals(op.getIdType())
                        &&!OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name().equals(op.getIdType())){
                    op.setIssued("");
                    op.setExpDate(null);
                }
            }

            if (op.getOrdPersonId()!=null) {
                ordPersonDao.updateByPrimaryKeySelective(op);
            }
        }
        //为游玩人时，更新签证审核中的游客名称
        try {
            for(OrdPerson op:updatePersonList){
                if(ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(op.getPersonType())){
                    if(order.getCategoryId()!=null && OrderUtil.showVisaApprovalFlag(order)){
                        updateVisaApprovalTraveller(order, op);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("orderId:"+order.getOrderId()+"updateVisaApprovalTraveller error:"+e);
        }
        if(sb.length()>0){
            sb.setLength(sb.length()-1);
            handle.setReturnContent(sb.toString());
        }
        return handle;
    }

	@Override
	public ResultHandleT<String> updateOrdInsurers(OrdOrder order, OrdOrderPersonVO vo) {
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(vo != null){
			List<OrdPerson> insurers = vo.getInsurers();
			if (insurers != null && insurers.size() > 0) {
				for (OrdPerson person : insurers) {
					if (person != null && !StringUtil.isEmptyString(person.getIdNo())) {
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		ResultHandleT<String> handle = new ResultHandleT<String>();
		StringBuffer sb = new StringBuffer();
		List<OrdPerson> ordPersonList = order.getOrdPersonList();
		List<OrdPerson> insurerList = new ArrayList<OrdPerson>();
		for (OrdPerson op : ordPersonList) {
			if (OrderEnum.ORDER_PERSON_TYPE.INSURER.name().equals(op.getPersonType())) {
				insurerList.add(op);
			}
		}


		Map<Long,OrdPerson> personMap = new HashMap<Long, OrdPerson>();
		if (null != insurerList && insurerList.size() > 0) {
			for (OrdPerson op : insurerList) {
				personMap.put(op.getOrdPersonId(), op);
			}
		}

		List<OrdPerson> updatePersonList = new ArrayList<OrdPerson>();
		boolean flag = false;
		StringBuffer travellerPerson = new StringBuffer();
		for (OrdPerson op : vo.getInsurers()) {
			if (!personMap.containsKey(op.getOrdPersonId())) {
				throw new IllegalArgumentException("不存在对应的游玩人");
			}
			OrdPerson oldPerson = personMap.get(op.getOrdPersonId());
			if (comparePerson(oldPerson, op)) {
				flag=true;
				updatePersonList.add(oldPerson);
				travellerPerson.append("_");
				travellerPerson.append(oldPerson.getOrdPersonId());
			}
            if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
                OrderUtils.resetPersonInfo(oldPerson);
            }
        }
        if(flag){
            sb.append(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
            sb.append(travellerPerson.toString());
            sb.append(",");
            flag=false;
        }

//        if(order.getContactPerson()!=null){
//            if(comparePerson(order.getContactPerson(), vo.getContact())){
//                sb.append(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
//                sb.append(",");
//                updatePersonList.add(order.getContactPerson());
//            }
//        }
//        if(vo.getEmergencyPerson()!=null){
//            OrdPerson op = getEmergencyPerson(order.getOrdPersonList());
//            if(op==null){
//                op=new OrdPerson();
//                op.setObjectId(order.getOrderId());
//                op.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
//                op.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
//            }
//
//            if(comparePerson(op, vo.getEmergencyPerson())){
//                sb.append(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
//                sb.append(",");
//                //order.getContactPerson()
//                updatePersonList.add(op);
//            }
//        }
        for(OrdPerson op:updatePersonList){
            if (order.getDistributorId().intValue()!=10) {//因原有逻辑在此处理身份证或客服联系我时清空生日和性别 门店不受此逻辑
                if(!OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name().equals(op.getIdType())
                        &&!OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name().equals(op.getIdType())){
                    op.setIssued("");
                    op.setExpDate(null);
                }
            }

            if (op.getOrdPersonId()!=null) {
                //ordPersonDao.updateByPrimaryKeySelective(op);
                ordPersonDao.updateByPrimaryKey(op);
            }
        }
        //为游玩人时，更新签证审核中的游客名称
        try {
            for(OrdPerson op:updatePersonList){
                if(ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(op.getPersonType())){
                    if(order.getCategoryId()!=null && OrderUtil.showVisaApprovalFlag(order)){
                        updateVisaApprovalTraveller(order, op);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("orderId:"+order.getOrderId()+"updateVisaApprovalTraveller error:"+e);
        }
        if(sb.length()>0){
            sb.setLength(sb.length()-1);
            handle.setReturnContent(sb.toString());
        }
        return handle;
    }
}
