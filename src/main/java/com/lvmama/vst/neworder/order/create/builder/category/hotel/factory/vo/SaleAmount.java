package com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo;

/**
 * Created by dengcheng on 17/3/6.
 */
public class SaleAmount {
    String type;
    Long goodsId;

    Long categoryId;
    Long amount;


    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
