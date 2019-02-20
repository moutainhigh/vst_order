/**
 * 
 */
package com.lvmama.vst.order.service.book.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_COMMON_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.back.prom.rule.PromFavorableFactory;
import com.lvmama.vst.back.prom.vo.LinerRoomInfo;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderPromotionBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * @author lancey
 *
 */
@Component("suppGoodsPromotionBussiness")
public class SuppGoodsPromotionBussiness extends AbstractBookService implements OrderPromotionBussiness,InitializingBean{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(SuppGoodsPromotionBussiness.class);

	@Autowired
	private PromotionService promotionService;
	
	//@Autowired
	PromFavorableFactory promFavorableFactory;
	
	@Override
	public List<OrdPromotion> initPromotion(OrdOrderDTO order, String key,
			List<Long> promotionIds) {
		String[] array = StringUtils.split(key,"_");
//		if(array.length<6){
//			throwIllegalException("促销信息错误");
//		}
		Long objectId=NumberUtils.toLong(array[1]);
		List<OrdPromotion> list = new ArrayList<OrdPromotion>();
		for(Long promId:promotionIds){
			OrdOrderItem orderItem=null;
			if(Constants.PROM_PRODUCT.equals(array[2])){
				 orderItem = order.getOrderItemByProductId(objectId);
			}
			if(Constants.PROM_GOODS.equals(array[2])){
				orderItem = order.getOrderItemBySuppGoodsId(objectId);
			}
			if(orderItem==null){
				throwNullException("不存在产品不可以使用促销");
			}
			
			PromPromotion promotion = promotionService.getPromPromotionById(promId, objectId, array[2],order.getUserNo());
			if(promotion==null){
				logger.info("promotion is null promid="+promId);
				continue;
			}
			IPromFavorable ipf = fillFavorableData(orderItem, promotion);
			OrdPromotion op = new OrdPromotion();
			//检查促销可用余额是否满足
			if(checkOrderChannel(order)&& promotion.getPromAmount()!=null&&"DISTRIBUTOR_TYPE".equals(promotion.getPriceType())){
				long usedAmount = promotion.getUsedAmount()==null?0L:promotion.getUsedAmount();
				long balance =promotion.getPromAmount()-usedAmount;
				//活动可用余额大于等于促销金额才存
				if(balance<ipf.getDiscountAmount()){
					logger.info("promotion.promAmount insufficient,promid="+promId);
					continue;
				}
				//占用促销额度标记
				op.setOccupyAmountFlag("Y");
			}
			
			op.setCode(promotion.getCode());
			op.setPromPromotionId(promotion.getPromPromotionId());
			op.setPriceType(promotion.getPriceType());
			op.setPromTitle(promotion.getTitle());
			op.setTarget(orderItem);
			op.setObjectType(OrdPromotion.ObjectType.ORDER_ITEM.name());
			op.setPromFavorable(ipf);
			op.setPromotion(promotion);
			list.add(op);
		}
		return list;
	}
	
	/**
	 * 验证下单渠道是否为前台和手机端
	 * @param order
	 * @return
	 */
	public boolean checkOrderChannel(OrdOrderDTO order){
		Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
		Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
		boolean check=false;
		if(distributorId==3){
			check=true;
		}
		if(distributorId==4){
			if(channelId==10000||channelId==10001||channelId==10002||channelId==107||channelId==108||channelId==110){
				check=true;
			}
		}
		return check;
	}
	
	private boolean checkPromValid(PromPromotion prom,Date visitTime){
		logger.info("-------------promHotel------------");
		if(prom.getTimeType()!=null){
			logger.info("-------------promHotel------------"+prom.getTimeType());
			if(prom.getTimeType().equalsIgnoreCase("TIME_AT_HOTEL")){
				return true;
			}
		}
		if(prom.getStartVistTime()!=null){
			if(visitTime.before(prom.getStartVistTime())){
				return false;
			}
		}
		if(prom.getEndVistTime()!=null){
			if(visitTime.after(prom.getEndVistTime())){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 是否区分成人儿童
	 * @param product
	 * @return
	 */
	private boolean existAdultChild(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return ("route".equalsIgnoreCase(bc.getProcessKey())&&!("category_route_hotelcomb".equals(bc.getCategoryCode())||"category_route_new_hotelcomb".equals(bc.getCategoryCode())));
		}
		return false;
	}
	

	private boolean hasRoute(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return "route".equalsIgnoreCase(bc.getProcessKey());
		}
		return false;
	}
	
	private void calcRouteAmount(OrdOrderItem orderItem,Map<String,Object> params,PromPromotion promotion){
		//long totalPrice=0;
		long adultPrice=0;
		long childPrice=0;
		long adultQuantity=0;
		long childQuantity=0;
		long price = 0;
		//针对分销商，取单价，供应商取结算价
		if(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name().equals(promotion.getPriceType())){
			price=orderItem.getPrice();
			adultPrice=orderItem.getPrice();
		}else if(PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(promotion.getPriceType())){
			price=orderItem.getSettlementPrice();
			adultPrice=orderItem.getSettlementPrice();
		}
		//totalPrice=price*orderItem.getQuantity();
		if(existAdultChild(orderItem.getSuppGoods().getProdProduct())){
			adultPrice=0;
			//totalPrice=0;
		List<OrdMulPriceRate> mulPriceList = orderItem.getOrdMulPriceRateList();
		
		for(OrdMulPriceRate rate:mulPriceList){
			//针对分销商，取单价
			if(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name().equals(promotion.getPriceType())){
				if(OrdMulPriceRate.AmountType.PRICE.name().equalsIgnoreCase(rate.getAmountType())){
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
						adultPrice=rate.getPrice();
						adultQuantity=rate.getQuantity();
					}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
						childPrice=rate.getPrice();
						childQuantity=rate.getQuantity();
					}
				}
			}
			//针对供应商，取结算价
			if(PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(promotion.getPriceType())){
				if(OrdMulPriceRate.AmountType.SETTLEMENT.name().equalsIgnoreCase(rate.getAmountType())){
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
						adultPrice=rate.getPrice();
						adultQuantity=rate.getQuantity();
					}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
						childPrice=rate.getPrice();
						childQuantity=rate.getQuantity();
					}/*else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.name().equalsIgnoreCase(rate.getPriceType())){
						adultPrice=rate.getPrice();
						adultQuantity=rate.getQuantity();
					}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.name().equalsIgnoreCase(rate.getPriceType())){
						childPrice=rate.getPrice();
						childQuantity=rate.getQuantity();
					}*/
				}
			}
		}
		//totalPrice=adultPrice*adultQuantity+childPrice*childQuantity;
		}else{
			adultQuantity=orderItem.getQuantity();
		}
		params.put("adultQuantity", adultQuantity);
		params.put("childQuantity", childQuantity);
		//params.put("amount", totalPrice);
		params.put("adultPrice", adultPrice);
		params.put("childPrice", childPrice);
		params.put("categoryIsRoute", existAdultChild(orderItem.getSuppGoods().getProdProduct()));
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		promFavorableFactory = new PromFavorableFactory();
	}
	


	@Override
	public IPromFavorable fillFavorableData(Object obj, PromPromotion promotion) {
		OrdOrderItem orderItem = (OrdOrderItem)obj;
		if(promotion==null){
			if (logger.isDebugEnabled()) {
				logger.debug("fillFavorableData(Object, PromPromotion) - promotion==null"); //$NON-NLS-1$
			}
			throwNullException("促销不存在");
		}
					
		if(!checkPromValid(promotion,orderItem.getVisitTime())){
			if (logger.isInfoEnabled()) {
				logger.info("fillFavorableData(Object, PromPromotion) - checkPromValid==false"); //$NON-NLS-1$
			}
			throwIllegalException("促销不满足使用");
		}
		promotion.setPromResult(promotionService.getPromResultByPromotionId(promotion.getPromPromotionId()));
		IPromFavorable ipf = promFavorableFactory.createFavorable(promotion);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderDate", new Date());
		params.put("visitDate", orderItem.getVisitTime());
		calcRouteAmount(orderItem, params,promotion);
		
		if(null == orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name())){
			logger.info("fillFavorableData(Object, PromPromotion) - orderItem.getContentStringByKey==null");
		}else{
			logger.info("fillFavorableData(Object, PromPromotion)- orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))="+orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()));
		}
		//酒店保持老的结构
		if(BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			logger.info("....fillFavorableData is hotel....");
			BuyInfoPromotion.Item promItem=new BuyInfoPromotion.Item();
			Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
			
			for(OrdOrderHotelTimeRate rate:orderItem.getOrderHotelTimeRateList()){
				BuyInfoPromotion.ItemPrice itemPrice = new BuyInfoPromotion.ItemPrice(rate.getVisitTime());
				itemPrice.setPrice(rate.getPrice());
				itemPrice.setSettlementPrice(rate.getSettlementPrice());
				itemPriceMap.put(rate.getVisitTime(), itemPrice);
			}
			List<Date> dates = new ArrayList<Date>(itemPriceMap.keySet());
			Collections.sort(dates);
			promItem.setGoodsId(orderItem.getSuppGoodsId());
			promItem.setVisitTime(orderItem.getVisitTime());
			promItem.setLeaveTime(DateUtils.addDays(dates.get(dates.size()-1), 1));
			promItem.setCategoryId(orderItem.getCategoryId());
			//promItem.setPayTarget((((OrdOrderItemDTO)orderItem).getOrderDTO()).getPaymentTarget());
			promItem.setQuantity(orderItem.getQuantity());
			promItem.setTotalAmount(orderItem.getPrice()*orderItem.getQuantity());
			promItem.setSettlementAmount(orderItem.getTotalSettlementPrice());
			promItem.setItemPriceMap(itemPriceMap);
			params.put("item", promItem);
			logger.info("....fillFavorableData is hotel and params is....+"+GsonUtils.toJson(params));
		}
		//邮轮
		if(BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
			LinerRoomInfo info = new LinerRoomInfo(); 
			List<OrdMulPriceRate> mulPriceList = orderItem.getOrdMulPriceRateList();
			for(OrdMulPriceRate rate:mulPriceList){
				if(PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(promotion.getPriceType())){
					
					if(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode().equals(rate.getPriceType())){
						 //一二人价
						 info.setFirPeoPrice(rate.getPrice());
				}
				 if(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode().equals(rate.getPriceType())){
						 //三四人成人价
						 info.setSecAduPrice(rate.getPrice());
				 }
				if(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode().equals(rate.getPriceType())){
						 //三四人儿童价
					 info.setSecChiPrice(rate.getPrice());
				 }
					
				}else if(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name().equals(promotion.getPriceType())){
					if(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode().equals(rate.getPriceType())){
							 //一二人价
							 info.setFirPeoPrice(rate.getPrice());
					}
					 if(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode().equals(rate.getPriceType())){
							 //三四人成人价
							 info.setSecAduPrice(rate.getPrice());
					 }
					if(ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode().equals(rate.getPriceType())){
							 //三四人儿童价
						 info.setSecChiPrice(rate.getPrice());
					 }
				}
				 }
				Item item = orderItemDto.getItem();
				info.setAdultQuantity(item.getAdultQuantity());
				info.setChildQuantity(item.getChildQuantity());
				info.setRommQuantity(orderItem.getQuantity().intValue());
				String maxPersonCount = orderItem.getContentStringByKey(ORDER_COMMON_TYPE.maxPersonCount.name());
				if(StringUtils.isNotEmpty(maxPersonCount)){
					info.setRoomType(Integer.parseInt(maxPersonCount));
				}
			params.put("linerRoomInfo", info);
		}
		
		params.put("categoryIsRoute", existAdultChild(orderItem.getSuppGoods().getProdProduct()));
		ipf.setData(params);
		return ipf;
	}
	
	
}
