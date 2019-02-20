/**
 * 
 */
package com.lvmama.vst.order.service.book.impl;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.prod.service.ProdProductSaleReClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductSaleRe;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.back.prom.rule.PromFavorableFactory;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderPromotionBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * @author lancey
 *
 */
@Component("productPromotionBussiness")
public class ProductPromotionBussiness extends AbstractBookService implements OrderPromotionBussiness,InitializingBean{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ProductPromotionBussiness.class);

	@Autowired
	private PromotionService promotionService;
	
	//@Autowired
	private PromFavorableFactory promFavorableFactory;
	
	@Autowired
	private ProdProductSaleReClientService prodProductSaleReClientService;
	
	@Override
	public List<OrdPromotion> initPromotion(OrdOrderDTO order, String key,
			List<Long> promotionIds) {
		String[] array = StringUtils.split(key,"_");
		//去除促销map中的key值的校验
		/*if(array.length!=5){
			throwIllegalException("促销信息错误");
		}*/
		Long objectId=NumberUtils.toLong(array[1]);
		List<OrdPromotion> promotionList = new ArrayList<OrdPromotion>();
		for(Long promId:promotionIds){
			OrdOrderPackDTO orderPack = (OrdOrderPackDTO)order.getOrderPackByProductId(objectId);
			if(orderPack==null){
				throwNullException("不存在产品不可以使用促销");
			}
			if(orderPack.getVisitTime()==null){
				orderPack.setVisitTime(DateUtil.toSimpleDate(order.getBuyInfo().getVisitTime()));
		}
			logger.info("promotionService.getPromPromotionById params promId="+promId+",objectId+"+objectId+",array="+array[2]);
			PromPromotion promotion = promotionService.getPromPromotionById(promId, objectId, array[2], order.getUserNo());
			if(promotion==null){
				logger.info("promotion is null promid="+promId);
				continue;
			}
			
			//添加促销支持数据
			if(order.getBuyInfo().getCategoryId()!=null&&order.getBuyInfo().getCategoryId() == 18L){
				initParams(order,orderPack);
			}
			
			IPromFavorable ipf = fillFavorableData(orderPack, promotion);
			
			OrdPromotion op = new OrdPromotion();
			//检查促销可用余额是否满足
			if(checkOrderChannel(order)&& promotion.getPromAmount()!=null&&"DISTRIBUTOR_TYPE".equals(promotion.getPriceType())){
				long usedAmount = promotion.getUsedAmount()==null?0L:promotion.getUsedAmount();
				long balance =promotion.getPromAmount()-usedAmount;
				//活动可用余额大于等于促销金额才存
				if(balance<ipf.getDiscountAmount()){
					continue;
				}
				//占用促销额度标记
				op.setOccupyAmountFlag("Y");	
			}
			
			
			op.setCode(promotion.getCode());
			op.setPromPromotionId(promotion.getPromPromotionId());
			op.setPriceType(promotion.getPriceType());
			op.setPromTitle(promotion.getTitle());
			op.setObjectType(OrdPromotion.ObjectType.ORDER_PACK.name());
			op.setTarget(orderPack);
			op.setPromFavorable(ipf);
			op.setPromotion(promotion);
			promotionList.add(op);
		}
		return promotionList;
	}
	
	public void initParams(OrdOrderDTO order, OrdOrderPackDTO orderPack){
		String saleType = ProdProductSaleRe.SALETYPE.COPIES.name();

		ResultHandleT<List<ProdProductSaleRe>> resultHandleT = prodProductSaleReClientService.queryByProductId(order.getBuyInfo().getProductId());
		if(resultHandleT != null && resultHandleT.isSuccess()){
			List<ProdProductSaleRe> prodProductSaleRes = resultHandleT.getReturnContent();
			if(!CollectionUtils.isEmpty(prodProductSaleRes)){
				saleType = prodProductSaleRes.get(0).getSaleType();
			}
		}
		orderPack.putContent("saleType", saleType);

		String quantity = "0";
		if(saleType.equals(ProdProductSaleRe.SALETYPE.COPIES.name())){
			quantity = String.valueOf(order.getBuyInfo().getQuantity());
			if(quantity==null||quantity.equals("0"))
				quantity = String.valueOf(order.getBuyInfo().getProductList().get(0).getQuantity());
			Integer adultQuantity = order.getBuyInfo().getAdultQuantity();
			if(quantity==null||quantity.equals("0")){
				quantity = String.valueOf(adultQuantity);
			}else{
				Integer q = Integer.valueOf(quantity);
				if(q>adultQuantity){
					quantity = String.valueOf(adultQuantity);
				}
			}
		} else if(saleType.equals(ProdProductSaleRe.SALETYPE.PEOPLE.name())){
			quantity = String.valueOf(order.getBuyInfo().getAdultQuantity());
		}
		
		Long actualAmt = order.getOughtAmount();
		//		for(OrdOrderItem orderItem:order.getOrderItemList()){
		//			//去除保险费
		//			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
		//				Long insuranceQuantity = 1L;
		//				if(null != orderItem.getQuantity()){
		//					insuranceQuantity = orderItem.getQuantity();
		//				}
		//				logger.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
		//				actualAmt = actualAmt-orderItem.getPrice()*insuranceQuantity;
		//			}
		//			//去除快递押金的费用
		//			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
		//				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
		//					Long depositQuantity = 1L;
		//					if(null != orderItem.getQuantity()){
		//						depositQuantity = orderItem.getQuantity();
		//					}
		//					logger.info("扣除押金费 :"+orderItem.getPrice());
		//					actualAmt = actualAmt  - orderItem.getPrice()*depositQuantity;
		//				}else{
		//					logger.info("扣除快递费 :"+orderItem.getPrice());
		//					actualAmt = actualAmt - orderItem.getPrice();
		//				}
		//			}
		//		}
		logger.info("---------------------order.getActualAmount()=" + actualAmt);
		orderPack.putContent("actualAmt", actualAmt);
		//设置购买份数
		orderPack.putContent("quantity", quantity);
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

	public IPromFavorable fillFavorableData(Object obj, PromPromotion promotion) {
		
		logger.info("$$--------$$" + "fillFavorableData() start ->");
		
		if(promotion==null){
			if (logger.isDebugEnabled()) {
				logger.debug("fillFavorableData(Object, PromPromotion) - promotion==null"); //$NON-NLS-1$
			}
			throwNullException("促销不存在");
		}
		if(obj==null){
			if (logger.isDebugEnabled()) {
				logger.debug("fillFavorableData(Object, PromPromotion) - obj==null"); //$NON-NLS-1$
			}
			throwNullException("促销数据对象不存在");
		}
		promotion.setPromResult(promotionService.getPromResultByPromotionId(promotion.getPromPromotionId()));
		IPromFavorable ipf = promFavorableFactory.createFavorable(promotion); //计算优惠金额
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderDate", new Date());
			
		OrdOrderPackDTO orderPack = (OrdOrderPackDTO) obj;
		if(!checkPromValid(promotion,orderPack.getVisitTime())){
			if (logger.isDebugEnabled()) {
				logger.debug("fillFavorableData(Object, PromPromotion) - checkPromValid==false"); //$NON-NLS-1$
			}
			throwIllegalException("促销不满足使用");
		}
		
		params.put("visitDate", orderPack.getVisitTime());
		
		params.put("quantity", orderPack.getContentValueByKey("quantity"));
		params.put("actualAmt", orderPack.getContentValueByKey("actualAmt"));
		
		params.put("saleType", orderPack.getContentValueByKey("saleType"));
		params.put("categoryId",orderPack.getCategoryId());
		params.put("subCategoryId",orderPack.getSubCategoryId());
		params.put("ownPack",orderPack.getOwnPack());
		calcRouteAmount(orderPack, params, promotion);
		params.put("categoryIsRoute", existAdultChild(orderPack.getProduct()));
		ipf.setData(params);
		
		logger.info("$$------------$$" + "fillFavorableData() end ->" + JSON.toJSONString(params));
		return ipf;
	}
	
	
	public IPromFavorable fillCruiseFavorableData(List<OrdOrderItem> itemList,
			PromPromotion promotion) {
		if(promotion==null){
			if (logger.isDebugEnabled()) {
				logger.debug("fillFavorableData(Object, PromPromotion) - promotion==null"); //$NON-NLS-1$
			}
			throwNullException("促销不存在");
		}
		
		IPromFavorable ipf = promFavorableFactory.createFavorable(promotion);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderDate", new Date());
		
		return null;
	}
	
	/**
	 * 是否区分成人儿童
	 * @param product
	 * @return
	 */
	private boolean existAdultChild(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return ("route".equalsIgnoreCase(bc.getProcessKey())&&!"category_route_hotelcomb".equals(bc.getCategoryCode()));
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
	
	private boolean hasCruise(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return "cruise".equalsIgnoreCase(bc.getProcessKey());
		}
		return false;
	}
	
	private void calcRouteAmount(OrdOrderPackDTO pack, Map<String,Object> params, PromPromotion promPromotion) {
		
		logger.info("$$--------$$" + "calcRouteAmount() start ->");
		
		String packageType = pack.getContentStringByKey("package_lvmama_saleType");
		
		long totalPrice=0;
		long adultPrice=0;
		long childPrice=0;
		long noMulPrice=0;
		logger.info("$$--------$$" + "existAdultChild(pack.getProduct()) " + existAdultChild(pack.getProduct()) );
		if( existAdultChild(pack.getProduct()) ) {
			for(OrdOrderItem orderItem : pack.getOrderItemList()){
				
				boolean flag = false;
				// fix bug
				if(pack.hasOwn() && (pack.getCategoryId() == 18L||pack.getCategoryId()==15L) && 
						promPromotion.getPromitionType().equals(Constant.ACTIVITY_TYPE.IMMEDIATELY_FAVORABLE.getCode()) ) {
					flag = true;
					logger.info("$$--------$$ set immediate_favorable's multiPrice to null");
				}
				
				logger.info("$$--------$$" + "orderItem.hasMainBranchAttach() " + orderItem.hasMainBranchAttach() );
				if(orderItem.hasMainBranchAttach()){
					totalPrice += orderItem.getPrice() * orderItem.getQuantity();
					logger.info("$$--------$$" + "orderItem.getPrice()=" + orderItem.getPrice()
					+ "\n, orderItem.getQuantity()=" + orderItem.getQuantity()
					+ "\n, totalPrice=" + totalPrice);
					
					List<OrdMulPriceRate> mulPriceList = orderItem.getOrdMulPriceRateList();
					logger.info("$$--------$$" + "mulPriceList=" + (null == mulPriceList ? "null" : mulPriceList) );
					
					if (null == mulPriceList || flag) {
						logger.info("$$--------$$" + "mulPriceList is null");
						noMulPrice+=orderItem.getPrice()*orderItem.getQuantity();
					} else {
						logger.info("$$--------$$" + "mulPriceList is not null");
						for(OrdMulPriceRate rate:mulPriceList){
							logger.info("$$--------$$" + "rate.getAmountType()=" + rate.getAmountType());
							if(OrdMulPriceRate.AmountType.PRICE.name().equalsIgnoreCase(rate.getAmountType())){
								logger.info("$$--------$$" + "rate.getPriceType()=" + rate.getPriceType());
								if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
									adultPrice += rate.getPrice();
									logger.info("$$--------$$" + "adultPrice=" + adultPrice);
								}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
									childPrice+=rate.getPrice();
									logger.info("$$--------$$" + "childPrice=" + childPrice);
								}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name().equalsIgnoreCase(rate.getPriceType())){
									//自由行自主打包按销售价进行计算，不区分成人价儿童价
									String package_type = pack.getProduct().getPackageType();
									logger.info("自由行自主打包按销售价进行计算: "+package_type);
									if(pack.getCategoryId() == 18L && package_type!=null && "LVMAMA".equalsIgnoreCase(package_type)){
										adultPrice += rate.getPrice();
										logger.info("自由行自主打包adultPrice="+adultPrice);
									}
								}
							}
						}
					}
				}
			}
			
			if(pack.hasOwn() && pack.getCategoryId() == 18L&&
					null != packageType && "COPIES".equals(packageType) &&
					promPromotion.getPromitionType().equals(Constant.ACTIVITY_TYPE.more_order_more_favorable.getCode()) ) {
				//成人数量
				params.put("adultQuantity", pack.getContentValueByKey("packageNums_lvmama"));
				logger.info("$$--------$$" + "param(packageNums_lvmama)=" + pack.getContentValueByKey("packageNums_lvmama"));
				logger.info("$$--------$$" + "param(adultQuantity)=" + pack.getContentValueByKey("packageNums_lvmama"));
			} else {
				params.put("adultQuantity", pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name()));
				logger.info("$$--------$$" + "param(adultQuantity)=" + pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name()));
			}
		} else {
			logger.info("$$--------$$" + "existAdultChild(pack.getProduct()) = false" );
			if(OrderUtils.hasTicketPackCategory(pack.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))||
					OrderUtils.hasCategoryRouteHotelcomb(pack.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
				long quantity = NumberUtils.toLong(pack.getContentValueByKey(OrderEnum.ORDER_TICKET_TYPE.quantity.name()).toString());
				params.put("adultQuantity", quantity);
				
				for(OrdOrderItem orderItem:pack.getOrderItemList()){
					//次规格产品不参与促销
					if("Y".equals(orderItem.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())){
						totalPrice+=orderItem.getPrice()*orderItem.getQuantity();
					}
				}
				adultPrice = totalPrice/quantity;
			}
			
		}
		
		//params.put("amount", totalPrice);
		params.put("noMulPrice", noMulPrice);
		params.put("adultPrice", adultPrice);
		logger.info("$$--------$$" + "param(adultPrice)=" + adultPrice);
		params.put("childQuantity", pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name()));
		
		logger.info("$$--------$$" + "param(childQuantity)=" + pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name()));
		params.put("childPrice", childPrice);
		logger.info("$$--------$$" + "param(childPrice)=" + childPrice);
		params.put("categoryIsRoute", existAdultChild(pack.getProduct()));
		
		logger.info("$$--------$$" + "calcRouteAmount() end ->");
	}
	
	
	
	
	
	

	private boolean checkPromValid(PromPromotion prom,Date visitTime){
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

	@Override
	public void afterPropertiesSet() throws Exception {
		promFavorableFactory = new PromFavorableFactory();
	}
}
