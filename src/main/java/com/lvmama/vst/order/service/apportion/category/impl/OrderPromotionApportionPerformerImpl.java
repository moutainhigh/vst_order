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
 * Created by zhouyanqun on 2017/4/16.
 */
@Component("orderPromotionApportionPerformerImpl")
public class OrderPromotionApportionPerformerImpl extends OrderPercentApportionAbsPerformer implements OrderInternalApportionPerformer {
    private static final Log log = LogFactory.getLog(OrderPromotionApportionPerformerImpl.class);
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
        //促销金额
        long promotionAmount = getDiscountAmount(order);
        log.info("Now apportion promotion amount for order [" + order.getOrderId() + "], promotionAmount is " + promotionAmount);
        if(promotionAmount <= 0) {
            return;
        }

        doApportionOrderAmount(order, apportionOrderItemList, promotionAmount);
    }

    private long getDiscountAmount(OrdOrder order){
        if(order == null) {
            return 0;
        }

        if(CollectionUtils.isEmpty(order.getOrderAmountItemList())) {
            return 0;
        }

        long discountAmount = 0;

        for (OrdOrderAmountItem ordOrderAmountItem : order.getOrderAmountItemList()) {
            if(StringUtils.equals(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name(), ordOrderAmountItem.getOrderAmountType())) {
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
        return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion;
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
        log.info("Now sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of promotion");

        Long itemTotalPromotionAmount = orderItemApportionInfoPO.getItemTotalPromotionAmount();
        if(NumberUtils.equalsOrBelowZero(itemTotalPromotionAmount)) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] promotion is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，然后中止循环，返回
            Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemPromotionApportionByPriceTypeMap(pureEmptyApportionByPriceTypeMap);
            return;
        }
        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemPromotionApportionByPriceTypeMap = orderItemApportionInfoPO.getItemPromotionApportionByPriceTypeMap();

        Map<String, PriceTypeVO> sortedAndCompletedApportionMap = createSortedAndCompletedApportionMap(itemPromotionApportionByPriceTypeMap, ordMulPriceList);
        orderItemApportionInfoPO.setItemPromotionApportionByPriceTypeMap(sortedAndCompletedApportionMap);
        
        Long payAmountReductTotalAmount=orderItemApportionInfoPO.getPayAmountReductTotalAmount();
        if(NumberUtils.equalsOrBelowZero(payAmountReductTotalAmount)) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] promotion is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，然后中止循环，返回
            Map<String, PriceTypeVO> itemReductPaidApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemReductPaidApportionByPriceTypeMap(itemReductPaidApportionByPriceTypeMap);
            return;
        }
        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemReductPaidByPriceTypeMap = orderItemApportionInfoPO.getItemReductPaidApportionByPriceTypeMap();

        Map<String, PriceTypeVO> sortedAndCompReductPaidletedApportionMap = createSortedAndCompletedApportionMap(itemReductPaidByPriceTypeMap, ordMulPriceList);
        orderItemApportionInfoPO.setItemReductPaidApportionByPriceTypeMap(sortedAndCompReductPaidletedApportionMap);
        log.info("Sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of promotion ends");
    }
}
