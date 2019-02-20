package com.lvmama.vst.order.constant;

import com.lvmama.vst.back.order.po.OrderEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/6/2.
 * 分摊相关的常量
 */
public class ApportionConstants {
    //分页分摊数据时，每页的最大记录数
    public static final long maxPageSize = 300;
    //批量修改数
    public static final int BATCH_UPDATE_SIZE = 300;
    /**
     *等待时间，即订单生成多久以后才会被批次分摊
     * 单位是分钟
     * */
    public static final Long waitTime = 2L;
    /**
     * 下单项分摊品类集合
     * */
    public static final List<String> bookingCostCategoryList = new ArrayList<>();
    /**
     * 支付前分摊品类集合
     * */
    public static final List<String> beforePaymentCostCategoryList = new ArrayList<>();
    /**
     * 分摊相关的所有价格类型数组
     * */
    public static final String[] apportionAllRelatedPriceTypeArray = new String[] {
        OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode(),
                OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
                OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode() ,
                OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()
    };
    /**
     * 按子单分摊的品类相关的价格类型数组
     * */
    public static final String[] apportionByItemRelatedPriceTypeArray = new String[] {
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode()
    };
    /**
     * 按多价格分摊且包含房差的品类相关的价格类型数组
     * */
    public static final String[] apportionByPriceTypeWithSpreadRelatedPriceTypeArray = new String[] {
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode() ,
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()
    };
    /**
     * 按多价格分摊且不包含房差的品类相关的价格类型数组
     * */
    public static final String[] apportionByPriceTypeNoSpreadRelatedPriceTypeArray = new String[] {
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode()
    };
    /**
     * 按入住记录分摊的品类相关的价格类型数组
     * */
    public static final String[] apportionByCheckInDateRelatedPriceTypeArray = new String[] {
            OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode()
    };
    /**
     * 分摊相关的金额类型
     * */
    public static final String[] apportionRelatedAmountTypeArray = new String[] {
            OrderEnum.ORDER_AMOUNT_TYPE.COUPON_AMOUNT.name(),
            OrderEnum.ORDER_AMOUNT_TYPE.COUPON_PRICE.name(),
            OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name(),
            OrderEnum.ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name(),
            OrderEnum.ORDER_AMOUNT_TYPE.PAY_PROMOTION_AMOUNT.name()};

    static {
        bookingCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon.name());
        bookingCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion.name());
        bookingCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name());

        beforePaymentCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon.name());
        beforePaymentCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion.name());
        beforePaymentCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name());
        beforePaymentCostCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual.name());
    }
}
