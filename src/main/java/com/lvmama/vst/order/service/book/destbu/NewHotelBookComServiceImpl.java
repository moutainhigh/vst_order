package com.lvmama.vst.order.service.book.destbu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombProdAdditionService;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prom.po.PromForbidKeyPo;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Coupon;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderTrackingService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.OrderSaveService;

import com.lvmama.vst.order.timeprice.service.impl.NewOrderHotelCompTimePriceServiceImpl;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.VstOrderUserRefundServiceAdapter;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Component("newHotelBookComService")
public class NewHotelBookComServiceImpl extends DestBuHotelBookServiceImpl {
     @Autowired
	private NewHotelComOrderSaveService newHotelCombOrderSaveService;

	private static final Logger LOG = LoggerFactory
			.getLogger(NewHotelBookComServiceImpl.class);
	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderService;
	@Autowired
	private ResPreControlService resControlBudgetRemote;
	@Autowired
	private NewHotelComOrderBussiness newHotelComOrderBussiness;
	@Autowired
	private OrdOrderStockDao orderStockDao;
	@Autowired
	private NewHotelComOrderInitService newHotelComOrderInitService;
	@Autowired
	private NewOrderHotelCompTimePriceServiceImpl orderHotelComp2HotelTimePriceService;
	@Autowired
	private INewHotelCombTimePriceService newHotelCombTimePriceClientRemote;

	@Autowired
	private ComActivitiRelationService comActivitiRelationService;

	private static ConcurrentMap<String, Long> bookUniqueMap = new ConcurrentHashMap<String, Long>();

	@Autowired
	private OrderSaveService orderSaveService;

	@Autowired
	private IOrdOrderTrackingService ordOrderTrackingService;

	@Autowired
	protected IComplexQueryService complexQueryService;

	@Autowired
	private ProcesserClientService processerClientService;
  
	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;

	@Resource(name = "orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;

	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
    private VstOrderUserRefundServiceAdapter vstOrderRefundServiceRemote;
	
	@Autowired
	private OrderTicketNoTimePriceServiceImpl orderTicketNoTimePriceServiceImpl;
	
	@Autowired
	private INewHotelCombProdAdditionService newHotelCombProdAdditionClientRemote;

	public ResultHandleT<OrdOrder> createOrder(final DestBuBuyInfo buyInfo,
			final String operatorId) {
		LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------createOrder------start"+buyInfo.toJsonStr());
		// 身份证输入规范中的字母为大写，所以在此统一转为大写
		if (buyInfo != null) {
			List<Person> travellers = buyInfo.getTravellers();
			if (travellers != null && travellers.size() > 0) {
				for (Person person : travellers) {
					if (person != null
							&& !StringUtil.isEmptyString(person.getIdNo())) {
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		ResultHandleT<OrdOrder> handle = null;
		boolean needClearToken = true;
		PromForbidKeyPo promForbidBuyOrder = null;
		LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------isPromForbidBuyOrder------start");
		if (newHotelComOrderBussiness != null) {
			promForbidBuyOrder = newHotelComOrderBussiness
					.isPromForbidBuyOrder(buyInfo);

 			if (promForbidBuyOrder != null) {

				if (promForbidBuyOrder.isIsforbid()) {
					handle = new ResultHandleT<OrdOrder>();
					if (StringUtils.isNotBlank(promForbidBuyOrder.getMsg())) {
						handle.setMsg(promForbidBuyOrder.getMsg());
						return handle;
					}
					handle.setMsg("抱歉，您的订单不满足限购要求，下单失败！");
					return handle;
				}
			}
		} else {
			LOG.info("is null forbid buy");
		}
		LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------isPromForbidBuyOrder------end");
		Long startCreateOrderTime = System.currentTimeMillis();
		Long startTime = null;
		String methodName = "DestBuHotelBookServiceImpl#createOrder【"
				+ buyInfo.getProductId() + "】";
		LOG.info("start create order...");
		// 计算token ，防止订单重复提交
		String key = getBuyInfoHashCode(buyInfo);
		long time = getTime();
		if (bookUniqueMap.putIfAbsent(key, time) != null) {
			needClearToken = false;
			throw new BusinessException(
					OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CREATE_ORDER
							.getErrorCode(),
					"重复创建订单");
		}

		try {

			// 检查库存
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------calOrderStock------start");
			startTime = System.currentTimeMillis();
			ResultHandle stockHandle = this.calOrderStock(buyInfo, operatorId);
			Log.info(ComLogUtil.printTraceInfo(methodName, "库存检查",
					"this.calOrderStock", System.currentTimeMillis() - startTime));
	
			if (stockHandle.isFail()) {
				String stockHandleErrorCode = stockHandle.getErrorCode();

				if (StringUtil.isNotEmptyString(stockHandleErrorCode)) {
					handle = new ResultHandleT<OrdOrder>();
					handle.setErrorCode(stockHandleErrorCode);
					handle.setMsg(stockHandle.getMsg());		
					return handle;
				}
				throw new IllegalArgumentException(stockHandle.getMsg());
			}
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------calOrderStock------end");
			// 初始化order并各种计算
			OrdOrderDTO order = new OrdOrderDTO();
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------initOrderAndCalc------start");

			newHotelComOrderInitService.initOrderAndCalc(buyInfo, order);
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------initOrderAndCalc------end");

			Coupon coupon = null;
			String code = null;
			boolean useCoupon = CollectionUtils.isNotEmpty(buyInfo
					.getCouponList());
			String youhuiType = buyInfo.getYouhui();
			/************************* 新版支付页优惠券、现金、奖金、礼品卡、储值卡 *******/
			startTime = System.currentTimeMillis();
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------chechOrderPayForOther------start");
			String checkResult = this.chechOrderPayForOther(buyInfo, order);
	
			LOG.info("==========cm============checkResult:" + checkResult);
			Log.info(ComLogUtil.printTraceInfo(methodName,
					"新版支付页优惠券、现金、奖金、礼品卡、储值卡验证开始", "this.checkOrderForPayOther",
					System.currentTimeMillis() - startTime));

			if (checkResult != "") {
				throw new IllegalArgumentException(checkResult);
			}
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------chechOrderPayForOther------end");

			/************************* 新版支付页优惠券、现金、奖金、礼品卡、储值卡验证结束 ******/
			LOG.info("==========cm====start========youhuiType:" + youhuiType);
			
			
			if (StringUtils.isNotEmpty(youhuiType)) {
				Long startCountPriceTime = System.currentTimeMillis();
				PriceInfo info = this.calOrderPrice(order, buyInfo);
				Log.info(ComLogUtil.printTraceInfo(methodName, "价格计算",
						"orderPriceService.countPrice",
						System.currentTimeMillis() - startCountPriceTime));

				if (ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)) {
					if (useCoupon) {
						coupon = buyInfo.getCouponList().get(0);
						code = coupon.getCode();
						LOG.info("==========cm============useCoupon code:"
								+ code);
						if (!StringUtils.isEmpty(code)) {// 有优惠劵
							if (!info.getCouponResutHandles().isEmpty()) {
								List<ResultHandle> rs = info
										.getCouponResutHandles();
								StringBuilder str = new StringBuilder("优惠券使用异常");
								for (ResultHandle resultHandle : rs) {
									if (null == resultHandle) {
										continue;
									}
									String msg = resultHandle.getMsg();
									str.append("[").append(msg).append("]");
								}
								throw new IllegalArgumentException(
										str.toString());
							}
						}

					}
				}

				if (ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)) {
					Float userBonus = buyInfo.getBonusYuan();
					if (userBonus != null && userBonus > 0) {

						// 最大可抵扣数
						Long maxBonus = info.getMaxBonus();
						Float maxBonusYuan = PriceUtil.convertToYuan(maxBonus);
						if (userBonus > maxBonusYuan) {
							throw new IllegalArgumentException("超过最高奖金抵扣数");
						}
					}
				}
			}

			Long startBookingCreateOrderTime = System.currentTimeMillis();
			LOG.info("==========cm=========end===youhuiType:" + youhuiType);
			// ---------------------------------
			// 判断购买人和紧急联系人包含游玩人名称中是否包含“测试下单”关键字，包含测试关键字时，将该订单标识为测试订单，标识设置成Y。默认是N
			// 获取测试订单
			// 得到定义的订单测试的常量
			char isTest = 'N';
			try {
				isTest = buyInfo.getIsTestOrder();
			} catch (NullPointerException e) {

			}
			if (isTest != 'Y') {
				String orderValue = Constant.getInstance().getOrderValue();
				LOG.info("静态变量的值" + orderValue);
				String contactName = "";
				String emergencyPersonName = "";
				try {
					contactName = buyInfo.getContact().getFullName();
				} catch (NullPointerException e) {
				}
				try {
					emergencyPersonName = buyInfo.getEmergencyPerson()
							.getFullName();
				} catch (NullPointerException e) {

				}
				// 如果购买人和联系人都不包含测试下单，那么就在游玩人中查找是否包含。直接包含直接设置成测试订单的标记。
				if (contactName.contains(orderValue)
						|| emergencyPersonName.contains(orderValue)) {
					buyInfo.setIsTestOrder('Y');
				} else {
					try {
						List<Person> travellers = buyInfo.getTravellers();
						for (Person person : travellers) {
							if (person.getFullName().contains(orderValue)) {
								buyInfo.setIsTestOrder('Y');
								break;
							}
						}
					} catch (NullPointerException e) {
					}
				}
			}
			// 计算

			boolean isOrderCreated = false;
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------saveOrder------start");
			saveOrder(order);
			LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------saveOrder------end");
			if(order.getOrderId()!=null){
				handle = new ResultHandleT<OrdOrder>();
				handle.setReturnContent(order);
			}
			

			LOG.info("createOrder设置了istsestorderbuyInfo的itsest值"
					+ buyInfo.getIsTestOrder() + "值在中间");
			if (handle == null) {
				handle = new ResultHandleT<OrdOrder>();
				handle.setMsg("生成订单失败");
				LOG.info("NewHotelBookComServiceImplNewHotelBookComServiceImpl------saveOrder------fail");
			}
			if(order.getDistributorId() != Constant.DIST_BACK_END){
				order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
				order.setCancelCode("");
				order.setReason("");
				try{
					orderSaveService.resetOrderToNormal(order.getOrderId());
				}catch(Exception e){
					LOG.info("orderSaveService.resetOrderToNormal异常"+e.getMessage());
				}
				
			} 

			if (handle != null && handle.isSuccess() && !handle.hasNull()) {
				isOrderCreated = true;
				LOG.info("OrdOrderClientServiceImpl.createOrder: bookService.createOrder,orderId="
						+ handle.getReturnContent().getOrderId());
				startTime = System.currentTimeMillis();
				OrdOrder ordorder = queryOrdorderByOrderId(handle
						.getReturnContent().getOrderId());
				Log.info(ComLogUtil.printTraceInfo(methodName, "查询单个订单详情",
						"this.queryOrdorderByOrderId",
						System.currentTimeMillis() - startTime));
				if (!ordorder.hasCanceled()) {

					LOG.info("OrdOrderClientServiceImpl.hasCanceled: ============="
							+ ordorder.hasCanceled());
					List<OrdOrderItem> listItem = new ArrayList<OrdOrderItem>();
					OrdOrderItem item = null;
					for (OrdOrderItem ordOrderItem : handle.getReturnContent()
							.getOrderItemList()) {
						item = new OrdOrderItem();
						BeanUtils.copyProperties(ordOrderItem, item);
						listItem.add(item);

					}
					handle.setReturnContent(ordorder);

					if (useCoupon
							&& handle.getReturnContent().hasNeedPrepaid()
							&& coupon != null
							&& !StringUtils.isEmpty(code)
							&& ORDER_FAVORABLE_TYPE.coupon.getCode().equals(
									youhuiType)) {
						// fillOrderData(order, buyInfo);
						// 老的下单页使用优惠券的，后续等新版全部上线后删除
						startTime = System.currentTimeMillis();
						orderUpdateService.updateOrderUsedFavor(ordorder, code);
						Log.info(ComLogUtil.printTraceInfo(methodName,
								"订单优惠劵使用后 依据订单优惠策略算出优惠金额，操作订单相关操作",
								"orderUpdateService.updateOrderUsedFavor",
								System.currentTimeMillis() - startTime));

					}
					// 新优惠券 奖金、现金、礼品卡、储值卡使用修改订单应付金额
					startTime = System.currentTimeMillis();
					String resultStr = orderUpdateService
							.updateOrderForBuyInfo(ordorder, buyInfo);
					Log.info(ComLogUtil.printTraceInfo(methodName,
							"新优惠券 奖金、现金、礼品卡、储值卡使用修改订单应付金额",
							"orderUpdateService.updateOrderForBuyInfo",
							System.currentTimeMillis() - startTime));

					if (StringUtil.isNotEmptyString(resultStr)) {
						cancelOrder(ordorder.getOrderId(),
								OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name(),
								resultStr + "废单", "SYSTEM", "");
						handle.setMsg("下单失败：" + resultStr);
						isOrderCreated = false;
					}
					// Thread.sleep(10*1000);
					LOG.info("OrdOrderClientServiceImpl.createOrder(orderID="
							+ handle.getReturnContent().getOrderId()
							+ "): send OrderCreateMessage");
					startTime = System.currentTimeMillis();

					try {
						orderMessageProducer.sendMsg(MessageFactory
								.newOrderCreateMessage(handle
										.getReturnContent().getOrderId()));
					} catch (Exception ex) {
						LOG.info("sendMsg Error", ex);
					}
					Log.info(ComLogUtil.printTraceInfo(methodName, "推送消息",
							"orderMessageProducer.sendMsg",
							System.currentTimeMillis() - startTime));

					if (LOG.isDebugEnabled()) {
						LOG.info("api::::::::::"
								+ order.getMainOrderItem().hasSupplierApi());
						LOG.info("distrion:::::::::"
								+ (order.getDistributorId() == 2L));
					}
					Map<String, Object> params = new HashMap<String, Object>();
					// 订单对象中缓存是否发送合同的标示，在工作流中根据此标示决定是否发送合同。
			//		order.setSendContractFlag(buyInfo.getSendContractFlag());
					fillStartProcessParam(ordorder, params);
					try {
						LOG.info("-------------------@---order.getDistributorId()"
								+ order.getDistributorId()
								+ "----getProcessKey:"
								+ handle.getReturnContent().getProcessKey()
								+ "--"
								+ ActivitiUtils.createOrderBussinessKey(order));
						startTime = System.currentTimeMillis();
						String processId = processerClientService
								.startProcesser(handle.getReturnContent()
										.getProcessKey(), ActivitiUtils
										.createOrderBussinessKey(order), params);
						LOG.info(ComLogUtil.printTraceInfo(methodName,
								"启动订单流传流程",
								"processerClientService.startProcesser",
								System.currentTimeMillis() - startTime));

						startTime = System.currentTimeMillis();
						comActivitiRelationService.saveRelation(handle
								.getReturnContent().getProcessKey(), processId,
								handle.getReturnContent().getOrderId(),
								ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
						LOG.info(ComLogUtil.printTraceInfo(methodName,
								"保存工作流信息",
								"comActivitiRelationService.saveRelation",
								System.currentTimeMillis() - startTime));

						LOG.info("order.hasNeedPrepaid="
								+ order.hasNeedPrepaid());
						if (order.hasNeedPrepaid()
								&& order.getDistributorId() != Constant.DIST_BACK_END) {
							if (order.getOughtAmount() == 0
									&& !order.isNeedResourceConfirm()) {// 操作0元支付
								LOG.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"
										+ order.getOrderId());
								ordPrePayServiceAdapter
										.vstOrder0YuanPayMsg(order.getOrderId());
							}
							/*
							 * //因为老支付接口不能使用，查无结果，为了保证功能，暂使用新接口支付 else
							 * if(order.getBonusAmount()>0){
							 * LOG.info("payFromBonusForVstOrder params:userId"
							 * +order
							 * .getUserId()+",orderId="+order.getOrderId()+
							 * ",bonusAmount="+order.getBonusAmount());
							 * payFromBonusForVstOrder(order.getUserId(),
							 * order.getOrderId(), order.getBonusAmount()); }
							 */
						}
					} catch (Exception exx) {
						cancelOrder(ordorder.getOrderId(),
								OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name(),
								"生成流程错误废单", "SYSTEM", "");
						isOrderCreated = false;
						throw exx;

						// }
					}
					// 生成订单权限
					startTime = System.currentTimeMillis();
					synManagerIdPerm(ordorder);
					LOG.info(ComLogUtil.printTraceInfo(methodName, "同步订单权限",
							"this.synManagerIdPerm", System.currentTimeMillis()
									- startTime));

					if (order.getDistributorId() == 2L) {
						order.setOrderItemList(listItem);// 设置itemlist用于传递orderitem和buyinfo中item的对应关系
					}
					// 设置下单公告
					LOG.info("-----create order start method setOrderNotice----");
					setOrderNotice(order);
				}
				// 订单跟踪信息保存
				if (handle.getReturnContent().getCategoryId() == 11L
						|| handle.getReturnContent().getCategoryId() == 12L
						|| handle.getReturnContent().getCategoryId() == 13L) {
					OrdOrderTracking orderTracking = new OrdOrderTracking();
					orderTracking.setOrderId(handle.getReturnContent()
							.getOrderId());
					orderTracking
							.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.UNPAY
									.getCode());
					orderTracking.setChangeStatusTime(new Date());
					orderTracking.setCreateTime(new Date());
					orderTracking.setCategoryId(handle.getReturnContent()
							.getCategoryId());
					ordOrderTrackingService.saveOrderTracking(orderTracking); // 保存未支付状态
					LOG.info("create orderTracking start method saveOrderTracking:"
							+ orderTracking.getOrderId()
							+ ","
							+ orderTracking.getOrderStatus());
					if (order.getPaymentTarget().equals(
							SuppGoods.PAYTARGET.PAY.name())) { // 如果为到付
						orderTracking
								.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.CREDITED
										.getCode());
						orderTracking.setChangeStatusTime(order.getVisitTime());
						orderTracking.setCreateTime(new Date());
						ordOrderTrackingService
								.saveOrderTracking(orderTracking); // 保存凭证已经生成
						LOG.info("create orderTracking start method saveOrderTracking:"
								+ orderTracking.getOrderId()
								+ ","
								+ orderTracking.getOrderStatus());
					}
				}
			}
			// --------------------------------
		} catch (BusinessException e) {
			LOG.info("=======BusinessException======="+e);
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg(e);
			LOG.info("=======BusinessException======="+e);

		} catch (IllegalArgumentException e) {
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg(e);
			LOG.info("=======IllegalArgumentException======="+e);
		}

		catch (Exception e) {
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg(e);
			LOG.info("=======Exception======="+e);
		} finally {
			if (needClearToken) {
				bookUniqueMap.remove(key);
			}
		}
		return handle;

	}

	private JsonConfig config = new JsonConfig();

	private synchronized String getBuyInfoHashCode(DestBuBuyInfo buyInfo) {
		JSONObject obj = JSONObject.fromObject(buyInfo, config);
		return DigestUtils.shaHex(obj.toString());
	}

	private void saveOrder(OrdOrderDTO order) {
		LOG.info("start save order");
		// 将订单保存分为两个步骤，第一步初始为取消状态，并保存相关信息；第二步扣减库存，并恢复订单为正常状态
		newHotelCombOrderSaveService.saveOrder(order);
		// 扣除库存，恢复订单为正常状态，走事务
		saveOrderOfSecond(order);
		LOG.info("end save order, orderId:" + order.getOrderId());
		Long categoryId = order.getFilterMainOrderItem().getCategoryId();
		ordUserOrderService.insertOrdUserOrder(order.getCreateTime(),
				order.getOrderId(), String.valueOf(categoryId),
				order.getUserNo());
	}

	// 扣除库存，恢复订单为正常状态，走事务
	private void saveOrderOfSecond(OrdOrderDTO order) {
		deductStock(order);
		decutResBackToPrecontrol(order);
		if (order.getDistributorId() == Constant.DIST_BACK_END
				|| order.getDistributorId() == Constant.DIST_OFFLINE_EXTENSION) {
			LOG.info("不是ShanghaiDisney 订单 设置订单回复正常状态");
			orderSaveService.resetOrderToNormal(order.getOrderId());
		}
		LOG.info("save order, orderId:" + order.getOrderId() + " end...");
	}

	@Override
	public ResultHandleT<Object> calOrderStock(DestBuBuyInfo buyInfo,
			String operatorId) {

		ResultHandleT<Object> result = new ResultHandleT<Object>();
		List<Item> itemLists = buyInfo.getItemList();
		SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
		suppGoodsParam.setProduct(true);
		suppGoodsParam.setProductBranch(true);
		suppGoodsParam.getProductParam().setBizCategory(true);
		suppGoodsParam.setSupplier(true);
		if (CollectionUtils.isNotEmpty(itemLists)) {
			for (Item item : itemLists) {
				ResultHandleT<Object> resultHandleObject=null;
				ResultHandleT<SuppGoods> resultHandleSuppGoods;
				resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(item.getGoodsId(), suppGoodsParam);
				if (resultHandleSuppGoods.isSuccess()
						&& resultHandleSuppGoods.getReturnContent() != null) {
					SuppGoods suppGoods = resultHandleSuppGoods
							.getReturnContent();
					if (suppGoods.isValid()) {
						if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId()==suppGoods.getCategoryId()){
							
						resultHandleObject = orderTicketNoTimePriceServiceImpl.destBucheckStock(suppGoods, item, buyInfo.getDistributionId(), null);
						
						}else{
							resultHandleObject= orderHotelComp2HotelTimePriceService.checkStock(suppGoods, item, buyInfo.getDistributionId(),null);
						}
					} else {
						result.setMsg("您购买的商品-" + suppGoods.getGoodsName()
								+ "(ID=" + suppGoods.getSuppGoodsId() + ")不可售。");
						return result;
					}
					
					if (!resultHandleObject.isSuccess()) {
						result.setMsg(resultHandleObject.getMsg());
						if (StringUtils.isNotBlank(resultHandleObject.getErrorCode())) {
							result.setErrorCode(resultHandleObject.getErrorCode());
						}
						return result;
					} 		
				}
			}
		}
		return result;
	}



	/**
	 * 新酒套餐预控资源扣减接口 迁移by caiyingshi from
	 * ----->com.lvmama.vst.order.service.book.OrderSaveService
	 * 
	 */
	@Override
	public void decutResBackToPrecontrol(OrdOrder order) {
		boolean reduceResult = false;
		List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
		if (CollectionUtils.isNotEmpty(ordOrderItems)) {
			for (OrdOrderItem orderItem : ordOrderItems) {
				// 如果是预控资源那么进行扣减
				if ("Y".equals(orderItem.getBuyoutFlag())) {
					SuppGoods goods = orderItem.getSuppGoods();
					Long goodsId = goods.getSuppGoodsId();
					Date visitDate = orderItem.getVisitTime();

					int thisOrderItemCategoryId = orderItem.getCategoryId()
							.intValue();
					switch (thisOrderItemCategoryId) {
					case 1:
						LOG.info("酒店更新预控资源请查看saveBussiness.saveAddition(order, orderItem)方法");
						break;

					default:
						// 通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
						GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote
								.getResPrecontrolPolicyByGoodsIdVisitdate(
										goodsId, visitDate);
						// 如果能找到该有效预控的资源
						// --不在检验是否还有金额或者库存的剩余
						// (goodsResPrecontrolPolicyVO.getLeftNum() >0 ||
						// goodsResPrecontrolPolicyVO.getLeftAmount()>0)
						if (goodsResPrecontrolPolicyVO != null) {
							Long controlId = goodsResPrecontrolPolicyVO.getId();
							String resType = goodsResPrecontrolPolicyVO
									.getControlType();
							// 购买该商品的数量
							Long reduceNum = orderItem.getBuyoutQuantity();
							Long leftQuantity = goodsResPrecontrolPolicyVO
									.getLeftNum();
							Long leftAmount = goodsResPrecontrolPolicyVO
									.getLeftAmount();

							if (ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount
									.name().equalsIgnoreCase(resType)
									&& leftAmount != null && leftAmount > 0) {
								// 该商品在该时间内的剩余库存
								Long amountId = goodsResPrecontrolPolicyVO
										.getAmountId();
								// 按金额预控
								Long value = orderItem.getBuyoutTotalPrice();
								Long leftValue = leftAmount - value;
								// 金额预控最小只能是0
								leftValue = leftValue < 0 ? 0L : leftValue;
								reduceResult = resControlBudgetRemote
										.updateAmountResPrecontrolPolicy(
												amountId, controlId, visitDate,
												leftValue);
								if (reduceResult) {
									LOG.info("按金额预控-更新成功");
									sendBudgetMsgToSendEmail(
											goodsResPrecontrolPolicyVO,
											leftAmount, leftValue);
								}
								// 如果预控金额已经没了，清空该商品在这一天的预控缓存
								if (leftValue == 0 && reduceResult) {
									resControlBudgetRemote
											.handleResPrecontrolSaledOut(
													goodsResPrecontrolPolicyVO,
													visitDate, goodsId);
								}
							} else if (ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory
									.name().equalsIgnoreCase(resType)
									&& leftQuantity != null && leftQuantity > 0) {
								// 该商品在该时间内的剩余库存
								Long leftStore = leftQuantity - reduceNum;
								// 库存最小只能是0
								leftStore = leftStore < 0 ? 0L : leftStore;
								Long storeId = goodsResPrecontrolPolicyVO
										.getStoreId();
								// 按库存预控
								reduceResult = resControlBudgetRemote
										.updateStoreResPrecontrolPolicy(
												storeId, controlId, visitDate,
												leftStore);
								if (reduceResult) {
									LOG.info("按库存预控-更新成功");
									sendBudgetMsgToSendEmail(
											goodsResPrecontrolPolicyVO,
											leftQuantity, leftStore);
								}
								// 如果预控库存已经没了，清空该商品在这一天的预控缓存
								if (leftStore == 0 && reduceResult) {
									resControlBudgetRemote
											.handleResPrecontrolSaledOut(
													goodsResPrecontrolPolicyVO,
													visitDate, goodsId);
								}
							}
							if (reduceResult) {
								LOG.info("扣减预控资源成功，订单号："
										+ orderItem.getOrderId() + "子订单号："
										+ orderItem.getOrderItemId() + ",商品id:"
										+ orderItem.getSuppGoodsId() + "，数量："
										+ orderItem.getBuyoutQuantity()
										+ ",总价："
										+ orderItem.getBuyoutTotalPrice());
							}
						}
						break;
					}

				}

			}
		}

	}

	/**
	 * 
	 * 新酒套餐扣减库存
	 * 
	 * @param order
	 */
	public void deductStock(OrdOrder order) {

		List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
		if (CollectionUtils.isNotEmpty(ordOrderItems)) {
			for (OrdOrderItem orderItem : ordOrderItems) {
				List<OrdOrderStock>ordOrderStocks=orderItem.getOrderStockList();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderItemId", orderItem.getOrderItemId());
				params.put("stockStatus",
						OrderEnum.INVENTORY_STATUS.INVENTORY_DEDUCTED.name());
				try {
					// String
					// stockType=iNewSuppGoodsTimePriceService.getStockTypeBySuppGoodsId(orderItem.getSuppGoodsId());
					// 独立库存时候只更新酒套餐
					if (orderItem.getSuppGoods() != null&& SuppGoods.BIZ_STOCK_TYPE.ALONE_STOCK.getCode().equals(orderItem.getSuppGoods().getStockType())) {
						
						if(CollectionUtils.isNotEmpty(ordOrderStocks)){
							for(OrdOrderStock oos:ordOrderStocks){	
								newHotelCombTimePriceClientRemote.updateStock(orderItem.getSuppGoodsId(), oos.getVisitTime(), -oos.getQuantity());
							}
						}
					
					} else if(orderItem.getSuppGoods() != null && SuppGoods.BIZ_STOCK_TYPE.SHARE_STOCK.getCode().equals(orderItem.getSuppGoods().getStockType())) {
						
						SuppGoods suppGoodsOfHotel =newHotelCombProdAdditionClientRemote.getSuppGoodsOfHotel(orderItem.getProductId(),orderItem.getSuppGoodsId());
						if(CollectionUtils.isNotEmpty(ordOrderStocks)){
							for(OrdOrderStock oos:ordOrderStocks){
								newHotelCombTimePriceClientRemote.updateStock(suppGoodsOfHotel.getSuppGoodsId(), oos.getVisitTime(),-oos.getQuantity());
							}
						}
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){//保险逻辑
						
						if(CollectionUtils.isNotEmpty(ordOrderStocks)){
							for(OrdOrderStock oos:ordOrderStocks){		
								orderTicketNoTimePriceServiceImpl.updateRevertStock(orderItem.getSuppGoodsId(), oos.getVisitTime(),  -oos.getQuantity(), null);
							}
						}
						
					}	
				} catch (RuntimeException re) {
					orderStockDao.updateStockStatusByOrderItemId(params);
					throw re;
				}
				orderStockDao.updateStockStatusByOrderItemId(params);
			}
		}
	}

	private synchronized long getTime() {
		long time = new Date().getTime();
		return time;
	}

	public OrdOrder queryOrdorderByOrderId(Long orderId) {
		return complexQueryService.queryOrderByOrderId(orderId);
	}


	public ResultHandle cancelOrder(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		LOG.info("OrdOrderClientServiceImpl.cancelOrder: orderId=" + orderId);
		final String key = "VST_CANCEL_ORDER_" + orderId;
		try {
			if (SynchronizedLock.isOnDoingMemCached(key)) {
				resultHandle.setMsg("订单在重复废单操作");
				return resultHandle;
			}
			if (orderId != null) {
				OrdOrder order = orderUpdateService
						.queryOrdOrderByOrderId(orderId);
				if (order == null) {
					resultHandle.setMsg("订单不存在");
					resultHandle
							.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIST_ORDER
									.getErrorCode());
				} else if (order.isCancel()) {
					resultHandle.setMsg("订单已经被废单");
				}
				// 如果销售渠道是O2O门店

				// TODO
				if (!isBackUser("admin") && !order.isCanCancel()) {
					LOG.info("订单不能取消的原因" + isBackUser(operatorId) + "是否是取消状态"
							+ order.isCanCancel());
					resultHandle.setMsg("订单已经不能取消。");
					resultHandle
							.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.CAN_NOT_CANCAEL_ORDER
									.getErrorCode());
				}

				if (OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(
						cancelCode)
						&& OrderEnum.PAYMENT_STATUS.PAYED.name().equals(
								order.getPaymentStatus())) {
					LOG.info("该订单已支付，不能进行支付等待超时取消, orderId:" + orderId);
					resultHandle.setMsg("该订单已支付，不能进行支付等待超时取消");
				}

				if (resultHandle.isFail()) {
					return resultHandle;
				}
				// 提前退
				if ("提前退".equals(reason)) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("preRefundStatus",
							OrderEnum.PRE_REFUND_STATUS.APPLY.name());
					params.put("orderId", orderId);
					orderUpdateService.updatePreRefundStatus(params);
					// 生成2张售后单(一张售后 一张资审)
					vstOrderRefundServiceRemote.createOrdSaleForPreRufund(
							orderId, operatorId);
				}
				boolean activitiNotCancel = false;
				// 存在流程的数据走流程废单
				if (ActivitiUtils.hasActivitiOrder(order)) {
					LOG.info(order.getOrderId() + "在流程废单");
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("cancelCode", cancelCode);
					LOG.info("cancelOrder reason=" + reason);
					if (reason == "" || reason == null || reason == "null") {
						reason = "SYSTEM";
					}
					LOG.info("cancelOrder reason=" + reason);
					params.put("reason", reason);
					params.put("operatorId", operatorId);
					params.put("memo", memo);
					/*
					 * if(!OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.
					 * getOrderStatus())){ //ziyuanyukong 取消订单后要将资源放入资源预控当中去
					 * LOG.info("正在返还资源start");
					 * updateResBackToPrecontrol(order.getOrderId());
					 * LOG.info("正在返还资源end"); }
					 */
					// 流程取消，并且退回预控资源
					ResultHandle handle = processerClientService.cancelOrder(
							createKeyByOrder(order), params);

					if (handle.isFail()) {
						// activitiNotCancel=true;
						LOG.info("cancel fail in  activiti,use default cancel orderId:"
								+ orderId);
						order = queryOrdorderByOrderId(orderId);
						fillStartProcessParam(order, params);
						String processId = processerClientService
								.startProcesser(
										"order_cancel_prepaid",
										"order_cancel_id:" + order.getOrderId(),
										params);
						LOG.info("CANCEL ORDER id:" + order.getOrderId()
								+ " ProcesserId:" + processId);
					} else {
						LOG.info("order id:" + order.getOrderId()
								+ " activiti cancel");
					}

				}

				if (ActivitiUtils.hasNotActivitiOrder(order)
						|| activitiNotCancel) {
					LOG.info(order.getOrderId() + "不在流程废单");
					if (activitiNotCancel) {
						orderUpdateService.updateClearOrderProcess(orderId);
					}
					resultHandle = cancelOrderLocal(orderId, cancelCode,
							reason, operatorId, memo);
					LOG.info("OrdOrderClientServiceImpl.cancelOrder: resultHandle.isSuccess="
							+ resultHandle.isSuccess()
							+ ", resultHandle.getMsg=" + resultHandle.getMsg());
					if (resultHandle.isFail()) {
						return resultHandle;
					} else {
						// ziyuanyukong 取消订单后要将资源放入资源预控当中去
						/* updateResBackToPrecontrol(order.getOrderId()); */
					}
					OrdOrder canceledOrder = queryOrdorderByOrderId(orderId);
					insertOrderLog(canceledOrder,
							OrderEnum.ORDER_STATUS.CANCEL.getCode(),
							operatorId, memo, cancelCode, reason);
			
				}
				OrdOrder oldOrder = queryOrdorderByOrderId(orderId);

				if (resultHandle.isSuccess()) {
					if (oldOrder.getActualAmount() > 0) {

						// 额外处理：107/108/110 这三个走新逻辑
						boolean ifAddition = false;
						if (oldOrder.getDistributionChannel() != null) {
							if (oldOrder.getDistributionChannel().equals("107")
									|| oldOrder.getDistributionChannel()
											.equals("108")
									|| oldOrder.getDistributionChannel()
											.equals("110")) {
								ifAddition = true;
							}
						}

						if ((ifAddition
								|| oldOrder.getDistributorId() == Constants.DISTRIBUTOR_2
										.longValue() || oldOrder
								.getDistributorId() == Constants.DISTRIBUTOR_3
								.longValue())
								&& SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE
										.name().equals(
												oldOrder.getCancelStrategy())
								&& OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT
										.name().equals(cancelCode)) {

							boolean ifNew = false;
							if (oldOrder.getDistributorId() == Constants.DISTRIBUTOR_4) {
								ifNew = false; // 分销走旧的
							} else {
								ifNew = true; // 除分销走新的
							}
							if (ifAddition) {
								ifNew = true; // 额外处理：107/108/110 这三个走新逻辑
							}

							// 新逻辑：除分销 + 属于：107/108/110
							if (ifNew) {
								// 自动创建全额退款的退款单并进入实际退款中，立即关闭售后
								LOG.info(
										"autoCreateOrderFullRefundCloseSaleServiceVst start, orderId:{}",
										orderId);
								Long refundId = ordPrePayServiceAdapter
										.autoCreateOrderFullRefundCloseSaleServiceVst(
												orderId, operatorId);
								LOG.info("autoCreateOrderFullRefundCloseSaleServiceVst end, orderId:"
										+ orderId + ", refundId:" + refundId != null ? String
										.valueOf(refundId) : "null");
							} else {
								LOG.info(
										"autoCreateRefundByCancelOrderVst start, orderId:{}",
										orderId);
								// 自动创建退款单
								Long refundId = ordPrePayServiceAdapter
										.autoCreateRefundByCancelOrderVst(
												orderId, operatorId);
								LOG.info("autoCreateRefundByCancelOrderVst end , orderId:"
										+ orderId + ", refundId:" + refundId);
							}

						}
					}
				}

				// add by zhouguoliang
				cancelOrderAndRemoveForbidBuyRecord(orderId);
				// 增加订单跟踪信息到数据库，提供给驴途使用
				LOG.info("OrdOrderClientServiceImpl.cancelOrder: saveTracking"
						+ oldOrder.getCategoryId());

			}

		} finally {
			SynchronizedLock.releaseMemCached(key);
		}
		return resultHandle;
	}
	private boolean isBackUser(String operatorId) {
		boolean isBackUser = false;
		if (operatorId != null) {
			if ("system".equalsIgnoreCase(operatorId)
					|| "admin".equalsIgnoreCase(operatorId)
					|| operatorId.startsWith("cs")
					|| operatorId.startsWith("lv")) {
				isBackUser = true;
			}
		}

		return isBackUser;
	}

	private ActivitiKey createKeyByOrder(OrdOrder order){
		ComActivitiRelation relation = getRelation(order);
		LOG.info("createKeyByOrder order.orderid="+order.getOrderId());
		return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
	}

	public ComActivitiRelation getRelation(OrdOrder order){
		try{
			ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
			if(comActiveRelation == null){//补偿机制,通过工作流再次去触发查询
				String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
				LOG.info("===processerClientService.queryProcessIdByBusinessKey==orderId:"+order.getOrderId()+"====processId:"+processId);
				if(processId != null){
					comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					comActiveRelation = new ComActivitiRelation();
					comActiveRelation.setObjectId(order.getOrderId());
					comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
					comActiveRelation.setProcessId(processId);
					comActiveRelation.setProcessKey(order.getProcessKey());
				}
			}
			return comActiveRelation;
		}catch(Exception e){
			LOG.error("ComActivitiRelation getRelation error:"+e);
		}
		return null;
	}
	
	
}
