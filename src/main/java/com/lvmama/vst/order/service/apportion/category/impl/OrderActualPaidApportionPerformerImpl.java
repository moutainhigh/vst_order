package com.lvmama.vst.order.service.apportion.category.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.abs.OrderAmountApportionAbsPerformer;
import com.lvmama.vst.order.factory.ApportionParticleServiceFactory;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/4/17.
 * 实付金额分摊
 */
@Component("orderActualPaymentApportionPerformerImpl")
public class OrderActualPaidApportionPerformerImpl extends OrderAmountApportionAbsPerformer implements OrderInternalApportionPerformer {
    private static final Log log = LogFactory.getLog(OrderActualPaidApportionPerformerImpl.class);
    @Resource
    private ApportionParticleServiceFactory orderDetailApportionCompleteServiceFactory;
    @Override
    public void doApportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList) {
        if(order == null || CollectionUtils.isEmpty(apportionOrderItemList)) {
            return;
        }

        for (OrdOrderItem ordOrderItem : apportionOrderItemList) {
            long totalApportionAmountBeforePayment = calcTotalApportionAmountBeforePayment(ordOrderItem);
            log.info("Now calculate actual paid amount for order [" + ordOrderItem.getOrderId() + "], item [" + ordOrderItem.getOrderItemId() + "], totalApportionAmountBeforePayment is " + totalApportionAmountBeforePayment);
            //生成分摊数据实体
            OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = ApportionUtil.judgeApportionParticle(ordOrderItem.getCategoryId());
            ApportionParticleService apportionParticleService = orderDetailApportionCompleteServiceFactory.catchOrderDetailApportionCompleteService(orderApportionParticle);
            List<OrdOrderCostSharingItem> orderCostSharingItemList = apportionParticleService.generateOrdOrderCostSharingItem4Paid(ordOrderItem);
            //计算订单实付金额
            long itemActualPaidAmount = ordOrderItem.getTotalAmount() - totalApportionAmountBeforePayment;
            //把实付金额的分摊情况加入子单中
            super.generateItemApportionState(ordOrderItem, itemActualPaidAmount);
            //添加实体到子单中去
            addOrderCostSharingItemToItem(ordOrderItem, orderCostSharingItemList);
        }
    }

    /**
     * 添加分摊数据到子单
     * */
    private void addOrderCostSharingItemToItem(OrdOrderItem ordOrderItem, List<OrdOrderCostSharingItem> orderCostSharingItem) {
        if(ordOrderItem == null || orderCostSharingItem == null) {
            return;
        }

        if(ordOrderItem.getOrderCostSharingItemList() == null) {
            ordOrderItem.setOrderCostSharingItemList(new ArrayList<OrdOrderCostSharingItem>());
        }

        ordOrderItem.getOrderCostSharingItemList().addAll(orderCostSharingItem);
    }

    /**
     * 计算支付前的总分摊金额，一共有4种分摊金额
     * 优惠、促销、分销变价、手工改价
     * */
    private long calcTotalApportionAmountBeforePayment(OrdOrderItem ordOrderItem) {
        if(ordOrderItem == null || CollectionUtils.isEmpty(ordOrderItem.getOrderCostSharingItemList())) {
            return 0;
        }
        long totalApportionAmountBeforePayment = 0;
        for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
            if(orderCostSharingItem == null ||ApportionUtil.isActualPaymentApportionType(orderCostSharingItem.getCostCategory())) {
                continue;
            }

            totalApportionAmountBeforePayment += orderCostSharingItem.getAmount();
        }

        return totalApportionAmountBeforePayment;
    }

    /**
     * 获取分摊类型
     */
    @Override
    protected OrderEnum.ORDER_APPORTION_TYPE getApportionType() {
        return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment;
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
        log.info("Now sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of actual paid");

        Long itemTotalActualPaidAmount = orderItemApportionInfoPO.getItemTotalActualPaidAmount();
        if(NumberUtils.equalsOrBelowZero(itemTotalActualPaidAmount)) {
            log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "] actual paid is null, will set a pure empty map");
            //如果优惠的多价格分摊是空，那么直接创建一个新的值都为0的Map，返回
            Map<String, PriceTypeVO> pureEmptyApportionByPriceTypeMap = createPureEmptyApportionByPriceTypeMap(ordMulPriceList);
            orderItemApportionInfoPO.setItemActualPaidApportionByPriceTypeMap(pureEmptyApportionByPriceTypeMap);
            return;
        }
        //原有的Map，用于取值
        Map<String, PriceTypeVO> itemActualPaidApportionByPriceTypeMap = orderItemApportionInfoPO.getItemActualPaidApportionByPriceTypeMap();
        Map<String, PriceTypeVO> sortedAndCompletedApportionMap = createSortedAndCompletedApportionMap(itemActualPaidApportionByPriceTypeMap, ordMulPriceList);

        orderItemApportionInfoPO.setItemActualPaidApportionByPriceTypeMap(sortedAndCompletedApportionMap);

        log.info("Sort and complete order item [" + orderItemApportionInfoPO.getOrderItemId() + "] multiple price apportion info of actual paid ends");
    }
}
