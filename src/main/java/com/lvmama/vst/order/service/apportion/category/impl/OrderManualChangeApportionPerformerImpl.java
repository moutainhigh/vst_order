package com.lvmama.vst.order.service.apportion.category.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.abs.OrderPercentApportionAbsPerformer;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.category.OrderManualChangeApportionPerformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/4/16.
 */
@Component
public class OrderManualChangeApportionPerformerImpl extends OrderPercentApportionAbsPerformer implements OrderManualChangeApportionPerformer {
    private static final Log log = LogFactory.getLog(OrderManualChangeApportionPerformerImpl.class);
    /**
     * 分摊主订单改价
     */
    @Override
    public void apportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList, Long changeAmount) {
        log.info("Now apportion manual change amount for order [" + order.getOrderId() + "], changeAmount is " + changeAmount);
        doApportionOrderAmount(order, apportionOrderItemList, changeAmount);
    }

    /**
     * 获取分摊类型
     */
    @Override
    protected OrderEnum.ORDER_APPORTION_TYPE getApportionType() {
        return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual;
    }

    /**
     * 重新分摊所有手工改价信息，手工改价可能有多条，需要逐条分摊
     *
     * @param order
     */
    @Override
    public void reDoManualChangeApportion(OrdOrder order, List<OrdOrderItem> apportionOrderItemList, List<OrdAmountChange> ordAmountChangeList) {
        if(CollectionUtils.isEmpty(ordAmountChangeList)) {
            return;
        }
        log.info("Now apportion full manual change amount for order " + order.getOrderId());
        for (OrdAmountChange ordAmountChange : ordAmountChangeList) {
            long apportionAmount = ordAmountChange.getAmount();
            if(StringUtils.equals(VstOrderEnum.ORDER_AMOUNT_FORMULAS.PLUS.name(), ordAmountChange.getFormulas())) {
                apportionAmount = -apportionAmount;
            }
            doApportionOrderAmount(order, apportionOrderItemList, apportionAmount);
        }
        log.info("Apportion full manual change amount for order " + order.getOrderId() + " completed");
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
        log.info("Now sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of manual change");

        Long itemTotalManualChangeAmount = orderItemApportionInfoPO.getItemTotalManualChangeAmount();
        if(NumberUtils.equalsOrBelowZero(itemTotalManualChangeAmount)) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] manual change is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，返回
            Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemManualChangeApportionByPriceTypeMap(pureEmptyApportionByPriceTypeMap);
            return;
        }
        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemManualChangeApportionByPriceTypeMap = orderItemApportionInfoPO.getItemManualChangeApportionByPriceTypeMap();
        Map<String, PriceTypeVO> sortedAndCompletedApportionMap = createSortedAndCompletedApportionMap(itemManualChangeApportionByPriceTypeMap, ordMulPriceList);

        orderItemApportionInfoPO.setItemManualChangeApportionByPriceTypeMap(sortedAndCompletedApportionMap);

        log.info("Sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of manual change ends");
    }
}
