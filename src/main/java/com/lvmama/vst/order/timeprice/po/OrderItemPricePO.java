package com.lvmama.vst.order.timeprice.po;

/**
 * Created  on 2016/11/7.
 */
public class OrderItemPricePO {
    /**
     * 成人价
     * */
    private Long adultPrice;
    /**
     * 儿童价
     * */
    private Long childPrice;
    /**
     * 成人结算价
     * */
    private Long adultSettlementPrice;
    /**
     * 儿童结算价
     * */
    private Long childSettlementPrice;

    public OrderItemPricePO() {
    }

    public OrderItemPricePO(Long adultPrice, Long childPrice) {
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
    }

    public OrderItemPricePO(Long adultPrice, Long childPrice, Long adultSettlementPrice, Long childSettlementPrice) {
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
        this.adultSettlementPrice = adultSettlementPrice;
        this.childSettlementPrice = childSettlementPrice;
    }

    public Long getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(Long adultPrice) {
        this.adultPrice = adultPrice;
    }

    public Long getChildPrice() {
        return childPrice;
    }

    public void setChildPrice(Long childPrice) {
        this.childPrice = childPrice;
    }

    public Long getAdultSettlementPrice() {
        return adultSettlementPrice;
    }

    public void setAdultSettlementPrice(Long adultSettlementPrice) {
        this.adultSettlementPrice = adultSettlementPrice;
    }

    public Long getChildSettlementPrice() {
        return childSettlementPrice;
    }

    public void setChildSettlementPrice(Long childSettlementPrice) {
        this.childSettlementPrice = childSettlementPrice;
    }
}
