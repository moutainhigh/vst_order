package com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.base.Throwables;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.lvmama.cmt.comm.vo.BusinessException;
import com.lvmama.comm.pay.utils.DateUtil;
import com.lvmama.comm.vo.Constant;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.common.ResponseBody;
import com.lvmama.dest.hotel.trade.common.ResponseBody.RESPONSE_CODE;
import com.lvmama.dest.hotel.trade.hotelcomb.interfaces.IHotelCombTradeOrderService;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderCancelVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderPriceListVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.PromPromotionVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.UserCouponVO;
import com.lvmama.dest.hotel.trade.vo.base.Person;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.PriceUtil;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cal.IOrderCalFactory;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.builder.category.IOrderDTOFactory;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.ICheckOrderStock;
import com.lvmama.vst.neworder.order.create.deduct.IDeductFactory;
import com.lvmama.vst.neworder.order.create.persistance.category.IOrderDbStoreFactory;
import com.lvmama.vst.neworder.order.service.IOrderBaseService;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo.Coupon;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;

/**
 * Created by dengcheng on 17/3/2.
 */
@Component("hotelCombTradeOrderService")
public class IHotelCombOrderServiceProxy extends IOrderBaseService implements IHotelCombTradeOrderService {

	private static final Log LOG = LogFactory.getLog(IHotelCombOrderServiceProxy.class);

	@Resource
	IHotelCombOrderService hotelCombOrderService;

	@Resource
	ICheckOrderStock hotelCombCheckOrderService;

	@Resource
	IOrderCancelService orderCancelService;

	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderService;

	@Resource
	private OrderService orderService;

	@Autowired
	private IBookService bookService;

	@Autowired
	private IOrderUpdateService orderUpdateService;
	// 分摊服务
	@Autowired
	private OrderAmountApportionService orderAmountApportionService;

	@Override
	public ResponseBody<HotelOrdOrderVo> submitOrder(RequestBody<HotelCombTradeBuyInfoVo> requestBody) {
		ResponseBody<HotelOrdOrderVo> responseBody = new ResponseBody<HotelOrdOrderVo>();
		Integer isSuccess = 0;
		Long orderId = null;
		OrdOrderDTO ordOrderDTO = null;
		try {
			// 先调用订单检查
			this.checkOrder(requestBody);
			// 先调用订单检查
			HotelCombTradeBuyInfoVo tradeuyInfo = requestBody.getT();

			this.checkCoupon(requestBody);
			ObjectMapper mapper = new ObjectMapper();
			try {
				LOG.info(String.format("====IHotelCombOrderServiceProxy.submitOrder tradeuyInfo %s",
						mapper.writeValueAsString(tradeuyInfo)));
			} catch (JsonGenerationException e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				LOG.info("json转换exception");
			} catch (JsonMappingException e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				LOG.info("json转换exception");
			} catch (IOException e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				LOG.info("json转换IOException");
			}
			OrderHotelCombBuyInfo orderBuyInfo = new OrderHotelCombBuyInfo();
			orderBuyInfo = this.traderByInfoConvertToOrderInfo(tradeuyInfo, orderBuyInfo, requestBody);

			// OrdOrderDTO ordOrderDTO = null;
			try {
				LOG.info(String.format("====IHotelCombOrderServiceProxy.submitOrder orderBuyInfo %s",
						mapper.writeValueAsString(orderBuyInfo)));
			} catch (JsonGenerationException ex) {
				LOG.error(ExceptionFormatUtil.getTrace(ex));
				LOG.info("json转换exception");
			} catch (JsonMappingException ex) {
				LOG.error(ExceptionFormatUtil.getTrace(ex));
				LOG.info("json转换exception");
			} catch (IOException ex) {
				LOG.error(ExceptionFormatUtil.getTrace(ex));
				LOG.info("json转换IOException");
			}
			BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo = new BaseBuyInfo<OrderHotelCombBuyInfo>();

			baseBuyInfo.setT(orderBuyInfo);

			/**
			 * DTO创建工厂 每个品类自己实现
			 */
			LOG.info("IHotelCombOrderServiceProxy.submitOrder--orderDTOCreator----start");
			ordOrderDTO = super.orderDTOCreator(baseBuyInfo);
			LOG.info("IHotelCombOrderServiceProxy.submitOrder--orderDTOCreator----end");
			/**
			 * 持久层工程 每个品类自己实现
			 */
			OrdOrder order = null;
			LOG.info("IHotelCombOrderServiceProxy.submitOrder--persistanceOrder----start");
			order = super.persistanceOrder(ordOrderDTO);
			LOG.info("IHotelCombOrderServiceProxy.submitOrder--persistanceOrder----end");
			// 添加到ORD_USER_ORDER，给前台我的订单用，add by ltwangwei 2017/05/02 18:05:xx
			int count = ordUserOrderService.insertOrdUserOrder(order.getCreateTime(), order.getOrderId(),
					String.valueOf(order.getCategoryId()), order.getUserNo());
			LOG.info(String.format("====insertOrdUserOrder %s record success...", count));

			NewOrderConstant.orderThreadLocalCache.get().put("currentThreadOrder", order);

			LOG.info("hotelComb orderId:" + order.getOrderId());

			orderId = order.getOrderId();
			// 添加分摊
			if (ApportionUtil.isApportionEnabled()) {
				orderAmountApportionService.addToOrderApportionDepot(orderId);
				LOG.info("Order " + orderId + " added to apportion table");
			} else {
				LOG.info("Apportion key is not enable, order " + orderId + " will not add to apportion table");
			}

			/**
			 * 库存抵扣服务 每个品类自己实现
			 */
			try {
				super.deductOrder(ordOrderDTO);
			} catch (BusinessException e) {
				LOG.error("扣减库存失败" + e);
				isSuccess = 1;

			}
			HotelOrdOrderVo apiOrder = new HotelOrdOrderVo();
			apiOrder.setOrderId(order.getOrderId());
			responseBody.setT(apiOrder);
			try {
				updateOrderOughtAmount(orderBuyInfo, order, requestBody);
			} catch (BusinessException e) {
				LOG.error("更新应付金额，优惠扣减失败" + e);
				isSuccess = 1;
			}

			super.setOrderNormal(ordOrderDTO);

		} catch (com.lvmama.dest.hotel.trade.utils.BusinessException businessException) {
			LOG.error(businessException);
			responseBody.setBusinessException(businessException);
			responseBody.setCode(RESPONSE_CODE.FAILURE.getCode());
		}catch (Throwable t) {
			t.printStackTrace();
			Throwables.propagate(t);
		} finally {
			if (isSuccess == 1) {
				LOG.info("库存扣减失败，启动工作流处理orderId:" + orderId);
				super.setOrderNormal(ordOrderDTO);
				OrderCancelInfo cancelInfo = new OrderCancelInfo();
				cancelInfo.setOrderId(orderId);
				cancelInfo.setCancelCode(OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name());
				cancelInfo.setMemo("订单库存扣减失败");
				cancelInfo.setOperatorId("System");
				cancelInfo.setReason("订单库存扣减失败");
				orderCancelService.doOrderCancel(cancelInfo);
			}
		}

		return responseBody;
	}

	private OrderHotelCombBuyInfo traderByInfoConvertToOrderInfo(HotelCombTradeBuyInfoVo tradeuyInfo,
			OrderHotelCombBuyInfo orderBuyInfo, RequestBody<?> request) {
		List<OrderHotelCombBuyInfo.GoodsItem> goods = Lists.newArrayList();
		List<OrderHotelCombBuyInfo.ProductItem> products = Lists.newArrayList();
		List<OrderHotelCombBuyInfo.Item> itemList = Lists.newArrayList();

		if (tradeuyInfo.getGoodsList() != null) {
			for (HotelCombTradeBuyInfoVo.GoodsItem tradeGoodsItem : tradeuyInfo.getGoodsList()) {
				OrderHotelCombBuyInfo.GoodsItem hotelGoodsItem = orderBuyInfo.new GoodsItem();
				EnhanceBeanUtils.copyProperties(tradeGoodsItem, hotelGoodsItem);
				hotelGoodsItem.setProductCategoryId(tradeGoodsItem.getCategoryId());
				hotelGoodsItem.setSubCategoryId(tradeGoodsItem.getSubCategoryId());
				goods.add(hotelGoodsItem);
			}
		}

		if (tradeuyInfo.getProductList() != null) {
			for (HotelCombTradeBuyInfoVo.ProductItem tradeProductItem : tradeuyInfo.getProductList()) {
				OrderHotelCombBuyInfo.ProductItem hotelProductItem = orderBuyInfo.new ProductItem();
				EnhanceBeanUtils.copyProperties(tradeProductItem, hotelProductItem);
				products.add(hotelProductItem);
			}
		}

		if (tradeuyInfo.getItemList() != null) {
			for (HotelCombTradeBuyInfoVo.Item tradeItem : tradeuyInfo.getItemList()) {
				OrderHotelCombBuyInfo.Item otherItem = orderBuyInfo.new Item();
				EnhanceBeanUtils.copyProperties(tradeItem, otherItem);
				itemList.add(otherItem);
				OrderHotelCombBuyInfo.GoodsItem hotelGoodsItem = orderBuyInfo.new GoodsItem();
				hotelGoodsItem.setPricePlanId(null);
				hotelGoodsItem.setQuantity(Long.valueOf(otherItem.getQuantity()));
				hotelGoodsItem.setCheckInDate(
						com.lvmama.vst.comm.utils.DateUtil.toDate(otherItem.getVisitTime(), "yyyy-MM-dd"));
				hotelGoodsItem.setGoodsId(otherItem.getGoodsId());
				hotelGoodsItem.setTotalAmount(otherItem.getTotalAmount());
				goods.add(hotelGoodsItem);
			}
		}
		List<com.lvmama.vst.neworder.order.vo.Person> orderTravellers = Lists.newArrayList();

		if (tradeuyInfo.getTravellers() != null) {
			for (Person person : tradeuyInfo.getTravellers()) {
				LOG.info("personType:" + person.getPersonType());
				com.lvmama.vst.neworder.order.vo.Person orderTraveller = new com.lvmama.vst.neworder.order.vo.Person();
				EnhanceBeanUtils.copyProperties(person, orderTraveller);

				orderTravellers.add(orderTraveller);
				LOG.info("orderPersonType:" + orderTraveller.getPersonType());
			}
		}

		/**
		 * 处理游玩人商品关系
		 */
		Map<Long, List<com.lvmama.vst.neworder.order.vo.Person>> goodsPersonListMap = new HashMap<Long, List<com.lvmama.vst.neworder.order.vo.Person>>();
		Iterator<Map.Entry<Long, List<Person>>> iter = tradeuyInfo.getGoodsPersonListMap().entrySet().iterator();

		while (iter.hasNext()) {
			Entry<Long, List<Person>> entry = iter.next();
			Long key = (Long) entry.getKey();
			List<Person> listPerson = (List<Person>) entry.getValue();
			List<com.lvmama.vst.neworder.order.vo.Person> targetPersonList = new ArrayList<com.lvmama.vst.neworder.order.vo.Person>();
			EnhanceBeanUtils.copyProperties(listPerson, targetPersonList);
			goodsPersonListMap.put(key, targetPersonList);

		}
		orderBuyInfo.setGoodsPersonListMap(goodsPersonListMap);
		Map<String, List<Long>> promotionMap = new HashMap<String, List<Long>>();
		/**
		 * 处理促销与产品商品的关系
		 */
		Iterator<Map.Entry<String, List<Long>>> promotionIter = tradeuyInfo.getPromotionMap().entrySet().iterator();
		while (promotionIter.hasNext()) {
			Entry<String, List<Long>> entry = promotionIter.next();
			String key = (String) entry.getKey();
			List<Long> listPromition = (List<Long>) entry.getValue();
			promotionMap.put(key, listPromition);

		}
		/**
		 * 处理优惠
		 */
		List<Coupon> userCouponVOList = Lists.newArrayList();
		if (tradeuyInfo.getUserCouponVOList() != null) {
			for (UserCouponVO userCouponVo : tradeuyInfo.getUserCouponVOList()) {
				Coupon coupon = new Coupon();
				EnhanceBeanUtils.copyProperties(userCouponVo, coupon);
				userCouponVOList.add(coupon);
			}
		}
		orderBuyInfo.setCouponList(userCouponVOList);
		orderBuyInfo.setYouhui(tradeuyInfo.getYouhui());
		orderBuyInfo.setPromotionMap(promotionMap);
		orderBuyInfo.setTravellers(orderTravellers);
		orderBuyInfo.setItemList(itemList);
		orderBuyInfo.setProductList(products);
		orderBuyInfo.setGoodsList(goods);
		orderBuyInfo.setMobileEquipmentNo(request.getDeviceId());
		orderBuyInfo.setUserId(request.getUserId());
		orderBuyInfo.setUserNo(request.getUserNo());
		orderBuyInfo.setIp(request.getIp());
		orderBuyInfo.setIp(tradeuyInfo.getIp());
		// orderBuyInfo.setMobileEquipmentNo(orderBuyInfo.getMobileEquipmentNo());
		orderBuyInfo.setDistributionChannel(request.getDistributionChannel());
		orderBuyInfo.setDistributionId(request.getDistributionId());
		orderBuyInfo.setDistributorCode(request.getDistributorCode());
		orderBuyInfo.setDistributorName(request.getDistributorName());
		orderBuyInfo.setIsTestOrder(tradeuyInfo.getIsTestOrder());
		orderBuyInfo.setSubDistributorId(tradeuyInfo.getSubDistributorId());// 子渠道
		orderBuyInfo.setWorkVersion(tradeuyInfo.getWorkVersion());
		return orderBuyInfo;
	}

	@Override
	public ResponseBody<OrderPriceListVo> calOrderPriceList(RequestBody<HotelCombTradeBuyInfoVo> request) {
		ResponseBody<HotelOrdOrderVo> responseBody = new ResponseBody<HotelOrdOrderVo>();
		HotelCombTradeBuyInfoVo tradeuyInfo = request.getT();
		OrderHotelCombBuyInfo orderBuyInfo = new OrderHotelCombBuyInfo();
		ObjectMapper mapper = new ObjectMapper();
		try {
			LOG.info(String.format("====IHotelCombOrderServiceProxy.calOrderPriceList tradeuyInfo %s",
					mapper.writeValueAsString(tradeuyInfo)));
		} catch (JsonGenerationException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换exception");
		} catch (JsonMappingException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换exception");
		} catch (IOException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换IOException");
		}

		orderBuyInfo = this.traderByInfoConvertToOrderInfo(tradeuyInfo, orderBuyInfo, request);

		orderBuyInfo.setMobileEquipmentNo(request.getDeviceId());
		orderBuyInfo.setIp(request.getIp());
		orderBuyInfo.setUserId(request.getUserId());
		orderBuyInfo.setUserNo(request.getUserNo());

		OrdOrderDTO ordOrderDTO = null;

		BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo = new BaseBuyInfo<OrderHotelCombBuyInfo>();
		baseBuyInfo.setT(orderBuyInfo);
		/**
		 * DTO创建工厂 每个品类自己实现
		 */
		ordOrderDTO = super.orderBaseCreator(baseBuyInfo);
		ProductAmountItem productAmountItem = calFactory.buildProductAmountItem(ordOrderDTO, baseBuyInfo);
		try {
			LOG.info(String.format("====IHotelCombOrderServiceProxy.calOrderPriceList tradeuyInfo %s",
					mapper.writeValueAsString(productAmountItem)));
		} catch (JsonGenerationException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换exception");
		} catch (JsonMappingException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换exception");
		} catch (IOException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			LOG.info("json转换IOException");
		}
		OrderPriceListVo priceListVo = new OrderPriceListVo();
		EnhanceBeanUtils.copyProperties(productAmountItem, priceListVo);
		// OG.info("calOrderPriceList==
		// result==="+JSONObject.fromObject(priceListVo));
		return new ResponseBody<OrderPriceListVo>().success(priceListVo, "ok");

	}

	@Override
	public void checkOrder(RequestBody<HotelCombTradeBuyInfoVo> request) {

		List<HotelCombBuyInfoVo> list = Lists.newArrayList();
		/**
		 * 订单校验 这里如果检测失败会抛出异常 前端捕获异常处理即可
		 */
		HotelCombBuyInfoVo hotelCombBuyInfoVo = new HotelCombBuyInfoVo();
		List<HotelCombBuyInfoVo> hotelCombBuyInfoVoList = convertOrderHotelCombBuyInfoToHotelCombBuyInfoVo(
				request.getT(), hotelCombBuyInfoVo);
		// update by ltwangwei 2017.4.7
		// hotelCombCheckOrderService.checkOrder(hotelCombBuyInfoVoList);
		hotelCombCheckOrderService.checkStock(hotelCombBuyInfoVoList);
		// 门票库存校验
		HotelCombTradeBuyInfoVo hotelCombTradeBuyInfoVo = request.getT();
		if (hotelCombTradeBuyInfoVo.getItemList() != null) {
			for (HotelCombTradeBuyInfoVo.Item item : hotelCombTradeBuyInfoVo.getItemList()) {
				boolean flag = hotelCombCheckOrderService.checkStock(item, hotelCombTradeBuyInfoVo.getDistributionId());

			}
		}

	}

	@Override
	public ResponseBody<Void> cancelOrderForWorkFlow(RequestBody<OrderCancelVo> requestBody) {
		OrderCancelVo orderCancelVo = requestBody.getT();
		OrderCancelInfo orderCancelInfo = new OrderCancelInfo();
		EnhanceBeanUtils.copyProperties(orderCancelVo, orderCancelInfo);
		newOrderCancelService.cancelForWorkFlow(orderCancelInfo);
		return new ResponseBody<Void>();
	}

	private List<HotelCombBuyInfoVo> convertOrderHotelCombBuyInfoToHotelCombBuyInfoVo(
			HotelCombTradeBuyInfoVo tradeuyInfo, HotelCombBuyInfoVo hotelCombBuyInfoVo) {
		List<HotelCombBuyInfoVo> list = Lists.newArrayList();
		List<HotelCombBuyInfoVo.GoodsItem> goods = Lists.newArrayList();
		List<HotelCombBuyInfoVo.ProductItem> products = Lists.newArrayList();

		if (tradeuyInfo.getGoodsList() != null) {
			for (HotelCombTradeBuyInfoVo.GoodsItem tradeGoodsItem : tradeuyInfo.getGoodsList()) {
				HotelCombBuyInfoVo.GoodsItem hotelGoodsItem = new HotelCombBuyInfoVo().new GoodsItem();
				EnhanceBeanUtils.copyProperties(tradeGoodsItem, hotelGoodsItem);
				goods.add(hotelGoodsItem);
			}
			hotelCombBuyInfoVo.setGoodsList(goods);
		}

		if (tradeuyInfo.getProductList() != null) {
			for (HotelCombTradeBuyInfoVo.ProductItem tradeProductItem : tradeuyInfo.getProductList()) {
				HotelCombBuyInfoVo.ProductItem hotelProductItem = new HotelCombBuyInfoVo().new ProductItem();
				EnhanceBeanUtils.copyProperties(tradeProductItem, hotelProductItem);
				products.add(hotelProductItem);
			}
			hotelCombBuyInfoVo.setProductList(products);
		}

		list.add(hotelCombBuyInfoVo);

		return list;
	}

	public synchronized long getTime() {
		long time = new Date().getTime();
		return time;
	}

	@Resource(name = "newHotelCombOrderFactory")
	public void setBaseDTOFactory(IOrderDTOFactory baseDTOFactory) {
		this.baseDTOFactory = baseDTOFactory;
	}

	@Resource(name = "hotelCombDbStroeFactory")
	public void setBaseDbStoreFactory(IOrderDbStoreFactory baseDbStoreFactory) {
		this.baseDbStoreFactory = baseDbStoreFactory;
	}

	@Resource(name = "newHotelCombCalFactory")
	public void setCalFactory(IOrderCalFactory calFactory) {
		this.calFactory = calFactory;
	}

	@Resource(name = "hotelCombDeductServiceFactory")
	public void setBaseDeductService(IDeductFactory baseDeductService) {
		this.baseDeductService = baseDeductService;
	}

	@Override
	public ResponseBody<List<UserCouponVO>> getHotelcombUserCouponVOList(RequestBody<HotelCombTradeBuyInfoVo> request) {
		LOG.info("====IHotelCombOrderServiceProxy.getHotelcombUserCouponVOList.====");
		ResponseBody<List<UserCouponVO>> responseBody = new ResponseBody<List<UserCouponVO>>();
		try {
			List<UserCouponVO> resUserCouponVOList = new ArrayList<UserCouponVO>();
			// 转换buyinfo
			BuyInfo buyInfo = convertBuyInfo(request);
			// 初始化订单
			com.lvmama.vst.comm.vo.order.OrdOrderDTO ordOrderDTO = initOrderAndCalc(request);

			List<com.lvmama.vst.pet.vo.UserCouponVO> userCouponVOList = orderService
					.getHotelcombUserCouponVOList(ordOrderDTO, buyInfo);
			if (CollectionUtils.isNotEmpty(userCouponVOList)) {
				for (com.lvmama.vst.pet.vo.UserCouponVO userCouponVO : userCouponVOList) {
					UserCouponVO resUserCouponVO = new UserCouponVO();
					EnhanceBeanUtils.copyProperties(userCouponVO, resUserCouponVO);
					resUserCouponVOList.add(resUserCouponVO);
				}
			}

			responseBody.setT(resUserCouponVOList);
		} catch (Exception e) {
			e.printStackTrace();
			Throwables.propagate(e);
		}
		return responseBody;
	}

	@Override
	public ResponseBody<List<PromPromotionVo>> checkHotelcombPromAmount(RequestBody<HotelCombTradeBuyInfoVo> request) {
		// 转换buyinfo
		BuyInfo buyInfo = convertBuyInfo(request);
		ResultHandleT<List<PromPromotion>> resultHandle = orderService.checkPromAmount(buyInfo);
		List<PromPromotion> promPromotionList = resultHandle.getReturnContent();
		List<PromPromotionVo> promPromotionVoList = new ArrayList<PromPromotionVo>();
		if (promPromotionList != null && promPromotionList.size() > 0) {
			for (PromPromotion promPromotion : promPromotionList) {
				PromPromotionVo promPromotionVo = new PromPromotionVo();
				BeanUtils.copyProperties(promPromotion, promPromotionVo);
				promPromotionVoList.add(promPromotionVo);
			}
		}
		ResponseBody<List<PromPromotionVo>> responseBody = new ResponseBody<List<PromPromotionVo>>();
		responseBody.setT(promPromotionVoList);
		return responseBody;
	}

	@Override
	public ResponseBody<String> checkHotelcombOrderForPayOther(RequestBody<HotelCombTradeBuyInfoVo> request) {
		BuyInfo buyInfo = convertBuyInfo(request);
		ResponseBody<List<UserCouponVO>> couponVoList = this.getHotelcombUserCouponVOList(request);
		ResponseBody<String> result = new ResponseBody<String>();
		String strResult = bookService.chechOrderPayForOther(buyInfo, couponVoList.getT());
		List<UserCouponVO> resUserCouponVOList = new ArrayList<UserCouponVO>();
		if(buyInfo!= null && buyInfo.getUserCouponVoList()!= null && buyInfo.getUserCouponVoList().size() >0  ){
			for(com.lvmama.vst.pet.vo.UserCouponVO userCop:buyInfo.getUserCouponVoList()){
			UserCouponVO userCouponVo = new   UserCouponVO();
			EnhanceBeanUtils.copyProperties(userCop,userCouponVo);
			resUserCouponVOList.add(userCouponVo);
			}
		}
		if(resUserCouponVOList != null && resUserCouponVOList.size()>0){
		request.getT().setUserCouponVOList(resUserCouponVOList);
		}
		result.setT(strResult);
		return result;
	}

	/**
	 * 初始化订单（酒套餐优惠促销使用）
	 * 
	 * @param requestBody
	 * @return
	 */
	private com.lvmama.vst.comm.vo.order.OrdOrderDTO initOrderAndCalc(
			RequestBody<HotelCombTradeBuyInfoVo> requestBody) {
		LOG.info("====IHotelCombOrderServiceProxy.initOrderAndCalc.====");
		com.lvmama.vst.comm.vo.order.OrdOrderDTO orderDTO = new com.lvmama.vst.comm.vo.order.OrdOrderDTO(new BuyInfo());
		try {
			// 预处理
			HotelCombTradeBuyInfoVo tradeuyInfo = requestBody.getT();
			ObjectMapper mapper = new ObjectMapper();
			try {
				LOG.info(String.format("====IHotelCombOrderServiceProxy.submitOrder tradeuyInfo %s",
						mapper.writeValueAsString(tradeuyInfo)));
			} catch (Exception e) {

			}
			OrderHotelCombBuyInfo orderBuyInfo = new OrderHotelCombBuyInfo();
			orderBuyInfo = this.traderByInfoConvertToOrderInfo(tradeuyInfo, orderBuyInfo, requestBody);
			try {
				LOG.info(String.format("====IHotelCombOrderServiceProxy.submitOrder tradeuyInfo %s",
						mapper.writeValueAsString(tradeuyInfo)));
			} catch (Exception e) {

			}
			BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo = new BaseBuyInfo<OrderHotelCombBuyInfo>();

			baseBuyInfo.setT(orderBuyInfo);
			/**
			 * DTO创建工厂 每个品类自己实现
			 */
			OrdOrderDTO ordOrderDTO = super.orderBaseCreator(baseBuyInfo);
			EnhanceBeanUtils.copyProperties(ordOrderDTO, orderDTO);
		} catch (Throwable t) {
			t.printStackTrace();
			Throwables.propagate(t);
		}
		return orderDTO;
	}

	/**
	 * 转换buyinfo（酒套餐优惠促销使用）
	 * 
	 * @param requestBody
	 * @return
	 */
	private BuyInfo convertBuyInfo(RequestBody<HotelCombTradeBuyInfoVo> requestBody) {
		LOG.info("====IHotelCombOrderServiceProxy.convertBuyInfo.====");
		BuyInfo buyInfo = new BuyInfo();
		buyInfo.setUserId(requestBody.getUserId());
		buyInfo.setUserNo(requestBody.getUserNo());
		HotelCombTradeBuyInfoVo hotelCombTradeBuyInfoVo = requestBody.getT();
		// productId
		buyInfo.setProductId(hotelCombTradeBuyInfoVo.getProductList().get(0).getProductId());
		// 品类
		buyInfo.setCategoryId(Long.parseLong(Constant.VST_CATEGORY.CATEGORY_ROUTE_NEW_HOTELCOMB.getCategoryId()));
		// 平台 目前只有两个值，"VST", "MOBILE"
		buyInfo.setDistributionId(hotelCombTradeBuyInfoVo.getDistributionId());
		buyInfo.setDistributionChannel(hotelCombTradeBuyInfoVo.getDistributionChannel());
		buyInfo.setDistributorCode(hotelCombTradeBuyInfoVo.getDistributorCode());
		// 优惠劵
		List<com.lvmama.vst.pet.vo.UserCouponVO> petUserCouponVOList = new ArrayList<com.lvmama.vst.pet.vo.UserCouponVO>();
		if (CollectionUtils.isNotEmpty(hotelCombTradeBuyInfoVo.getUserCouponVOList())) {
			for (UserCouponVO userCouponVO : hotelCombTradeBuyInfoVo.getUserCouponVOList()) {
				com.lvmama.vst.pet.vo.UserCouponVO petUserCouponVO = new com.lvmama.vst.pet.vo.UserCouponVO();
				EnhanceBeanUtils.copyProperties(userCouponVO, petUserCouponVO);
				petUserCouponVOList.add(petUserCouponVO);
			}
		}
		List<com.lvmama.comm.vst.vo.CardInfo> cardInfoList = new ArrayList<com.lvmama.comm.vst.vo.CardInfo>();

		if (CollectionUtils.isNotEmpty(hotelCombTradeBuyInfoVo.getStoreCardList())
				&& hotelCombTradeBuyInfoVo.getStoreCardList().size() > 0) {
			for (com.lvmama.dest.hotel.trade.hotelcomb.vo.CardInfo cardInfo : hotelCombTradeBuyInfoVo
					.getStoreCardList()) {
				com.lvmama.comm.vst.vo.CardInfo cardInf = new com.lvmama.comm.vst.vo.CardInfo();
				BeanUtils.copyProperties(cardInfo, cardInf);
				cardInfoList.add(cardInf);
			}
		}
		List<com.lvmama.comm.vst.vo.CardInfo> cardInfoGiftList = new ArrayList<com.lvmama.comm.vst.vo.CardInfo>();

		if (CollectionUtils.isNotEmpty(hotelCombTradeBuyInfoVo.getGiftCardList())
				&& hotelCombTradeBuyInfoVo.getGiftCardList().size() > 0) {
			for (com.lvmama.dest.hotel.trade.hotelcomb.vo.CardInfo cardInfo : hotelCombTradeBuyInfoVo
					.getStoreCardList()) {
				com.lvmama.comm.vst.vo.CardInfo cardInf = new com.lvmama.comm.vst.vo.CardInfo();
				BeanUtils.copyProperties(cardInfo, cardInf);
				cardInfoGiftList.add(cardInf);
			}
		}
		buyInfo.setStoreCardList(cardInfoList);
		buyInfo.setGiftCardList(cardInfoGiftList);
		buyInfo.setUserCouponVoList(petUserCouponVOList);
		// z增加新优惠数据copy
		EnhanceBeanUtils.copyProperties(hotelCombTradeBuyInfoVo, buyInfo);
		return buyInfo;
	}

	@Override
	public ResponseBody<List<UserCouponVO>> getHotelcombUserCouponList(RequestBody<HotelCombTradeBuyInfoVo> request) {
		LOG.info("====IHotelCombOrderServiceProxy.getHotelcombUserCouponList.====");
		ResponseBody<List<UserCouponVO>> responseBody = new ResponseBody<List<UserCouponVO>>();
		try {
			List<UserCouponVO> resUserCouponVOList = new ArrayList<UserCouponVO>();
			// 转换buyinfo
			BuyInfo buyInfo = convertBuyInfo(request);
			// 初始化订单
			com.lvmama.vst.comm.vo.order.OrdOrderDTO ordOrderDTO = initOrderAndCalc(request);

			List<com.lvmama.vst.pet.vo.UserCouponVO> userCouponVOList = orderService
					.getHotelcombUserCouponList(ordOrderDTO, buyInfo);
			if (CollectionUtils.isNotEmpty(userCouponVOList)) {
				for (com.lvmama.vst.pet.vo.UserCouponVO userCouponVO : userCouponVOList) {
					UserCouponVO resUserCouponVO = new UserCouponVO();
					EnhanceBeanUtils.copyProperties(userCouponVO, resUserCouponVO);
					resUserCouponVOList.add(resUserCouponVO);
				}
			}
			responseBody.setT(resUserCouponVOList);
		} catch (Exception e) {
			e.printStackTrace();
			Throwables.propagate(e);
		}
		return responseBody;
	}

	public void checkCoupon(RequestBody<HotelCombTradeBuyInfoVo> request) {
		// 验证优惠信息
		String checkResult = this.checkHotelcombOrderForPayOther(request).getT();
		if (checkResult != "") {
			throw new IllegalArgumentException(checkResult);
		}
		HotelCombTradeBuyInfoVo hotelCombTradeBuyInfoVo = request.getT();
		OrderHotelCombBuyInfo orderBuyInfo = new OrderHotelCombBuyInfo();

		orderBuyInfo = this.traderByInfoConvertToOrderInfo(hotelCombTradeBuyInfoVo, orderBuyInfo, request);

		orderBuyInfo.setMobileEquipmentNo(request.getDeviceId());
		orderBuyInfo.setIp(request.getIp());
		orderBuyInfo.setUserId(request.getUserId());
		orderBuyInfo.setUserNo(request.getUserNo());

		OrdOrderDTO ordOrderDTO = null;

		BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo = new BaseBuyInfo<OrderHotelCombBuyInfo>();
		baseBuyInfo.setT(orderBuyInfo);
		ordOrderDTO = super.orderDTOCreator(baseBuyInfo);

		this.checkUserCoupon(ordOrderDTO, baseBuyInfo);

	}

	// 优惠使用的验证
	public void checkUserCoupon(OrdOrderDTO ordOrderDTO, BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo) {
		Coupon coupon = null;
		String code = null;
		OrderHotelCombBuyInfo orderHotelCombBuyInfo = baseBuyInfo.getT();
		boolean useCoupon = CollectionUtils.isNotEmpty(orderHotelCombBuyInfo.getCouponList());
		String youhuiType = orderHotelCombBuyInfo.getYouhui();
		if (StringUtils.isNotEmpty(youhuiType)) {
			Long startCountPriceTime = System.currentTimeMillis();
			ProductAmountItem productAmountItem = calFactory.buildProductAmountItem(ordOrderDTO, baseBuyInfo);
			List<ResultHandle> info = productAmountItem.getCouponAmountList().get(0).getCouponResultHandles();
			if (ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)) {
				if (useCoupon) {
					coupon = (Coupon) orderHotelCombBuyInfo.getCouponList().get(0);
					code = coupon.getCode();
					LOG.info("==========cm============useCoupon code:" + code);
					if (!StringUtils.isEmpty(code)) {// 有优惠劵
						if (!info.isEmpty()) {

							StringBuilder str = new StringBuilder("优惠券使用异常");
							for (ResultHandle resultHandle : info) {
								if (null == resultHandle) {
									continue;
								}
								String msg = resultHandle.getMsg();
								str.append("[").append(msg).append("]");
							}
							throw new IllegalArgumentException(str.toString());
						}
					}

				}
			}

			if (ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)) {
				Float userBonus = orderHotelCombBuyInfo.getBonusYuan();
				if (userBonus != null && userBonus > 0) {

					// 最大可抵扣数
					Long maxBonus = productAmountItem.getCouponAmountList().get(0).getMaxBonus();
					Float maxBonusYuan = PriceUtil.convertToYuan(maxBonus);
					if (userBonus > maxBonusYuan) {
						throw new IllegalArgumentException("超过最高奖金抵扣数");
					}
				}
			}
		}
	}

	/**
	 * @author fangxiang 应付金额减去优惠金额,优惠券更新状态
	 */
	public void updateOrderOughtAmount(OrderHotelCombBuyInfo orderBuyInfo, OrdOrder order,
			RequestBody<HotelCombTradeBuyInfoVo> request) {

		boolean useCoupon = CollectionUtils.isNotEmpty(orderBuyInfo.getCouponList());
		if (useCoupon) {
			Coupon coupon = (Coupon) orderBuyInfo.getCouponList().get(0);
			String code = coupon.getCode();
			// 更新应付金额
			// 针对老的优惠系统 据说无线和分销用老的优惠系统
			if (useCoupon && coupon != null && !StringUtils.isEmpty(code)
					&& ORDER_FAVORABLE_TYPE.coupon.getCode().equals(orderBuyInfo.getYouhui())) {
				// 老的下单页使用优惠券的，后续等新版全部上线后删除
				orderUpdateService.updateOrderUsedFavor(order, code);

			}
		}
		// 新的优惠系统
		BuyInfo buyInfo = convertBuyInfo(request);
		String resultStr = orderUpdateService.updateOrderForBuyInfo(order, buyInfo);

	}
}
