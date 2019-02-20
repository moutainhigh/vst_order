package com.lvmama.vst.neworder.order.cal.category.hotelcomb.product;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.lvmama.comm.order.Order;
import com.lvmama.dest.annotation.validate.Null;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;

import com.lvmama.dest.api.hotelcomb.vo.CalAmountResponse;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo.GoodsItem;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.prom.service.BuyPresentClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.service.VstPromotionOrderService;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant.ACTIVITY_TYPE;
import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.ICalProduct;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.BonusCalService;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.CouponCalService;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.PromotionForHotelcomService;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.BonusAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.CouponAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ExpressAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.GoodsAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.SaleAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.service.impl.PromBuyPresentBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dengcheng on 17/2/23.
 */
@Component("calProductService")
public class CalProductServiceImpl implements ICalProduct {

	private static final Logger logger = LoggerFactory.getLogger(CalProductServiceImpl.class);

	@Resource(name = "hotelCombOrderService")
	IHotelCombOrderService hotelCombOrderService;


	@Autowired
	private PromotionForHotelcomService  hotelCombPromotion;
	@Autowired
	private  CouponCalService   couponCalService;
	
	@Autowired
	private PromBuyPresentBussiness promBuyPresentBussiness;
	
	@Autowired
    private	BonusCalService  bonusCalService;

	@Override
	public List<SaleAmount> buildSaleAmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo,
			ProductAmountItem ammountItem) {
		logger.info("CalProductServiceImpl  buildSaleAmount  start");
		List<PromPromotion> promotionList = hotelCombPromotion.findPromPromotion(order, buyInfo);
		// 校验
		hotelCombPromotion.checkPromotion(promotionList, buyInfo);
		List<SaleAmount> listSaleAmount = null;
		if (promotionList != null && promotionList.size() > 0) {
			listSaleAmount = new ArrayList<SaleAmount>();
			ammountItem.setPromotionList(promotionList);
			// 计算
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				SaleAmount saleAmount = new SaleAmount();
				saleAmount = calPromotion(promotionList, orderItem);
				listSaleAmount.add(saleAmount);
			}

		}
		return listSaleAmount;
	}

	

	public SaleAmount calPromotion(List<PromPromotion> promotionList,OrdOrderItem orderItem) {
		
	    	 SaleAmount saleAmount = new SaleAmount();
	    	 saleAmount.setCategoryId(orderItem.getCategoryId());
	    	 saleAmount.setGoodsId(orderItem.getSuppGoodsId());
	    	 Long amount = 0L;
	    	 //促销绑定有的以商品绑定，有的以产品绑定(门票是商品绑定)  渠道促销价格计算不参与,需要持久化,具体由支付的时候根据持久化的渠道进行优惠
	    	 for(PromPromotion prom:promotionList){
	    		 if(orderItem.getSuppGoods().getSuppGoodsId().equals(prom.getGoodsId()) &&  (!ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType()))){
	    			 amount+=prom.getDiscountAmount();
	    		 }else if(orderItem.getSuppGoods().getProdProduct().getProductId().equals(prom.getGoodsId()) && (!ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType()))){
	    			 amount+=prom.getDiscountAmount();
	    		 }
	    	 }
	         saleAmount.setAmount(amount);
		
		return saleAmount;

	}

	@Override
	public List<ExpressAmount> buildExpressAmount(OrdOrderDTO order) {
		return null;
	}

	@Override
	public List<GoodsAmount> buildGoodsAmount(OrdOrderDTO order) {

		HotelCombBuyInfoVo buyInfoVo = new HotelCombBuyInfoVo();
		List<HotelCombBuyInfoVo> buyInfoVos = Lists.newArrayList();
		List<GoodsItem> goodsItems = Lists.newArrayList();
		buyInfoVo.setGoodsList(goodsItems);

		List<GoodsAmount> goodsAmounts = Lists.newArrayList();

		for (OrdOrderItem item : order.getOrderItemList()) {
			GoodsAmount ga = new GoodsAmount();
			ga.setSellAmount(item.getPrice());
			ga.setGoodsId(item.getSuppGoodsId());
			ga.setQuantity(item.getQuantity());
			ga.setCategoryId(item.getCategoryId() + "");
			ga.setMarketAmount(item.getMarketPrice());
			goodsAmounts.add(ga);
		}

		return goodsAmounts;
	}

	@Override
	public List<CouponAmount> buildCouponAmount(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo) {
		List<CouponAmount> couponAmountList = Lists.newArrayList();
		CouponAmount couponAmount = couponCalService.getOrderCoupoAmount(order, buyInfo);
		couponAmountList.add(couponAmount);
		return couponAmountList;
	}



	@Override
	public BonusAmount buildBonusAmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		BonusAmount bonusAmount =	bonusCalService.getBonusAmountOfBuyUserNo(order, buyInfo);
		
		return bonusAmount;
	}

	
}




	

