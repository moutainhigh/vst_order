package com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo;

import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;

/**
 * Created by dengcheng on 17/3/6.
 */
public class ProductAmountItem {

	// 单位分
	List<SaleAmount> saleAmountList = new ArrayList<SaleAmount>();

	List<GoodsAmount> goodsAmount = new ArrayList<GoodsAmount>();

	List<ExpressAmount> expressAmountList = new ArrayList<ExpressAmount>();
	List<CouponAmount> couponAmountList = new ArrayList<CouponAmount>();
	
	List<PromPromotion> promotionList = new ArrayList<PromPromotion>();
	
	BonusAmount  bonusAmount ;//奖金部分
	public List<PromPromotion> getPromotionList() {
		return promotionList;
	}

	public void setPromotionList(List<PromPromotion> promotionList) {
		this.promotionList = promotionList;
	}

	//满赠信息
	private BuyPresentActivityInfo buyPresentInfo;

	public BuyPresentActivityInfo getBuyPresentInfo() {
		return buyPresentInfo;
	}

	public void setBuyPresentInfo(BuyPresentActivityInfo buyPresentInfo) {
		this.buyPresentInfo = buyPresentInfo;
	}

	public List<SaleAmount> getSaleAmountList() {
		return saleAmountList;
	}

	public void setSaleAmountList(List<SaleAmount> saleAmountList) {
		this.saleAmountList = saleAmountList;
	}

	public List<GoodsAmount> getGoodsAmount() {
		return goodsAmount;
	}

	public void setGoodsAmount(List<GoodsAmount> goodsAmount) {
		this.goodsAmount = goodsAmount;
	}

	public List<ExpressAmount> getExpressAmountList() {
		return expressAmountList;
	}

	public void setExpressAmountList(List<ExpressAmount> expressAmountList) {
		this.expressAmountList = expressAmountList;
	}

	public Long getTotalAmount() {
		Long totalAmount = null;
		if(null != saleAmountList){
			for (SaleAmount sale : saleAmountList) {
				totalAmount = +sale.amount;
			}
		}
		if(null != goodsAmount){
			for (GoodsAmount goods : goodsAmount) {
				totalAmount = +goods.getTotalAmount();
			}
		}
		if(null != expressAmountList){
			for (ExpressAmount expressAmount : expressAmountList) {
				totalAmount = +expressAmount.getAmount();
			}
		}
		return totalAmount;
	}

	public List<CouponAmount> getCouponAmountList() {
		return couponAmountList;
	}

	public void setCouponAmountList(List<CouponAmount> couponAmountList) {
		this.couponAmountList = couponAmountList;
	}

	public BonusAmount getBonusAmount() {
		return bonusAmount;
	}

	public void setBonusAmount(BonusAmount bonusAmount) {
		this.bonusAmount = bonusAmount;
	}

}