/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.goods.po.SuppGoodsExp;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant.ACTIVITY_TYPE;
import com.lvmama.vst.comm.vo.Constant.VST_CATEGORY;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.order.client.ord.service.impl.OrderWorkflowServiceImpl;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.service.book.OrderPromotionBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * @author yanliping
 *
 */
@Component
public class PromotionBussiness {
	
	private static Logger logger = LoggerFactory.getLogger(PromotionBussiness.class);

	@Autowired
	private PromotionService promotionService;
	
	@Autowired
	private OrderOrderFactory orderOrderFactory;
	

	public List<PromPromotion> makeProductPromotion(OrdOrderDTO order, Object obj) {
		
		logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "makeProductPromotion start->");
		
		BuyInfoPromotion.Item promItem = new BuyInfoPromotion.Item();
		OrderPromotionBussiness bussiness=null;
		OrdOrderPack orderPack = (OrdOrderPack) obj;
		
		if(orderPack.getVisitTime()==null){
			orderPack.setVisitTime(DateUtil.toSimpleDate(order.getBuyInfo().getVisitTime()));
		}
		promItem.setCategoryId(orderPack.getCategoryId());
		promItem.setGoodsId(orderPack.getProductId());
		promItem.setVisitTime(orderPack.getVisitTime());
		promItem.setObjectType(Constants.PROM_PRODUCT);
		//下单渠道ID
		promItem.setDistributorId(order.getDistributorId());
		promItem.setPriceType(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name());
		Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
		//分销渠道下单分销商ID
		promItem.setDistributionChannelId(channelId);
		bussiness = orderOrderFactory.createInitPromition(Constants.PROM_PRODUCT+"_");
		List<PromPromotion> list = promotionService.getPromotionListByItemAndUser(promItem, order.getBuyInfo().getUserNo());	//获取promotion list,再根据登陆用户信息，过滤无效的promotion
		logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "PromotionList size is = " + list.size());
		
		int flag = 0;
		List<PromPromotion> result=new ArrayList<PromPromotion>();
		for(PromPromotion pp:list){
			logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "PromPromotion#- " + (null == pp.getPromPromotionId() ? "不存在PromPromotionID" : pp.getPromPromotionId()) );
			try{
				flag++;
				IPromFavorable pf = bussiness.fillFavorableData(obj, pp);
				logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$ --- pf.hasApplyAble() start>>>>>");
				if(pf.hasApplyAble()){
					pp.setDiscountAmount(pf.getDiscountAmount());
					logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "pp.getDiscountAmount()- " + pp.getDiscountAmount());
					pp.setKey(Constants.PROM_PRODUCT+"_"+orderPack.getProductId()+"_"+promItem.getObjectType()+"_"+pp.getDiscountAmount()+"_"+flag);
					// 大于0的才存
					if(pp.getDiscountAmount()>0){
						logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "checkOrderChannel(order) = " + checkOrderChannel(order));
						if(checkOrderChannel(order)&& pp.getPromAmount()!=null){
							long usedAmount = pp.getUsedAmount()==null?0L:pp.getUsedAmount();
							long balance = pp.getPromAmount()-usedAmount;
							//活动可用余额大于等于促销金额才存
							if(balance>=pp.getDiscountAmount()){
								logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "balance = " + balance + ", pp.getDiscountAmount() = " + pp.getDiscountAmount());
								result.add(pp);
							}
						}else{
							logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "直接添加pp");
							result.add(pp);
						}
					}
					
				}
			}catch(Exception ex){
				logger.error(ExceptionUtil.getExceptionDetails(ex));
			}
		}
		result = separateExclusivePromotionList(result);
		
		logger.info("$$" + order.getBuyInfo().getUserNo()  + "$$" + "makeProductPromotion end->");
		
		return result;
	}
	
	
	public List<PromPromotion> makeSuppGoodsPromotion(OrdOrderDTO order,
			OrdOrderItem orderItem,String priceType) {
		logger.info("....makeSuppGoodsPromotion is start...");
		BuyInfoPromotion.Item promItem = new BuyInfoPromotion.Item();
		promItem.setCategoryId(orderItem.getCategoryId());

		BizCategory category = orderItem.getSuppGoods().getProdProduct().getBizCategory();
		//根据品类属性来判断改产品是绑定到商品一级还是产品一级
		String promTarget = category.getPromTarget();

		if(Constants.PROM_GOODS.equals(promTarget)||PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
			promItem.setGoodsId(orderItem.getSuppGoodsId());
			promItem.setObjectType(Constants.PROM_GOODS);
		}
		if(Constants.PROM_PRODUCT.equals(promTarget)&& !PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
			promItem.setGoodsId(orderItem.getProductId());
			promItem.setObjectType(Constants.PROM_PRODUCT);
		}
		//期票取可用时间段开始时间
		if(isAperiodic(orderItem)){
			SuppGoodsExp exp = orderItem.getSuppGoods().getSuppGoodsExp();
			if(exp!=null){
				promItem.setVisitTime(exp.getStartTime());
			}else{
				logger.info(orderItem.getSuppGoods().getSuppGoodsId()+" SuppGoodsExp is null");
			}
		}else{
			promItem.setVisitTime(orderItem.getVisitTime());
		}
		
		logger.info("....makeSuppGoodsPromotion is hotel...");
		if(BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
			for(OrdOrderHotelTimeRate rate:orderItem.getOrderHotelTimeRateList()){
			BuyInfoPromotion.ItemPrice itemPrice = new BuyInfoPromotion.ItemPrice(rate.getVisitTime());
			itemPrice.setPrice(rate.getPrice());
			itemPrice.setSettlementPrice(rate.getSettlementPrice());
			itemPriceMap.put(rate.getVisitTime(), itemPrice);
			}
			List<Date> dates = new ArrayList<Date>(itemPriceMap.keySet());
			Collections.sort(dates);
			promItem.setLeaveTime(DateUtils.addDays(dates.get(dates.size()-1), 1));
		}
		promItem.setPriceType(priceType);
		List<PromPromotion> list = new ArrayList<PromPromotion>();
		if(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name().equals(priceType)){
			promItem.setDistributorId(order.getDistributorId());
			//分销渠道下单ID
			Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
			promItem.setDistributionChannelId(channelId);
		}
		logger.info("....makeSuppGoodsPromotion》》 getPromotionListByItemAndUser...");
		list = promotionService.getPromotionListByItemAndUser(promItem, order.getBuyInfo().getUserNo());		//获取promotion list,再根据登陆用户信息，过滤无效的promotion
		logger.info("....makeSuppGoodsPromotion》》 getPromotionListByItemAndUser and list size is"+list.size());
		OrderPromotionBussiness bussiness = orderOrderFactory.createInitPromition(Constants.PROM_GOODS+"_");
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		int flag = 0;
		for(PromPromotion pp:list){
			try{
				flag++;
				logger.info("....makeSuppGoodsPromotion》》 start fillFavorableData...");
				IPromFavorable pf = bussiness.fillFavorableData(orderItem, pp);
				logger.info("....makeSuppGoodsPromotion》》 end fillFavorableData...");
				if(pf.hasApplyAble()){
					logger.info("....makeSuppGoodsPromotion》》 is hasApplyAble...");
					pp.setDiscountAmount(pf.getDiscountAmount());
					pp.setKey(Constants.PROM_GOODS+"_"+promItem.getGoodsId()+"_"+promItem.getObjectType()+"_"+pp.getDiscountAmount()+"_"+flag);
					//拆分的优惠明细
					pp.setFavorableAmount(pf.countFavorableAmount());
					// 大于0的才存
					if(pp.getDiscountAmount()>0){
						logger.info("....makeSuppGoodsPromotion》》 is >0....");
						if(checkOrderChannel(order)&& pp.getPromAmount()!=null&&!PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
							long usedAmount = pp.getUsedAmount()==null?0L:pp.getUsedAmount();
							long balance = pp.getPromAmount()-usedAmount;							
							//活动可用余额大于等于促销金额才存
							if(balance>=pp.getDiscountAmount()){
								logger.info("....makeSuppGoodsPromotion》》 balance > discountAmount....");
								result.add(pp);
							}
							logger.info("....makeSuppGoodsPromotion》》balance is:"+balance+",discountAmount is:"+pp.getDiscountAmount());
						}else{
							logger.info("....makeSuppGoodsPromotion》》 add pp");
							result.add(pp);
						}
					}
					
					
				}
			}catch(Exception ex){
				logger.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
		logger.info("....makeSuppGoodsPromotion》》 start separateExclusivePromotionList...and result is "+GsonUtils.toJson(result));
		result = separateExclusivePromotionList(result);
		logger.info("....makeSuppGoodsPromotion》》 end separateExclusivePromotionList...and result is "+GsonUtils.toJson(result));
		return result;
	}
	
	/**
	 * 验证下单渠道是否为前台和手机端
	 * @param order
	 * @return
	 */
	public boolean checkOrderChannel(OrdOrder order){
		Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
		Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
		boolean check=false;
		if(distributorId==3){
			check=true;
		}
		if(distributorId==4){
			if(channelId==10000||channelId==10001||channelId==10002||channelId==107||channelId==108||channelId==110||channelId==1339||channelId==1340||channelId==972){
				check=true;
			}
		}
		return check;
	}
	
	/**
	 * 验证是否期票
	 * @param orderItem
	 * @return
	 */
	private boolean isAperiodic(OrdOrderItem orderItem){
		BizCategory category = orderItem.getSuppGoods().getProdProduct().getBizCategory();
		if(VST_CATEGORY.CATEGORY_SINGLE_TICKET.getCode().equalsIgnoreCase(category.getCategoryCode())){
			String aperiFlag = orderItem.getSuppGoods().getAperiodicFlag();
			if(StringUtil.isNotEmptyString(aperiFlag)&&"Y".equalsIgnoreCase(aperiFlag)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 排他处理
	 * 促销额度最高的排他促销与其他非排他促销总额比较，取促销额度最高的
	 * @param source
	 * @param nonExclusiveDest
	 * @param exclusiveDest
	 */
	public List<PromPromotion> separateExclusivePromotionList(
			List<PromPromotion> promotionList) {
		PromPromotion exclusiveProm = null;
		List<PromPromotion> noExclusiveProm = new ArrayList<PromPromotion>();
		Map<String, PromPromotion> noExclusiveMap = new HashMap<String, PromPromotion>();
		// 渠道促销不做排他处理，由用户选择
		List<PromPromotion> channelPromotionList = new ArrayList<PromPromotion>();
		long noExclusiveAmount = 0L;

		if (promotionList != null) {
			for (PromPromotion promotion : promotionList) {

				if (ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(
						promotion.getPromitionType())) {
					channelPromotionList.add(promotion);
				} else {
					if (promotion.isExclusivePromotion() != null
							&& promotion.isExclusivePromotion().booleanValue()) {
						if (exclusiveProm == null) {
							exclusiveProm = promotion;
						} else {
							if (exclusiveProm.getDiscountAmount() < promotion
									.getDiscountAmount()) {
								exclusiveProm = promotion;
							}
						}
					} else {
						PromPromotion oldProm = noExclusiveMap.get(promotion
								.getPromitionType());
						if (oldProm == null
								|| oldProm.getDiscountAmount() < promotion
										.getDiscountAmount()) {
							noExclusiveMap.put(promotion.getPromitionType(),
									promotion);
						}
					}
				}
			}

			Iterator it = noExclusiveMap.keySet().iterator();
			while (it.hasNext()) {
				String typeKey = it.next().toString();
				PromPromotion currprom = noExclusiveMap.get(typeKey);
				noExclusiveAmount += currprom.getDiscountAmount();
				noExclusiveProm.add(currprom);
			}

			if (exclusiveProm == null) {
				noExclusiveProm.addAll(channelPromotionList);
				return noExclusiveProm;
			} else {

				if (exclusiveProm.getDiscountAmount() >= noExclusiveAmount) {
					List<PromPromotion> excl = new ArrayList<PromPromotion>();
					excl.add(exclusiveProm);
					// excl.addAll(channelPromotionList);
					return excl;
				} else {
					noExclusiveProm.addAll(channelPromotionList);
					return noExclusiveProm;
				}
			}
		}
		return null;
	}
	public List<PromPromotion> makeSuppGoodsPromotion(OrdOrderDTO order,
			OrdOrderItem orderItem,String priceType,Long userNo) {
		logger.info("....makeSuppGoodsPromotion is start...");
		BuyInfoPromotion.Item promItem = new BuyInfoPromotion.Item();
		promItem.setCategoryId(orderItem.getCategoryId());

		BizCategory category = orderItem.getSuppGoods().getProdProduct().getBizCategory();
		//根据品类属性来判断改产品是绑定到商品一级还是产品一级
		String promTarget = category.getPromTarget();

		if(Constants.PROM_GOODS.equals(promTarget)||PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
			promItem.setGoodsId(orderItem.getSuppGoodsId());
			promItem.setObjectType(Constants.PROM_GOODS);
		}
		if(Constants.PROM_PRODUCT.equals(promTarget)&& !PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
			promItem.setGoodsId(orderItem.getProductId());
			promItem.setObjectType(Constants.PROM_PRODUCT);
		}
		//期票取可用时间段开始时间
		if(isAperiodic(orderItem)){
			SuppGoodsExp exp = orderItem.getSuppGoods().getSuppGoodsExp();
			if(exp!=null){
				promItem.setVisitTime(exp.getStartTime());
			}else{
				logger.info(orderItem.getSuppGoods().getSuppGoodsId()+" SuppGoodsExp is null");
			}
		}else{
			promItem.setVisitTime(orderItem.getVisitTime());
		}
		
		logger.info("....makeSuppGoodsPromotion is hotel...");
		if(BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			Map<Date,ItemPrice> itemPriceMap=new HashMap<Date, BuyInfoPromotion.ItemPrice>();
			for(OrdOrderHotelTimeRate rate:orderItem.getOrderHotelTimeRateList()){
			BuyInfoPromotion.ItemPrice itemPrice = new BuyInfoPromotion.ItemPrice(rate.getVisitTime());
			itemPrice.setPrice(rate.getPrice());
			itemPrice.setSettlementPrice(rate.getSettlementPrice());
			itemPriceMap.put(rate.getVisitTime(), itemPrice);
			}
			List<Date> dates = new ArrayList<Date>(itemPriceMap.keySet());
			Collections.sort(dates);
			promItem.setLeaveTime(DateUtils.addDays(dates.get(dates.size()-1), 1));
		}
		promItem.setPriceType(priceType);
		List<PromPromotion> list = new ArrayList<PromPromotion>();
		if(PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name().equals(priceType)){
			promItem.setDistributorId(order.getDistributorId());
			//分销渠道下单ID
			Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
			promItem.setDistributionChannelId(channelId);
		}
		logger.info("....makeSuppGoodsPromotion》》 getPromotionListByItemAndUser...");
		list = promotionService.getPromotionListByItemAndUser(promItem, userNo);		//获取promotion list,再根据登陆用户信息，过滤无效的promotion
		logger.info("....makeSuppGoodsPromotion》》 getPromotionListByItemAndUser and list size is"+list.size());
		OrderPromotionBussiness bussiness = orderOrderFactory.createInitPromition(Constants.PROM_GOODS+"_");
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		int flag = 0;
		for(PromPromotion pp:list){
			try{
				flag++;
				logger.info("....makeSuppGoodsPromotion》》 start fillFavorableData...");
				IPromFavorable pf = bussiness.fillFavorableData(orderItem, pp);
				logger.info("....makeSuppGoodsPromotion》》 end fillFavorableData...");
				if(pf.hasApplyAble()){
					logger.info("....makeSuppGoodsPromotion》》 is hasApplyAble...");
					pp.setDiscountAmount(pf.getDiscountAmount());
					pp.setKey(Constants.PROM_GOODS+"_"+promItem.getGoodsId()+"_"+promItem.getObjectType()+"_"+pp.getDiscountAmount()+"_"+flag);
					//拆分的优惠明细
					pp.setFavorableAmount(pf.countFavorableAmount());
					// 大于0的才存
					if(pp.getDiscountAmount()>0){
						logger.info("....makeSuppGoodsPromotion》》 is >0....");
						if(checkOrderChannel(order)&& pp.getPromAmount()!=null&&!PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name().equals(priceType)){
							long usedAmount = pp.getUsedAmount()==null?0L:pp.getUsedAmount();
							long balance = pp.getPromAmount()-usedAmount;							
							//活动可用余额大于等于促销金额才存
							if(balance>=pp.getDiscountAmount()){
								logger.info("....makeSuppGoodsPromotion》》 balance > discountAmount....");
								result.add(pp);
							}
							logger.info("....makeSuppGoodsPromotion》》balance is:"+balance+",discountAmount is:"+pp.getDiscountAmount());
						}else{
							logger.info("....makeSuppGoodsPromotion》》 add pp");
							result.add(pp);
						}
					}
					
					
				}
			}catch(Exception ex){
				logger.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
		logger.info("....makeSuppGoodsPromotion》》 start separateExclusivePromotionList...and result is "+GsonUtils.toJson(result));
		result = separateExclusivePromotionList(result);
		logger.info("....makeSuppGoodsPromotion》》 end separateExclusivePromotionList...and result is "+GsonUtils.toJson(result));
		return result;
	}
}
