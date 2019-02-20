package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.lvmama.comm.utils.JsonUtil;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductSaleReClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderPrice;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.prod.po.ProdProductSaleRe;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromResult;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.prom.po.PromotionEnum.AMOUNT_TYPE;
import com.lvmama.vst.back.service.VstPromotionOrderService;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.ACTIVITY_TYPE;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Coupon;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.TimePriceVO;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.order.service.ICouponService;
import com.lvmama.vst.order.service.IHotelTradeApiService;
import com.lvmama.vst.order.service.IOrderInitService;
import com.lvmama.vst.order.service.IOrderPriceService;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.util.PromtionUtil;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.QueryPaymentGatewayServiceAdapter;
import com.lvmama.vst.pet.vo.VstCashAccountVO;
import com.lvmama.vst.prom.dto.PromotionInfo;
import com.lvmama.vst.prom.dto.PromotionInfo.PromotionData;
import com.lvmama.vst.prom.dto.PromotionQueryDTO;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemData;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemData.SALE_UNIT;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemPromotionInfo;
import com.lvmama.vst.prom.dto.PromotionQueryDTO.ItemPromotionInfo.ITEM_TYPE;
import com.lvmama.vst.prom.response.Response;
import com.lvmama.vst.prom.response.ResponseInfoable;

import net.sf.json.JSONObject;

@Service
public class OrderPriceServiceImpl implements IOrderPriceService {

	private static final Logger logger = LoggerFactory.getLogger(OrderPriceServiceImpl.class);	
	/**
	 * @Autowired private SuppGoodsTimePriceService
	 * suppGoodsTimePriceService;//供应商商品时间价格服务
	 */
	@Autowired
	private DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;

	@Autowired
	private VstPromotionOrderService vstPromotionOrderServiceRemote;
	
	@Autowired
	private PromotionService promotionService;
	
	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientRemote;
	
	@Autowired
	private ICouponService couponService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Resource(name="orderLineTimePriceService")
	private OrderTimePriceService orderLineTimePriceService;
	
	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;
	@Autowired
	private IOrderInitService orderInitService;
	@Autowired
	private QueryPaymentGatewayServiceAdapter queryPaymentGatewayServiceAdapter;	
	@Autowired
	private PromotionBussiness promotionBussiness;
	@Autowired
	private PromBuyPresentBussiness promBuyPresentBussiness;
	@Autowired
	private IHotelTradeApiService hotelTradeApiService;
	@Autowired
	private NewHotelComOrderInitService newHotelComOrderInitService;
	@Autowired
	private ProdProductSaleReClientService prodProductSaleReClientService;
	
	public List<PromPromotion> findPromPromotion(OrdOrderDTO order){
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		try{
			if(order.getOrderPackList()!=null){
				logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "order.getOrderPackList() != null");
				for(OrdOrderPack pack:order.getOrderPackList()){
					OrdOrderPackDTO orderPack = (OrdOrderPackDTO)pack;
					//邮轮组合产品
					if(hasCruiseComb(orderPack.getProduct())){
						logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "邮轮组合产品");
						for(OrdOrderItem item :pack.getOrderItemList()){
							//次规格产品不参与促销
							if("Y".equals(item.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())){
								
								if(StringUtils.isNotEmpty((item.getSuppGoods().getProdProduct().getBizCategory().getPromTarget()))){
									List<PromPromotion> list = promotionBussiness.makeSuppGoodsPromotion(order, item,PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name());
									if(!list.isEmpty()) result.addAll(list);
								}
							}
						}
					} else {
						logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "非邮轮组合产品");
						List<PromPromotion> list = promotionBussiness.makeProductPromotion(order,pack);
						if(!list.isEmpty()) result.addAll(list);
					}
				}
			}
			if(org.apache.commons.collections.CollectionUtils.isNotEmpty(order.getNopackOrderItemList())){
				logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "order.getNopackOrderItemList() != null");
				for(OrdOrderItem orderItem:order.getNopackOrderItemList()){
					//次规格产品不参与促销
					if("Y".equals(orderItem.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())){
						if(StringUtils.isNotEmpty((orderItem.getSuppGoods().getProdProduct().getBizCategory().getPromTarget()))){
							List<PromPromotion> list = promotionBussiness.makeSuppGoodsPromotion(order,
									orderItem,PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name());
							if(!list.isEmpty()){
								result.addAll(list);
							}
						}
					}
				}
			}
			removeDuplicatePromotion(result) ;
		}catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		return result;
	}
	 //去重
	  private void removeDuplicatePromotion(List<PromPromotion> result ){
	    if(result==null||result.size()<=1){
	      return ;
	    }
	    Set<Long> set=new HashSet<Long>();
	    for(Iterator<PromPromotion> it = result.iterator(); it.hasNext(); ) {
	      PromPromotion pp = it.next();
	          if(set.add(pp.getPromPromotionId())==false){//已有了
	            logger.info("duplicate PromotionId :"+pp.getPromPromotionId());
	            it.remove();
	          }
	      } 
	  }
	



	
	


	
	@Override
	public ResultHandleT<List<PromPromotion>> queryPromPromotion(BuyInfo buyInfo){
		ResultHandleT<List<PromPromotion>> result = new ResultHandleT<List<PromPromotion>>();
		try {
			OrdOrderDTO order =orderInitService.initOrderAndCalc(buyInfo);
			List<PromPromotion> promotionList = findPromPromotion(order);
			result.setReturnContent(promotionList);
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
			result.setMsg(e);
		}
		return result;
	}
	
	@Override
	public ResultHandleT<List<PromPromotion>> checkPromAmount(BuyInfo buyInfo){
		ResultHandleT<List<PromPromotion>> result = new ResultHandleT<List<PromPromotion>>();
		List<PromPromotion> noExtPromList = new ArrayList<PromPromotion>();
		try {
			if(!buyInfo.getPromotionMap().isEmpty()){
				for(String key:buyInfo.getPromotionMap().keySet()){
					List<Long> promotionIds = buyInfo.getPromotionMap().get(key);
					String[] keys = key.split("_");
					Long amount = Long.valueOf(keys[3]);
					Long promotionId = promotionIds.get(0);
					logger.info("method checkPromAmount promotionId"+promotionId);
						PromPromotion promotion = promotionService.getPromPromotionById(promotionId);
						//已关闭的
						if(!"Y".equals(promotion.getValid())){
							noExtPromList.add(promotion);
							continue;
						}
						if(promotion.getPromAmount()!=null){
							Long usedAmount = promotion.getUsedAmount()==null?0L:promotion.getUsedAmount();
							Long balance = promotion.getPromAmount()-usedAmount;
							if(amount>balance){
								noExtPromList.add(promotion);
							}
						}
				}
				
				if(CollectionUtils.isEmpty(noExtPromList)){
					logger.info("noExtPromList.isEmpty()");
				}else{
					logger.info("noExtPromList is:"+GsonUtils.toJson(noExtPromList));
				}
				result.setReturnContent(noExtPromList);
			}else{
				logger.info("buyInfo.getPromotionMap().isEmpty()");
			}
		} catch (Exception e) {
			logger.error("OrderPriceServiceImpl.checkPromAmount error",e);
			result.setMsg(e);
		}
		return result;
	}
	
	
	@Override
	public PriceInfo countPrice(BuyInfo buyInfo){
		
		PriceInfo priceInfo = new PriceInfo();
		
		long orderMarketPrice = 0L;// 订单市场价金额
		long orderPrice = 0L;// 订单销售价金额
		long orderOughtPay = 0L;// 订单应付金额
		long promotionAmount=0L; //订单促销商品优惠总金额
		long couponAmount=0L; //订单优惠券抵扣总金额
		long quantitySum = 0L;
		long ticketAmount=0L;  
		long insurancePrice = 0L;
		long expressPrice=0L;
		long bonus=0L;
		long maxBonus=0L;
		long rebateAmount=0L;//点评返现金额
		long depositPrice=0L;
		long selfDrivingChildPrice=0L;//自驾游儿童价		
		
		//记录商品品类
		List<String> goodsCategorys = new ArrayList<String>();
		List<String> goodsCatetoryNameList = new ArrayList<String>();
		Long mainCategoryId = 0l;
		try{
			
			logger.info("countPriceStart buyinfo:" + buyInfo.toJsonStr());
			OrdOrderDTO order =orderInitService.initOrderAndCalcWithOutPromotion(buyInfo);
			orderPrice = order.getOughtAmount();
			rebateAmount=order.getRebateAmount();
			//修改毛利率是否大于0.00
			//计算毛利是否大于0.03
			boolean isCanBoundLipinkaPay = false;
			Long originalPrice = order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE.name());
			Long originalSettlePrice = order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
			if(originalPrice != null && originalSettlePrice != null) {
				isCanBoundLipinkaPay = (originalPrice > (originalSettlePrice * (1 + 0.00)));
			}
			priceInfo.setCanBoundLipinkaPay(isCanBoundLipinkaPay);
			priceInfo.setRebateAmount(rebateAmount);
			//end
			try {
				if((BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
						&&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()))){
					// 保存对接机票和其对应商品总价的键值对，用于后续验舱验价
					Map<String, String> flightTicketPriceMap = new HashMap<String, String>();
					for (OrdOrderItem orderItem : order.getOrderItemList()) {
						String categoryCode = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
						String supplierApiFlag = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name());
						//如果是“其他机票  && 对接”
						if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCode().equals(categoryCode) && "Y".equalsIgnoreCase(supplierApiFlag)){
							flightTicketPriceMap.put(orderItem.getSuppGoodsId().toString(), orderItem.getTotalAmount().toString());
						}
					}
					if(CollectionUtils.isEmpty(flightTicketPriceMap)){
						priceInfo.setFlightTicketPrice("");
					}else{
						priceInfo.setFlightTicketPrice(JSONObject.fromObject(flightTicketPriceMap).toString());
					}
				}
			} catch (Exception e1) {
				logger.error(e1.getMessage());
			}
			
			priceInfo.setPaymentTarget(order.getPaymentTarget());
			priceInfo.setPaymentType(order.getPaymentType());
			priceInfo.setResourceStatus(order.getResourceStatus());
			priceInfo.setDoBookPolicyStr(getCancelStrategy(order));
			
			//促销
			List<PromPromotion>  promotionList=getPromotions(order,priceInfo);
			
			BuyPresentActivityInfo buyPresentInfo =  null;
			//满赠
			if(buyInfo.isPromBuyFlag()){
				buyPresentInfo = promBuyPresentBussiness.findPromBuyPresent(order);
			}else{
				buyPresentInfo = promBuyPresentBussiness.findPromBuyPresentForOrder(order);
			}
			

			priceInfo.setBuyPresentActivityInfo(buyPresentInfo);
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			TimePrice hotelSuppGoodsTimePrice=null;
			for (OrdOrderItem ordOrderItem : orderItemList) {
				boolean isHotelPack = ordOrderItem.getOrderPack()==null;	
				if(hotelSuppGoodsTimePrice==null && isHotelPack){
						hotelSuppGoodsTimePrice= getsuppGoodsTimePriceByRules(orderItemList);	
				}
				long itemsPrice = 0 ; //一条商品的价格
				
				SuppGoods suppGoods = ordOrderItem.getSuppGoods();
				
				BizCategory category=suppGoods.getProdProduct().getBizCategory();
				String categoryCode = category.getCategoryCode();
				goodsCatetoryNameList.add(category.getCategoryName());
				if("true".equals(ordOrderItem.getMainItem())){
					mainCategoryId = ordOrderItem.getCategoryId();
				}
				//记录商品品类以及距离类型
				if(ProductCategoryUtil.isRoute(categoryCode)){
					String type = category.getCategoryCode()+"_"+suppGoods.getProdProduct().getProductType();
					goodsCategorys.add(type);
				}else{
					goodsCategorys.add(categoryCode);
				}
				
				// 酒店情况的时候
				if(ProductCategoryUtil.isHotel(categoryCode)){
					
//					BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
					Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
					List<OrdOrderHotelTimeRate> ordHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
					if (!CollectionUtils.isEmpty(ordHotelTimeRateList)) {
						 
						//endDate = DateUtils.addDays(endCal.getTime(), -1);
						
						//入住期间内最高单价
						long maxPrice=0;
						//入住期间内最高单价日
						Date highPriceDate=null;
						//全程担保
						TimePrice allTimePrice=null;
						
						//得到最早到点时间
						priceInfo.setEarliestArriveTime(getEarliestArriveTime(ordOrderItem.getVisitTime(), suppGoods));
						
						//退改政策
						String cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
						int i = 0;
						for (OrdOrderHotelTimeRate ordHotelTimeRate : ordHotelTimeRateList) {
							SuppGoodsTimePrice timePrice=(SuppGoodsTimePrice) ordHotelTimeRate.getTimePrice();
							long itemPrice = timePrice.getPrice() * ordOrderItem.getQuantity();//每个商品的价格
							itemsPrice += itemPrice;//同类商品的总价
							
							//商品促销优惠用
							BuyInfoPromotion.ItemPrice itemPc=new BuyInfoPromotion.ItemPrice(timePrice.getSpecDate());
							itemPc.setPrice(timePrice.getPrice());
							itemPriceMap.put(timePrice.getSpecDate(), itemPc);
							
							//入住期间内最高单价及日期
							if(itemPrice>maxPrice){
								maxPrice=itemPrice;
								highPriceDate=timePrice.getSpecDate();
							}
							//预付情况
							if(StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())){
								if(StringUtils.equals(timePrice.getCancelStrategy(),SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
									cancelStrategy = timePrice.getCancelStrategy();
								}
							//现付情况
							}else{
								//入住期间内全程担保
								if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePrice.getBookLimitType())){
									allTimePrice=new TimePrice();
									BeanUtils.copyProperties(timePrice,allTimePrice);
								}
								
								if(i==0){
									cancelStrategy = timePrice.getCancelStrategy();
								}
							}
							i++;
						}
						/*退订政策 start*/
						String doBookPolicyStr="";
						if(StringUtils.isEmpty(cancelStrategy)){
							cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
						}
						String earlyArrivalTime=ordOrderItem.getContentStringByKey(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name());
						TimePriceVO timePriceVO = new TimePriceVO();
						boolean hasHotelPack = ordOrderItem.getOrderPack()==null;
						long deductValue = 0;
						long productId = ordOrderItem.getProductId();
						if(null!=allTimePrice){
							if(hasHotelPack){
								doBookPolicyStr=this.getNewDoBookPolicy(hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
							
							    logger.info("------getNewDoBookPolicyPrice------productId----" + productId + "---orderPrice---" + orderPrice+ "----quality----"+ordOrderItem.getQuantity().intValue() +"----maxPrice---" + maxPrice +"--SuppGoodsTimePrice---"+ hotelSuppGoodsTimePrice.getPrice());
								deductValue = this.getNewDoBookPolicyPrice(productId,hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
								logger.info("------getNewDoBookPolicyPrice------productId----" + productId + "---deductValue---"+deductValue);
							}
							timePriceVO.setBookLimitType(allTimePrice.getBookLimitType());
							Long latestCancelTime = allTimePrice.getLatestCancelTime();
							Long latestUnguarTime = allTimePrice.getLatestUnguarTime();
							timePriceVO.setLatestCancelTime(latestCancelTime);
							timePriceVO.setLatestUnguarTime(latestUnguarTime);
							timePriceVO.setGuarType(allTimePrice.getGuarType());
							timePriceVO.setDeductType(allTimePrice.getDeductType());
							timePriceVO.setPrice(PriceUtil.trans2YuanStr(allTimePrice.getPrice() * ordOrderItem.getQuantity()));
							timePriceVO.setMaxPrice(PriceUtil.trans2YuanStr(maxPrice));
							timePriceVO.setHighPriceDate(DateUtil.formatSimpleDate(highPriceDate));
							timePriceVO.setDeductValue(deductValue);
							timePriceVO.setVisitTime(ordOrderItem.getVisitTime());
							timePriceVO.setGuarQuantity(allTimePrice.getGuarQuantity());
							
							
						}else{
							SuppGoodsTimePrice timePrice=(SuppGoodsTimePrice) ordHotelTimeRateList.get(0).getTimePrice();
							TimePrice firstTimePrice=new TimePrice();
							BeanUtils.copyProperties(timePrice,firstTimePrice);
							if(hasHotelPack){
								doBookPolicyStr=this.getNewDoBookPolicy(hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
								
								logger.info("------getNewDoBookPolicyPrice------productId----" + productId + "---orderPrice---" + orderPrice+ "----quality----"+ordOrderItem.getQuantity().intValue() +"----maxPrice---" + maxPrice +"--SuppGoodsTimePrice---"+ hotelSuppGoodsTimePrice.getPrice());								
								deductValue = this.getNewDoBookPolicyPrice(productId,hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
								logger.info("------getNewDoBookPolicyPrice------productId----" + productId + "---deductValue---"+deductValue);

							}
							 
							timePriceVO.setBookLimitType(timePrice.getBookLimitType());
							Long latestCancelTime = timePrice.getLatestCancelTime();
							Long latestUnguarTime = timePrice.getLatestUnguarTime();
							timePriceVO.setLatestCancelTime(latestCancelTime);
							timePriceVO.setLatestUnguarTime(latestUnguarTime);
							timePriceVO.setGuarType(timePrice.getGuarType());
							timePriceVO.setDeductType(timePrice.getDeductType());
							timePriceVO.setPrice(PriceUtil.trans2YuanStr(timePrice.getPrice() * ordOrderItem.getQuantity()));
							timePriceVO.setMaxPrice(PriceUtil.trans2YuanStr(maxPrice));
							timePriceVO.setHighPriceDate(DateUtil.formatSimpleDate(highPriceDate));
							timePriceVO.setDeductValue(deductValue);
							timePriceVO.setVisitTime(timePrice.getSpecDate());
							timePriceVO.setGuarQuantity(timePrice.getGuarQuantity());
							
						}
						
						priceInfo.setTimePriceVO(timePriceVO);
						if(hasHotelPack){
						 priceInfo.setDoBookPolicyStr(doBookPolicyStr);
						}
						/*退订政策 end*/
						 
					}
					priceInfo.setEarliestArriveTime(ordOrderItem.getContentStringByKey(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name()));
					
					//门票类(景点门票,其它票,组合套餐票(自主，供应商))
				}else if(ProductCategoryUtil.isTicket(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					ticketAmount+=itemsPrice;
				}else if(ProductCategoryUtil.isInsurance(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					insurancePrice+=itemsPrice;
				}else if(ProductCategoryUtil.isRoute(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isVisa(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isCruise(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isWifi(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isOther(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					if(PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(ordOrderItem))){
						depositPrice+=itemsPrice;
					}else{
						expressPrice+=itemsPrice;
					}
				}
				else{
					itemsPrice += getTotalAmount(ordOrderItem);
				}
				
				//国内景酒，解决一个商品被打包多次，或者一个商品及打包了也在可选商品中存在的问题
				priceInfo.getItemSiglePriceMap().put(
						ordOrderItem.getSuppGoodsId() + "_" + DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()), ordOrderItem.getPrice()); 
				
				priceInfo.getItemPriceMap().put(ordOrderItem.getSuppGoodsId(), PriceUtil.trans2YuanStr(itemsPrice));
				priceInfo.getItemMulPriceMap().put(ordOrderItem.getSuppGoodsId(), ordOrderItem.getOrdMulPriceRateList());
				quantitySum += ordOrderItem.getQuantity();// 购买商品数量总和
			}
			/*优惠券验证计算 start*/
			String youhuiType=buyInfo.getYouhui();
			if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)){
				List<Coupon> couponList=buyInfo.getCouponList();
				if(null!=couponList&&couponList.size()>0){
					buyInfo.setOrderTotalPrice(orderPrice);//设置订单总价
					List<ResultHandle> couponResultHandles=new ArrayList<ResultHandle>(2);
					
					//for (Coupon coupon : couponList) {
					Coupon coupon = couponList.get(0);
					if(StringUtil.isNotEmptyString(coupon.getCode())){
						Pair<FavorStrategyInfo, Object> resultPair=couponService.calCoupon(buyInfo);
						if(resultPair.isSuccess()){
							FavorStrategyInfo fsi=resultPair.getFirst();
							couponAmount+=fsi.getDiscountAmount();
							if (couponAmount==0) {
							Pair<FavorStrategyInfo, Long> resultPairNotUse=new Pair<FavorStrategyInfo, Long>();
									 resultPairNotUse.setMsg(fsi.getDisplayInfo());
									 couponResultHandles.add(resultPairNotUse);
								}
						    }else{
						    	couponResultHandles.add(resultPair);
						    }
						}
					//}
					priceInfo.setCouponResutHandles(couponResultHandles);
				}
			}
			/*优惠券验证计算end */

			/*自驾游儿童价计算start*/
			if(buyInfo.getSelfDrivingChildQuantity()>0){
				long selfDrivingChildPriceAmount=getSelfDrivingChildPriceAmount(order,buyInfo);
				if(selfDrivingChildPriceAmount>0){
					selfDrivingChildPrice=selfDrivingChildPriceAmount*buyInfo.getSelfDrivingChildQuantity();
				}
			}
			/*自驾游儿童价计算end*/
			
		
			logger.info("已经完成订单计算buyInfo.getUserNo:"+buyInfo.getUserNo()+",goodsCategorys:"+goodsCategorys);
			//获取订单可使用奖金金额
			try {
				maxBonus = ordUserOrderServiceAdapter.getOrderBonusCanPayAmount(buyInfo.getUserNo(), orderPrice, goodsCategorys);
				logger.info("-----------------------------------maxBonus:"+maxBonus);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
				logger.info("ordUserOrderServiceAdapter.getOrderBonusCanPayAmount userNo:"+buyInfo.getUserNo()+",orderPrice:"+orderPrice+",goodsCategorys:"+goodsCategorys);
				logger.info("maxBonus ======"+maxBonus);
				bonus=maxBonus;
				String target =buyInfo.getTarget();
				//如果是抵扣现金框触发
				if(StringUtils.isNotEmpty(target)&&target.equals(ORDER_FAVORABLE_TYPE.bonus.getCode())){
					bonus = PriceUtil.convertToFen(buyInfo.getBonusYuan());
					if(bonus>maxBonus){
						bonus=maxBonus;
					}
				}
			}
			logger.info("可用奖金计算完成---------------------------------");

			if(promotionList!=null){
				for(PromPromotion prom :promotionList){
					//排除掉邮轮促销，邮轮促销在前台页面通过用户手动选择
					if(!ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType())){
						promotionAmount+=prom.getDiscountAmount();
					}
				}
			}
			logger.info("应付金额计算"+"orderPrice:"+orderPrice+"couponAmount:"+couponAmount+"promotionAmount"+promotionAmount+"bonus"+bonus);
			orderOughtPay = orderPrice-couponAmount-promotionAmount-bonus;// 应付金额 
			if(orderOughtPay<1){
				orderOughtPay=0;
			}
			priceInfo.setGoodsTotalPrice(orderPrice-expressPrice-insurancePrice-depositPrice);
			priceInfo.setBonusYuan(PriceUtil.convertToYuan(bonus));
			priceInfo.setBonus(bonus);
			priceInfo.setMaxBonus(maxBonus);
			priceInfo.setCoupon(couponAmount);
			priceInfo.setMarketPrice(orderMarketPrice);
			priceInfo.setPrice(orderPrice);
			priceInfo.setOughtPay(orderOughtPay);
			priceInfo.setOrderQuantity(quantitySum);
			priceInfo.setPromotionAmount(promotionAmount);
			priceInfo.setTicketGoodsPrice(ticketAmount);
			priceInfo.setInsurancePrice(insurancePrice);
			priceInfo.setExpressPrice(expressPrice);
			priceInfo.setDepositPrice(depositPrice);
			priceInfo.setSelfDrivingChildPrice(selfDrivingChildPrice);			
			priceInfo.setGoodsCatetoryNameList(goodsCatetoryNameList);
			priceInfo.setMainCategoryId(mainCategoryId);
			priceInfo.setValidPromotionAmount(order.getValidPromtionAmount());
			
			

		}catch(OrderException ex){
			logger.error("=com.lvmama.vst.order.service.impl.OrderPriceServiceImpl.countPrice error:", ex);
			priceInfo.sendError(ex.getMessage());
		}
		logger.info("countPriceEnd orderOughtPay:"+orderOughtPay+",validPromotionAmount:"+priceInfo.getValidPromotionAmount());		
		
		return priceInfo;
	}
	//15,16,17,18走新促销
	private boolean isNewPromotion(BuyInfo buyInfo){
		logger.info("isNewPromotion categoryId"+buyInfo.getCategoryId()+" distributionId"+buyInfo.getDistributionId()+" isNewPromotion"+buyInfo.getIsNewPromotion());
		if(buyInfo.getCategoryId()==null||buyInfo.getDistributionId()==null){
			return false;
		}
		
		//品类是15. 16 . 17 .18
		if(buyInfo.getCategoryId()>=15l&&buyInfo.getCategoryId()<=18l){
			
			//无线走新促销标识
			if(null!=buyInfo.getIsNewPromotion()&&buyInfo.getIsNewPromotion()){
				return true;
			}
			
			//主站走新促销
			if(buyInfo.getDistributionId()==Constant.DIST_BACK_END || buyInfo.getDistributionId()==Constant.DIST_FRONT_END || buyInfo.getDistributionId()==Constant.DIST_O2O_SELL){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public List<PromPromotion> getPromotions(OrdOrderDTO order,PriceInfo priceInfo){
		List<PromPromotion> promotionList=null;
		BuyInfo buyInfo=order.getBuyInfo();
		if(isNewPromotion(buyInfo)){
			promotionList= getAvailablePromotions(order, priceInfo);
		}else{
			promotionList = findPromPromotion(order);
			buildPayChannelCnName(promotionList);	//如果是渠道优惠，绑定支付渠道中文名
		}
		if(buyInfo.getSubDistributorId()!=null){
			logger.info("buyInfo.getSubDistributorId()" +buyInfo.getSubDistributorId());
			promotionList=vstPromotionOrderServiceRemote.getOrderPromotionList(promotionList, buyInfo.getSubDistributorId());
			if(!CollectionUtils.isEmpty(promotionList)) {
				for (PromPromotion promPromotion : promotionList) {
					logger.info("$$" + buyInfo.getUserNo()  + "$$" + "分销筛选平台促销log" + promPromotion.getPromPromotionId());
				}
			} else {
					logger.info("$$" + buyInfo.getUserNo()  + "$$" + "分销筛选平台促销log信息为空");
			}
			if(promotionList!=null){
				Long subDistributorId = buyInfo.getSubDistributorId();
				if(subDistributorId == 107 ||subDistributorId == 108 ||subDistributorId == 110) {
					//检查促销可用余额是否满足
					Iterator<PromPromotion> it = promotionList.iterator();
					while(it.hasNext()){
						PromPromotion promPromotion = it.next();
						if(promPromotion.getPromAmount()!=null){
							long usedAmount = promPromotion.getUsedAmount()==null?0L:promPromotion.getUsedAmount();
							long balance =promPromotion.getPromAmount()-usedAmount;
							//活动可用余额大于等于促销金额才存
							logger.info("优惠条件剩余可用金额"+promPromotion.getPromAmount()+usedAmount+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());
							if (balance < promPromotion.getDiscountAmount()) {
								it.remove();
								logger.info("去除优惠条件剩余可用金额"+balance+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());

							}
						}
					}
					
				}
			}
		}
		priceInfo.setPromotionList(promotionList);
		return promotionList;
	}
	
	private List<PromPromotion> getAvailablePromotions(OrdOrderDTO order,PriceInfo priceInfo){
		List<PromPromotion> list =new ArrayList<PromPromotion>();
		try{
			BuyInfo buyInfo=order.getBuyInfo();
			PromotionQueryDTO pq=new PromotionQueryDTO();
			pq.setDistributorChannel(buyInfo.getDistributionChannel());
			pq.setDistributorId(buyInfo.getDistributionId());
			pq.setUserNo(buyInfo.getUserNo());			
			List<ItemPromotionInfo> itemPromotionInfoList=new ArrayList<ItemPromotionInfo>();
			ItemPromotionInfo ipi=null;
			List<OrdOrderPack> orderPackList=order.getOrderPackList();
			if(orderPackList!=null&&orderPackList.size()>0){//自由行，跟团游
				ipi= new ItemPromotionInfo();
				OrdOrderPack orderPack=orderPackList.get(0);
				if(orderPack.getVisitTime()==null){
					orderPack.setVisitTime(DateUtil.toSimpleDate(buyInfo.getVisitTime()));
				}	
				Map<String, Object> params = PromtionUtil.calcRouteAmount(orderPack);
				ipi.setPackageType(ITEM_TYPE.PRODUCT);
				ipi.setItemPromTarget(ITEM_TYPE.PRODUCT);
				ipi.setCategoryId(orderPack.getCategoryId());
				ipi.setSubCategoryId(buyInfo.getSubCategoryId());
				ipi.setItemId(orderPack.getProductId());
				ipi.setOwnPack(orderPack.getOwnPack());
				ItemData data=new ItemData();
				
				data.setVisitTime(orderPack.getVisitTime());
				if("true".equals(params.get("categoryIsRoute").toString())){
					data.setSaleUnit(SALE_UNIT.PEOPLE);
				}else{
					data.setSaleUnit(SALE_UNIT.COPIES);
				}
				data.setAdultPrice(Long.valueOf(params.get("adultPrice").toString()));
				if(params.get("adultQuantity")!=null){
					data.setAdultQuantity(Integer.valueOf(params.get("adultQuantity").toString()));
				}
				if(params.get("childQuantity")!=null){
					data.setChildQuantity(Integer.valueOf(params.get("childQuantity").toString()));
				}
				data.setChildPrice(Long.valueOf(params.get("childPrice").toString()));
				data.setNoMultiPrice(Long.valueOf(params.get("noMulPrice").toString()));
				data.setSaleType( (String)orderPack.getContentValueByKey("saleType"));
				data.setCopyQuantity(buyInfo.getQuantity());
				data.setVisitTime( orderPack.getVisitTime());
				data.setOrderDate(new Date());
				if(orderPack.getContentValueByKey("quantity")!=null){
					data.setQuantity(Integer.valueOf(orderPack.getContentValueByKey("quantity").toString()));
				}
				if(orderPack.getContentValueByKey("actualAmt")!=null){
					data.setActualAmount(Long.valueOf(orderPack.getContentValueByKey("actualAmt").toString()));
				}
		
				data.setTotalPrice(order.getValidPromtionAmount());
				ipi.setItemData(data);
				itemPromotionInfoList.add(ipi);
			}else if(order.getOrderItemList()!=null){//当地游 ,酒店套餐 （供应商打包）
				for(OrdOrderItem item:order.getOrderItemList()){
					if(PromtionUtil.validPromtionItem(item)){
						ipi= new ItemPromotionInfo();
						ipi.setPackageType(ITEM_TYPE.GOODS);
						BizCategory bc =item.getSuppGoods().getProdProduct().getBizCategory();
						if("PRODUCT".equals(bc.getPromTarget())){
							ipi.setItemPromTarget(ITEM_TYPE.PRODUCT);
						}else{
							ipi.setItemPromTarget(ITEM_TYPE.GOODS);
						}
						ipi.setCategoryId(item.getCategoryId());
						ipi.setItemId(item.getProductId());
						ItemData data=new ItemData();
						Map<String, Object> params = PromtionUtil.calcItemAmount(item);
						if(item.getCategoryId()==16l){//当地游
							data.setSaleUnit(SALE_UNIT.PEOPLE);
						}else{//酒店套餐
							data.setSaleUnit(SALE_UNIT.COPIES);
						}
						data.setAdultPrice(Long.valueOf(params.get("adultPrice").toString()));
						if(params.get("adultQuantity")!=null){
							data.setAdultQuantity(Integer.valueOf(params.get("adultQuantity").toString()));
						}
						if(params.get("childQuantity")!=null){
							data.setChildQuantity(Integer.valueOf(params.get("childQuantity").toString()));
						}
						data.setChildPrice(Long.valueOf(params.get("childPrice").toString()));
						data.setNoMultiPrice(Long.valueOf(params.get("noMulPrice").toString()));
						data.setVisitTime(item.getVisitTime());
						data.setOrderDate(new Date());
						data.setCopyQuantity(buyInfo.getQuantity());
						data.setTotalPrice(order.getValidPromtionAmount());
						ipi.setItemData(data);
						itemPromotionInfoList.add(ipi);
					}
				}
			}
			
			pq.setItemPromotionInfoList(itemPromotionInfoList);
			logger.info("getAvailablePromotions request"+JsonUtil.getJsonString4JavaPOJO(pq));
			Response<ResponseInfoable, PromotionInfo> r=promotionService.getAvailablePromotions(pq);
			logger.info("getAvailablePromotions response"+JsonUtil.getJsonString4JavaPOJO(r));
			PromotionInfo dataInfo=r.getData();
			if(dataInfo!=null&&dataInfo.getPromotionDataList()!=null){
				priceInfo.setCouponExclusion(dataInfo.getCouponExclusion());
				convertPromotionList(list,dataInfo.getPromotionDataList());
			}
		}catch(Exception e){
			logger.error("getAvailablePromotions",e);
		}
		
		return list;
	}
	
	
	
	private void  convertPromotionList(List<PromPromotion> list,List<PromotionData> dataList){
		PromPromotion  pp=null;
		PromResult pr =null;
		for(PromotionData pd:dataList){
		    pp=new PromPromotion();
		    pr =new PromResult();
		    pp.setPromPromotionId(pd.getPromotionId());
			pp.setTitle(pd.getPromotionTitle());
			pp.setCode(pd.getCode());
			pp.setPriceType(pd.getPriceType());
			pp.setDiscountAmount(pd.getFavorableAmount());
			if(pd.getPromotionType()!=null){
				pp.setPromitionType(pd.getPromotionType().toString());
			}
			pp.setBranchs(pd.getBranches());
			if(pd.getAmountType()!=null){
				pr.setAmountType(pd.getAmountType().name());
				if(pd.getAmountType().name().equals(AMOUNT_TYPE.AMOUNT_FIXED.name())){
					if(pd.getRuleResultValue()!=null){
						pr.setFixedAmount(pd.getRuleResultValue().longValue());//单位分
					}
				
				}else if(pd.getAmountType().name().equals(AMOUNT_TYPE.AMOUNT_PERCENT.name())){
					if(pd.getRuleResultValue()!=null){
						pr.setRateAmount(pd.getRuleResultValue().longValue());
					}
				}
			}
			if(pd.getRuleMiddleValue()!=null){
				pr.setAddEach(pd.getRuleMiddleValue().longValue());
			}
		
			pr.setRoomType(pd.getRoomType());
			if(pd.getRuleType()!=null){
				pp.setRuleType(pd.getRuleType().name());
			}
			if(pd.getRuleValue()!=null){
				pp.setRuleValue(String.valueOf(pd.getRuleValue()));
			}
			
			if(pd.getTimeType()!=null){
				pp.setTimeType(pd.getTimeType().name());
			}
			if(pd.getResultType()!=null){
				pr.setResultType(pd.getResultType().name());
			}
			if(pd.getResultSecondType()!=null){
				pr.setResultSecondType(pd.getResultSecondType().name());
			}
			pp.setKey(pd.getPromotionKey());//3.0下单页校验还有用到
			pp.setItemId(pd.getItemTd());
			pp.setPromResult(pr);
			list.add(pp);
		}
	}

	
	/**
	 * 获取酒店的退改规则（ 当用户一个房间订购了多晚（天）时，以最严格的规则作为退改政策：不退不改>可退改（无损时间最早的条件）（扣款金额最多的条件）
	 * 规则 优先级排序：1.不可取消   2.限时取消   3.免费取消
	 * 传入某商品多天或一天时间价格，和商品的支付方式
	 * @param suppGoodsTimePriceList
	 * @param payTarget
	 * @return
	 */

	public static TimePrice getsuppGoodsTimePriceByRules(
			List<OrdOrderItem> orderItemList) {
		for(OrdOrderItem ordOrderItem : orderItemList){
			SuppGoods suppGoods = ordOrderItem.getSuppGoods();
			String payTarget= suppGoods.getPayTarget();
			List<OrdOrderHotelTimeRate> ordHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
			boolean isHotelPack = ordOrderItem.getOrderPack()==null;	
			if (isHotelPack && !CollectionUtils.isEmpty(ordHotelTimeRateList)) {
				if (ordHotelTimeRateList != null && ordHotelTimeRateList.size() > 0 && StringUtils.isNotBlank(payTarget)){
					
					TimePrice suppGoodsTimePriceRe = null;
					for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : ordHotelTimeRateList) {
						
						TimePrice suppGoodsTimePrice =(TimePrice) ordOrderHotelTimeRate.getTimePrice();
						
						//如果某一天存在不可退改,不可取消优先级最高,直接返回不可取消
						if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) {
							
							return  suppGoodsTimePrice;
						
							//如果不存在在不可退改，限时取消优先于免费取消,对限时取消过滤,获取最早的可退改时间退改规则
						} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) {
							//现付和预付取最早退改时间那天策略
							if(SuppGoods.PAYTARGET.PREPAID.name().equalsIgnoreCase(payTarget) && suppGoodsTimePrice.getLatestCancelTime()==null){ //预付可退改取最严格的
								return suppGoodsTimePrice;	
							}else {
								if (suppGoodsTimePriceRe != null) {
									
									Date lastCacleDateOld = DateUtil.toDate(suppGoodsTimePriceRe.getLatestCancelDate(),"yyyy-MM-dd HH:mm");

									Date lastCacleDateNew = DateUtil.toDate(suppGoodsTimePrice.getLatestCancelDate(),"yyyy-MM-dd HH:mm");
									
									if ( lastCacleDateNew.before(lastCacleDateOld)) {
										suppGoodsTimePriceRe = suppGoodsTimePrice;
									}// 无损取消时间相同，取扣款最多的
									else if(DateUtils.isSameDay(lastCacleDateOld, lastCacleDateNew)){
										if(SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equals(suppGoodsTimePrice.getDeductType())){
											suppGoodsTimePriceRe = suppGoodsTimePrice;
											continue;
										}
										Long oldDeductValue = suppGoodsTimePriceRe.getDeductValue()==null?0:suppGoodsTimePriceRe.getDeductValue();
										Long newDeductValue = suppGoodsTimePrice.getDeductValue()==null?0:suppGoodsTimePrice.getDeductValue();
										if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equals(suppGoodsTimePriceRe.getDeductType())){
											oldDeductValue = (oldDeductValue * suppGoodsTimePriceRe.getPrice())/100;
										}
										if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equals(suppGoodsTimePrice.getDeductType())){
											newDeductValue = (newDeductValue * suppGoodsTimePrice.getPrice())/100;
										}
										if(newDeductValue < oldDeductValue){
											suppGoodsTimePriceRe = suppGoodsTimePrice;
											continue;
										}
									}

								} else {

									suppGoodsTimePriceRe = suppGoodsTimePrice;

								}

							} 

						}else{//支持艺龙等退改类型为null情况
							return suppGoodsTimePrice;
						}

					}
					
					if (suppGoodsTimePriceRe != null) {

						return suppGoodsTimePriceRe;

					} 

				}
			}
			
		}

		return null;
	}
	
	public long getTotalAmount(OrdOrderItem ordOrderItem) {
		if(ordOrderItem.getTotalAmount()!=null){
			return ordOrderItem.getTotalAmount();
		}
		return ordOrderItem.getPrice() * ordOrderItem.getQuantity();
	}
	
	/**
	 * 如果是渠道优惠，绑定支付渠道中文名
	 * @param promotionList
	 */
	public void buildPayChannelCnName(List<PromPromotion> promotionList){
		try {
			if(promotionList!=null){
				Map<String, String> paymentGate = queryPaymentGatewayServiceAdapter.getPaymentGateway();
				for(PromPromotion prom:promotionList){
					if(ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType())){
						prom.setChannelOrder(paymentGate.get(prom.getChannelOrder()));
					}
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
	}
	
/*	public PriceInfo countPriceOld(BuyInfo buyInfo){

		PriceInfo priceInfo = new PriceInfo();
		long orderMarketPrice = 0L;// 订单市场价金额
		long orderPrice = 0L;// 订单销售价金额
		long orderOughtPay = 0L;// 订单应付金额
		long promotionAmount=0L; //订单促销商品优惠总金额
		long couponAmount=0L; //订单优惠券抵扣总金额
		long quantitySum = 0L;
		
		long ticketAmount=0L;  
		long insurancePrice = 0L;
		//记录商品品类
		List<String> goodsCategorys = new ArrayList<String>();
		int refunCount=0;
		SuppGoodsRefund  suppGoodsRefund=null;
		
		
		Long distributorId = buyInfo.getDistributionId();
		
		List<Item> itemList = buyInfo.getItemList();
		if(null!=itemList&&itemList.size()>0){
			for (Item item : itemList) {
				
				List<TimePrice> timePriceList = null;
				TimePrice timePrice = null;
				long itemsPrice = 0 ; //一条商品的价格
				Long goodsId = item.getGoodsId();
				Date beginDate = null;
				Date endDate = null;
				BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
				Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
				HotelAdditation hotelAdditation = item.getHotelAdditation();
				
				try {
					if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
						beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
					}else{
						beginDate = CalendarUtils.getDateFormatDate(item.getVisitTime(), "yyyy-MM-dd");// 到访时间
					}
					
					//获得商品折扣信息
					ResultHandleT<Map> rebateResultHandleT=suppGoodsRebateClientService.getGoodsRebateAmount(item.getGoodsId(), beginDate);
					if(rebateResultHandleT.isSuccess()&&rebateResultHandleT.getReturnContent()!=null){
						Map map=rebateResultHandleT.getReturnContent();
						Map<String,String> result=new HashMap<String, String>();
						if(map.containsKey("pcRebate")){
							Long amount=Long.parseLong(map.get("pcRebate").toString());
							result.put("pcRebate", PriceUtil.trans2YuanStr(amount));
						}
						if(map.containsKey("mobileRebate")){
							Long amount=Long.parseLong(map.get("mobileRebate").toString());
							result.put("mobileRebate", PriceUtil.trans2YuanStr(amount));
						}
						priceInfo.getGoodsRebateAmountMap().put(item.getGoodsId(),result);
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("method countPrice() error, ", e1);
					e1.printStackTrace();
				}
				
				int quantity = item.getQuantity();// 购买商品数量
				if(quantity<=0){
					continue;
				}
				
				//查询商品
				SuppGoods suppGoods = null;
				try {
					ResultHandleT<SuppGoods> suppGoodsHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, goodsId);
					suppGoods = suppGoodsHandleT.getReturnContent();
				}catch (Exception e) {
					e.printStackTrace();
				}
				if(suppGoods!=null){
					BizCategory category=suppGoods.getProdProduct().getBizCategory();
					
					
					String categoryCode = category.getCategoryCode();
					
					//记录商品品类以及距离类型
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryCode)
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(categoryCode)
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)
							||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryCode)){
						String type = category.getCategoryCode()+"_"+suppGoods.getProdProduct().getProductType();
						goodsCategorys.add(type);
					}else{
						goodsCategorys.add(categoryCode);
					}
					
					// 酒店情况的时候
					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode().equalsIgnoreCase(category.getCategoryCode())){
						
						if (hotelAdditation != null) {
							String endDateStr = hotelAdditation.getLeaveTime();
							Calendar endCal = Calendar.getInstance();
							try {
								endCal.setTime(CalendarUtils.getDateFormatDate(endDateStr, "yyyy-MM-dd"));
								// beginDate=CalendarUtils.getDateFormatDate(item.getVisitTime(),
								// "yyyy-MM-dd");
							} catch (Exception e) {
								logger.error("method countPrice() error, ", e);
								e.printStackTrace();
							}
							endDate = DateUtils.addDays(endCal.getTime(), -1);
							ResultHandleT<List<TimePrice>> resultHandleT = distGoodsTimePriceClientService.findTimePriceList(distributorId, goodsId, beginDate, endDate);
							timePriceList = resultHandleT.getReturnContent();
							if (resultHandleT.hasNull() ||timePriceList.isEmpty()) {
								StringBuffer buffer = new StringBuffer();
								buffer.append("未找到该商品信息 distributorId:").append(distributorId).append(",goodsId:").append(goodsId).append(",beginDate:").append(beginDate).append(",endDate：").append(endDate);
								logger.info(buffer.toString());
								priceInfo.sendError(buffer.toString());
								return priceInfo;
							}
							//入住期间内最高单价
							long maxPrice=0;
							//入住期间内最高单价日
							Date highPriceDate=null;
							//全程担保
							TimePrice allTimePrice=null;
							
							//得到最早到点时间
							priceInfo.setEarliestArriveTime(getEarliestArriveTime(beginDate, suppGoods));
							
							//退改政策
							String cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
							int i = 0;
							for (TimePrice timePriceObj : timePriceList) {
								long itemPrice = timePriceObj.getPrice() * quantity;//每个商品的价格
								itemsPrice += itemPrice;//同类商品的总价
								
								//商品促销优惠用
								BuyInfoPromotion.ItemPrice itemPc=new BuyInfoPromotion.ItemPrice(timePriceObj.getSpecDate());
								itemPc.setPrice(timePriceObj.getPrice());
								itemPriceMap.put(timePriceObj.getSpecDate(), itemPc);
								
								//入住期间内最高单价及日期
								if(itemPrice>maxPrice){
									maxPrice=itemPrice;
									highPriceDate=timePriceObj.getSpecDate();
								}
								//预付情况
								if(StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())){
									if(StringUtils.equals(timePriceObj.getCancelStrategy(),SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
										cancelStrategy = timePriceObj.getCancelStrategy();
									}
								//现付情况
								}else{
									//入住期间内全程担保
									if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePriceObj.getBookLimitType())){
										allTimePrice=timePriceObj;
									}
									
									if(i==0){
										cancelStrategy = timePriceObj.getCancelStrategy();
									}
								}
								i++;
							}
							退订政策 start
							String doBookPolicyStr="";
							if(StringUtils.isEmpty(cancelStrategy)){
								cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
							}
							TimePriceVO timePriceVO = new TimePriceVO();
							if(null!=allTimePrice){
								
								doBookPolicyStr=this.getDoBookPolicy(allTimePrice, beginDate, quantity, hotelAdditation.getArrivalTime(),
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO);
							}else{
							
								doBookPolicyStr=this.getDoBookPolicy(timePriceList.get(0), beginDate, quantity, hotelAdditation.getArrivalTime(),
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO);
							}
							 priceInfo.setDoBookPolicyStr(doBookPolicyStr);
							退订政策 end
							
							商品促销参数填充 start
							if(null!=buyInfo.getPromotionIdList()){
								
								promItem.setGoodsId(goodsId);
								promItem.setVisitTime(beginDate);
								promItem.setLeaveTime(endCal.getTime());
								promItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
								promItem.setPayTarget(suppGoods.getPayTarget());
								promItem.setQuantity(quantity);
								promItem.setTotalAmount(itemsPrice);
								promItem.setSettlementAmount(itemsPrice);
								promItem.setItemPriceMap(itemPriceMap);
								//计算单个商品优惠的金额
								long reducePrice=goodsReducePrice(promItem, buyInfo.getPromotionIdList(), false,distributorId);
								//商品应付金额等于总金额减去优惠的金额
								//itemsPrice=itemsPrice-reducePrice*quantity;
								promotionAmount+=reducePrice*quantity;
							}
							商品促销参数填充 end

						}
					
					//门票类(景点门票,其它票,组合套餐票(自主，供应商))
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())||
							BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())||
							BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())){
						
//						if(StringUtils.isEmpty(suppGoods.getProdProduct().getPackageType())||"SUPPLIER".equals(suppGoods.getProdProduct().getPackageType())){
						
							ResultHandleT<SuppGoodsAddTimePrice> resultHandleT = distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(distributorId, goodsId, beginDate);
							SuppGoodsAddTimePrice suppGoodsAddTimePrice = resultHandleT.getReturnContent();
							itemsPrice += suppGoodsAddTimePrice.getPrice() * quantity;
							ticketAmount+=itemsPrice;
							
							//商品详情和退改
							 ResultHandleT<SuppGoodsTicketDetailVO>  ticketDetailVoResultHandleT=suppGoodsClientRemote.findSuppGoodsTicketDetailById(item.getGoodsId());
							 SuppGoodsTicketDetailVO detailVO=ticketDetailVoResultHandleT.getReturnContent();
							 if(ticketDetailVoResultHandleT.isSuccess()&&detailVO!=null){
								 SuppGoodsRefund  itemRefund =detailVO.getSuppGoodsRefund();
								 if(suppGoodsRefund!=null){
									 if(itemRefund==null||StringUtil.isEmptyString(itemRefund.getCancelStrategy())){
										 
									 }else if(suppGoodsRefund==null||StringUtil.isEmptyString(suppGoodsRefund.getCancelStrategy())){
										 suppGoodsRefund=itemRefund;
										 refunCount+=1;
									 }else if(!suppGoodsRefund.getCancelStrategy().equalsIgnoreCase(itemRefund.getCancelStrategy())){
										 refunCount+=1;
									 }
								 }
							  }
							 priceInfo.setDoBookPolicyStr(getTicketCancelStrategy(refunCount, suppGoodsRefund));
//						}
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(category.getCategoryCode())){
							ResultHandleT<SuppGoodsNotimeTimePrice> resultHandleT = distGoodsTimePriceClientService.findSuppGoodsNotimeTimePriceList(distributorId, goodsId, beginDate);
							SuppGoodsNotimeTimePrice suppGoodsNotimeTimePrice = resultHandleT.getReturnContent();
							itemsPrice += suppGoodsNotimeTimePrice.getPrice() * quantity;
							insurancePrice+=itemsPrice;
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equalsIgnoreCase(category.getCategoryCode())||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equalsIgnoreCase(category.getCategoryCode())||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equalsIgnoreCase(category.getCategoryCode())||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equalsIgnoreCase(category.getCategoryCode())){
						
						ResultHandleT<SuppGoodsBaseTimePrice> resultHandleT=orderLineTimePriceService.getTimePrice(goodsId, beginDate, true);
						SuppGoodsLineTimePrice suppGoodsLineTimePrice = (SuppGoodsLineTimePrice) resultHandleT.getReturnContent();
						if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equals(suppGoods.getPriceType())){
							itemsPrice += suppGoodsLineTimePrice.getAuditPrice() * quantity;
						}else{
							itemsPrice += suppGoodsLineTimePrice.getAuditPrice() * item.getAdultQuantity();
							itemsPrice += suppGoodsLineTimePrice.getChildPrice() * item.getChildQuantity();
							if((item.getAdultQuantity()+item.getAdultQuantity())%2==1){
							itemsPrice += suppGoodsLineTimePrice.getGapPrice() * 1;
							}
						}
					}else if(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCode().equalsIgnoreCase(category.getCategoryCode())){
						ResultHandleT<SuppGoodsSimpleTimePrice> resultHandleT=distGoodsTimePriceClientService.findSuppGoodsSimpleTimePrice(distributorId, goodsId, beginDate);
						SuppGoodsSimpleTimePrice suppGoodsSimpleTimePrice=resultHandleT.getReturnContent();
						itemsPrice += suppGoodsSimpleTimePrice.getPrice() * quantity;
					}
				}
				orderPrice += itemsPrice;//所有不同商品的总价
				priceInfo.getItemPriceMap().put(item.getGoodsId(), PriceUtil.trans2YuanStr(itemsPrice));
				quantitySum += quantity;// 购买商品数量总和
			}
			
		}
			
		//自主打包情况（产品下面没有商品）
		List<Product> productList = buyInfo.getProductList();
		
		if(null!=productList&&productList.size()>0){
			for (Product product : productList) {
				
				ResultHandleT<ProdProduct> prodResultHandleT = prodProductClientService.findHotelProduct4Front(product.getProductId(), false, false);
				if(prodResultHandleT.isFail()||prodResultHandleT.getReturnContent()==null){
					 
				}
				
				ProdProduct prodProduct=prodResultHandleT.getReturnContent();
				BizCategory category = prodProduct.getBizCategory();
				String categoryCode = category.getCategoryCode();
				
				//记录产品品类以及距离类型
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryCode)
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(categoryCode)
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)
						||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryCode)){
					String type = category.getCategoryCode()+"_"+prodProduct.getProductType();
					goodsCategorys.add(type);
				}else{
					goodsCategorys.add(categoryCode);
				}
				
				
				if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equalsIgnoreCase(prodProduct.getBizCategory().getCategoryCode())){
					
					Date beginDate=null;
					try {
						if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
							beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
						}else{
						 	beginDate = CalendarUtils.getDateFormatDate(product.getVisitTime(), "yyyy-MM-dd");// 到访时间
						}
						
					}catch (Exception e1){
						// TODO Auto-generated catch block
						logger.error("method countPrice() error, ", e1);
						e1.printStackTrace();
					} 
					
					
					 ResultHandleT<TicketProductForOrderVO> productResultHandleT=prodProductClientService.findTicketProductForOrder(product.getProductId(),beginDate);//
					 TicketProductForOrderVO ticketProductForOrderVO =productResultHandleT.getReturnContent();
					 long itemPrice=(long) (ticketProductForOrderVO.getTotalPrice() * product.getQuantity());
					 orderPrice +=itemPrice;
					 priceInfo.getItemPriceMap().put(product.getProductId(), PriceUtil.trans2YuanStr(itemPrice));
					 ticketAmount+=itemPrice;
					 
					 for (SuppGoodsVO suppGoodsVO : ticketProductForOrderVO.getSuppGoodsList()) {
							//商品详情和退改
							 ResultHandleT<SuppGoodsTicketDetailVO>  ticketDetailVoResultHandleT=distGoodsClientService.findSuppGoodsTicketDetailById(Constant.DIST_FRONT_END,suppGoodsVO.getSuppGoodsId());
							 SuppGoodsTicketDetailVO detailVO=ticketDetailVoResultHandleT.getReturnContent();
							 if(ticketDetailVoResultHandleT.isSuccess()&&detailVO!=null){
								 SuppGoodsRefund  itemRefund =detailVO.getSuppGoodsRefund();
								 if(suppGoodsRefund!=null){
									 if(itemRefund==null||StringUtil.isEmptyString(itemRefund.getCancelStrategy())){
										 
									 }else if(suppGoodsRefund==null||StringUtil.isEmptyString(suppGoodsRefund.getCancelStrategy())){
										 suppGoodsRefund=itemRefund;
										 refunCount+=1;
									 }else if(!suppGoodsRefund.getCancelStrategy().equalsIgnoreCase(itemRefund.getCancelStrategy())){
										 refunCount+=1;
									 }
								 }
							  }
					 }
				}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equalsIgnoreCase(prodProduct.getBizCategory().getCategoryCode())||
						BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equalsIgnoreCase(prodProduct.getBizCategory().getCategoryCode())||
						BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equalsIgnoreCase(prodProduct.getBizCategory().getCategoryCode())||
						BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equalsIgnoreCase(prodProduct.getBizCategory().getCategoryCode())){
					
					itemList = product.getItemList();
					if(null!=itemList&&itemList.size()>0){
						for (Item item : itemList) {
							
							List<TimePrice> timePriceList = null;
							TimePrice timePrice = null;
							long itemsPrice = 0 ; //一条商品的价格
							Long goodsId = item.getGoodsId();
							Date beginDate = null;
							Date endDate = null;
							BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
							Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
							HotelAdditation hotelAdditation = item.getHotelAdditation();
							
							try {
								if(StringUtil.isNotEmptyString(buyInfo.getSameVisitTime())&&buyInfo.getSameVisitTime().equals("true")){
									beginDate = CalendarUtils.getDateFormatDate(buyInfo.getVisitTime(), "yyyy-MM-dd");// 到访时间
								}else{
									beginDate = CalendarUtils.getDateFormatDate(item.getVisitTime(), "yyyy-MM-dd");// 到访时间
								}
								
								//获得商品折扣信息
								ResultHandleT<Map> rebateResultHandleT=suppGoodsRebateClientService.getGoodsRebateAmount(item.getGoodsId(), beginDate);
								if(rebateResultHandleT.isSuccess()&&rebateResultHandleT.getReturnContent()!=null){
									Map map=rebateResultHandleT.getReturnContent();
									Map<String,String> result=new HashMap<String, String>();
									if(map.containsKey("pcRebate")){
										Long amount=Long.parseLong(map.get("pcRebate").toString());
										result.put("pcRebate", PriceUtil.trans2YuanStr(amount));
									}
									if(map.containsKey("mobileRebate")){
										Long amount=Long.parseLong(map.get("mobileRebate").toString());
										result.put("mobileRebate", PriceUtil.trans2YuanStr(amount));
									}
									priceInfo.getGoodsRebateAmountMap().put(item.getGoodsId(),result);
								}
								
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								logger.error("method countPrice() error, ", e1);
								e1.printStackTrace();
							}
							
							int quantity = item.getQuantity();// 购买商品数量
							if(quantity<=0){
								continue;
							}
							
							//查询商品
							SuppGoods suppGoods = null;
							try {
								ResultHandleT<SuppGoods> suppGoodsHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, goodsId);
								suppGoods = suppGoodsHandleT.getReturnContent();
							} catch (Exception e) {
								e.printStackTrace();
							}
							if(suppGoods!=null){
								category=suppGoods.getProdProduct().getBizCategory();
								
								
								categoryCode = category.getCategoryCode();
								
								//记录商品品类以及距离类型
								if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryCode)
										||BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(categoryCode)
										||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)
										||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryCode)){
									String type = category.getCategoryCode()+"_"+suppGoods.getProdProduct().getProductType();
									goodsCategorys.add(type);
								}else{
									goodsCategorys.add(categoryCode);
								}
								
								// 酒店情况的时候
								if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode().equalsIgnoreCase(category.getCategoryCode())){
									
									if (hotelAdditation != null) {
										String endDateStr = hotelAdditation.getLeaveTime();
										Calendar endCal = Calendar.getInstance();
										try {
											endCal.setTime(CalendarUtils.getDateFormatDate(endDateStr, "yyyy-MM-dd"));
											// beginDate=CalendarUtils.getDateFormatDate(item.getVisitTime(),
											// "yyyy-MM-dd");
										} catch (Exception e) {
											logger.error("method countPrice() error, ", e);
											e.printStackTrace();
										}
										endDate = DateUtils.addDays(endCal.getTime(), -1);
										ResultHandleT<List<TimePrice>> resultHandleT = distGoodsTimePriceClientService.findTimePriceList(distributorId, goodsId, beginDate, endDate);
										timePriceList = resultHandleT.getReturnContent();
										if (resultHandleT.hasNull() ||timePriceList.isEmpty()) {
											StringBuffer buffer = new StringBuffer();
											buffer.append("未找到该商品信息 distributorId:").append(distributorId).append(",goodsId:").append(goodsId).append(",beginDate:").append(beginDate).append(",endDate：").append(endDate);
											logger.info(buffer.toString());
											priceInfo.sendError(buffer.toString());
											return priceInfo;
										}
										//入住期间内最高单价
										long maxPrice=0;
										//入住期间内最高单价日
										Date highPriceDate=null;
										//全程担保
										TimePrice allTimePrice=null;
										
										//得到最早到点时间
										priceInfo.setEarliestArriveTime(getEarliestArriveTime(beginDate, suppGoods));
										
										//退改政策
										String cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
										int i = 0;
										for (TimePrice timePriceObj : timePriceList) {
											long itemPrice = timePriceObj.getPrice() * quantity;//每个商品的价格
											itemsPrice += itemPrice;//同类商品的总价
											
											//商品促销优惠用
											BuyInfoPromotion.ItemPrice itemPc=new BuyInfoPromotion.ItemPrice(timePriceObj.getSpecDate());
											itemPc.setPrice(timePriceObj.getPrice());
											itemPriceMap.put(timePriceObj.getSpecDate(), itemPc);
											
											//入住期间内最高单价及日期
											if(itemPrice>maxPrice){
												maxPrice=itemPrice;
												highPriceDate=timePriceObj.getSpecDate();
											}
											//预付情况
											if(StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())){
												if(StringUtils.equals(timePriceObj.getCancelStrategy(),SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
													cancelStrategy = timePriceObj.getCancelStrategy();
												}
											//现付情况
											}else{
												//入住期间内全程担保
												if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePriceObj.getBookLimitType())){
													allTimePrice=timePriceObj;
												}
												
												if(i==0){
													cancelStrategy = timePriceObj.getCancelStrategy();
												}
											}
											i++;
										}
										退订政策 start
										String doBookPolicyStr="";
										if(StringUtils.isEmpty(cancelStrategy)){
											cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
										}
										TimePriceVO timePriceVO = new TimePriceVO();
										if(null!=allTimePrice){
											
											doBookPolicyStr=this.getDoBookPolicy(allTimePrice, beginDate, quantity, hotelAdditation.getArrivalTime(),
													cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO);
										}else{
										
											doBookPolicyStr=this.getDoBookPolicy(timePriceList.get(0), beginDate, quantity, hotelAdditation.getArrivalTime(),
													cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO);
										}
										 priceInfo.setDoBookPolicyStr(doBookPolicyStr);
										退订政策 end
										
										商品促销参数填充 start
										if(null!=buyInfo.getPromotionIdList()){
											
											promItem.setGoodsId(goodsId);
											promItem.setVisitTime(beginDate);
											promItem.setLeaveTime(endCal.getTime());
											promItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
											promItem.setPayTarget(suppGoods.getPayTarget());
											promItem.setQuantity(quantity);
											promItem.setTotalAmount(itemsPrice);
											promItem.setSettlementAmount(itemsPrice);
											promItem.setItemPriceMap(itemPriceMap);
											//计算单个商品优惠的金额
											long reducePrice=goodsReducePrice(promItem, buyInfo.getPromotionIdList(), false,distributorId);
											//商品应付金额等于总金额减去优惠的金额
											//itemsPrice=itemsPrice-reducePrice*quantity;
											promotionAmount+=reducePrice*quantity;
										}
										商品促销参数填充 end

									}
								
								//门票类(景点门票,其它票,组合套餐票(自主，供应商))
								}else if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())||
										BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())||
										BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equalsIgnoreCase(category.getCategoryCode())){
									
//									if(StringUtils.isEmpty(suppGoods.getProdProduct().getPackageType())||"SUPPLIER".equals(suppGoods.getProdProduct().getPackageType())){
									
										ResultHandleT<SuppGoodsAddTimePrice> resultHandleT = distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(distributorId, goodsId, beginDate);
										SuppGoodsAddTimePrice suppGoodsAddTimePrice = resultHandleT.getReturnContent();
										itemsPrice += suppGoodsAddTimePrice.getPrice() * quantity;
										ticketAmount+=itemsPrice;
										
										//商品详情和退改
										 ResultHandleT<SuppGoodsTicketDetailVO>  ticketDetailVoResultHandleT=suppGoodsClientRemote.findSuppGoodsTicketDetailById(item.getGoodsId());
										 SuppGoodsTicketDetailVO detailVO=ticketDetailVoResultHandleT.getReturnContent();
										 if(ticketDetailVoResultHandleT.isSuccess()&&detailVO!=null){
											 SuppGoodsRefund  itemRefund =detailVO.getSuppGoodsRefund();
											 if(suppGoodsRefund!=null){
												 if(itemRefund==null||StringUtil.isEmptyString(itemRefund.getCancelStrategy())){
													 
												 }else if(suppGoodsRefund==null||StringUtil.isEmptyString(suppGoodsRefund.getCancelStrategy())){
													 suppGoodsRefund=itemRefund;
													 refunCount+=1;
												 }else if(!suppGoodsRefund.getCancelStrategy().equalsIgnoreCase(itemRefund.getCancelStrategy())){
													 refunCount+=1;
												 }
											 }
										  }
										 priceInfo.setDoBookPolicyStr(getTicketCancelStrategy(refunCount, suppGoodsRefund));
//									}
								}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equalsIgnoreCase(category.getCategoryCode())){
										ResultHandleT<SuppGoodsNotimeTimePrice> resultHandleT = distGoodsTimePriceClientService.findSuppGoodsNotimeTimePriceList(distributorId, goodsId, beginDate);
										SuppGoodsNotimeTimePrice suppGoodsNotimeTimePrice = resultHandleT.getReturnContent();
										itemsPrice += suppGoodsNotimeTimePrice.getPrice() * quantity;
										insurancePrice+=itemsPrice;
								}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equalsIgnoreCase(category.getCategoryCode())||
										BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equalsIgnoreCase(category.getCategoryCode())||
										BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode().equalsIgnoreCase(category.getCategoryCode())||
										BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode().equalsIgnoreCase(category.getCategoryCode())){
									
									ResultHandleT<SuppGoodsBaseTimePrice> resultHandleT=orderLineTimePriceService.getTimePrice(goodsId, beginDate, true);
									SuppGoodsLineTimePrice suppGoodsLineTimePrice = (SuppGoodsLineTimePrice) resultHandleT.getReturnContent();
									if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equals(suppGoods.getPriceType())){
										itemsPrice += suppGoodsLineTimePrice.getAuditPrice() * quantity;
									}else{
										itemsPrice += suppGoodsLineTimePrice.getAuditPrice() * item.getAdultQuantity();
										itemsPrice += suppGoodsLineTimePrice.getChildPrice() * item.getChildQuantity();
										if((item.getAdultQuantity()+item.getAdultQuantity())%2==1){
										itemsPrice += suppGoodsLineTimePrice.getGapPrice() * 1;
										}
									}
								}else if(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCode().equalsIgnoreCase(category.getCategoryCode())){
									ResultHandleT<SuppGoodsSimpleTimePrice> resultHandleT=distGoodsTimePriceClientService.findSuppGoodsSimpleTimePrice(distributorId, goodsId, beginDate);
									SuppGoodsSimpleTimePrice suppGoodsSimpleTimePrice=resultHandleT.getReturnContent();
									itemsPrice += suppGoodsSimpleTimePrice.getPrice() * quantity;
								}
							}
							orderPrice += itemsPrice;//所有不同商品的总价
							priceInfo.getItemPriceMap().put(item.getGoodsId(), PriceUtil.trans2YuanStr(itemsPrice));
							quantitySum += quantity;// 购买商品数量总和
						}
						
					}
					
					
				}
				 
			}
			 priceInfo.setDoBookPolicyStr(getTicketCancelStrategy(refunCount, suppGoodsRefund));
		}
		
		优惠券验证计算 start
		String youhuiType=buyInfo.getYouhui();
		if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)){
		List<Coupon> couponList=buyInfo.getCouponList();
		if(null!=couponList&&couponList.size()>0){
			buyInfo.setOrderTotalPrice(orderPrice);//设置订单总价
			List<ResultHandle> couponResultHandles=new ArrayList<ResultHandle>(2);
			
			for (Coupon coupon : couponList) {
				if(StringUtil.isNotEmptyString(coupon.getCode())){
					ResultHandle couponResultHandle=validateCoupon(buyInfo);
					couponResultHandles.add(couponResultHandle);
					if(null!=couponResultHandle&&couponResultHandle.isSuccess()){
						FavorStrategyInfo fsi=calCoupon(buyInfo);
						if(fsi!=null){
						 	 
						 	couponAmount+=fsi.getDiscountAmount();
						}
					}
					Pair<FavorStrategyInfo, Long> resultPair=couponService.calCoupon(buyInfo);
					if(resultPair.isSuccess()){
						 FavorStrategyInfo fsi=resultPair.getFirst();
						 couponAmount+=fsi.getDiscountAmount();
				    }else{
				    	couponResultHandles.add(resultPair);
				    }
				}
			}
			priceInfo.setCouponResutHandles(couponResultHandles);
		}
		}
		优惠券验证计算end 
		
		计算快递费用start
		Long expressPrice=0L;
		PriceInfo expressPriceInfo=null;
		try {
			expressPriceInfo = this.findOrderExpressPrice(buyInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("method countPrice() error, ", e);
			e.printStackTrace();
		}
		if(expressPriceInfo!=null){
			expressPrice=expressPriceInfo.getExpressPrice();			
			priceInfo.getItemPriceMap().putAll(expressPriceInfo.getItemPriceMap());
		}
		计算快递费用end		
		
		//获取订单可使用奖金金额
		Long bonus=0l;
		Long maxBonus=0L;
		if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
			maxBonus = ordUserOrderServiceAdapter.getOrderBonusCanPayAmount(buyInfo.getUserNo(), orderPrice, goodsCategorys);
			bonus=maxBonus;
			String target =buyInfo.getTarget();
			//如果是抵扣现金框触发
			if(StringUtils.isNotEmpty(target)&&target.equals(ORDER_FAVORABLE_TYPE.bonus.getCode())){
				bonus = PriceUtil.convertToFen(buyInfo.getBonusYuan());
				if(bonus>maxBonus){
					bonus=maxBonus;
				}
			}
		}
		
		orderOughtPay = orderPrice-couponAmount-promotionAmount-bonus+expressPrice;// 应付金额 
		
		priceInfo.setBonus(bonus);
		priceInfo.setMaxBonus(maxBonus);
		priceInfo.setCoupon(couponAmount);
		priceInfo.setMarketPrice(orderMarketPrice);
		priceInfo.setPrice(orderPrice);
		priceInfo.setOughtPay(orderOughtPay);
		priceInfo.setOrderQuantity(quantitySum);
		priceInfo.setPromotionAmount(promotionAmount);
		priceInfo.setTicketGoodsPrice(ticketAmount);
		priceInfo.setInsurancePrice(insurancePrice);
		priceInfo.setExpressPrice(expressPrice);
		
		return priceInfo;
	}
*/	

	
	/**
	 * 得到单个商品优惠的金额
	 * @param item
	 * @param promotionIds
	 * @param isProcessSupplierPrice
	 * @return
	 */
	private long goodsReducePrice(BuyInfoPromotion.Item item,List<Long> promotionIds, boolean isProcessSupplierPrice,Long distributionId){
		long result=0;
		ResultHandleT<BuyInfoPromotion.Item> resultItem=promotionService.calcPromotion(item, distributionId,promotionIds, isProcessSupplierPrice);
		BuyInfoPromotion.Item promotionItem=resultItem.getReturnContent();
		 if(null==promotionItem||null==promotionItem.getPromPriceMap()){
			 return 0;
		 }
		for (int i = 0; i < promotionIds.size(); i++) {
			Long price=promotionItem.getPromPriceMap().get(promotionIds.get(i));
			if(null!=price){
				result+=price.longValue();
			}
		}
		return result;
	}

	@Override
	public Long cancelOrderDeductAmount(BuyInfo buyInfo) {
		// TODO Auto-generated method stub
		List<Item> itemList = buyInfo.getItemList();
		//合计
		long deductAmountTotal=0;
		// 商品子项存在
		if ((itemList != null) && (itemList.size() > 0)) {
			for (Item item : itemList) {
				List<TimePrice> timePriceList = null;
				//全程担保
				List<TimePrice> allTimeGuaranteeTimePriceList = new ArrayList<TimePrice>();
				TimePrice deductTimePrice = null;
				ResultHandleT<TimePrice> guaranteeTimePriceHolder = null;
				Long goodsId = item.getGoodsId();
				Date beginDate = null;
				Date endDate = null;
				long itemsPrice = 0 ; //一条商品的价格
				Long distributorId = buyInfo.getDistributionId();
				HotelAdditation hotelAdditation = item.getHotelAdditation();
				if(hotelAdditation==null){//目前这个功能只针对酒店有效
					continue;
				}
				int quantity = item.getQuantity();// 购买商品数量
				try {
					beginDate = CalendarUtils.getDateFormatDate(item.getVisitTime(), "yyyy-MM-dd");// 到访时间
				} catch (Exception e1) {
					logger.error(ExceptionFormatUtil.getTrace(e1));
				}
				
//				if (hotelAdditation != null) {// 酒店情况的时候
					
					String endDateStr = hotelAdditation.getLeaveTime();
					Calendar endCal = Calendar.getInstance();
					try {
						endCal.setTime(CalendarUtils.getDateFormatDate(endDateStr, "yyyy-MM-dd"));
					} catch (Exception e) {
						logger.error(ExceptionFormatUtil.getTrace(e));
					}
					endDate = DateUtils.addDays(endCal.getTime(), -1);
					ResultHandleT<List<TimePrice>> resultHandleT = distGoodsTimePriceClientServiceAdaptor.findTimePriceList(distributorId, goodsId, beginDate, endDate);
					timePriceList = resultHandleT.getReturnContent();
					
					if (resultHandleT.hasNull() ||timePriceList.isEmpty()) {
						continue;
					}
					
					//查询商品
					ResultHandleT<SuppGoods> suppGoodsHandleT = null;
					try {
						suppGoodsHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, goodsId);
					} catch (Exception e) {
						logger.error(ExceptionFormatUtil.getTrace(e));
					}
					SuppGoods suppGoods = suppGoodsHandleT == null ? new SuppGoods() : suppGoodsHandleT.getReturnContent();
					
					
					int i = 0;
					for (TimePrice timePrice : timePriceList) {
						//long itemPrice = timePrice.getPrice() * quantity;//每个商品的价格
						
						//预付情况
						if(StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())){
							
							//首日
							if (i == 0) {
								deductTimePrice = timePrice;
							}
							
							
						//现付情况
						}else{
							//入住期间内全程担保
							if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePrice.getBookLimitType())){
								allTimeGuaranteeTimePriceList.add(timePrice);
							}
							
							if(i==0){
								guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(quantity,item, timePrice);
								if (guaranteeTimePriceHolder.isFail()) {
									continue;
								}
								deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
								
								if (deductTimePrice != null
										&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
									allTimeGuaranteeTimePriceList.add(deductTimePrice);
								}
								
							} else {
								if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(timePrice.getBookLimitType())) {
									guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(quantity,item, timePrice);
									if (guaranteeTimePriceHolder.isFail()) {
										continue;
									}
									deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
									if (deductTimePrice != null
											&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
										allTimeGuaranteeTimePriceList.add(deductTimePrice);
									}
								}
							}
							
						}
						
						i++;
					}
					
					//存在全程担保
					if (allTimeGuaranteeTimePriceList.size() > 0) {
						deductTimePrice = getMaxDeductAmountTimePrice(quantity, allTimeGuaranteeTimePriceList, timePriceList);
					}
					
					//计算退改价格
					long deductAmount = 0;
					if (deductTimePrice != null) {
						deductAmount = computeOrderItemDeductAmount(quantity, deductTimePrice, timePriceList);
					}
					//设置扣款金额
					deductAmountTotal+=deductAmount;
					  
//				} else {// 非酒店情况
//					
//					ResultHandleT<TimePrice> resultHandleT = distGoodsTimePriceClientService.findTimePrice(distributorId, goodsId, beginDate);
//					TimePrice timePrice = resultHandleT.getReturnContent();
//					if (timePrice == null) {
//						continue;
//					}
//					deductTimePrice = timePrice;
//					
//					//计算退改价格
//					long deductAmount = 0;
//					if (deductTimePrice != null) {
//						deductAmount = computeOrderItemDeductAmount(quantity, deductTimePrice, timePriceList);
//					}
//					//设置扣款金额
//					deductAmountTotal+=deductAmount;
//				}
				
			}
		}
		return deductAmountTotal;
	}
	
	
	/**
	 * 设置担保类型
	 * 
	 * @param orderItem
	 * @param item
	 * @param timePrice
	 * @return
	 */
	private ResultHandleT<TimePrice> setHotelOrderItemGuaranteeInfo(int quantity,Item item, TimePrice timePrice) {
		ResultHandleT<TimePrice> guaranteeTimePriceHolder = new ResultHandleT<TimePrice>();
		 
		if (item.getHotelAdditation() != null) {
			HotelAdditation hotelAdditation = item.getHotelAdditation();
			String bookLimitType = timePrice.getBookLimitType();
			//全程担保
			if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//一律担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//超时担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				if (timePrice.getLatestUnguarTime() != null && timePrice.getLatestUnguarTime() > 0) {
					int totalMinute = 0;
					String arrivaltime = hotelAdditation.getArrivalTime();
					String[] timeStrings = arrivaltime.split(":");
					int hour = Integer.valueOf(timeStrings[0]).intValue();
					int minute = Integer.valueOf(timeStrings[1]).intValue();
					totalMinute = hour * 60 + minute;
					
					if (totalMinute > timePrice.getLatestUnguarTime() * 60) {
						guaranteeTimePriceHolder.setReturnContent(timePrice);
					} 
				
				}
			//房量担保
			} else if (timePrice.getGuarQuantity() != null && timePrice.getGuarQuantity() > 0) {
				if (quantity > timePrice.getGuarQuantity()) {
					guaranteeTimePriceHolder.setReturnContent(timePrice);
				}
			} else if (bookLimitType == null || SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equalsIgnoreCase(bookLimitType)) {
				
			}
		}
		
		return guaranteeTimePriceHolder;
	}
	
	/**
	 * 计算退改金额
	 * 
	 * @param orderItem
	 * @param applyTimePrice
	 * @param everydayTimePriceList
	 * @return
	 */
	private long computeOrderItemDeductAmount(int quantity,TimePrice applyTimePrice, List<TimePrice> everydayTimePriceList) {
		long deductAmount = 0;
		if (applyTimePrice.getDeductType() != null) {
			if (SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = applyTimePrice.getPrice() * quantity;
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if (everydayTimePriceList.get(0) != null) {
					deductAmount = everydayTimePriceList.get(0).getPrice() * quantity;
				}
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if(applyTimePrice.getDeductValue()==null){
					deductAmount = applyTimePrice.getPrice() * quantity;
					return deductAmount;
				}
				deductAmount = applyTimePrice.getDeductValue() * quantity;
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if(applyTimePrice.getDeductValue()==null){
					deductAmount = applyTimePrice.getPrice() * quantity;
					return deductAmount;
				}
				deductAmount = (long) ((applyTimePrice.getPrice() * quantity) * applyTimePrice.getDeductValue() / 100.0 + 0.5);
				
			} else {
				//throw new IllegalArgumentException("TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=" + applyTimePrice.getDeductType() + ", is illegal.");
			}
		} else {
			 
		}
		
		return deductAmount;
	}
	
	/**
	 * 在时间价格表List中，找出退改最大的时间价格表
	 * 
	 * @param orderItem
	 * @param timePriceList
	 * @param everydayTimePriceList
	 * @return
	 */
	private TimePrice getMaxDeductAmountTimePrice(int quantity, List<TimePrice> timePriceList, List<TimePrice> everydayTimePriceList) {
		TimePrice maxDeductTimePrice = null;
		long maxDeductAmount = -1;
		long deductAmount = -1;
		for (TimePrice timePrice : timePriceList) {
			deductAmount = computeOrderItemDeductAmount(quantity, timePrice, everydayTimePriceList);
			if (maxDeductTimePrice == null) {
				maxDeductTimePrice = timePrice;
				maxDeductAmount = deductAmount;
			} else {
				if (deductAmount > maxDeductAmount) {
					maxDeductTimePrice = timePrice;
					maxDeductAmount = deductAmount;
				}
			}
		}
		
		return maxDeductTimePrice;
	}
	
	/**
	 * 
	 * @param visitDate
	 * @return
	 */
	
	private String getEarliestArriveTime(Date visitDate,SuppGoods suppGoods){
		Map<String,Object> propMap = prodProductClientService.findProdProductProp(suppGoods.getProdProduct().getBizCategoryId(), suppGoods.getProductId());
		String earliestArriveTime=(String) propMap.get("earliest_arrive_time");
		earliestArriveTime=getArriveTime(earliestArriveTime);
		Date now = new Date();
		if(DateUtils.isSameDay(visitDate, now)){
			String str = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
			Date date = DateUtil.toDate(str+" "+earliestArriveTime, "yyyy-MM-dd HH:mm");
			if(date.before(now)){//如果时间早于当前系统时间，后推70分钟取整
				date = DateUtils.addMinutes(now, 70);
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				StringBuffer sb = new StringBuffer();
				sb.append(c.get(Calendar.HOUR_OF_DAY));
				sb.append(":");
				int minute = c.get(Calendar.MINUTE);
				if(minute<30){
					sb.append("00");
				}else{
					sb.append("30");
				}
				return sb.toString();
			}
		}
		return earliestArriveTime;
	}
	
	/**
	 * 退改政策
	 * @param timePrice
	 * @param needGuarantee
	 * @param quantity
	 * @param arrivalTime
	 * @param beginDate
	 * @return
	 */
	public String getDoBookPolicy(TimePrice timePrice,Date visitTime,int quantity,String arrivalTime,
			String cancelStrategy,String payTarget,Date highPriceDate,long maxPrice,TimePriceVO timePriceVo){
		
		StringBuilder result = new StringBuilder("");
		Date lastCacleDateNew = DateUtil.toDate(timePrice.getLatestCancelDate(),"yyyy-MM-dd HH:mm"); //最晚无损取消时间
		Date bookTime = DateUtil.toDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm"),"yyyy-MM-dd HH:mm");//DateUtil.toDate(suppGoodsTimePrice.getAheadBookDate(),"yyyy-MM-dd HH:mm");
	
		
		
		if(cancelStrategy.equals(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
			result.append("退改政策：订单一经预订成功，不可变更/取消，如未按时入住，将按");
		}else{
			if(lastCacleDateNew.after(bookTime)){//预付最晚无损取消时间>预订时间
				result.append("退改政策：如您不能按时入住，请在"+getLatestCancelDate(visitTime,timePrice.getLatestCancelTime())+"前取消订单，超时将按");
			}else{
				result.append("退改政策：订单一经预订成功，不可变更/取消，如未按时入住，将按");
      	    }	
		}
		 
		//预付
		if(StringUtils.equals(payTarget, SuppGoods.PAYTARGET.PREPAID.name())){
			
			result=getContentByDeductType(timePrice,result,payTarget,visitTime,quantity,highPriceDate,maxPrice);
			return result.toString();
		}else{ //到付
			
			//担保类型
			if(StringUtils.isEmpty(timePrice.getGuarType())||OrderEnum.GUARANTEE_TYPE.NONE.name().equals(timePrice.getGuarType())){
				return "";
			}
			
			//房量担保
			if(null!=timePrice.getGuarQuantity()
					&&0<timePrice.getGuarQuantity()
					&&quantity>timePrice.getGuarQuantity()){
				result=getContentByDeductType(timePrice,result,payTarget,visitTime,quantity,highPriceDate,maxPrice);
				timePriceVo.setNeedGuarantee("true");
				return result.toString();
			}
			
			if(StringUtils.isEmpty(timePrice.getBookLimitType())||SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equals(timePrice.getBookLimitType())){
				return "";
			}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equals(timePrice.getBookLimitType())){
				 
				int hours=Integer.valueOf(arrivalTime.split(":")[0]);
				int min=Integer.valueOf(arrivalTime.split(":")[1]);
				Long laterstUnguarTime=timePrice.getLatestUnguarTime();
				
				//下单填写的最晚到店时间>后台的保留时间(入住当天为基数)，不用担保
				if(hours<laterstUnguarTime||(hours==laterstUnguarTime&&min==0)){
					 
				}else{
					result=getContentByDeductType(timePrice,result,payTarget,visitTime,quantity,highPriceDate,maxPrice);
					timePriceVo.setNeedGuarantee("true");
					return result.toString();
				}
				
			}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equals(timePrice.getBookLimitType())){
				result=getContentByDeductType(timePrice,result,payTarget,visitTime,quantity,highPriceDate,maxPrice);
				timePriceVo.setNeedGuarantee("true");
				return result.toString();
			}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePrice.getBookLimitType())){
				result=getContentByDeductType(timePrice,result,payTarget,visitTime,quantity,highPriceDate,maxPrice);
				timePriceVo.setNeedGuarantee("true");
				return result.toString();
			}
			
		}
		return "";
	}
	
	
	/**
	 * 新版的退改规则
	 * @param timePrice
	 * @param visitTime
	 * @param quantity
	 * @param arrivalTime
	 * @param cancelStrategy
	 * @param payTarget
	 * @param highPriceDate
	 * @param maxPrice
	 * @param timePriceVo
	 * @return
	 */
	public String getNewDoBookPolicy(TimePrice suppGoodsTimePrice,Date visitTime,int quantity,String arrivalTime,
			String cancelStrategy,String payTarget,Date highPriceDate,long maxPrice,TimePriceVO timePriceVo,Long orderPrice){
		    StringBuilder result = new StringBuilder("");
			if (suppGoodsTimePrice != null && StringUtils.isNotBlank(payTarget)) {
				Date lastCacleDateNew = DateUtil.toDate(suppGoodsTimePrice.getLatestCancelDate(),"yyyy-MM-dd HH:mm"); //最晚无损取消时间
				Date bookTime = DateUtil.toDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm"),"yyyy-MM-dd HH:mm");//DateUtil.toDate(suppGoodsTimePrice.getAheadBookDate(),"yyyy-MM-dd HH:mm");
			
				if (SuppGoods.PAYTARGET.PREPAID.name().equalsIgnoreCase(payTarget)) {
					if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 预付可退改
						if(suppGoodsTimePrice.getLatestCancelTime()==null){
							result.append("退改政策：订单一经预订成功，不可变更/取消，如未入住将扣除"+getReturnMessage(suppGoodsTimePrice));
							result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
							return  result.toString();
						}
						else if(lastCacleDateNew.after(bookTime)){//预付最晚无损取消时间>预订时间
								
								result.append("退改政策：在"+suppGoodsTimePrice.getLatestCancelDate()+"前您可免费变更/取消订单，超时变更/取消订单，酒店将扣除"+getReturnMessage(suppGoodsTimePrice));
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								return  result.toString();
							}else{
								result.append("退改政策：订单一经预订成功，不可变更/取消，如未入住将扣除"+getReturnMessage(suppGoodsTimePrice));
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								return  result.toString();
							}
					} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 预付不退不改
						result.append("退改政策：订单一经预订成功，不可变更/取消，如未入住将扣除"+getReturnMessage(suppGoodsTimePrice));
						result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
						return  result.toString();

					}
					else{  //退改类型=null的情况(cancelStrategy) add by caiyingshi
						result.append("退改政策：订单一经预订成功，不可变更/取消，如未入住将扣除"+getReturnMessage(suppGoodsTimePrice));
						result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
						return  result.toString();
					}
				} else if (SuppGoods.PAYTARGET.PAY.name().equalsIgnoreCase(payTarget)) {

					//Date latestUnguarDate = DateUtil.toDate(suppGoodsTimePrice.getLatestUnguarDate(),"yyyy-MM-dd HH:mm"); //最晚无损取消时间
					//Date theLastArrayingTime=DateUtil.toDate(suppGoodsTimePrice.getTheLastArrayingTime(),"yyyy-MM-dd HH:mm");
					
					if(SuppGoodsTimePrice.GUARTYPE.CREDITCARD.name().equalsIgnoreCase(suppGoodsTimePrice.getGuarType())){ //信用卡
						
						if(SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType())){
						int hours=Integer.valueOf(arrivalTime.split(":")[0]);
						int min=Integer.valueOf(arrivalTime.split(":")[1]);
						Long laterstUnguarTime=suppGoodsTimePrice.getLatestUnguarTime();
						if(hours<laterstUnguarTime||(hours==laterstUnguarTime&&min==0)){	
						/*	result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
							result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
							return  result.toString();
						}else{
							
						if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 现付可退改
							if(suppGoodsTimePrice.getLatestCancelTime()==null){
								if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
									result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
									result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								}
								return  result.toString();
							}else if(lastCacleDateNew.after(bookTime)){//预付最晚无损取消时间>预订时间
								if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
									result.append("退改政策：在"+suppGoodsTimePrice.getLatestCancelDate()+"前可以免费变更/取消订单，超时变更/取消订单，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
									result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								}
									return  result.toString();
								}else{
									if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
										result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
										result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
									}
									return  result.toString();
					      	    }
						} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 现付不退不改
							if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
								result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
							}
							return  result.toString();
						}
						else{  //退改类型=null的情况(cancelStrategy) add by caiyingshi
							if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
								result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
							
							}
							return  result.toString();
						}
					}			
					}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType()) || SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType())){
							
						if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 现付可退改

							if(suppGoodsTimePrice.getLatestCancelTime()==null){
								if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
								result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								}
								return  result.toString();
							}else if(lastCacleDateNew.after(bookTime)){//预付最晚无损取消时间>预订时间
									if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断	
										result.append("退改政策：在"+suppGoodsTimePrice.getLatestCancelDate()+"前可以免费变更/取消订单，超时变更/取消订单，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
										result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
									}
									return  result.toString();
								}else{
									if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
										result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
										result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
									}
									return  result.toString();
							
					      	    }
						} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsTimePrice.getCancelStrategy())) { // 现付不退不改
								if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
								result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
								result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
								}
								return  result.toString();
						}
						else{  //退改类型=null的情况(cancelStrategy) add by caiyingshi
							if(!(StringUtils.isBlank(suppGoodsTimePrice.getDeductType()) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(suppGoodsTimePrice.getDeductType()))){//业务让加一层扣款类型判断
							result.append("退改政策：订单一经预订成功，不可变更/取消。如未按时入住，酒店将扣除您的担保金额"+getPriceInfo(suppGoodsTimePrice,quantity, maxPrice, orderPrice)+"作为违约金。");
							result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
							}
							return  result.toString();
						}
		
					}else{
						/*result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
						result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
						return  result.toString();

					}
				
					}else{//担保方式为null或者为"无"
						/*result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
						result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
						return  result.toString();
					}		
			}
			}
		
		return null;
	}
	/**
	 * 根据扣款类型返回对应的提示信息
	 * @param suppGoodsTimePrice
	 * @return String
	 */
	public static String getReturnMessage(SuppGoodsTimePrice suppGoodsTimePrice){
		String deducttype=suppGoodsTimePrice.getDeductType();
		String message="";
	
		if(StringUtils.isBlank(deducttype) || SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(deducttype)){
			message="全额房费";
		}
		if(SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(deducttype)){
			message="全额房费";
		}
		if(SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(deducttype)){
			message="首日房费";
		}
		if(SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(deducttype)){
			if(suppGoodsTimePrice.getDeductValue()==null){
				return "全额房费";
			}
			message="房费￥"+suppGoodsTimePrice.getDeductValue()/100+"。";
		}
		if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(deducttype)){
			if(suppGoodsTimePrice.getDeductValue()==null){
				return "全额房费";
			}
			message=suppGoodsTimePrice.getDeductValue()+"%房费";
		}
		return message;
	}
	private String getPriceInfo(TimePrice timePrice,int quantity,Long maxPrice,Long orderPrice){
		
		if(StringUtils.isEmpty(timePrice.getDeductType())||SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equals(timePrice.getDeductType())){
			return " ￥"+PriceUtil.trans2YuanStr(orderPrice);
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equals(timePrice.getDeductType())){
			return " ￥"+PriceUtil.trans2YuanStr(orderPrice);
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equals(timePrice.getDeductType())){
			return " ￥"+getDeductValueToYuan(timePrice.getPrice()* quantity,timePrice.getDeductType());
			
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equals(timePrice.getDeductType())){
			return "￥"+getDeductValueToYuan(timePrice.getDeductValue(), timePrice.getDeductType());
			
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equals(timePrice.getDeductType())){
			return "￥"+getDeductValueToYuan(timePrice.getPrice()* quantity,timePrice.getDeductType());
			
		}else if("PEAK".equals(timePrice.getDeductType())){
			return "￥"+PriceUtil.trans2YuanStr(maxPrice);
		}else{
			return " ￥"+PriceUtil.trans2YuanStr(orderPrice);
		}
	}
	
	private String getLatestCancelDate(Date visitTime,Long latestCancelTime){
		Calendar cal = Calendar.getInstance();
			if(null!=visitTime){
				cal.setTime(visitTime);
			}
			if(null!=latestCancelTime){
				long milliseconds =cal.getTime().getTime()-latestCancelTime*1000*60;
				cal.setTimeInMillis(milliseconds);
			}
		return DateUtil.formatDate(cal.getTime(), "yyyy-MM-dd HH:mm");
	}
	
	private StringBuilder getContentByDeductType(TimePrice timePrice,StringBuilder result,String payTarget,Date visitTime,int quantity,Date highPriceDate,Long maxPrice){
		
		if(StringUtils.isEmpty(timePrice.getDeductType())||SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equals(timePrice.getDeductType())){
			return new StringBuilder("");
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equals(timePrice.getDeductType())){
			result.append("订单全额扣款。");
			if(OrderEnum.PAYMENT_TYPE.PAY.name().equals(payTarget)){
				result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
			}
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equals(timePrice.getDeductType())){
			result.append("入住首日房费("+DateUtil.formatDate(visitTime, "yyyy-MM-dd")+" ￥"+getDeductValueToYuan(timePrice.getPrice()* quantity,timePrice.getDeductType())+")进行扣款 。");
			if(OrderEnum.PAYMENT_TYPE.PAY.name().equals(payTarget)){
				result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
			}
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equals(timePrice.getDeductType())){
			result.append(" ￥"+getDeductValueToYuan(timePrice.getDeductValue(), timePrice.getDeductType())+" 进行扣款。");
			if(OrderEnum.PAYMENT_TYPE.PAY.name().equals(payTarget)){
				result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
			}
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equals(timePrice.getDeductType())){
			result.append("订单金额的  "+getDeductValueToYuan(timePrice.getDeductValue(), timePrice.getDeductType())+"% 进行扣款。");
			if(OrderEnum.PAYMENT_TYPE.PAY.name().equals(payTarget)){
				result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
			}
		}else if("PEAK".equals(timePrice.getDeductType())){
			result.append("入住期间内最高房费（"+DateUtil.formatDate(highPriceDate, "yyyy-MM-dd")+" ￥"+PriceUtil.trans2YuanStr(maxPrice)+"）进行扣款。");
			if(OrderEnum.PAYMENT_TYPE.PAY.name().equals(payTarget)){
				result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");
			}
			
		}else{
			return new StringBuilder("");
		}
		return result;
	}
	
	public String getDeductValueToYuan(Long deductValue,String deductType) {
		if(deductValue==null){
			return "0";
		}
		if(!StringUtils.equals(com.lvmama.vst.back.goods.po.SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name(), deductType)){
			return PriceUtil.trans2YuanStr(deductValue);
		}else{
			return String.valueOf(deductValue);
		}
	}
	
	public ResultHandle validateCoupon(BuyInfo buyInfo){
		ResultHandle resultHandle=new ResultHandle();
		 try {
			 	resultHandle=couponService.validateCoupon(buyInfo);
			   
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg("验证发生异常");
		}
		return resultHandle; 
	}
	
	private FavorStrategyInfo calCoupon(BuyInfo buyInfo){
		FavorStrategyInfo fsi=null;
		try {
			Pair<FavorStrategyInfo, Object> resultPair=couponService.calCoupon(buyInfo);
			if(resultPair.isSuccess()){
			 	fsi=resultPair.getFirst();
		    }
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		return fsi;
	}
	
	// 根据商品信息查询快递价格
	private PriceInfo findOrderExpressPrice(BuyInfo info) throws Exception {
		if(info==null||info.getExpressage()==null){
			return null;
		}
		List<Long> productIdsList = new ArrayList<Long>();
		String provinceCode = "";
		String cityCode = "";
		// 得到需要查询价格的商品ID
		if (!CollectionUtils.isEmpty(info.getItemList())) {
			Iterator<BuyInfo.Item> itr = info.getItemList().iterator();
			BuyInfo.Item item = null;
			SuppGoods suppGoods=null;
			while (itr.hasNext()) {
				item = itr.next();
				if (item.getQuantity() > 0 ) {
					ResultHandleT<SuppGoods> suppGoodsHandleT;
					try {
						suppGoodsHandleT = distGoodsClientService.findSuppGoodsById(Constant.DIST_FRONT_END, item.getGoodsId());
						suppGoods = suppGoodsHandleT.getReturnContent();
						if(suppGoods!=null&&StringUtils.isNotEmpty(suppGoods.getGoodsType())&&suppGoods.getGoodsType().equals(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name())){
							productIdsList.add(item.getGoodsId());
						}						
					} catch (Exception e) {
						logger.error(ExceptionFormatUtil.getTrace(e));
						throw new Exception("调用商品查询服务出错");
					}					
				
				}
			}
			provinceCode = info.getExpressage().getProvinceCode();
			cityCode = info.getExpressage().getCityCode();
			if (StringUtils.isEmpty(provinceCode)
					|| StringUtils.isEmpty(cityCode)
					|| provinceCode.equals("-1") || cityCode.equals("-1")
					|| productIdsList.size() == 0||provinceCode.indexOf("选择")>-1||cityCode.indexOf("选择")>-1) {
				return null;
			}
		}
		PriceInfo priceInfo = new PriceInfo();
		Map<Long, String> itemPriceMap = new HashMap<Long, String>();
		long totalPrice = 0;

		// 调用远程服务，进行价格查询
		ResultHandleT<Map<Long, ExpressSuppGoodsVO>> resultHandler = suppGoodsClientRemote
				.findSuppGoodsExpreeCost(productIdsList, provinceCode, cityCode);
		if (resultHandler != null&&resultHandler.isSuccess()) {
			Map<Long, ExpressSuppGoodsVO> map = resultHandler.getReturnContent();
			if (MapUtils.isNotEmpty(map)) {
				ExpressSuppGoodsVO item = null;
				Iterator<ExpressSuppGoodsVO> itr = map.values().iterator();
				while (itr.hasNext()) {
					item = itr.next();
					SuppGoodsNotimeTimePrice suppGoodsNotimeTimePrice = item
							.getSuppGoodsNotimeTimePrice();
					if (suppGoodsNotimeTimePrice != null
							&& suppGoodsNotimeTimePrice.getPrice() > 0) {
						totalPrice += suppGoodsNotimeTimePrice.getPrice();
						itemPriceMap.put(item.getSuppGoodsId(),
								suppGoodsNotimeTimePrice.getPrice() + "");
					}
				}
			}
		}
		priceInfo.setExpressPrice(totalPrice);
		priceInfo.setItemPriceMap(itemPriceMap);
		logger.info("express price:to:"+provinceCode+","+cityCode+" 快递费用:"+totalPrice);
		return priceInfo;
	}
	
	private String getCancelStrategy(OrdOrderDTO order){
		StringBuilder str=new StringBuilder(25);
		
		String cancelStrategy="";
		int refunCount=0;
		
		List<OrdOrderItem> orderItemList=order.getOrderItemList();
		long amount = 0;
		for (OrdOrderItem ordOrderItem : orderItemList) {
			if(StringUtil.isEmptyString(cancelStrategy)&&!StringUtil.isEmptyString(ordOrderItem.getCancelStrategy())){
				cancelStrategy=ordOrderItem.getCancelStrategy();
				refunCount+=1;
			}else if(!cancelStrategy.equals(ordOrderItem.getCancelStrategy())){
				refunCount+=1;
			}
			if(ordOrderItem.getDeductAmount()!=null){
				amount+=ordOrderItem.getDeductAmount();
			}
		}
		
		if(refunCount>1){
			str.append("人工退改. ");
		}else if(refunCount==1){
			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(cancelStrategy)){
				str.append("请在"+DateUtil.formatDate(order.getLastCancelTime(), "yyyy-MM-dd HH:mm:ss")+"之前取消订单 逾期将收取金额"+PriceUtil.convertToYuan(amount)+"作为违约金。");
			}else if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(cancelStrategy)){
				str.append("不退不改 . ");
			}else if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(cancelStrategy)){
				str.append("人工退改. ");
			}else{
				str.append("无");
			}
		}else{
			str.append("无");
		}
		return str.toString();
	}
	
	private String getArriveTime(String earliestArriveTime){
		Calendar calendar = Calendar.getInstance();
		Date now=new Date();
		calendar.setTime(now);
		String[] arriveTime=earliestArriveTime.split(":");
		int hour=Integer.valueOf(arriveTime[0]);
		int minutes=30;
		if(arriveTime.length>1){
			minutes+=Integer.valueOf(arriveTime[1]);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		return DateUtil.formatDate(calendar.getTime(), "HH:mm");
	}
	
	/**
	 * 是否为邮轮组合产品
	 * @param product
	 * @return
	 */
	private boolean hasCruiseComb(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return "category_comb_cruise".equalsIgnoreCase(bc.getCategoryCode());
		}
		return false;
	}
	
	/**
	 * 获取自驾游儿童价
	 * @param order
	* @param buyInfo
	* @date 2016-12-19 下午1:50:29
	 */
	private long getSelfDrivingChildPriceAmount(OrdOrderDTO order,BuyInfo buyInfo){
		long selfDrivingChildPriceAmount = 0L;
		try {
			Boolean destBuOrder = OrdOrderUtils.isDestBuFrontOrder(order);
			if(destBuOrder && buyInfo!=null && buyInfo.getSelfDrivingChildQuantity()>0){
				ProdProduct prodProduct =null;
				ResultHandleT<ProdProduct> bugProduct = prodProductClientService.findProdProductByIdFromCache(buyInfo.getProductId());
				if (bugProduct.isFail() || bugProduct.getReturnContent() == null) {
					return selfDrivingChildPriceAmount;
				}
				prodProduct=bugProduct.getReturnContent();
		    	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(prodProduct.getBizCategoryId())
		    			&& BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(prodProduct.getSubCategoryId())){
		    		ResultHandleT<List<ProdProductSaleRe>> resultHandleT = prodProductSaleReClientService.queryByProductId(prodProduct.getProductId());
					if(resultHandleT != null && resultHandleT.isSuccess()){
						List<ProdProductSaleRe> prodProductSaleRes = resultHandleT.getReturnContent();
						if(!CollectionUtils.isEmpty(prodProductSaleRes)){
							if(ProdProductSaleRe.SALETYPE.PEOPLE.name().equals(prodProductSaleRes.get(0).getSaleType()) &&
								ProdProductSaleRe.HOUSEDIFFTYPE.AMOUNT.name().equals(prodProductSaleRes.get(0).getChildPriceType()) && 
								prodProductSaleRes.get(0).getChildPriceAmount()!=null && prodProductSaleRes.get(0).getChildPriceAmount()>0){
								selfDrivingChildPriceAmount=prodProductSaleRes.get(0).getChildPriceAmount();
							}
						}
					}
		    	}
			}
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		logger.info("getSelfDrivingChildPriceAmount ======"+selfDrivingChildPriceAmount);
    	return selfDrivingChildPriceAmount;
	}
	
	@Override
	public long queryMaxBounsAmount(BuyInfo buyInfo){
		OrdOrderDTO order =orderInitService.initOrderAndCalc(buyInfo);
		long orderPrice = order.getOughtAmount();
		long maxBonus=0;
		//记录商品品类
		List<String> goodsCategorys = new ArrayList<String>();
		List<OrdOrderItem> orderItemList=order.getOrderItemList();
		for (OrdOrderItem ordOrderItem : orderItemList) {
			SuppGoods suppGoods = ordOrderItem.getSuppGoods();
			BizCategory category=suppGoods.getProdProduct().getBizCategory();
			String categoryCode = category.getCategoryCode();
			
			//记录商品品类以及距离类型
			if(ProductCategoryUtil.isRoute(categoryCode)){
				String type = category.getCategoryCode()+"_"+suppGoods.getProdProduct().getProductType();
				goodsCategorys.add(type);
			}else{
				goodsCategorys.add(categoryCode);
			}
		}
		logger.info("ordUserOrderServiceAdapter.getOrderBonusCanPayAmount userNo:"+buyInfo.getUserNo()+",orderPrice:"+orderPrice+",goodsCategorys:"+goodsCategorys);
		maxBonus = ordUserOrderServiceAdapter.getOrderBonusCanPayAmount(buyInfo.getUserNo(), orderPrice, goodsCategorys);
		logger.info("maxBonus ======"+maxBonus);
		return maxBonus;	
	}


	@Override
	public VstCashAccountVO queryMoneyAccountByUserId(Long userId) {
		return ordUserOrderServiceAdapter.queryMoneyAccountByUserId(userId);
	}


	@Override
	public boolean vstPayFromMoneyAccount(String bizType, Long userId,
			Long orderId, Long payAmount) {
		return ordUserOrderServiceAdapter.vstPayFromMoneyAccount(bizType, userId, orderId, payAmount);
	}


	@Override
	public boolean vstPayFromBonusAccount(String bizType, Long orderId,
			Long userId, Long payAmount) {
		return ordUserOrderServiceAdapter.vstPayFromBonusAccount(bizType, orderId, userId, payAmount);
	}

	@Override
	public List<PromPromotion> vstFindPromPromotion(OrdOrderPrice ordOrderPrice) {
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		OrdOrderDTO order = new OrdOrderDTO(ordOrderPrice.getBuyInfo());
		order.setDistributorId(ordOrderPrice.getDistributorId());
		order.setDistributionChannel(ordOrderPrice.getDistributionChannel());
		try{
			if(org.apache.commons.collections.CollectionUtils.isNotEmpty(ordOrderPrice.getOrderPackList())){
				logger.info("$$" + ordOrderPrice.getBuyInfo().getUserNo()  + "$$" + "order.getOrderPackList() != null");
				for(OrdOrderPack pack:ordOrderPrice.getOrderPackList()){
					logger.info("$$" + ordOrderPrice.getBuyInfo().getUserNo()  + "$$" + "非邮轮组合产品");
					List<PromPromotion> list = promotionBussiness.makeProductPromotion(order,pack);
					if(!list.isEmpty()) result.addAll(list);
				}
			}
			if(org.apache.commons.collections.CollectionUtils.isNotEmpty(ordOrderPrice.getOrderItemList())){
				logger.info("$$" + ordOrderPrice.getBuyInfo().getUserNo()  + "$$" + "order.getNopackOrderItemList() != null");
				for(OrdOrderItem orderItem:ordOrderPrice.getOrderItemList()){
					//次规格产品不参与促销
					if("Y".equals(orderItem.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())){
						if(StringUtils.isNotEmpty((orderItem.getSuppGoods().getProdProduct().getBizCategory().getPromTarget()))){
							List<PromPromotion> list = promotionBussiness.makeSuppGoodsPromotion(order,
									orderItem,PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name());
							if(!list.isEmpty()){
								result.addAll(list);
							}
						}
					}
				}
			}
		}catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		return result;
	}

	@Override
	public PriceInfo countPriceComb(final DestBuBuyInfo buyInfo){
		PriceInfo priceInfo = new PriceInfo();
		
		long orderMarketPrice = 0L;// 订单市场价金额
		long orderPrice = 0L;// 订单销售价金额
		long orderOughtPay = 0L;// 订单应付金额
		long promotionAmount=0L; //订单促销商品优惠总金额
		long couponAmount=0L; //订单优惠券抵扣总金额
		long quantitySum = 0L;
		long ticketAmount=0L;  
		long insurancePrice = 0L;
		long expressPrice=0L;
		long bonus=0L;
		long maxBonus=0L;
		long gapPrice=0L; //房差价格
		long rebateAmount=0L;//点评返现金额
		long depositPrice=0L;
		
		//记录商品品类
		List<String> goodsCategorys = new ArrayList<String>();
		
		
		
		try{
			
			logger.info("$$" + buyInfo.getUserNo()  + "$$" + "接收到价格计算请求，进行buyInfo信息打印" + buyInfo.toJsonStr());
			OrdOrderDTO order = new OrdOrderDTO();
			//调用初始化方法计算订单
			newHotelComOrderInitService.initOrderAndCalcForFront(buyInfo, order);
			logger.info("$$" + buyInfo.getUserNo()  + "$$" + "已经完成订单计算，进行buyInfo信息打印" + buyInfo.toJsonStr());
			orderPrice = order.getOughtAmount();
			rebateAmount=order.getRebateAmount();
			//修改毛利率是否大于0.00
			//计算毛利是否大于0.03
			boolean isCanBoundLipinkaPay = false;
			Long originalPrice = order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE.name());
			Long originalSettlePrice = order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
			if(originalPrice != null && originalSettlePrice != null) {
				isCanBoundLipinkaPay = (originalPrice > (originalSettlePrice * (1 + 0.00)));
			}
			priceInfo.setCanBoundLipinkaPay(isCanBoundLipinkaPay);
			priceInfo.setRebateAmount(rebateAmount);
			//end
			
			priceInfo.setPaymentTarget(order.getPaymentTarget());
			priceInfo.setPaymentType(order.getPaymentType());
			priceInfo.setResourceStatus(order.getResourceStatus());
			priceInfo.setDoBookPolicyStr(getCancelStrategy(order));
			//修改分销子渠道的促销
			//促销
			List<PromPromotion> promotionList = findPromPromotion(order);
			if(!CollectionUtils.isEmpty(promotionList)) {
				for (PromPromotion promPromotion : promotionList) {
					logger.info("$$" + buyInfo.getUserNo()  + "$$" + "平台促销log" + promPromotion.getPromPromotionId());
				}
			} else {
				logger.info("$$" + buyInfo.getUserNo()  + "$$" + "平台促销log信息为空");
			}
			if(buyInfo.getSubDistributorId()!=null){
				promotionList=vstPromotionOrderServiceRemote.getOrderPromotionList(promotionList, buyInfo.getSubDistributorId());
				if(!CollectionUtils.isEmpty(promotionList)) {
					for (PromPromotion promPromotion : promotionList) {
						logger.info("$$" + buyInfo.getUserNo()  + "$$" + "分销筛选平台促销log" + promPromotion.getPromPromotionId());
					}
				} else {
						logger.info("$$" + buyInfo.getUserNo()  + "$$" + "分销筛选平台促销log信息为空");
				}
				if(promotionList!=null){
					Long subDistributorId = buyInfo.getSubDistributorId();
					if(subDistributorId == 107 ||subDistributorId == 108 ||subDistributorId == 110) {
						//检查促销可用余额是否满足
						Iterator<PromPromotion> it = promotionList.iterator();
						while(it.hasNext()){
							PromPromotion promPromotion = it.next();
							if(promPromotion.getPromAmount()!=null){
								long usedAmount = promPromotion.getUsedAmount()==null?0L:promPromotion.getUsedAmount();
								long balance =promPromotion.getPromAmount()-usedAmount;
								//活动可用余额大于等于促销金额才存
								logger.info("优惠条件剩余可用金额"+promPromotion.getPromAmount()+usedAmount+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());
								if (balance < promPromotion.getDiscountAmount()) {
									it.remove();
									logger.info("去除优惠条件剩余可用金额"+balance+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());

								}
							}
						}
						
//						for (PromPromotion promPromotion : promotionList) {
//							if(promPromotion.getPromAmount()!=null){
//								long usedAmount = promPromotion.getUsedAmount()==null?0L:promPromotion.getUsedAmount();
//								long balance =promPromotion.getPromAmount()-usedAmount;
//								//活动可用余额大于等于促销金额才存
//								logger.info("优惠条件剩余可用金额"+promPromotion.getPromAmount()+usedAmount+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());
//								if (balance < promPromotion.getDiscountAmount()) {
//									promotionList.remove(promPromotion);
//									logger.info("去除优惠条件剩余可用金额"+balance+"+++++++++++++当前优惠金额"+promPromotion.getDiscountAmount());
//
//								}
//							}
//						}
					}
				}
			}
			//如果是渠道优惠，绑定支付渠道中文名
			buildPayChannelCnName(promotionList);
			priceInfo.setPromotionList(promotionList);
			//满赠
			BuyPresentActivityInfo buyPresentInfo = promBuyPresentBussiness.findPromBuyPresent(order);

			priceInfo.setBuyPresentActivityInfo(buyPresentInfo);
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			TimePrice hotelSuppGoodsTimePrice=null;
			for (OrdOrderItem ordOrderItem : orderItemList) {
				boolean isHotelPack = ordOrderItem.getOrderPack()==null;	
				if(hotelSuppGoodsTimePrice==null && isHotelPack){
						hotelSuppGoodsTimePrice= getsuppGoodsTimePriceByRules(orderItemList);	
				}
				long itemsPrice = 0 ; //一条商品的价格
				
				List<OrdMulPriceRate> mulPriceRateList=ordOrderItem.getOrdMulPriceRateList();
				if(!CollectionUtils.isEmpty(mulPriceRateList)){
					for (OrdMulPriceRate mul : mulPriceRateList) {
						if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name().equals(mul.getPriceType())){
							gapPrice+=mul.getPrice(); //计算房差价格
						}
					}
				}
				SuppGoods suppGoods = ordOrderItem.getSuppGoods();
				
				BizCategory category=suppGoods.getProdProduct().getBizCategory();
				String categoryCode = category.getCategoryCode();
				
				//记录商品品类以及距离类型
				if(ProductCategoryUtil.isRoute(categoryCode)){
					String type = category.getCategoryCode()+"_"+suppGoods.getProdProduct().getProductType();
					goodsCategorys.add(type);
				}else{
					goodsCategorys.add(categoryCode);
				}
				
				// 酒店情况的时候
				if(ProductCategoryUtil.isHotel(categoryCode)){
					
//					BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
					Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
					List<OrdOrderHotelTimeRate> ordHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
					if (!CollectionUtils.isEmpty(ordHotelTimeRateList)) {
						 
						//endDate = DateUtils.addDays(endCal.getTime(), -1);
						
						//入住期间内最高单价
						long maxPrice=0;
						//入住期间内最高单价日
						Date highPriceDate=null;
						//全程担保
						TimePrice allTimePrice=null;
						
						//得到最早到点时间
						priceInfo.setEarliestArriveTime(getEarliestArriveTime(ordOrderItem.getVisitTime(), suppGoods));
						
						//退改政策
						String cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
						int i = 0;
						for (OrdOrderHotelTimeRate ordHotelTimeRate : ordHotelTimeRateList) {
							SuppGoodsTimePrice timePrice=(SuppGoodsTimePrice) ordHotelTimeRate.getTimePrice();
							long itemPrice = timePrice.getPrice() * ordOrderItem.getQuantity();//每个商品的价格
							itemsPrice += itemPrice;//同类商品的总价
							
							//商品促销优惠用
							BuyInfoPromotion.ItemPrice itemPc=new BuyInfoPromotion.ItemPrice(timePrice.getSpecDate());
							itemPc.setPrice(timePrice.getPrice());
							itemPriceMap.put(timePrice.getSpecDate(), itemPc);
							
							//入住期间内最高单价及日期
							if(itemPrice>maxPrice){
								maxPrice=itemPrice;
								highPriceDate=timePrice.getSpecDate();
							}
							//预付情况
							if(StringUtils.equals(suppGoods.getPayTarget(), SuppGoods.PAYTARGET.PREPAID.name())){
								if(StringUtils.equals(timePrice.getCancelStrategy(),SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
									cancelStrategy = timePrice.getCancelStrategy();
								}
							//现付情况
							}else{
								//入住期间内全程担保
								if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equals(timePrice.getBookLimitType())){
									allTimePrice=new TimePrice();
									BeanUtils.copyProperties(timePrice,allTimePrice);
								}
								
								if(i==0){
									cancelStrategy = timePrice.getCancelStrategy();
								}
							}
							i++;
						}
						/*退订政策 start*/
						String doBookPolicyStr="";
						if(StringUtils.isEmpty(cancelStrategy)){
							cancelStrategy = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
						}
						String earlyArrivalTime=ordOrderItem.getContentStringByKey(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name());
						TimePriceVO timePriceVO = new TimePriceVO();
						boolean hasHotelPack = ordOrderItem.getOrderPack()==null;
						if(null!=allTimePrice){
							if(hasHotelPack){
								doBookPolicyStr=this.getNewDoBookPolicy(hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
							}						
							timePriceVO.setBookLimitType(allTimePrice.getBookLimitType());
							Long latestCancelTime = allTimePrice.getLatestCancelTime();
							Long latestUnguarTime = allTimePrice.getLatestUnguarTime();
							timePriceVO.setLatestCancelTime(latestCancelTime);
							timePriceVO.setLatestUnguarTime(latestUnguarTime);
							timePriceVO.setGuarType(allTimePrice.getGuarType());
							timePriceVO.setDeductType(allTimePrice.getDeductType());
							timePriceVO.setPrice(PriceUtil.trans2YuanStr(allTimePrice.getPrice() * ordOrderItem.getQuantity()));
							timePriceVO.setMaxPrice(PriceUtil.trans2YuanStr(maxPrice));
							timePriceVO.setHighPriceDate(DateUtil.formatSimpleDate(highPriceDate));
							timePriceVO.setDeductValue(ordOrderItem.getDeductAmount());
							timePriceVO.setVisitTime(ordOrderItem.getVisitTime());
							timePriceVO.setGuarQuantity(allTimePrice.getGuarQuantity());
							
							
						}else{
							SuppGoodsTimePrice timePrice=(SuppGoodsTimePrice) ordHotelTimeRateList.get(0).getTimePrice();
							TimePrice firstTimePrice=new TimePrice();
							BeanUtils.copyProperties(timePrice,firstTimePrice);
							if(hasHotelPack){
								doBookPolicyStr=this.getNewDoBookPolicy(hotelSuppGoodsTimePrice, ordOrderItem.getVisitTime(), ordOrderItem.getQuantity().intValue(), earlyArrivalTime,
										cancelStrategy, suppGoods.getPayTarget(), highPriceDate, maxPrice,timePriceVO,orderPrice);
							}
							timePriceVO.setBookLimitType(timePrice.getBookLimitType());
							Long latestCancelTime = timePrice.getLatestCancelTime();
							Long latestUnguarTime = timePrice.getLatestUnguarTime();
							timePriceVO.setLatestCancelTime(latestCancelTime);
							timePriceVO.setLatestUnguarTime(latestUnguarTime);
							timePriceVO.setGuarType(timePrice.getGuarType());
							timePriceVO.setDeductType(timePrice.getDeductType());
							timePriceVO.setPrice(PriceUtil.trans2YuanStr(timePrice.getPrice() * ordOrderItem.getQuantity()));
							timePriceVO.setMaxPrice(PriceUtil.trans2YuanStr(maxPrice));
							timePriceVO.setHighPriceDate(DateUtil.formatSimpleDate(highPriceDate));
							timePriceVO.setDeductValue(ordOrderItem.getDeductAmount());
							timePriceVO.setVisitTime(timePrice.getSpecDate());
							timePriceVO.setGuarQuantity(timePrice.getGuarQuantity());
							
						}
						
						priceInfo.setTimePriceVO(timePriceVO);
						if(hasHotelPack){
						 priceInfo.setDoBookPolicyStr(doBookPolicyStr);
						}
						/*退订政策 end*/
						 
					}
					priceInfo.setEarliestArriveTime(ordOrderItem.getContentStringByKey(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name()));
					
					//门票类(景点门票,其它票,组合套餐票(自主，供应商))
				}else if(ProductCategoryUtil.isTicket(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					ticketAmount+=itemsPrice;
				}else if(ProductCategoryUtil.isInsurance(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					insurancePrice+=itemsPrice;
				}else if(ProductCategoryUtil.isRoute(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isVisa(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isCruise(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isWifi(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
				}else if(ProductCategoryUtil.isOther(categoryCode)){
					itemsPrice += getTotalAmount(ordOrderItem);
					if(PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(ordOrderItem))){
						depositPrice+=itemsPrice;
					}else{
						expressPrice+=itemsPrice;
					}
				}
				else{
					itemsPrice += getTotalAmount(ordOrderItem);
				}
				priceInfo.getItemPriceMap().put(ordOrderItem.getSuppGoodsId(), PriceUtil.trans2YuanStr(itemsPrice));
				priceInfo.getItemMulPriceMap().put(ordOrderItem.getSuppGoodsId(), ordOrderItem.getOrdMulPriceRateList());
				quantitySum += ordOrderItem.getQuantity();// 购买商品数量总和
			}
			/*优惠券验证计算 start*/
			String youhuiType=buyInfo.getYouhui();
//			if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)){
//				List<DestBuBuyInfo.Coupon> couponList=buyInfo.getCouponList();
//				if(null!=couponList&&couponList.size()>0){
//					buyInfo.setOrderTotalPrice(orderPrice);//设置订单总价
//					List<ResultHandle> couponResultHandles=new ArrayList<ResultHandle>(2);
//					
//					//for (Coupon coupon : couponList) {
//					DestBuBuyInfo.Coupon coupon = couponList.get(0);
//					if(StringUtil.isNotEmptyString(coupon.getCode())){
//						Pair<FavorStrategyInfo, Object> resultPair=couponService.calCoupon(buyInfo);
//						if(resultPair.isSuccess()){
//							FavorStrategyInfo fsi=resultPair.getFirst();
//							couponAmount+=fsi.getDiscountAmount();
//							if (couponAmount==0) {
//							Pair<FavorStrategyInfo, Long> resultPairNotUse=new Pair<FavorStrategyInfo, Long>();
//									 resultPairNotUse.setMsg(fsi.getDisplayInfo());
//									 couponResultHandles.add(resultPairNotUse);
//								}
//						    }else{
//						    	couponResultHandles.add(resultPair);
//						    }
//						}
//					//}
//					priceInfo.setCouponResutHandles(couponResultHandles);
//				}
//			}
			/*优惠券验证计算end */
			
			/*计算快递费用start*/
			/*PriceInfo expressPriceInfo=null;
			try {
				expressPriceInfo = this.findOrderExpressPrice(buyInfo);
			}catch (Exception e){
				// TODO Auto-generated catch block
				logger.error("method countPrice() error, ", e);
				e.printStackTrace();
			}
			if(expressPriceInfo!=null){
				expressPrice=expressPriceInfo.getExpressPrice();			
				priceInfo.getItemPriceMap().putAll(expressPriceInfo.getItemPriceMap());
				priceInfo.setExpressItemPriceMap(expressPriceInfo.getItemPriceMap());
			}*/
			/*计算快递费用end*/		
			logger.info("已经完成订单计算-----------------------------533");
			//获取订单可使用奖金金额
			try {
				logger.info("-----------------------------------buyInfo.getUserNo():"+buyInfo.getUserNo());
				logger.info("-----------------------------------orderPrice"+buyInfo.getUserNo());
				logger.info("-----------------------------------goodsCategorys:"+goodsCategorys);
				maxBonus = ordUserOrderServiceAdapter.getOrderBonusCanPayAmount(buyInfo.getUserNo(), orderPrice, goodsCategorys);
				logger.info("-----------------------------------maxBonus:"+maxBonus);


			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
				logger.info("ordUserOrderServiceAdapter.getOrderBonusCanPayAmount userNo:"+buyInfo.getUserNo()+",orderPrice:"+orderPrice+",goodsCategorys:"+goodsCategorys);
				logger.info("maxBonus ======"+maxBonus);
				bonus=maxBonus;
				String target =buyInfo.getTarget();
				//如果是抵扣现金框触发
				if(StringUtils.isNotEmpty(target)&&target.equals(ORDER_FAVORABLE_TYPE.bonus.getCode())){
					bonus = PriceUtil.convertToFen(buyInfo.getBonusYuan());
					if(bonus>maxBonus){
						bonus=maxBonus;
					}
				}
			}
			logger.info("可用奖金计算完成---------------------------------");

			/*订单促销金额start*/
			/*Map<String, List<OrdPromotion>>  promotionMap=order.getPromotionMap();
			if(MapUtils.isNotEmpty(promotionMap)){
				for(String key:order.getPromotionMap().keySet()){
					List<OrdPromotion> list = order.getPromotionMap().get(key);
					for(OrdPromotion op:list){
						if(op.getPromFavorable().hasApplyAble()){
							promotionAmount+=op.getPromFavorable().getDiscountAmount();
						}
					}
				}
			}*/
			if(promotionList!=null){
				for(PromPromotion prom :promotionList){
					//排除掉邮轮促销，邮轮促销在前台页面通过用户手动选择
					if(!ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType())){
						promotionAmount+=prom.getDiscountAmount();
					}
				}
			}
//			promotionAmount = order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name());
			/*订单促销金额end*/	
			logger.info("应付金额计算"+"orderPrice:"+orderPrice+"couponAmount:"+couponAmount+"promotionAmount"+promotionAmount+"bonus"+bonus);
			orderOughtPay = orderPrice-couponAmount-promotionAmount-bonus;// 应付金额 
			if(orderOughtPay<1){
				orderOughtPay=0;
			}
			priceInfo.setGoodsTotalPrice(orderPrice-expressPrice-insurancePrice-depositPrice);
			priceInfo.setBonusYuan(PriceUtil.convertToYuan(bonus));
			priceInfo.setBonus(bonus);
			priceInfo.setMaxBonus(maxBonus);
			priceInfo.setCoupon(couponAmount);
			priceInfo.setMarketPrice(orderMarketPrice);
			priceInfo.setPrice(orderPrice);
			priceInfo.setOughtPay(orderOughtPay);
			priceInfo.setOrderQuantity(quantitySum);
			priceInfo.setPromotionAmount(promotionAmount);
			priceInfo.setTicketGoodsPrice(ticketAmount);
			priceInfo.setInsurancePrice(insurancePrice);
			priceInfo.setExpressPrice(expressPrice);
			priceInfo.setDepositPrice(depositPrice);
			
			logger.info("价格计算完成————————————————————————————————————————-");

		}catch(OrderException ex){
			logger.error("=com.lvmama.vst.order.service.impl.OrderPriceServiceImpl.countPrice error:", ex);
			logger.info(ex.getMessage());
			priceInfo.sendError(ex.getMessage());
		}
		
		
		
		return priceInfo;
	}

	
	public PriceInfo countPriceBase(BuyInfo buyInfo){
		PriceInfo priceInfo = null;
		boolean isHotel = hotelTradeApiService.checkIsHotelProduct(buyInfo);
		logger.info("...countPriceBase isHotel:"+isHotel);
		if(isHotel)
		{
			priceInfo = hotelTradeApiService.countPriceByHotel(buyInfo);
		}else{
			priceInfo = countPrice(buyInfo);
		}
		return priceInfo;
	}
	
	/**
	 * 设置APP端担保显示金额
	 * @param timePrice
	 * @param visitTime
	 * @param quantity
	 * @param arrivalTime
	 * @param cancelStrategy
	 * @param payTarget
	 * @param highPriceDate
	 * @param maxPrice
	 * @param timePriceVo
	 * @return
	 */
	public long getNewDoBookPolicyPrice(long productId,TimePrice suppGoodsTimePrice,Date visitTime,int quantity,String arrivalTime,
			String cancelStrategy,String payTarget,Date highPriceDate,long maxPrice,TimePriceVO timePriceVo,Long orderPrice){
		
		   logger.info("------getNewDoBookPolicyPrice--------productId----0000----"+ productId);
	       long deductValue = 0;
		if (suppGoodsTimePrice != null && StringUtils.isNotBlank(payTarget)) {
			Date lastCacleDateNew = DateUtil.toDate(suppGoodsTimePrice.getLatestCancelDate(),"yyyy-MM-dd HH:mm"); //最晚无损取消时间
			Date bookTime = DateUtil.toDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm"),"yyyy-MM-dd HH:mm");//DateUtil.toDate(suppGoodsTimePrice.getAheadBookDate(),"yyyy-MM-dd HH:mm");
		
			logger.info("------getNewDoBookPolicyPrice--------productId----1111----"+ productId);
			if (SuppGoods.PAYTARGET.PREPAID.name().equalsIgnoreCase(payTarget)) {
				 return deductValue;
			} else if (SuppGoods.PAYTARGET.PAY.name().equalsIgnoreCase(payTarget)) {

				//Date latestUnguarDate = DateUtil.toDate(suppGoodsTimePrice.getLatestUnguarDate(),"yyyy-MM-dd HH:mm"); //最晚无损取消时间
				//Date theLastArrayingTime=DateUtil.toDate(suppGoodsTimePrice.getTheLastArrayingTime(),"yyyy-MM-dd HH:mm");
				
				if(SuppGoodsTimePrice.GUARTYPE.CREDITCARD.name().equalsIgnoreCase(suppGoodsTimePrice.getGuarType())){ //信用卡
					
					if(SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType())){
					int hours=Integer.valueOf(arrivalTime.split(":")[0]);
					int min=Integer.valueOf(arrivalTime.split(":")[1]);
					Long laterstUnguarTime=suppGoodsTimePrice.getLatestUnguarTime();
					if(hours<laterstUnguarTime||(hours==laterstUnguarTime&&min==0)){	
					/*	result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
						result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
						logger.info("------getNewDoBookPolicyPrice--------productId----2222----"+ productId);
						return  deductValue;
					}else{
						 logger.info("------getNewDoBookPolicyPrice--------productId----3333----"+ productId);
						 long price = getPriceInfoWireless(suppGoodsTimePrice,quantity, maxPrice, orderPrice);
						 return price;
					}			
				}else if(SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType()) || SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equalsIgnoreCase(suppGoodsTimePrice.getBookLimitType())){
					   
					    logger.info("------getNewDoBookPolicyPrice--------productId----4444----"+ productId);
					    long price = getPriceInfoWireless(suppGoodsTimePrice,quantity, maxPrice, orderPrice);
					    return price;
				}else{
					   logger.info("------getNewDoBookPolicyPrice--------productId----5555----"+ productId);
					/*result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
					result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
					return  deductValue;

				}
			
				}else{//担保方式为null或者为"无"
					/*result.append("退改政策：订单提交后可随时取消，驴妈妈不收取任何费用。");
					result.append("<br/>担保说明：由于房间资源紧张，您所预订的产品需要担保支付，才能完成预订，实际扣款仍在酒店前台进行。");*/
					logger.info("------getNewDoBookPolicyPrice--------productId----6666----"+ productId);
					return  deductValue;
				}		
		}
		}
	
	return deductValue;
}
	
private long getPriceInfoWireless (TimePrice timePrice,int quantity,Long maxPrice,Long orderPrice){    
	    long price = 0;
		if(StringUtils.isEmpty(timePrice.getDeductType())||SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equals(timePrice.getDeductType())){
			if(orderPrice == null){
				return price;
			}else{
				return orderPrice;	
			}
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equals(timePrice.getDeductType())){
			if(orderPrice == null){
				return price;
			}else{
				return orderPrice;	
			}
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equals(timePrice.getDeductType())){
			return timePrice.getPrice()* quantity;
			
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equals(timePrice.getDeductType())){
			return timePrice.getDeductValue();
			
		}else if(SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equals(timePrice.getDeductType())){
			return timePrice.getPrice()* quantity;
			
		}else if("PEAK".equals(timePrice.getDeductType())){
			if(maxPrice == null){
				return price;
			}else{
				return maxPrice;	
			}
		}else{
			if(orderPrice == null){
				return price;
			}else{
				return orderPrice;	
			}
		}
	}

	
	

	
}
	

