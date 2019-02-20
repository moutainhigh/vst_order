package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.order.constant.ApportionConstants;
import com.lvmama.vst.order.service.OrdOrderCostSharingItemService;
import com.lvmama.vst.order.service.apportion.ApportionOrderDataPrepareService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhouyanqun on 2017/4/17.
 */
@Service
public class ApportionOrderDataPrepareServiceImpl implements ApportionOrderDataPrepareService {
    private static final Log log = LogFactory.getLog(ApportionOrderDataPrepareServiceImpl.class);

    @Resource
    private OrdOrderCostSharingItemService orderCostSharingItemService;
    @Resource
    private ApportionDataAssist apportionDataAssist;

    /**
     * 为下单分摊做准备
     */
    @Override
    public OrdOrder prepareApportionDataForBookingApportion(Long orderId) {
        if(orderId == null || orderId < 0) {
            return null;
        }
        log.info("Now prepare data for order " + orderId + " to redo apportion");
        List<Long> orderIdList = new ArrayList<>();
        orderIdList.add(orderId);
        List<OrdOrder> orderList = this.prepareApportionDataForBookingApportion(orderIdList);
        log.info("Prepare data for order " + orderId + " completed");
        if (CollectionUtils.isEmpty(orderList)) {
            log.error("No order data found for order id " + orderId);
            return null;
        }
        return orderList.get(0);
    }

    /**
     * 批次准备数据，对每个订单号，需要做两件事
     * 1. 查询出子订单、多价格、入住记录(如果有)、其它金额的分摊明细
     * 2. 补全优惠、促销、渠道优惠3种信息
     * 为了提升性能，需要先把所有数据查询出来，然后关联到每个订单上
     *
     * @param orderIdList
     */
    @Override
    public List<OrdOrder> prepareApportionDataForBookingApportion(List<Long> orderIdList) {
        if (CollectionUtils.isEmpty(orderIdList)) {
            log.info("No order to prepare for booking apportion");
        }
        log.info("Begin to prepare for booking apportion, first order is " + orderIdList.get(0));
        //根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
        List<OrdOrder> orderListWithPercentCases = apportionDataAssist.findOrderListWithPercentCases(orderIdList);
        log.info("Prepare percent cases for booking apportion completed, first order is " + orderIdList.get(0));
        //分配优惠、促销、渠道优惠信息
        apportionDataAssist.assignBookingAmount(orderListWithPercentCases);
        log.info("Prepare apportion data for booking apportion completed, first order is " + orderIdList.get(0));
        return orderListWithPercentCases;
    }

    /**
     * 为实付金额分摊准备数据
     * 需要的数据为 订单、子订单、多价格、入住记录(如果有)、其它金额的分摊明细
     *
     * @param orderId
     */
    @Override
    public OrdOrder prepareOrderDataForPayment(Long orderId) {

        if(orderId == null || orderId < 0) {
            return null;
        }

        log.info("Now prepare data for payment for order " + orderId);
        OrdOrder order = apportionDataAssist.findOrderListWithPercentCases(orderId);
        if (order == null) {
            return null;
        }
        //分配优惠、促销、渠道优惠信息（为重复支付提供orderAmountItemList）
        List<OrdOrder> orderList = new ArrayList<>();
        orderList.add(order);
        apportionDataAssist.assignBookingAmount(orderList);
        //补全优惠、促销、渠道优惠、手工改价的分摊信息
        log.info("Prepare mul price and hotel check in rate for payment for order " + orderId + " completed, now complement before payment apportion info");
        this.completeApportionInfoBeforePayment(order);
        log.info("Order " + orderId + " complement before payment apportion info end");

        return order;
    }

    /**
     * 补全支付前的各种分摊信息，也即优惠、促销、渠道优惠、手工改价、支付立减的分摊信息
     * */
    private void completeApportionInfoBeforePayment(OrdOrder order) {
        if(order == null || CollectionUtils.isEmpty(order.getOrderItemList())) {
            return;
        }
        Long orderId = order.getOrderId();
        
        List<Long> orderItemIdList = OrdOrderItemUtils.getOrderItemIdList(order);
        //1、获取子单分摊信息（优惠、促销、渠道优惠、手工改价、支付立减）
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        orderCostSharingItemQueryVO.setOrderItemIdList(orderItemIdList);
        List<String> beforeApportionArray=new ArrayList<String>();
        beforeApportionArray.addAll(ApportionConstants.beforePaymentCostCategoryList);
        beforeApportionArray.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name());
        orderCostSharingItemQueryVO.setCostCategoryList(beforeApportionArray);
        log.info("To complement before payment apportion info for order " + orderId + ", query from db");
        List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
        if(CollectionUtils.isEmpty(orderCostSharingItemList)) {
            return;
        }
        log.info("Order " + orderId + ", query before payment info from db completed, size is " + orderCostSharingItemList.size());
        //2、遍历二次，把分摊信息和对应的子单关联起来
        for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                if (!Objects.equals(orderItem.getOrderItemId(), orderCostSharingItem.getOrderItemId())) {
                    continue;
                }
                if(orderItem.getOrderCostSharingItemList() == null) {
                    orderItem.setOrderCostSharingItemList(new ArrayList<OrdOrderCostSharingItem>());
                }
                orderItem.getOrderCostSharingItemList().add(orderCostSharingItem);
            }
        }
    }

    /**
     * 为分摊准备订单数据，需要查询出子订单、多价格、入住记录
     *
     * @param orderId
     */
    @Override
    public OrdOrder prepareOrderDataForManualChange(Long orderId) {
        log.info("Now prepare date for manual change apportion for order " + orderId);
        if(orderId == null || orderId < 0) {
            return null;
        }

        OrdOrder order = apportionDataAssist.findOrderListWithPercentCases(orderId);
        if (order == null) {
            return null;
        }
        log.info("Completed prepare date for manual change apportion for order " + orderId);

        return order;
    }
}
