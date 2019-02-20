package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import java.math.BigDecimal;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsQueryApiService;
import com.lvmama.dest.api.goods.vo.HotelGoodsBaseVo;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombSuppGoodsTimePriceVo;
import com.lvmama.dest.api.hotelcomb.vo.TimePriceQueryVo;
import com.lvmama.dest.api.order.enums.OrderEnum.RESOURCE_STATUS;
import com.lvmama.dest.api.prodrefund.vo.ProdRefundRequest;
import com.lvmama.dest.api.prodrefund.vo.ProdRefundResponse;
import com.lvmama.dest.api.product.vo.HotelProductVo;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum.SUPPGOODS_KEY;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsRebateClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_CHANNEL;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.REBATE_TYPE;
import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prom.po.SuppGoodsRebate;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.DefaultRebateConfig;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.NewHotelCombOrderFactory;
import com.lvmama.vst.neworder.order.router.IGoodsRouterService;
import com.lvmama.vst.neworder.order.router.ILookUpService;
import com.lvmama.vst.neworder.order.router.ITimePriceRouterService;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.BuyOutTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.neworder.order.vo.Person;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.service.book.OrderPromotionBussiness;
import com.lvmama.vst.order.service.book.OrderRebateBussiness;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

import net.sf.json.JSONArray;

/**
 * Created by dengcheng on 17/2/23.
 */
@Component("hotelCombDTOProductService")
public class DTOProductServiceImpl extends AbstractDTOProduct {

	@Resource
	ILookUpService lookUpService;

	@Resource
	IHotelGoodsQueryApiService hotelGoodsQueryApiService;

	@Resource
	IHotelCombProductService hotelCombProductService;

	@Resource
	IHotelCombOrderService hotelCombOrderService;

	@Autowired
	private OrderOrderFactory orderOrderFactory;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
    @Autowired
    private SuppGoodsRebateClientService suppGoodsRebateClientService;	

	static ThreadLocal<Map<String, Object>> queryThreadCache = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return Maps.newConcurrentMap();
		}
	};

	private static final Logger LOG = LoggerFactory.getLogger(NewHotelCombOrderFactory.class);

	@Override
	public void dbLoader(OrderHotelCombBuyInfo buyInfo) {

		Preconditions.checkArgument(buyInfo.getProductList().size() > 0, "缺少必要数据 product");
		ResponseBody<HotelProductVo> hotelProdVoResponse = hotelProductQueryApiService
				.findProductDetail(new RequestBody<Long>().setTFlowStyle(buyInfo.getProductList().get(0).getProductId(),
						NewOrderConstant.VST_ORDER_TOKEN));

		Preconditions.checkNotNull(hotelProdVoResponse.getT(),
				"产品id" + buyInfo.getProductList().get(0).getProductId() + "Not found");

		queryThreadCache.get().put("hotelProdProduct", hotelProdVoResponse.getT());

		ProdRefundRequest prr = new ProdRefundRequest();

		prr.setProductId(buyInfo.getProductList().get(0).getProductId());

		prr.setVisitDate(buyInfo.getEarliestVisitTime());

		ResponseBody<List<ProdRefundResponse>> hotelCombProdRefundResponse = prodRefundService.getProdRefund(
				new RequestBody<ProdRefundRequest>().setTFlowStyle(prr, NewOrderConstant.VST_ORDER_TOKEN));

		List<ProdRefundResponse> prodRefundResponsesList = hotelCombProdRefundResponse.getT();

		Preconditions.checkArgument(prodRefundResponsesList != null, "退改策略为空[" + prr.getProductId() + "]");
		Preconditions.checkArgument(!prodRefundResponsesList.isEmpty(), "退改策略为空[" + prr.getProductId() + "]");
		Preconditions.checkArgument(prodRefundResponsesList.size() == 1, "过多的退改策略[" + prr.getProductId() + "]");

		queryThreadCache.get().put("prodRefund", prodRefundResponsesList.get(0));

		List<HotelGoodsBaseVo> allHotelGoodsList = Lists.newArrayList();
		for (OrderHotelCombBuyInfo.GoodsItem item : buyInfo.getGoodsList()) {
			IGoodsRouterService goodsRouterService = lookUpService.lookUpGoodsService(item.getSubCategoryId());

			SuppGoods suppGoods = goodsRouterService.findGoodsById(item.getGoodsId());
			LOG.info("SettlementEntityCode:"+ suppGoods.getSettlementEntityCode()+",BuyoutSettlementEntityCode" + suppGoods.getBuyoutSettlementEntityCode());

			RequestBody<TimePriceQueryVo> requesnt = new RequestBody<TimePriceQueryVo>();

			TimePriceQueryVo tpQ = new TimePriceQueryVo();
			tpQ.setGoodsId(item.getGoodsId());

			tpQ.setPricePlanId(item.getPricePlanId());
			tpQ.setCheckInDate(item.getCheckInDate());
			OrderHotelCombBuyInfo.Item otherItem = null;
			if (!(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb
					.getCategoryId() == (suppGoods.getCategoryId()))) {
				for (OrderHotelCombBuyInfo.Item other : buyInfo.getItemList()) {
					if (suppGoods.getSuppGoodsId().equals(other.getGoodsId())) {
						otherItem = other;
						break;
					}
				}
			}
			ITimePriceRouterService timePriceService = lookUpService.lookUptTimePriceService(item.getSubCategoryId(),
					item.getGoodsId(), item.getCheckInDate(),item.isWithBuyOutPrice());
			// ITimePriceRouterService timePriceRouterService =
			// lookUpService.lookUptTimePriceService(item.getSubCategoryId());
			BaseTimePrice timePrice = timePriceService.findTimePrice(suppGoods, item, otherItem);

			/**
			 * s 按照key 规则 生成threadLoacl 数据
			 */

			queryThreadCache.get().put("time_price_goods_" + item.getGoodsId(), timePrice);
			LOG.info("商品id:"+item.getGoodsId()+"时间价格timePrice："+JSONObject.toJSONString(timePrice));
			queryThreadCache.get().put("goods_" + item.getGoodsId(), suppGoods);

		}

		queryThreadCache.get().put("goodsList", allHotelGoodsList);

	}

	@Override
	public OrdOrderDTO buildOrderHeader(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		HotelProductVo hotelProductVo = (HotelProductVo) queryThreadCache.get().get("hotelProdProduct");
		order.setCreateTime(new Date());
		order.setOrderUpdateTime(new Date());
		order.setBonusAmount(0L);

		order.setRebateAmount(0L);
		order.setRebateFlag("N");
		order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
		order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());
		order.setPaymentTime(new Date());
		order.setCategoryId(hotelProductVo.getBizCategoryId());
		// order.setManagerId(hotelProductVo.getManagerId());
		// order.setManagerIdPerm(hotelProductVo.get);
		order.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());
		order.setSubCategoryId(hotelProductVo.getSubCategoryId());
		// order.setDistributionName(hotelProductVo.getdis);
		order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());
		order.setActualAmount(0L);
		order.setDepositsAmount(0L);
		order.setNeedInvoice("false");
		order.setCurrencyCode(OrderEnum.ORDER_CURRENCY_CODE.RMB.name());
		order.setUserId(buyInfo.getUserId());
		order.setUserNo(buyInfo.getUserNo());

		ProdRefundRequest prr = new ProdRefundRequest();


		prr.setProductId(order.getProductId());
		prr.setVisitDate(order.getVisitTime());

		order.setMobileEquipmentNo(buyInfo.getMobileEquipmentNo());
		order.setClientIpAddress(buyInfo.getIp());

		if (LOG.isInfoEnabled()) {
			LOG.info("distributorCode==========" + buyInfo.getDistributorCode());
		}
		order.setRemark(buyInfo.getRemark());
		// 设置分销商ID
		order.setDistributorId(buyInfo.getDistributionId());
		order.setDistributorCode(buyInfo.getDistributorCode());
		order.setDistributorName(buyInfo.getDistributorName());
		order.setLineRouteId(buyInfo.getProdLineRoute() != null ? buyInfo.getProdLineRoute().getId() : null);
		order.setDistributionChannel(buyInfo.getDistributionChannel());
		if (LOG.isInfoEnabled()) {
			LOG.info("distributionChannel==========" + buyInfo.getDistributionChannel());
		}

		if (StringUtils.isNotEmpty(buyInfo.getNeedInvoice())) {
			order.setInvoiceStatus(buyInfo.getNeedInvoice());
		} else {
			order.setInvoiceStatus(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		}
		order.setRemark(buyInfo.getRemark());
		order.setClientIpAddress(buyInfo.getIp());

		order.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
		order.setCancelCertConfirmStatus(OrderEnum.CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());

		order.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
		order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name());
		// order.setStartDistrictId(buyInfo.getStartDistrictId());
		order.setIsTestOrder(buyInfo.getIsTestOrder());
		// order.setSmsLvmamaFlag(buyInfo.getSmsLvmamaFlag());
		order.setSupplierApiFlag(OrderEnum.SUPPLIER_API_FLAG.N.name());
		// 所属分公司
		order.setFilialeName(hotelProductVo.getFiliale());
		//设置工作流
		order.setWorkVersion(buyInfo.getWorkVersion());

		return order;
	}

	private OrdOrderItemDTO initOrderItem(OrdOrderDTO order, OrderHotelCombBuyInfo.GoodsItem item,
			SuppGoods suppGoods) {

		ProdRefundResponse prodRefundResponse = (ProdRefundResponse) queryThreadCache.get().get("prodRefund");


		LOG.info("----start initItem-----");
		
		OrdOrderItemDTO orderItem =   new OrdOrderItemDTO();
		 orderItem.setVisitTime(item.getCheckInDate());

         orderItem.setSuppGoodsId(item.getGoodsId());
         
         orderItem.setQuantity((long) item.getQuantity());

         orderItem.setOrderStockList(Lists.<OrdOrderStock>newArrayList());
         
         orderItem.setTotalAmount(item.getTotalAmount());
		SuppGoodsParam param = new SuppGoodsParam();
		param.setProduct(true);
		ProdProductParam ppp = new ProdProductParam();
		ppp.setBizCategory(true);
		param.setProductBranch(true);
		param.setSupplier(true);
		param.setProductParam(ppp);
		param.setSuppGoodsExp(true);
		param.setSuppGoodsEventAndRegion(true);
		/**
		 * 从查询缓存里面取对象数据 数据已经验证过 保证完整
		 */
		BaseTimePrice timePrice = new BaseTimePrice();
		suppGoods = (SuppGoods) queryThreadCache.get().get("goods_" + item.getGoodsId());
		Object obj = queryThreadCache.get().get("time_price_goods_" + item.getGoodsId());
		if (obj instanceof BuyOutTimePrice) {
			LOG.info("商品设置买断suppGoodsId:"+item.getGoodsId());
			BuyOutTimePrice timePriceBuyout = (BuyOutTimePrice) obj;
			EnhanceBeanUtils.copyProperties(timePriceBuyout, timePrice);
			initBuyOutTimeForOrderItem(suppGoods,timePriceBuyout,orderItem);
		} else if (obj instanceof BaseTimePrice) {
			LOG.info("商品没有设置买断suppGoodsId:"+item.getGoodsId());
			timePrice = (BaseTimePrice) obj;
			
		}
		// orderItem = timePrice.getOrderItem();

		item.setAheadBookTime(timePrice.getAheadBookTime());

		/**
		 * 设置价格计划ID 必要
		 */
		orderItem.setPricePlanId(timePrice.getPricePlanId());
		orderItem.setPrice(timePrice.getSalePrice());
		LOG.info("商品suppGoodsId:"+item.getGoodsId()+"price："+timePrice.getSalePrice());
		/**
		 * 设置deduct默认值
		 */
		orderItem.setDeductAmount(0L);
		/**
		 * 设置deduct默认值
		 */
		orderItem.setSettlementPrice(timePrice.getSettmentPrice());
		LOG.info("商品suppGoodsId:"+item.getGoodsId()+"settlementPrice："+timePrice.getSettmentPrice());
		/**
		 * end 将酒店子系统 HotelGoodsBaseVo 转化成vst 系统 SuppGoods
		 */

		// 设置子单“公司主体”--SuppGoodsId--
		if (StringUtils.isNotBlank(suppGoods.getCompanyType())) {
			orderItem.setCompanyType(suppGoods.getCompanyType());
		}

		orderItem.setProductId(suppGoods.getProductId());

		orderItem.setCategoryId(suppGoods.getCategoryId());
		item.setProductCategoryId(suppGoods.getCategoryId());
		orderItem.setSuppGoods(suppGoods);
      // 增加是否扣库存的标示（针对门票和保险用）
		orderItem.setStockFlag(timePrice.getStockFlag());
		orderItem.setCancelStrategy(prodRefundResponse.getCancelStrategy());
		
		// 子订单商品校验 不再做子单校验 一律前置校验
		// checkSaleAble(suppGoods);

		// orderItem.setItem(item);
		initItemDetail(order, orderItem, suppGoods);

		if (item.getGoodType() != null && "localRoute".equals(item.getGoodType())) {// orderitem标识是否为关联销售商品
			orderItem.putContent("relatedMarketingFlag", "localRoute");
		}
		// 结算价code，买断价code存入大字段
		orderItem.putContent(SUPPGOODS_KEY.settlementCode.name(), suppGoods.getSettlementEntityCode());
		orderItem.putContent(SUPPGOODS_KEY.buyoutSettlementCode.name(), suppGoods.getBuyoutSettlementEntityCode());
        //构建子订单扣存扣减记录与库存扣减的map
		 List<OrdOrderStock> ordOrderStockList =new ArrayList<OrdOrderStock>();
		 buildOrdeStockList(orderItem, suppGoods, order,timePrice, ordOrderStockList);
		 orderItem.setOrderStockList(ordOrderStockList);
		
		LOG.info("---initItem return orderItem----");
		return orderItem;
	}

	@Override
	public OrdOrderDTO buildOrderItem(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		// buyInfo.gets

		List<OrdOrderItem> ordOrderItemList = new ArrayList<OrdOrderItem>();

		SuppGoods suppGoodsMain = null;
		List<OrderItemAdditSuppGoods> OrderItemAdditSuppGoodsList = new ArrayList<OrderItemAdditSuppGoods>();
		for (OrderHotelCombBuyInfo.GoodsItem item : buyInfo.getGoodsList()) {

			SuppGoods suppGoods = (SuppGoods) queryThreadCache.get().get("goods_" + item.getGoodsId());

			// SuppGoods suppGoods = new SuppGoods();

			OrdOrderItem orderItem = initOrderItem(order, item, suppGoods);
			Preconditions.checkArgument(suppGoods.getPayTarget() != null, "payTaget not been null");
			// 主订单设置和计算
			if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == (suppGoods.getCategoryId())) {

				order.setPaymentTarget(suppGoods.getPayTarget());
				suppGoodsMain = suppGoods;
				orderItem.setMainItem("true");
				order.setFilterMainOrderItem(orderItem);

				// 添加bu计算，
				LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

				calcBuCode(order, suppGoodsMain);
				LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

			}else{
				orderItem.setMainItem("false");
			}
			orderItem.setOrderItemAdditSuppGoods(OrderItemAdditSuppGoodsList);
			ordOrderItemList.add(orderItem);
		}
		order.setOrderItemList(ordOrderItemList);
		// 初始化促销
		if (!buyInfo.getPromotionMap().isEmpty()) {
			boolean haveExcludeProm = false;
			boolean haveChannelProm = false;
			int promCount = 0;
			Set<String> promotionStr = buyInfo.getPromotionMap().keySet();
			for (String key : promotionStr) {
				List<Long> promotionIds = (List<Long>) buyInfo.getPromotionMap().get(key);
				if (CollectionUtils.isNotEmpty(promotionIds)) {
					// 对促销的list进行去重
					List<Long> prodIdsNew = new ArrayList<Long>();
					for (Long id : promotionIds) {
						if (!prodIdsNew.contains(id)) {
							prodIdsNew.add(id);
						}
					}
					OrderPromotionBussiness bussiness = orderOrderFactory.createInitPromition(key);
					LOG.info("bussiness.initPromotion params:key=" + key + ",promotionIds=" + prodIdsNew);
					List<OrdPromotion> list = bussiness.initPromotion(order, key, prodIdsNew);

					order.addOrdPromotions(key, list);
				}
			}
		} else {
			LOG.info("buyInfo.getPromotionMap().isEmpty()");
		}

	//	order.setOrderItemList(ordOrderItemList);
		return null;
	}

	@Override
	public OrdOrderDTO buildOrderPerson(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {

		Preconditions.checkNotNull(buyInfo.getTravellers(), "buyInfo.getTravellers() not been null");
		Preconditions.checkArgument(buyInfo.getTravellers().size() != 0, "traveler size not been empty");

		List<OrdPerson> ordPersonList = Lists.newArrayList();
		List<Person> list = buyInfo.getTravellers();
		for (Person person : list) {
			OrdPerson ordPerson = new OrdPerson();
			EnhanceBeanUtils.copyProperties(person, ordPerson);
			ordPersonList.add(ordPerson);
		}

		order.setOrdPersonList(ordPersonList);

		return null;
	}

	@Override
	public OrdOrderDTO buildOrderVisitTime(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		order.setVisitTime(buyInfo.getEarliestVisitTime());
		return null;
	}

	public OrdOrderDTO buildOrderLastCancelTime(OrdOrderDTO order, OrderHotelCombBuyInfo.GoodsItem item) {

		/**
		 * 从本地线程缓存里面拿取db命中的数据 避免多此查询数据库
		 */

		ProdRefundResponse prodRefund = (ProdRefundResponse) queryThreadCache.get().get("prodRefund");
		Date lastCancelTime = DateUtils.addMinutes(item.getCheckInDate(), -prodRefund.getCancelTime().intValue());
		if (item.getLastCancelTime() == null || lastCancelTime.before(item.getLastCancelTime())) {
			item.setLastCancelTime(lastCancelTime);
		}

		return null;
	}

	@Override
	public OrdOrderDTO buildOrderCancelStrategy(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		HotelProductVo hotelProdVoResponse = (HotelProductVo) queryThreadCache.get().get("hotelProdProduct");
		// ResultHandleT<ProdProduct> resultProduct = productClientService
		// .findProdProductByIdFromCache(order.getProductId());

		ProdRefundRequest prr = new ProdRefundRequest();

		prr.setProductId(order.getProductId());
		prr.setVisitDate(order.getVisitTime());

		Map<String, Object> threadObjCache = queryThreadCache.get();

		ProdRefundResponse hotelCombProdRefund = (ProdRefundResponse) threadObjCache.get("prodRefund");

		ProdRefund refund = new ProdRefund();

		EnhanceBeanUtils.copyProperties(hotelCombProdRefund, refund);

		// if (refund == null) {
		// LOG.error("prodProduct product id:{} 无退改策略"
		// + order.getProductId());
		// throw new BusinessException(ErrorCodeMsg.ERR_PROREFUND_001);
		// }
		order.setRealCancelStrategy(refund.getCancelStrategy());
		this.setCancelStrategyToOrderItem(order, refund);
		return null;

	}

	@Override
	public OrdOrderDTO buildOrderLatestPayedForWait(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		return null;
	}

	public void initItemDetail(OrdOrderDTO order, OrdOrderItem orderItem, SuppGoods suppGoods) {
		initOrderItemBase(order, orderItem, suppGoods);
		// 出团公告
		if (orderItem.getOrderPack() == null) {
			OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(
					OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(),
					OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
			order.addOrdAdditionStatus(ordAdditionStatus);
		}
		ProdProductParam param = new ProdProductParam();
		param.setLineRoute(true);
		

		// ResponseBody<HotelProductVo> hotelProductVoResponse =
		// hotelProductQueryApiService.getProdProductBy(new
		// RequestBody<Long>().setTFlowStyle(orderItem.getProductId()));
		// hotelProductVoResponse.getT().getprodLin
		// / ResultHandleT<ProdProduct>
		// product=prodProductClientService.findLineProductByProductId(orderItem.getProductId(),
		// param);
		// if (product!=null && product.getReturnContent()!=null &&
		// CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList()))
		// {//根据Item的产品ID获取其产品信息
		// ProdLineRouteVO route =
		// product.getReturnContent().getProdLineRouteList().get(0);
		// if(route!=null){//获取产品上的线路信息
		// orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_days.name(),
		// route.getRouteNum());
		// orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_nights.name(),
		// route.getStayNum());
		// }
		// }
		//
		//
		//
		// orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(),
		// orderItem.getSuppGoods().getProdProduct().getProductType());
	}

	private void initOrderItemBase(OrdOrderDTO order, OrdOrderItem orderItem, SuppGoods suppGoods) {

		orderItem.setSuppGoods(suppGoods);
		HotelProductVo hotelProduct = (HotelProductVo) queryThreadCache.get().get("hotelProdProduct");

		orderItem.setBranchId(suppGoods.getProdProductBranch().getProductBranchId());
		// 产品经理ID
		if (suppGoods.getManagerId() == null) {
			LOG.error("supp_goods_id:" + suppGoods.getSuppGoodsId() + " have no manager_id");
		} else {
			orderItem.setManagerId(suppGoods.getManagerId());
		}
		orderItem.setBuCode(suppGoods.getBu());// 赋予商品真实BU，改字段值根据业务逻辑判断是否改变
		orderItem.setRealBuType(suppGoods.getBu());// 赋予商品真实BU
		orderItem.setAttributionId(suppGoods.getAttributionId());// 赋予商品归属地
		// 凭证确认状态
		orderItem.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.UNCONFIRMED.name());

		// 取消凭证确认
		orderItem.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());

		// orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());

		// 扣款类型
		orderItem.setDeductType(SuppGoodsTimePrice.DEDUCTTYPE.NONE.name());

		orderItem.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());

		orderItem.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());

		// 传真备注，设置在订单子项中
		String faxMemo = suppGoods.getFaxRemark();
		if (faxMemo != null && !"".equals(faxMemo)) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxMemo);
		}
		// 合同ID
		orderItem.setContractId(suppGoods.getContractId());

		// 产品ID
		orderItem.setProductId(suppGoods.getProductId());

		// 供应商ID
		orderItem.setSupplierId(suppGoods.getSupplierId());

		// 产品名称
		orderItem.setProductName(suppGoods.getProdProduct().getProductName());

		// 商品名称
		orderItem.setSuppGoodsName(suppGoods.getGoodsName());

		// 供应商产品名称 酒店套餐 注释掉这个属性 已经没有了
		// orderItem.setSuppProductName(suppGoods.getProdProduct()
		// .getSuppProductName());

		// 履行状态
		orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());

		// 结算状态
		orderItem.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name());

		// 信息状态-未确认
		orderItem.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());

		// 品类code
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(),
				suppGoods.getProdProduct().getBizCategory().getCategoryCode());

		// 添加子订单流程key
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(),
				suppGoods.getProdProduct().getBizCategory().getProcessKey());

		// 供应商标识
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name(), suppGoods.getApiFlag());

		// order.putApiFlag(suppGoods.getSuppGoodsId(),"Y".equals(suppGoods.getApiFlag()));

		String branchName = suppGoods.getProdProductBranch().getBranchName();

		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchAttachFlag.name(),
				suppGoods.getProdProductBranch().getBizBranch().getAttachFlag());
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchCode.name(),
				suppGoods.getProdProductBranch().getBizBranch().getBranchCode());
		orderItem.putContent("branchName", suppGoods.getProdProductBranch().getBranchName());
		// orderItem.putContent("prodAddress",hotelProduct.getAddress());
		orderItem.putContent("lastOrderTime", orderItem.getLastAheadTime());
		// hotelProduct.getProductPropList().
		// orderItem.putContent("prodTel",hotelProduct.gett);
    
		if (suppGoods.getProdProduct().getPropValue()!= null &&suppGoods.getProdProduct().getPropValue().get("address") != null   && suppGoods.getProdProduct().getPropValue().get("address")  instanceof String) {
			orderItem.putContent("prodAddress", hotelProduct.getPropValueMap().get("address").toString());
		}
		

		// 传真规则
		if (suppGoods.getFaxRuleId() != null) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_rule.name(), suppGoods.getFaxRuleId());
		}

		// 是否使用传真
		if (suppGoods.getFaxFlag() != null) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name(), suppGoods.getFaxFlag());
		}

		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), branchName.trim());

		orderItem.setNeedResourceConfirm("false");

		// 冗余下单时间
		orderItem.setCreateTime(order.getCreateTime());
		orderItem.setOrderUpdateTime(order.getOrderUpdateTime());
		List<OrdMulPriceRate> ordMulPriceRateList =  new ArrayList<OrdMulPriceRate>();
		//初始化OrdMulPriceRate
		if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(suppGoods.getCategoryId())) {
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name(),orderItem.getQuantity(),orderItem.getPrice(),OrdMulPriceRate.AmountType.PRICE.name());
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.MARKET_ADULT.name(),orderItem.getQuantity(),orderItem.getMarketPrice(),OrdMulPriceRate.AmountType.MARKET.name());
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name(),orderItem.getQuantity(),orderItem.getSettlementPrice(),OrdMulPriceRate.AmountType.SETTLEMENT.name());
			
		}else {
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.PRICE.name(),orderItem.getQuantity(),orderItem.getPrice(),OrdMulPriceRate.AmountType.PRICE.name());
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.MARKET_PRICE.name(),orderItem.getQuantity(),orderItem.getMarketPrice(),OrdMulPriceRate.AmountType.MARKET.name());
			addMulPriceRate(ordMulPriceRateList,ORDER_PRICE_RATE_TYPE.SETTLEMENT_PRICE.name(),orderItem.getQuantity(),orderItem.getSettlementPrice(),OrdMulPriceRate.AmountType.SETTLEMENT.name());
		}
		orderItem.setOrdMulPriceRateList(ordMulPriceRateList);
	}

	@Override
	public OrdOrderDTO buildMainWorkFlow(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		LOG.info("ActivitiAble=" + Constant.getInstance().isActivitiAble());
		if (Constant.getInstance().isActivitiAble()) {

			if (order.hasNeedPrepaid()) {
				if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == order.getCategoryId()) {
					order.setProcessKey("destbu_order_prepaid_main");

					for (OrdOrderItem orderItem : order.getOrderItemList()) {
						if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem
								.getCategoryId()) {// 酒店套餐子单
							orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb_new");
		                    orderItem.setConfirmStatus(Confirm_Enum.CONFIRM_STATUS.UNCONFIRM.name());

						} else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem
								.getCategoryId()) {// 酒店子单
							orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotel_new");
						} else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem
								.getCategoryId()) {// 酒店套餐子单
							orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb_new");
						}
					}
				}
			} else {
				order.setProcessKey("order_pay_main_process");
			}
		}
		return order;

	}

	@Override
	public OrdOrderDTO buildOrderPaymentType(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		if (order.hasNeedPrepaid()) {
			OrdOrderItem orderItem = order.getOrderItemList().get(0);
			Date aheadTime = orderItem.getAheadTime();
			order.setApproveTime(order.getCreateTime());

			order.setWaitPaymentTime(OrdOrderUtils.calcWaitPaymentTimeForDestBu(aheadTime, order.getCreateTime()));// 等待时间

		}
		return order;
	}

	@Override
	public OrdOrderDTO buildOrderResourceConfirmStatus(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		LOG.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++" + order);
		if (order != null && CollectionUtils.isNotEmpty(order.getOrderItemList())) {
			LOG.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++" + order.getOrderItemList().size());
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if(!(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem
						.getCategoryId())){
					  if(CollectionUtils.isNotEmpty(orderItem.getOrderStockList())){

		                    for(OrdOrderStock stock:orderItem.getOrderStockList()){
		                        if("true".equalsIgnoreCase(stock.getNeedResourceConfirm())){
		                            orderItem.setNeedResourceConfirm("true");
		                            break;
		                        }
		                    }
		                    String status=orderItem.getOrderStockList().get(0).getResourceStatus();
		                    int size = orderItem.getOrderStockList().size();
		                    if(size>1){
		                        for(int i=1;i<orderItem.getOrderStockList().size();i++){
		                            status = getOrderResourceStatus(status, orderItem.getOrderStockList().get(i).getResourceStatus());
		                        }
		                    }
		                    if(orderItem.getCategoryId().equals(99L)){
		                        orderItem.setResourceStatus("AMPLE");
		                    }else{
		                        orderItem.setResourceStatus(status);
		                    }
		                 

		                }
					
				}
		
				if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == orderItem
						.getCategoryId()) {
					BaseTimePrice timePrice = (BaseTimePrice) queryThreadCache.get()
							.get("time_price_goods_" + orderItem.getSuppGoodsId());
					Preconditions.checkArgument(timePrice != null, "时间价格不能为空");
					Log.info("timePrice.getResrouseStatus()====" + timePrice.getResrouseStatus());
					orderItem.setResourceStatus(timePrice.getResrouseStatus());
					if(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(timePrice.getResrouseStatus())){
						orderItem.setNeedResourceConfirm("true");
					}else{
						orderItem.setNeedResourceConfirm("false");
					}
					order.setResourceStatus(timePrice.getResrouseStatus());
				} else {
					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
				}
				   //计算主订单的资源状态
                setOrderResourceStatus(orderItem, order);
			}
			// 计算主订单的
             
		}
		return order;
	}

	@Override
	public OrdOrderDTO buildOrderAmmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		long totalAmount = 0L;
		long totalSettlement = 0L;
		// 分销展示vst价格
		long totalVstAmount = 0l;
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
			if (orderItem.getTotalSettlementPrice() == null) {
				orderItem.setTotalSettlementPrice(orderItem.getActualSettlementPrice() * orderItem.getQuantity());
			}
			if (orderItem.getTotalAmount() == null) {
				orderItem.setTotalAmount(orderItem.getPrice() * orderItem.getQuantity());

				LOG.info("-------------------------------------------------------" + orderItem.getTotalAmount()
						+ "orderItem.getPrice()" + orderItem.getPrice() + "orderItem.getQuantity()");
			}
			   if("Y".equals(orderItem.getBuyoutFlag())){
	                long totalQuantity = orderItem.getQuantity();
	                long preQuantity = orderItem.getBuyoutQuantity();
	                if(totalQuantity>preQuantity){
	                    Long notBuyoutTotalPrice = orderItem.getNotBuyoutSettleAmout();
	                    notBuyoutTotalPrice = notBuyoutTotalPrice==null?0L:notBuyoutTotalPrice;
	                    orderItem.setTotalSettlementPrice(orderItem.getBuyoutTotalPrice() + notBuyoutTotalPrice);
	                    orderItem.setSettlementPrice((long)orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
	                    orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
	                }
	            }

	            //end


	            LOG.debug("........calc........."+orderItem.getSuppGoodsId()+"["+orderItem.hashCode()+"]\t"+orderItem.getSuppGoodsName()+"\ttotalamount="+orderItem.getTotalAmount());
	 
			totalAmount += orderItem.getTotalAmount();
			totalVstAmount += orderItem.getPrice() * orderItem.getQuantity();
			totalSettlement += orderItem.getTotalSettlementPrice();
		}
		OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE,
				OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER, totalVstAmount);
		order.addOrderAmountItem(item);
		item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE,
				OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER, totalSettlement);
		order.addOrderAmountItem(item);
		// 分销优惠价
		long distributorPrice = totalVstAmount - totalAmount;

		item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE,
				OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION, distributorPrice);
		order.addOrderAmountItem(item);
		LOG.info("distributor discount price=======" + distributorPrice);

		LOG.info("totalAmount=======" + totalAmount);
		order.setOughtAmount(totalAmount);
		return null;
	}

	private OrdOrderAmountItem makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE type, OrderEnum.ORDER_AMOUNT_NAME name,
			long totalAmount) {
		OrdOrderAmountItem item = new OrdOrderAmountItem();
		item.setItemAmount(totalAmount);
		item.setOrderAmountType(type.name());
		item.setItemName(name.getCode());
		return item;
	}

	@Override
	public OrdOrderDTO buildManagerId(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		Long managerId = null;
		if (managerId == null) {
			OrdOrderItem mainOrderItem = null;
			List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
			if (CollectionUtils.isNotEmpty(ordOrderItemList)) {
				for (OrdOrderItem item : ordOrderItemList) {
					if ("true".equalsIgnoreCase(item.getMainItem())) {
						mainOrderItem = item;
						break;
					}
				}
			}
			if (mainOrderItem != null) {
				managerId = mainOrderItem.getManagerId();
			}
		}

		// 3.设置值.
		if (managerId != null) {
			order.setManagerId(managerId);
		} else {
			LOG.error("no manager_id");
		}
		return null;
	}

	@Override
	public OrdOrderDTO buildRebate(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		try {
			calcRebate(order);
		} catch (Exception e) {
			order.setMobileMoreRebate(0L);
			order.setRebateAmount(0L);
			order.setRebateFlag("N");
			LOG.error("DTOProductServiceImpl.buildRebate error no Rebate orderId:"+order.getOrderId()+" !Msg:"+e.getMessage());
			e.printStackTrace();
		}
		return order;
	}

	@Override
	public OrdOrderDTO buildtravelContract(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		// 订单所有合同集合
		List<OrdTravelContract> contracts = new ArrayList<OrdTravelContract>();
		Long distributorId = order.getDistributorId();// 销售渠道ID

		createOrderTravelContract(
				CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts,
				order.getOrderItemList(), distributorId);
		if (CollectionUtils.isNotEmpty(contracts)) {
			LOG.info("contracts size = " + contracts.size());
		} else {
			throw new BusinessException("初始化电子合同出现异常");
		}
		// 订单合同
		order.setOrdTravelContractList(contracts);
		return null;
	}

	@Override
	public OrdOrderDTO checkTestOrder(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
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
				emergencyPersonName = buyInfo.getEmergencyPerson().getFullName();
			} catch (NullPointerException e) {

			}
			// 如果购买人和联系人都不包含测试下单，那么就在游玩人中查找是否包含。直接包含直接设置成测试订单的标记。
			if (contactName.contains(orderValue) || emergencyPersonName.contains(orderValue)) {
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
		order.setIsTestOrder(isTest);
		return order;
	}

	public OrdOrderDTO buildOrderViewStatus(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		initDestBuViewStatusAndEndTime(order, buyInfo.getProdLineRoute());
		return order;
	}

	/**
	 * 计算目的地订单展示状态和结束时间
	 * 
	 * @param order
	 * @param prodLineRoute
	 */
	public void initDestBuViewStatusAndEndTime(OrdOrder order,
			com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.ProdLineRoute prodLineRoute) {
		Log.info("######start initDestBuViewStatusAndEndTime and orderId is" + order.getOrderId() + "categoryId is"
				+ order.getCategoryId());
		boolean isDestBu = false;
		isDestBu = OrdOrderUtils.isAllDestBuFrontOrder(order);
		if (isDestBu) {
			// 设置订单展示状态
			if (SuppGoods.PAYTARGET.PREPAID.getCode().equals(order.getPaymentTarget())) {
				order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.getCode());
			} else {
				if (null != order.getVisitTime()) {
					order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.getCode());
				}
			}
			Log.info("setDsetOrderViewStatus method result is " + order.getViewOrderStatus() + "paymentTarget="
					+ order.getPaymentTarget());
			// 设置订单结束时间
			calDestBuOrderEndTime(order, prodLineRoute);
		}

	}

	/**
	 * 计算目的地BU订单的结束时间/离店时间
	 * 
	 * @param order
	 */
	private void calDestBuOrderEndTime(OrdOrder order,
			com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.ProdLineRoute prodLineRoute) {
		Log.info("-----------START METHOD calDestBuOrderEndTime() by DESTBU ORDER-----------");
		Date endTime = null;// 订单结束时间
		int routeNum = 0;
		if (null != prodLineRoute) {
			routeNum = prodLineRoute.getDayNum();
			if (routeNum > 0) {
				endTime = DateUtil.addDays(order.getVisitTime(), routeNum);
				endTime = DateUtil.toYMDDate(endTime);
				order.setEndTime(endTime);
			}
		}
		if (null != endTime) {
			Log.info("endTime is " + DateUtil.formatSimpleDate(endTime));
		}
		Log.info("-----------END METHOD calDestBuOrderEndTime() by DESTBU ORDER-----------");
	}

	public OrdOrderDTO buildOrderItemPersonRelation(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		Map<Long, List<OrdPerson>> goodsOrderPersonRelationMap = Maps.newHashMap();

		Iterator<Map.Entry<Long, List<Person>>> iter = buyInfo.getGoodsPersonListMap().entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<Long, List<Person>> entry = iter.next();

			List<OrdPerson> orderRelationPersonList = goodsOrderPersonRelationMap.get(entry.getKey());
			if (orderRelationPersonList == null) {
				orderRelationPersonList = Lists.newArrayList();
			}

			if (entry.getValue() != null) {
				for (Person person : entry.getValue()) {
					OrdPerson ordPerson = new OrdPerson();
					EnhanceBeanUtils.copyProperties(person, ordPerson);
					orderRelationPersonList.add(ordPerson);
				}
			}

			goodsOrderPersonRelationMap.put(entry.getKey(), orderRelationPersonList);
		}
		order.setGoodsPersonRelationMap(goodsOrderPersonRelationMap);
		return null;
	}

	/*
	 * 门票退改策略
	 * 
	 * @param orderItem
	 * 
	 * @param suppGoodsClientService
	 */
	public void setTicketRefund(OrdOrderItem orderItem) {
		if (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().longValue() != orderItem.getSuppGoods()
				.getCategoryId().longValue() && // 景点门票
				BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue() != orderItem.getSuppGoods()
						.getCategoryId().longValue()
				&& // 组合套餐票
				BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().longValue() != orderItem.getSuppGoods()
						.getCategoryId().longValue()
				&& // 其他票
				BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().longValue() != orderItem.getSuppGoods()
						.getCategoryId().longValue()) { // 演出票
			setOtherRefund(orderItem);
			return;
		}
		List<SuppGoodsRefund> list = suppGoodsClientService.getTicketRefund(orderItem.getSuppGoodsId());
		SuppGoodsRefund refund = null;
		Integer lastestCancelTime = null;// 默认最晚取消时间
		boolean isOther = false;
		Calendar cal = Calendar.getInstance();
		// 找到最匹配的最晚取消时间
		for (SuppGoodsRefund suppGoodsRefund : list) {
			refund = suppGoodsRefund;
			if (SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(suppGoodsRefund.getCancelTimeType())) {
				isOther = true;
				cal.set(Calendar.YEAR, 2099);// 默认设置为2099年
				break;
			} else {
				if (suppGoodsRefund.getLatestCancelTime() != null) {
					int tempCancelTime = suppGoodsRefund.getLatestCancelTime().intValue();
					if (lastestCancelTime == null || tempCancelTime < lastestCancelTime) {
						lastestCancelTime = tempCancelTime;
					}
				}
			}
		}
		if (lastestCancelTime != null) {
			if (isOther) {
				orderItem.setLastCancelTime(cal.getTime());
			} else {
				orderItem.setLastCancelTime(DateUtils.addMinutes(orderItem.getVisitTime(), -lastestCancelTime));
			}
		} else {
			if (isOther) {
				orderItem.setLastCancelTime(cal.getTime());
			}
		}
		if (refund != null) {
			orderItem.setCancelStrategy(refund.getCancelStrategy());
			if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name()
					.equalsIgnoreCase(refund.getCancelStrategy())) {
				orderItem.setDeductType(refund.getDeductType());
				if (refund.getDeductValue() != null && refund.getDeductType() != null) {
					if (SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(refund.getDeductType())) {
						orderItem.setDeductAmount(
								orderItem.getPrice() * orderItem.getQuantity() * refund.getDeductValue() / 10000);
					} else {
						orderItem.setDeductAmount(orderItem.getQuantity() * refund.getDeductValue());
					}
				}
			}
			// 商品退改规则快照-----品类@门票
			if (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()
					.equals(orderItem.getSuppGoods().getCategoryId())
					|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()
							.equals(orderItem.getSuppGoods().getCategoryId())
					|| BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
							.equals(orderItem.getSuppGoods().getCategoryId())
					|| BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()
							.equals(orderItem.getSuppGoods().getCategoryId())) {
				if (list != null && list.size() > 0) {
					try {
						String jsonSt = JSONArray.fromObject(list).toString();
						if (jsonSt.length() >= 4000) {
							LOG.warn("SuppGoods [" + orderItem.getSuppGoodsId()
									+ "]'s refundRules_json is out of size 4000/" + jsonSt.length() + " .Has ["
									+ list.size() + "] refund rules.");
							jsonSt = jsonSt.substring(0, 3999);
						}
						orderItem.setRefundRules(jsonSt);
						// orderItem.setDeductAmount(null);
						// orderItem.setDeductType(null);
					} catch (Exception ex) {
						LOG.error(ExceptionFormatUtil.getTrace(ex));
						LOG.error("设置商品[" + orderItem.getSuppGoodsId() + "]退改规则快照时异常。" + ex.getMessage());
					}
				}
			}
		}
	}

	/**
	 * 其他退改策略
	 * 
	 * @param orderItem
	 * @param suppGoodsClientService
	 */
	public void setOtherRefund(OrdOrderItem orderItem) {

		List<SuppGoodsRefund> list = suppGoodsClientService.getTicketRefund(orderItem.getSuppGoodsId());
		if (!list.isEmpty()) {
			SuppGoodsRefund suppGoodsRefund = list.get(0);
			orderItem.setCancelStrategy(suppGoodsRefund.getCancelStrategy());
			if (suppGoodsRefund.getLatestCancelTime() != null) {
				orderItem.setLastCancelTime(DateUtils.addMinutes(orderItem.getVisitTime(),
						-suppGoodsRefund.getLatestCancelTime().intValue()));
			}
			if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name()
					.equalsIgnoreCase(suppGoodsRefund.getCancelStrategy())) {
				orderItem.setDeductType(suppGoodsRefund.getDeductType());
				if (suppGoodsRefund.getDeductValue() != null && suppGoodsRefund.getDeductType() != null) {
					if (SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefund.getDeductType())) {
						orderItem.setDeductAmount(orderItem.getPrice() * orderItem.getQuantity()
								* suppGoodsRefund.getDeductValue() / 10000);
					} else {
						orderItem.setDeductAmount(orderItem.getQuantity() * suppGoodsRefund.getDeductValue());
					}
				}
			}
			// 商品退改规则快照-----品类@门票
			if (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().longValue() == orderItem.getSuppGoods()
					.getCategoryId().longValue() || // 景点门票
					BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue() == orderItem
							.getSuppGoods().getCategoryId().longValue()
					|| // 组合套餐票
					BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().longValue() == orderItem
							.getSuppGoods().getCategoryId().longValue()
					|| // 其他票
					BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().longValue() == orderItem
							.getSuppGoods().getCategoryId().longValue()) { // 演出票
				if (list != null && list.size() > 0) {
					try {
						String jsonSt = JSONArray.fromObject(list).toString();
						if (jsonSt.length() >= 4000) {
							LOG.warn("SuppGoods [" + orderItem.getSuppGoodsId()
									+ "]'s refundRules_json is out of size 4000/" + jsonSt.length() + " .Has ["
									+ list.size() + "] refund rules.");
							jsonSt = jsonSt.substring(0, 3999);
						}
						orderItem.setRefundRules(jsonSt);
						// orderItem.setDeductAmount(null);
						// orderItem.setDeductType(null);
					} catch (Exception ex) {
						LOG.error(ExceptionFormatUtil.getTrace(ex));
						LOG.error("设置商品[" + orderItem.getSuppGoodsId() + "]退改规则快照时异常。" + ex.getMessage());
					}
				}
			}
		}
	}
  public void initBuyOutTimeForOrderItem(SuppGoods suppGoods,BuyOutTimePrice timePriceBuyout,OrdOrderItemDTO orderItem ){
	  if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == (suppGoods.getCategoryId())) {

			// Map<String,Object> threadObjCache = queryThreadCache.get();
			// ProdRefundResponse hotelCombProdRefund =
			// (ProdRefundResponse)threadObjCache.get("prodRefund");
			// ProdRefund refund = new ProdRefund();
			// EnhanceBeanUtils.copyProperties(hotelCombProdRefund,refund);
			// String jsonSt = getRefundRulesByOrderItem(null,refund);
			// orderItem.setRefundRules(jsonSt);
			// orderItem.setCancelStrategy(refund.getCancelStrategy());
			// orderItem.setBuyoutFlag(timePrice.getBuyoutFlag());
			if ((SuppGoods.BIZ_STOCK_TYPE.SHARE_STOCK.name().equals(suppGoods.getStockType()))) {
				// List<>
			} else {
				List<Map<Long, BuyOutTimePrice>> listBuyOutTimePrice = timePriceBuyout.getBuyOutTimePriceList();
				Map<Long, BuyOutTimePrice> buyOutTimePriceMap = listBuyOutTimePrice.get(0);
				BuyOutTimePrice buyOutTimePrice = buyOutTimePriceMap.get(suppGoods.getSuppGoodsId());
				orderItem.setBuyoutFlag(buyOutTimePrice.getBuyoutFlag());
				orderItem.setBuyoutPrice(buyOutTimePrice.getBuyoutPrice());
				orderItem.setBuyoutQuantity(buyOutTimePrice.getBuyoutQuantity());
				orderItem.setBuyoutTotalPrice(buyOutTimePrice.getBuyoutTotalPrice());
				orderItem.setNotBuyoutSettleAmout(buyOutTimePrice.getNotBuyoutSettleAmout());
				orderItem.setNebulaProjectId(buyOutTimePrice.getNebulaProjectId());
			}
		} else {
			// 退改
			// this.setTicketRefund(orderItem);
			orderItem.setBuyoutFlag(timePriceBuyout.getBuyoutFlag());
			orderItem.setBuyoutPrice(timePriceBuyout.getBuyoutPrice());
			orderItem.setBuyoutQuantity(timePriceBuyout.getBuyoutQuantity());
			orderItem.setBuyoutTotalPrice(timePriceBuyout.getBuyoutTotalPrice());
			orderItem.setNotBuyoutSettleAmout(timePriceBuyout.getNotBuyoutSettleAmout());
		}
  } 
  /**
	 * @param list   
	 * @param priceType   价格类型
	 * @param quantity    数量
	 * @param settlePrice 结算价
	 * @param amoutType   
	 */
	protected void addMulPriceRate(List<OrdMulPriceRate> list,String priceType ,Long quantity ,Long settlePrice ,String amoutType){
		OrdMulPriceRate price2 = new OrdMulPriceRate();
		//非买断结算价
		price2.setPriceType(priceType);
		price2.setQuantity((long)quantity);
		price2.setPrice(settlePrice);
		price2.setAmountType(amoutType);
		list.add(price2);
	}
	@Override
    public OrdOrderDTO buildPromotion(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){
    	  if(MapUtils.isNotEmpty(order.getPromotionMap())){
              long discountAmount=0;
              //支付渠道
              String paymentChannel=null;
              for(String key:order.getPromotionMap().keySet()){
                  List<OrdPromotion> list = order.getPromotionMap().get(key);
                  for(OrdPromotion op:list){
                      if(op.getPromFavorable().hasApplyAble()){
                          long amount = op.getPromFavorable().getDiscountAmount();
                          discountAmount+=amount;
                          op.setFavorableAmount(amount);
                          LOG.info("促销id----"+op.getOrdPromotionId()+"--amount:"+amount+"---promotionType:"+op.getPromotion().getPromitionType());
                          //orderSaveService.setOrdPromotionFavorableAmount(op);
                          if(Constant.ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.name().equalsIgnoreCase(op.getPromotion().getPromitionType())){
                              if(StringUtils.isNotEmpty(paymentChannel)){
                                  throwIllegalException("渠道促销一订单只允许使用一次");
                              }
                              paymentChannel = op.getPromotion().getChannelOrder();
                              LOG.info("promotion---paymentChannel:"+paymentChannel);

                          }
                      }
                  }
              }
              LOG.info("order.getOughtAmount():"+order.getOughtAmount());
              LOG.info("promotion---discountAmount:"+discountAmount);
              if(paymentChannel!=null){
                  order.setPromPaymentChannel(paymentChannel);
              }
              if(discountAmount>0){
                  if(discountAmount>order.getOughtAmount()){
                      discountAmount = order.getOughtAmount();
                  }
                  
                  order.setOughtAmount(order.getOughtAmount()-discountAmount);
                  OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION,-discountAmount);
                  order.addOrderAmountItem(item);
              }
          }else{
              LOG.info("order.getPromotionMap() is null");
          }
		return null;


	  }
	@Override
	 public OrdOrderDTO buildBonus(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){
		 
		 if(buyInfo!=null){
	            String youhuiType = buyInfo.getYouhui();
	            if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
	                Float bonusYuan = buyInfo.getBonusYuan();
	                if(bonusYuan!=null){
	                    order.setBonusAmount(PriceUtil.convertToFen(bonusYuan));
	                }
	            }
	        }
		 return null;
	 }
	public void buildOrdeStockList(OrdOrderItem orderItem,SuppGoods suppGoods, OrdOrderDTO order,BaseTimePrice baseTimePrice,List<OrdOrderStock> stockList){
		if (!(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb
				.getCategoryId() == (suppGoods.getCategoryId()))){
		    OrderTimePriceService  orderTimePriceService = lookUpService.lookupTicketTimePrice(suppGoods.getCategoryId());
			SuppGoodsBaseTimePrice timePrice =null;
			
	//		EnhanceBeanUtils.copyProperties(baseTimePrice,timePrice);
			//保险
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId()==(suppGoods.getCategoryId())){
				timePrice = new SuppGoodsNotimeTimePrice();
				EnhanceBeanUtils.copyProperties(baseTimePrice,timePrice);
				if ("Y".equalsIgnoreCase(orderItem.getStockFlag())) {// 限库存在的
					order.addUpdateStock(timePrice, orderItem.getQuantity(), orderTimePriceService);
				}
			}else{
				//门票、组合票 其他票
				timePrice = new SuppGoodsAddTimePrice();
				//有库存限制
				EnhanceBeanUtils.copyProperties(baseTimePrice,timePrice);
				if("Y".equalsIgnoreCase(orderItem.getStockFlag())){
					if(timePrice.getStock()<orderItem.getQuantity()){//库存不满足
						if("N".equalsIgnoreCase(timePrice.getOversellFlag())){
							throwIllegalException("库存不足");
						}else{
							if(timePrice.getStock()>0){//存在部分库存
								OrdOrderStock stock = createStock(orderItem.getVisitTime(), timePrice.getStock());
								makeNotNeedResourceConfirm(stock);
								stockList.add(stock);
								order.addUpdateStock(timePrice, orderItem.getQuantity(), orderTimePriceService);
							}
							//超卖部分
							OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity()-timePrice.getStock());
							makeNeedResourceConfirm(stock);
							stockList.add(stock);
						}
					}else{//库存满足的情况下
						OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
						makeNotNeedResourceConfirm(stock);
						stock.setShareTotalStockId(baseTimePrice.getShareTotalStockId());
						stock.setShareDayLimitId(baseTimePrice.getShareDayLimitId());
						stockList.add(stock);
						order.addUpdateStock(timePrice, orderItem.getQuantity(), orderTimePriceService);
					}
				}else{//支持不限情况下
					OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
					makeNotNeedResourceConfirm(stock);
					stock.setInventory(OrderEnum.INVENTORY_STATUS.FREESALE.name());
					stockList.add(stock);
				}
				LOG.info("makeNeedResourceConfirm开始");	
				makeNeedResourceConfirm(orderItem, stockList);
				orderItem.setOrderStockList(stockList);
			}
			
			
		}
	}
	
    /**
     *  订单返现计算
     */
    private void calcRebate(OrdOrderDTO order){
        if(DefaultRebateConfig.getInstance().isNewRebate()){
            calcRebateBaseNew(order);
        }else{
            calcRebateBaseOld(order);
        }

    }
    @Autowired
    private OrderRebateBussiness orderRebateBussiness;
    private void calcRebateBaseNew(OrdOrderDTO order){
        orderRebateBussiness.calcRebate(order);
    }

    private void calcRebateBaseOld(OrdOrderDTO order){
        try {
            long totalRebateAmount=0L;
            Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
            Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
            String channel="";
            if(distributorId==2||distributorId==3){
                channel="pc";
            }
            if(distributorId==4){
                if(channelId==10000||channelId==10001||channelId==10002){
                    channel="mobile";
                }
                if(channelId==107||channelId==108||channelId==110||channelId==103){
                    channel="pc";
                }
            }
            if(channel.equals("")){
                return;
            }
            for(OrdOrderItem orderItem:order.getOrderItemList()){

                SuppGoodsRebate rebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(orderItem.getSuppGoodsId(), channel).getReturnContent();
                if(rebate!=null){
                    //pc端下单
                    if(channel.equals(OrderEnum.ORDER_CHANNEL.pc.getCode())){
                        totalRebateAmount+=pcGoodsRebate(orderItem, rebate);
                    }
                     //手机端下单
                    if(channel.equals(OrderEnum.ORDER_CHANNEL.mobile.getCode())){
                        //固定金额返现
                        if(rebate.getRebateType().equals(REBATE_TYPE.fixed.getCode())){
                            totalRebateAmount+=(rebate.getFixedAmount()*orderItem.getQuantity());
                        }else{
                            SuppGoodsRebate pcRebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(orderItem.getSuppGoodsId(), ORDER_CHANNEL.pc.getCode()).getReturnContent();
                            long pcRebateAmount = pcGoodsRebate(orderItem, pcRebate);

                            //全局倍率返现
                            if(rebate.getRebateType().equals(REBATE_TYPE.global.getCode())){
                                float globalRebate = suppGoodsRebateClientService.getGlobalRateRebate();
                                totalRebateAmount+= pcRebateAmount*globalRebate;
                            }
                            if(rebate.getRebateType().equals(REBATE_TYPE.more.getCode())){
                                long moreAmount = rebate.getMoreAmount();
                                totalRebateAmount+=pcRebateAmount+(moreAmount*orderItem.getQuantity());
                            }
                            if(rebate.getRebateType().equals(REBATE_TYPE.multiplyingPower.getCode())){
                                totalRebateAmount+=pcRebateAmount*rebate.getMultiplyingPowerAmount();
                            }
                        }

                    }
                }
            }
            totalRebateAmount =new BigDecimal(Math.ceil( PriceUtil.convertToYuan(totalRebateAmount))*100).longValue();
            //保存订单返现信息
            order.setRebateAmount(totalRebateAmount);
            order.setRebateFlag("N");
        } catch (Exception e) {
        	LOG.error(e.getMessage());
        }
    }



    public long pcGoodsRebate(OrdOrderItem orderItem,SuppGoodsRebate rebate){
        if(rebate!=null){
            //是否到付门票
            boolean payTicketFlag = suppGoodsClientService.checkPayTicket(orderItem.getSuppGoodsId());
            //固定金额返现
            if(REBATE_TYPE.fixed.getCode().equals(rebate.getRebateType())){
                long fixedAmount = rebate.getFixedAmount()==null?0:rebate.getFixedAmount();
                if(payTicketFlag){
                    //到付门票
                    return fixedAmount;
                }else{
                    //非到付门票固定金额返现返现金额*商品数量
                    return (fixedAmount*orderItem.getQuantity());
                }
            }else if(rebate.getRebateType().equals(REBATE_TYPE.rate.getCode())){
                //房差售价
                long priceSpread=0;
                //房差结算价
                long settlementSpread=0;
                long spreadQuantity = 0;
                //单个商品返现=毛利*折扣比例
                if(orderItem.getOrdMulPriceRateList()!=null){
                    for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
                        if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
                            priceSpread=rate.getPrice();
                            spreadQuantity = rate.getQuantity();
                        }
                        if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
                            settlementSpread=rate.getPrice();
                        }
                    }
                }
                //房差毛利
                long spreadProfit= (priceSpread-settlementSpread)*spreadQuantity;
                //返现毛利=总毛利-房差毛利
                long profit = orderItem.getTotalAmount()-orderItem.getTotalSettlementPrice()-spreadProfit;
                if(profit<0){
                    profit=0;
                }
                Long rebateFen =new BigDecimal((profit*(rebate.getRateAmount().floatValue()/100))).setScale(0,BigDecimal.ROUND_HALF_UP).longValue();
                long totalRebate = (rebateFen+99)/100*100;
                return totalRebate;
            }
        }
        return 0;
    }	
}
