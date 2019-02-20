package com.lvmama.vst.order.abs;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by zhouyanqun on 2017/4/13.
 */
public abstract class OrderAmountApportionAbsPerformer {
    private static final Log log = LogFactory.getLog(OrderAmountApportionAbsPerformer.class);

    /**根据多价格，创建完全0值的多价格分摊数据
     *
     */
    protected Map<String, PriceTypeVO> createPureEmptyApportionByPriceTypeMap(List<OrdMulPriceRate> ordMulPriceList) {
        if (CollectionUtils.isEmpty(ordMulPriceList)) {
            return null;
        }
        Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = new HashMap<>();
        for (OrdMulPriceRate ordMulPriceRate : ordMulPriceList) {
            String priceType = ordMulPriceRate.getPriceType();
            PriceTypeVO priceTypeVO = new PriceTypeVO(0L, 0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            pureEmptyApportionByPriceTypeMap.put(priceType, priceTypeVO);
        }
        return pureEmptyApportionByPriceTypeMap;
    }

    /**
     * 根据多价格，以及分摊数据Map，排序并且补全多价格分摊信息
     * */
    protected Map<String, PriceTypeVO> createSortedAndCompletedApportionMap(Map<String, PriceTypeVO> oldApportionMap, List<OrdMulPriceRate> ordMulPriceList){
        //建立一个新Map，这个Map已经排好序，而且针对多价格没有的值，做了补充
        Map<String, PriceTypeVO> newItemCouponApportionByCheckInMap = new HashMap<>();
        for (OrdMulPriceRate ordMulPriceRate : ordMulPriceList) {
            String priceType = ordMulPriceRate.getPriceType();
            PriceTypeVO priceTypeVO;
            if (oldApportionMap == null || oldApportionMap.get(priceType) == null) {
                priceTypeVO = new PriceTypeVO(0L, 0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            } else {
                priceTypeVO = oldApportionMap.get(priceType);
            }
            newItemCouponApportionByCheckInMap.put(priceType, priceTypeVO);
        }

        return newItemCouponApportionByCheckInMap;
    }

    /**
     * 根据金额生成子单分摊情况实体(如果实体存在，补充数据，如果不存在，申请一个)
     * */
    protected void generateItemApportionState(OrdOrderItem orderItem, Long apportionAmount) {
        if(orderItem == null || NumberUtils.isNotAboveZero(orderItem.getOrderItemId())) {
            return;
        }
        OrderEnum.ORDER_APPORTION_TYPE apportionType = getApportionType();
        log.info("Now generate state for order " + orderItem.getOrderId() + ",item " + orderItem.getOrderItemId() + ",apportion type " + apportionType + ", apportion amount is " + apportionAmount);
        if(orderItem.getOrderItemApportionStateMap() == null) {
            orderItem.setOrderItemApportionStateMap(new HashMap<OrderEnum.ORDER_APPORTION_TYPE, OrderItemApportionState>());
        }
        Map<OrderEnum.ORDER_APPORTION_TYPE, OrderItemApportionState> orderItemApportionStateMap = orderItem.getOrderItemApportionStateMap();
        //假如map中没有对应的分摊类型的实体，申请一个，并且复制必要的信息
        if(orderItemApportionStateMap.get(apportionType) == null) {
            OrderItemApportionState orderItemApportionState = generateItemApportionState(orderItem);
            orderItemApportionStateMap.put(apportionType, orderItemApportionState);
        }
        //把分摊金额写入实体
        OrderItemApportionState orderItemApportionState = orderItemApportionStateMap.get(apportionType);
        if(apportionAmount == null) {
            apportionAmount = 0L;
        }
        orderItemApportionState.setApportionAmount(apportionAmount);
        log.info("Generate state for order " + orderItem.getOrderId() +
                ",item " + orderItem.getOrderItemId() + ", apportion type " + apportionType
                + " completed, result is " + GsonUtils.toJson(orderItemApportionState));
    }

    private OrderItemApportionState generateItemApportionState(OrdOrderItem orderItem) {
        OrderItemApportionState orderItemApportionState = new OrderItemApportionState();
        if (orderItem == null) {
            return orderItemApportionState;
        }
        orderItemApportionState.setOrderId(orderItem.getOrderId());
        orderItemApportionState.setOrderItemId(orderItem.getOrderItemId());
        orderItemApportionState.setCostCategory(getApportionType().name());
        orderItemApportionState.setValidFlag(Constants.Y_FLAG);
        Date currentDateTime = Calendar.getInstance().getTime();
        orderItemApportionState.setCreateTime(currentDateTime);
        orderItemApportionState.setUpdateTime(currentDateTime);
        return orderItemApportionState;
    }

    /**
     * 获取分摊类型
     * */
    protected abstract OrderEnum.ORDER_APPORTION_TYPE getApportionType();
}
