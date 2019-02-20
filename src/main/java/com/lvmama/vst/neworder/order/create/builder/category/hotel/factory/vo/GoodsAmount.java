package com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo;

import java.util.Date;

/**
 * Created by dengcheng on 17/3/6.
 */
public class GoodsAmount {
    String categoryId;
    Long goodsId;
    Long sellAmount;
    Long marketAmount;

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    Long quantity;
    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    Date checkInDate;

    public Long getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(Long sellAmount) {
        this.sellAmount = sellAmount;
    }

    public Long getMarketAmount() {
        return marketAmount;
    }

    public void setMarketAmount(Long marketAmount) {
        this.marketAmount = marketAmount;
    }


    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getTotalAmount(){
        return this.sellAmount*this.quantity;
    }
}
