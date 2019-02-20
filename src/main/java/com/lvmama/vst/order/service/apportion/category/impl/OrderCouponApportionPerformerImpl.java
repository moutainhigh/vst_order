package com.lvmama.vst.order.service.apportion.category.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.abs.OrderPercentApportionAbsPerformer;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/4/12.
 * 订单优惠金额分摊的命令
 */
@Component("orderCouponApportionPerformerImpl")
public class OrderCouponApportionPerformerImpl extends OrderPercentApportionAbsPerformer implements OrderInternalApportionPerformer {
    private static final Log log = LogFactory.getLog(OrderCouponApportionPerformerImpl.class);
    /**
     * 针对订单做分摊
     *
     * @param order 将被分摊的订单
     */
    @Override
    public void doApportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList) {
        if(order == null || CollectionUtils.isEmpty(apportionOrderItemList)) {
            return;
        }
        //总优惠金额
        long couponAmount = getCouponAmount(order);
        log.info("Now apportion coupon amount for order " + order.getOrderId() + ", couponAmount is " + couponAmount);
        if(couponAmount <= 0) {
            return;
        }

        doApportionOrderAmount(order, apportionOrderItemList, couponAmount);
    }

    private long getCouponAmount(OrdOrder order) {
        if(order == null) {
            return 0;
        }

        if(CollectionUtils.isEmpty(order.getOrderAmountItemList())) {
            return 0;
        }

        long discountAmount = 0;

        for (OrdOrderAmountItem ordOrderAmountItem : order.getOrderAmountItemList()) {
            if(StringUtils.equals(OrderEnum.ORDER_AMOUNT_TYPE.COUPON_PRICE.name(), ordOrderAmountItem.getOrderAmountType())
                    || StringUtils.equals(OrderEnum.ORDER_AMOUNT_TYPE.COUPON_AMOUNT.name(), ordOrderAmountItem.getOrderAmountType())) {
                discountAmount += ordOrderAmountItem.getItemAmount();
            }
        }

        return -discountAmount;
    }

    /**
     * 获取分摊类型
     */
    @Override
    protected OrderEnum.ORDER_APPORTION_TYPE getApportionType() {
        return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon;
    }

    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     */
    @Override
    public void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList) {
        if(orderItemApportionInfoPO == null || CollectionUtils.isEmpty(ordMulPriceList)) {
            return;
        }
        log.info("Now sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of coupon");

        Long itemTotalCouponAmount = orderItemApportionInfoPO.getItemTotalCouponAmount();
        if(NumberUtils.equalsOrBelowZero(itemTotalCouponAmount)) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] coupon amount is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，然后中止循环，返回
            Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemCouponApportionByPriceTypeMap(pureEmptyApportionByPriceTypeMap);
            return;
        }

        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemCouponApportionByPriceTypeMap = orderItemApportionInfoPO.getItemCouponApportionByPriceTypeMap();
        Map<String, PriceTypeVO> sortedAndCompletedApportionMap = createSortedAndCompletedApportionMap(itemCouponApportionByPriceTypeMap, ordMulPriceList);
        orderItemApportionInfoPO.setItemCouponApportionByPriceTypeMap(sortedAndCompletedApportionMap);

        log.info("Sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of coupon ends");
    }
}
