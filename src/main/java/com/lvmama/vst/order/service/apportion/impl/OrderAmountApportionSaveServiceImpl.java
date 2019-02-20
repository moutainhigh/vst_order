package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.service.OrdOrderCostSharingItemService;
import com.lvmama.vst.order.service.OrderItemApportionStateService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionSaveService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/4/17.
 */
@Service
public class OrderAmountApportionSaveServiceImpl implements OrderAmountApportionSaveService {
    private static final Log log = LogFactory.getLog(OrderAmountApportionSaveServiceImpl.class);
    /**
     * 分摊信息服务
     * */
    @Resource
    private OrdOrderCostSharingItemService orderCostSharingItemService;
    /**
     * 子单分摊情况服务
     * */
    @Resource
    private OrderItemApportionStateService orderItemApportionStateService;

    @Override
    public void saveAllApportion(OrdOrder order) {
        if(order == null || CollectionUtils.isEmpty(order.getOrderItemList())) {
            return;
        }
        Long orderId = order.getOrderId();
        log.info("Now begin to persist apportion info for order " + orderId);
        log.info("Order " + orderId + " item list is " + GsonUtils.toJson(order.getOrderItemList()));
        for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
            int size = ordOrderItem.getOrderCostSharingItemList() == null ? 0 : ordOrderItem.getOrderCostSharingItemList().size();
            log.info("Now persist cost items for order " + orderId + ", item " + ordOrderItem.getOrderItemId() + ", size is " + size);
            if(CollectionUtils.isNotEmpty(ordOrderItem.getOrderCostSharingItemList())) {
                for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
                    orderCostSharingItemService.saveOrdOrderCostSharingItem(orderCostSharingItem);
                }
            }
            size = ordOrderItem.getOrderItemApportionStateMap() == null ? 0 : ordOrderItem.getOrderItemApportionStateMap().size();
            log.info("Persist cost items for order " + orderId + ", item " + ordOrderItem.getOrderItemId()
                    + " completed, now begin to persist item apportion states, size is " + size);
            if (MapUtils.isEmpty(ordOrderItem.getOrderItemApportionStateMap())) {
                continue;
            }

            for (OrderItemApportionState orderItemApportionState : ordOrderItem.getOrderItemApportionStateMap().values()) {
                if(orderItemApportionState == null) {
                    continue;
                }

                orderItemApportionStateService.saveOrderItemApportionState(orderItemApportionState);
            }
            log.info("Persist items apportion states for order " + orderId + ", item " + ordOrderItem.getOrderItemId()
                    + " completed");
        }
        log.info("Persist apportion info for order " + orderId + " completed");
    }

    /**
     * 仅仅保存支付立减及实付分摊信息
     *
     * @param order
     */
    @Override
    public void savePaymentApportion(OrdOrder order) {

        if(order == null || CollectionUtils.isEmpty(order.getOrderItemList())) {
            return;
        }
        Long orderId = order.getOrderId();
        log.info("Now begin to persist actual paid apportion info for order " + order.getOrderId());
        log.info("Order " + orderId + " item list is " + GsonUtils.toJson(order.getOrderItemList()));

        for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
            if(CollectionUtils.isNotEmpty(ordOrderItem.getOrderCostSharingItemList())) {
                for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
                	if(orderCostSharingItem==null){
                		continue;
                	}
                	if(StringUtils.equals(orderCostSharingItem.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name())||
                			StringUtils.equals(orderCostSharingItem.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name())){
                		orderCostSharingItemService.saveOrdOrderCostSharingItem(orderCostSharingItem);
                	}
                   /* if(!StringUtils.equals(orderCostSharingItem.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name())) {
                        continue;
                    }
                    orderCostSharingItemService.saveOrdOrderCostSharingItem(orderCostSharingItem);*/
                }
            }
            log.info("Persist cost items for order " + orderId + ", item " + ordOrderItem.getOrderItemId()
                    + " completed, now begin to persist item apportion states");

            if(MapUtils.isEmpty(ordOrderItem.getOrderItemApportionStateMap())) {
                continue;
            }
            for (OrderItemApportionState orderItemApportionState : ordOrderItem.getOrderItemApportionStateMap().values()) {
            	  if(orderItemApportionState == null){
            		  continue;
            	  }
            	  if(StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name())||
            			  StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name())) {
                      orderItemApportionStateService.saveOrderItemApportionState(orderItemApportionState);

                  }
                /*if(orderItemApportionState == null || !StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name())) {
                    continue;
                }
                orderItemApportionStateService.saveOrderItemApportionState(orderItemApportionState);*/
            }
            log.info("Persist cost items apportion states for order " + orderId + ", item " + ordOrderItem.getOrderItemId()
                    + " completed");
        }
        log.info("Persist actual paid apportion info for order " + orderId + " completed");
    }
}
