package com.lvmama.vst.order.service.apportion.category.impl;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.abs.OrderPercentApportionAbsPerformer;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/4/12.
 */
@Component("orderDistributorApportionPerformerImpl")
public class OrderDistributorApportionPerformerImpl extends OrderPercentApportionAbsPerformer implements OrderInternalApportionPerformer {
    private static final Log log = LogFactory.getLog(OrderDistributorApportionPerformerImpl.class);
    /**
     * 针对订单做分摊
     *
     */
    @Override
    public void doApportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList) {
        if(order == null || CollectionUtils.isEmpty(apportionOrderItemList)) {
            return;
        }
        //分销渠道减少金额
        long distributorAmount = getDiscountAmount(order);
        log.info("Now apportion distributor amount for order " + order.getOrderId() + ", distributorAmount is " + distributorAmount);
        if(distributorAmount == 0) {
            return;
        }

        doApportionOrderAmount(order, apportionOrderItemList, distributorAmount);
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
            if(StringUtils.equals(OrderEnum.ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name(), ordOrderAmountItem.getOrderAmountType())) {
                discountAmount += ordOrderAmountItem.getItemAmount();
            }
        }

        return discountAmount;
    }

    /**
     * 获取分摊粒度
     */
    @Override
    protected OrderEnum.ORDER_APPORTION_TYPE getApportionType() {
        return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor;
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
        log.info("Now sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of distributor");
        Long itemTotalDistributorAmount = orderItemApportionInfoPO.getItemTotalDistributorAmount();
        if(itemTotalDistributorAmount == null || itemTotalDistributorAmount == 0L) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] distributor is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，返回
            Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemDistributorApportionByPriceTypeMap(pureEmptyApportionByPriceTypeMap);
            return;
        }

        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemDistributorApportionByPriceTypeMap = orderItemApportionInfoPO.getItemDistributorApportionByPriceTypeMap();
        Map<String, PriceTypeVO> sortedAndCompletedApportionMap = createSortedAndCompletedApportionMap(itemDistributorApportionByPriceTypeMap, ordMulPriceList);

        orderItemApportionInfoPO.setItemDistributorApportionByPriceTypeMap(sortedAndCompletedApportionMap);

        log.info("Sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of distributor ends");
    }
}
