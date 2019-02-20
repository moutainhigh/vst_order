package com.lvmama.vst.order.factory;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.service.apportion.category.OrderAmountApportionPerformer;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import com.lvmama.vst.order.service.apportion.category.OrderManualChangeApportionPerformer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/4/21.
 */
@Service
public class ApportionCategoryServiceFactory {
    /**优惠券分摊服务*/
    @Resource(name = "orderCouponApportionPerformerImpl")
    private OrderInternalApportionPerformer orderCouponApportionPerformer;
    /**促销分摊服务*/
    @Resource(name = "orderPromotionApportionPerformerImpl")
    private OrderInternalApportionPerformer orderPromotionApportionPerformer;
    /**渠道优惠分摊服务*/
    @Resource(name = "orderDistributorApportionPerformerImpl")
    private OrderInternalApportionPerformer orderDistributorApportionPerformer;
    /**手工分摊服务*/
    @Resource
    private OrderManualChangeApportionPerformer orderManualChangeApportionPerformer;
    /**实际分摊服务*/
    @Resource(name = "orderActualPaymentApportionPerformerImpl")
    private OrderInternalApportionPerformer orderActualPaidApportionPerformer;

    //根据种类获取金额分摊服务
    public OrderAmountApportionPerformer catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE orderApportionType) {
        switch (orderApportionType) {
            case apportion_type_coupon: return orderCouponApportionPerformer;
            case apportion_type_promotion: return orderPromotionApportionPerformer;
            case apportion_type_distributor: return orderDistributorApportionPerformer;
            case apportion_type_manual: return orderManualChangeApportionPerformer;
            case apportion_type_payment: return orderActualPaidApportionPerformer;
            default: return null;
        }
    }
}
