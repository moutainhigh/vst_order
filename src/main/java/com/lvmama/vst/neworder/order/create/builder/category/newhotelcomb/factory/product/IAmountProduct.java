package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dengcheng on 17/2/23.
 */
public interface IAmountProduct {
    //总金额的计算方式  saleAmmountList[i] +  goodsAmmount[i] + expressAmmountList[i]
    public class ProductAmmountItem {


        //单位分
        List<SaleAmount> saleAmountList = new ArrayList<SaleAmount>() ;

        List<GoodsAmount> goodsAmount = new ArrayList<GoodsAmount>();

        List<ExpressAmount> expressAmountList = new ArrayList<ExpressAmount>() ;


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


    }

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



    public class GoodsAmount {
        String categoryId;
        String goodsId;
        Long sellAmount;
        Long marketAmount;

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

        public String getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }



    }

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


    /**
     * 构建促销相关金额信息
     * @param buyInfo
     * @return
     */
    List<SaleAmount> buildSaleAmount(DestBuBuyInfo buyInfo);

    /**
     * 构建快递相关金额信息
     * @param buyInfo
     * @return
     */
    List<ExpressAmount> buildExpressAmount(DestBuBuyInfo buyInfo);

    /**
     * 构建商品相关金额信息
     * @param buyInfo
     * @return
     */
    List<GoodsAmount> buildGoodsAmount(DestBuBuyInfo buyInfo);

}
