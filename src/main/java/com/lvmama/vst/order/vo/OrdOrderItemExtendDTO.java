package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 外币项目，子订单扩展信息
 * @Author: yinhuiming
 * @CreateDate: 2018/10/8  下午 5:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2018/10/8  下午 5:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class OrdOrderItemExtendDTO implements Serializable {
    //时间价格接口返回  开始
    /**
     * 外币结算单价
     */
    private Long foreignSettlementPrice;
    /**
     * 外币市场单价
     */
    private Long foreignMarketPrice;
    /**
     * 外币销售单价
     */
    private Long foreignPrice;
    /**汇率模式*/
    private Integer exchangeRateModel;
    /**自定义汇率*/
    private BigDecimal customizeExchangeRate;
    //时间价格接口返回  结束


    private BigDecimal settlementPriceRate;//结算汇率
    private BigDecimal priceRate;//销售汇率
    private Long foreignTotalSettlementPrice; //外币结算总价
    private String  currencyCode;//币种
    private String currencyName;//币种名称
    /**
     * 外币实际结算价
     */
    private Long foreignActualSettlementPrice;
    /**
     * 外币实际结算总价  FOREIGN_ACT_TOTAL_SETTLE_PRICE
     */
    private Long foreignActTotalSettlePrice;

    public Long getForeignSettlementPrice() {
        return foreignSettlementPrice;
    }

    public void setForeignSettlementPrice(Long foreignSettlementPrice) {
        this.foreignSettlementPrice = foreignSettlementPrice;
    }

    public Long getForeignMarketPrice() {
        return foreignMarketPrice;
    }

    public void setForeignMarketPrice(Long foreignMarketPrice) {
        this.foreignMarketPrice = foreignMarketPrice;
    }

    public Long getForeignPrice() {
        return foreignPrice;
    }

    public void setForeignPrice(Long foreignPrice) {
        this.foreignPrice = foreignPrice;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public Long getForeignActualSettlementPrice() {
        return foreignActualSettlementPrice;
    }

    public void setForeignActualSettlementPrice(Long foreignActualSettlementPrice) {
        this.foreignActualSettlementPrice = foreignActualSettlementPrice;
    }

    public Integer getExchangeRateModel() {
        return exchangeRateModel;
    }

    public void setExchangeRateModel(Integer exchangeRateModel) {
        this.exchangeRateModel = exchangeRateModel;
    }

    public BigDecimal getCustomizeExchangeRate() {
        return customizeExchangeRate;
    }

    public void setCustomizeExchangeRate(BigDecimal customizeExchangeRate) {
        this.customizeExchangeRate = customizeExchangeRate;
    }

    public BigDecimal getSettlementPriceRate() {
        return settlementPriceRate;
    }

    public void setSettlementPriceRate(BigDecimal settlementPriceRate) {
        this.settlementPriceRate = settlementPriceRate;
    }

    public BigDecimal getPriceRate() {
        return priceRate;
    }

    public void setPriceRate(BigDecimal priceRate) {
        this.priceRate = priceRate;
    }

    public Long getForeignTotalSettlementPrice() {
        return foreignTotalSettlementPrice;
    }

    public void setForeignTotalSettlementPrice(Long foreignTotalSettlementPrice) {
        this.foreignTotalSettlementPrice = foreignTotalSettlementPrice;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Long getForeignActTotalSettlePrice() {
        return foreignActTotalSettlePrice;
    }

    public void setForeignActTotalSettlePrice(Long foreignActTotalSettlePrice) {
        this.foreignActTotalSettlePrice = foreignActTotalSettlePrice;
    }

    @Override
    public String toString() {
        return "OrdOrderItemExtendDTO{" +
                "foreignSettlementPrice=" + foreignSettlementPrice +
                ", foreignMarketPrice=" + foreignMarketPrice +
                ", foreignPrice=" + foreignPrice +
                ", exchangeRateModel=" + exchangeRateModel +
                ", customizeExchangeRate=" + customizeExchangeRate +
                ", settlementPriceRate=" + settlementPriceRate +
                ", priceRate=" + priceRate +
                ", foreignTotalSettlementPrice=" + foreignTotalSettlementPrice +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyName='" + currencyName + '\'' +
                ", foreignActualSettlementPrice=" + foreignActualSettlementPrice +
                ", foreignActTotalSettlePrice=" + foreignActTotalSettlePrice +
                '}';
    }
}
