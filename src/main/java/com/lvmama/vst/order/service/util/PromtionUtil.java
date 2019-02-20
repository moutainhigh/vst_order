package com.lvmama.vst.order.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.comm.bee.vo.ord.BuyInfo;
import com.lvmama.scenic.api.comm.vo.order.BuyInfo.ItemRelation;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.order.utils.OrderUtils;

/** 
* @ImplementProject vst_order
* @Description: 促销工具类
* @author guobiao
* @date 2018年1月25日 上午10:16:33 
*/
public class PromtionUtil {
	private static final Logger logger = LoggerFactory.getLogger(PromtionUtil.class);	
	/**
	 * 参考ProductPromotionBussiness.calcRouteAmount
	 * 计算促销3.0需要的价格
	 */
	public static  Map<String, Object>  calcRouteAmount(OrdOrderPack pack) {
		Map<String, Object> params =new HashMap<String, Object>();
		logger.info("$$--------$$calcRouteAmount() start ->");
		
		
		long totalPrice=0;
		long adultPrice=0;
		long childPrice=0;
		long noMulPrice=0;
		logger.info("$$--------$$existAdultChild(pack.getProduct()) " + existAdultChild(pack.getProduct()) );
		if( existAdultChild(pack.getProduct()) ) {
			for(OrdOrderItem orderItem : pack.getOrderItemList()){
				

				
				logger.info("$$--------$$orderItem.hasMainBranchAttach() " + orderItem.hasMainBranchAttach() );
				if(orderItem.hasMainBranchAttach()){
					totalPrice += orderItem.getPrice() * orderItem.getQuantity();
					logger.info("$$--------$$orderItem.getPrice()=" + orderItem.getPrice()
					+ "\n, orderItem.getQuantity()=" + orderItem.getQuantity()
					+ "\n, totalPrice=" + totalPrice);
					
					List<OrdMulPriceRate> mulPriceList = orderItem.getOrdMulPriceRateList();
					logger.info("$$--------$$mulPriceList=" + (null == mulPriceList ? "null" : mulPriceList) );
					
					if (null == mulPriceList ) {
						noMulPrice+=orderItem.getPrice()*orderItem.getQuantity();
					} else {
						for(OrdMulPriceRate rate:mulPriceList){
							logger.info("$$--------$$rate.getAmountType()=" + rate.getAmountType());
							if(OrdMulPriceRate.AmountType.PRICE.name().equalsIgnoreCase(rate.getAmountType())){
								logger.info("$$--------$$rate.getPriceType()=" + rate.getPriceType());
								if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
									adultPrice += rate.getPrice();
									logger.info("$$--------$$adultPrice=" + adultPrice);
								}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
									childPrice+=rate.getPrice();
									logger.info("$$--------$$childPrice=" + childPrice);
								}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name().equalsIgnoreCase(rate.getPriceType())){
									//自主打包按销售价进行计算，不区分成人价儿童价
									String package_type = pack.getProduct().getPackageType();
									if( "LVMAMA".equalsIgnoreCase(package_type)){
										adultPrice += rate.getPrice();
										logger.info("自主打包adultPrice="+adultPrice);
									}
								}
							}
						}
					}
				}
			}
			
//			if(pack.hasOwn() && pack.getCategoryId() == 18L&&
//					null != packageType && "COPIES".equals(packageType)  ) {
//				//成人数量
//				params.put("adultQuantity", pack.getContentValueByKey("packageNums_lvmama"));
//				logger.info("$$--------$$param(packageNums_lvmama)=" + pack.getContentValueByKey("packageNums_lvmama"));
//				logger.info("$$--------$$param(adultQuantity)=" + pack.getContentValueByKey("packageNums_lvmama"));
//			} else {
			logger.info("$$--------$$param(adultQuantity)=" + pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name()));
			params.put("adultQuantity", pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name()));
			
//			}
		} else {
			logger.info("$$--------$$existAdultChild(pack.getProduct()) = false" );
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
		
		params.put("noMulPrice", noMulPrice);
		params.put("adultPrice", adultPrice);
		params.put("childQuantity", pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name()));
		params.put("childPrice", childPrice);
		params.put("categoryIsRoute", existAdultChild(pack.getProduct()));
		
		logger.info("$$--------$$calcRouteAmount end params:"+params);
		return params;
	}
	
	public static  Map<String, Object>  calcItemAmount(OrdOrderItem orderItem) {
		Map<String, Object> params =new HashMap<String, Object>();
		logger.info("calcItemAmount start");
		long totalPrice=0;
		long adultPrice=0;
		long childPrice=0;
		long noMulPrice=0;
		logger.info("$$--------$$orderItem.hasMainBranchAttach() " + orderItem.hasMainBranchAttach() );
		if(orderItem.hasMainBranchAttach()){
			totalPrice += orderItem.getPrice() * orderItem.getQuantity();
			logger.info("$$--------$$orderItem.getPrice()=" + orderItem.getPrice()
			+ "\n, orderItem.getQuantity()=" + orderItem.getQuantity()
			+ "\n, totalPrice=" + totalPrice);
			
			List<OrdMulPriceRate> mulPriceList = orderItem.getOrdMulPriceRateList();
			logger.info("$$--------$$mulPriceList=" + (null == mulPriceList ? "null" : mulPriceList) );
			
			if (null == mulPriceList ) {
				noMulPrice+=orderItem.getPrice()*orderItem.getQuantity();
			} else {
				for(OrdMulPriceRate rate:mulPriceList){
					logger.info("$$--------$$rate.getAmountType()=" + rate.getAmountType());
					if(OrdMulPriceRate.AmountType.PRICE.name().equalsIgnoreCase(rate.getAmountType())){
						logger.info("$$--------$$rate.getPriceType()=" + rate.getPriceType());
						if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
							adultPrice += rate.getPrice();
							logger.info("$$--------$$adultPrice=" + adultPrice);
						}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
							childPrice+=rate.getPrice();
							logger.info("$$--------$$childPrice=" + childPrice);
						}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name().equalsIgnoreCase(rate.getPriceType())){
							//自由行自主打包按销售价进行计算，不区分成人价儿童价
//									String package_type = pack.getProduct().getPackageType();
//									logger.info("自由行自主打包按销售价进行计算: "+package_type);
//									if(pack.getCategoryId() == 18L && package_type!=null && "LVMAMA".equalsIgnoreCase(package_type)){
//										adultPrice += rate.getPrice();
//										logger.info("自由行自主打包adultPrice="+adultPrice);
//									}
						}
					}
				}
			}
		}
		
		//params.put("adultQuantity", orderItem.getAdultQuantity());酒店套餐这里没值
		//params.put("childQuantity",  orderItem.getChildQuantity());		
		params.put("adultQuantity", orderItem.getItem().getAdultQuantity());
		params.put("childQuantity", orderItem.getItem().getChildQuantity());
		params.put("noMulPrice", noMulPrice);
		params.put("adultPrice", adultPrice);
		params.put("childPrice", childPrice);
		logger.info("$$--------$$calcItemAmount end params:"+params);
		return params;
	}
	
	/**
	 * 是否区分成人儿童
	 * @param product
	 * @return
	 */
	public  static boolean existAdultChild(ProdProduct product){
		BizCategory bc = product.getBizCategory();
		if(bc!=null){
			return ("route".equalsIgnoreCase(bc.getProcessKey())&&!"category_route_hotelcomb".equals(bc.getCategoryCode()));
		}
		return false;
	}
	
	
	/**
	 * 是否参与促销
	 * @return
	 */
	public  static boolean  validPromtionItem(OrdOrderItem item){
		long  categoryId=item.getCategoryId();
		if(3l==categoryId||90l==categoryId){//3保险,90运费
			return false;
		}
		if(item.getItem().getRouteRelation()!=null&&"ADDITION".equals(item.getItem().getRouteRelation().toString())){//附加
			return false;
		}
		return true;
	}
	
}
