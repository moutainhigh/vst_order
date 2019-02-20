package com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo;

/**
 * Created by dengcheng on 17/3/6.
 */
public class ExpressAmount {
    String categoryId;
    String goodsId;
    Long amount;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
