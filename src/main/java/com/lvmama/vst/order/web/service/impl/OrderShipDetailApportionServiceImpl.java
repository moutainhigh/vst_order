package com.lvmama.vst.order.web.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.service.OrderItemApportionStateService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.web.service.OrderShipDetailApportionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhouyanqun on 2017/6/12.
 */
@Component
public class OrderShipDetailApportionServiceImpl implements OrderShipDetailApportionService {
    private static final Log log = LogFactory.getLog(OrderShipDetailApportionServiceImpl.class);

    @Resource
    private OrderItemApportionStateService orderItemApportionStateService;
    /**
     * 计算订单详情中，子单的分摊信息
     * 目前订单详情中，子单的信息只有一列：实付金额
     *
     * @param orderId
     * @param orderMonitorRstList
     */
    @Override
    public void calcOrderDetailItemApportion(Long orderId, List<OrderMonitorRst> orderMonitorRstList) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, can't calculate order detail item apportion, please check");
            return;
        }
        if (CollectionUtils.isEmpty(orderMonitorRstList)) {
            log.warn("Order " + orderId + ", monitor list is null");
            return;
        }
        int size = orderMonitorRstList.size();
        log.info("Now begin to calculate order apportion information for order " + orderId + ", list size is " + size);
        //子单id集合
        List<Long> orderItemIdList = new ArrayList<>();
        for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
            if (orderMonitorRst == null || NumberUtils.isNotAboveZero(orderMonitorRst.getOrderItemId())) {
                continue;
            }
            orderItemIdList.add(orderMonitorRst.getOrderItemId());
        }
        String orderItemIdListJson = GsonUtils.toJson(orderItemIdList);
        log.info("Now begin to calculate item apportion state for order " + orderId + " completed, item id list is " + orderItemIdListJson);
        //计算子单分摊情况数据，如果没有从数据库中查询到，分配一个0值的对象过来
        this.calcItemApportionStateInfo(orderItemIdList, orderMonitorRstList);
        log.info("Now calculate item apportion state for order " + orderId + " completed, item id list is " + orderItemIdListJson);
        log.info("Now completed calculating order apportion information for order " + orderId + ", item id list is " + orderItemIdListJson);
    }

    /**
     * 计算子单分摊情况数据，如果没有从数据库中查询到，分配一个0值的对象过来
     * */
    private void calcItemApportionStateInfo(List<Long> orderItemIdList, List<OrderMonitorRst> orderMonitorRstList) {
        if (CollectionUtils.isEmpty(orderItemIdList) || CollectionUtils.isEmpty(orderMonitorRstList)) {
            log.warn("Order item id list is empty or order monitor list is null");
            return;
        }

        //根据条件查询所有子单实付分摊情况list，先查后关联
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        orderItemApportionStateQueryVO.setOrderItemIdList(orderItemIdList);
        orderItemApportionStateQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);
        log.info("Now item apportion state queried from database, content is " + GsonUtils.toJson(orderItemApportionStateList));
        this.assignApportionStateToItem(orderItemApportionStateList, orderMonitorRstList);
        
        //支付立减分摊金额
        orderItemApportionStateQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name());
        List<OrderItemApportionState> payItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);
        log.info("Now item apportion state queried from database, payItemApportionStateList is " + GsonUtils.toJson(payItemApportionStateList));
        this.assignPayApportionStateToItem(payItemApportionStateList, orderMonitorRstList);
    }
    
    /**
     * 支付立减分摊
     * @param payItemApportionStateList
     * @param orderMonitorRstList
     */
    private void assignPayApportionStateToItem(List<OrderItemApportionState> payItemApportionStateList,List<OrderMonitorRst> orderMonitorRstList) {
    	if (CollectionUtils.isEmpty(payItemApportionStateList) || CollectionUtils.isEmpty(orderMonitorRstList)) {
            log.warn("Order item apportion state list is empty or order monitor list is null");
            return;
        }

        for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
            if (orderMonitorRst == null) {
                continue;
            }
            Long orderId = orderMonitorRst.getOrderId();
            Long orderItemId = orderMonitorRst.getOrderItemId();
            log.info("Now assign apportion state for order " + orderId + ", item " + orderItemId);

            if (orderMonitorRst.getPayProAmountList() == null) {
                orderMonitorRst.setPayProAmountList(new ArrayList<PriceTypeVO>());
            }
            //是否匹配到了分摊情况数据
            boolean matched = false;
            for (OrderItemApportionState orderItemApportionState : payItemApportionStateList) {
                if (orderItemApportionState == null || !Objects.equals(orderItemApportionState.getOrderItemId(), orderMonitorRst.getOrderItemId())) {
                    continue;
                }
                matched = true;
                PriceTypeVO priceTypeVO = new PriceTypeVO(orderItemApportionState.getApportionAmount(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName());
                orderMonitorRst.getPayProAmountList().add(priceTypeVO);
            }
            //没有匹配到分摊情况数据时，new一个0值的对象进去
            if (!matched) {
                PriceTypeVO priceTypeVO = new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName());
                orderMonitorRst.getPayProAmountList().add(priceTypeVO);
            }
            log.info("Finished assign apportion state for order " + orderId + ", item " + orderItemId + ", matched = " + matched);
		
        }
    }
	/**
     * 子单分摊情况数据
     * */
    private void assignApportionStateToItem(List<OrderItemApportionState> orderItemApportionStateList, List<OrderMonitorRst> orderMonitorRstList) {
        if (CollectionUtils.isEmpty(orderItemApportionStateList) || CollectionUtils.isEmpty(orderMonitorRstList)) {
            log.warn("Order item apportion state list is empty or order monitor list is null");
            return;
        }

        for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
            if (orderMonitorRst == null) {
                continue;
            }
            Long orderId = orderMonitorRst.getOrderId();
            Long orderItemId = orderMonitorRst.getOrderItemId();
            log.info("Now assign apportion state for order " + orderId + ", item " + orderItemId);

            if (orderMonitorRst.getActualPaidAmountList() == null) {
                orderMonitorRst.setActualPaidAmountList(new ArrayList<PriceTypeVO>());
            }
            //是否匹配到了分摊情况数据
            boolean matched = false;
            for (OrderItemApportionState orderItemApportionState : orderItemApportionStateList) {
                if (orderItemApportionState == null || !Objects.equals(orderItemApportionState.getOrderItemId(), orderMonitorRst.getOrderItemId())) {
                    continue;
                }
                matched = true;
                PriceTypeVO priceTypeVO = new PriceTypeVO(orderItemApportionState.getApportionAmount(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName());
                orderMonitorRst.getActualPaidAmountList().add(priceTypeVO);
            }
            //没有匹配到分摊情况数据时，new一个0值的对象进去
            if (!matched) {
                PriceTypeVO priceTypeVO = new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName());
                orderMonitorRst.getActualPaidAmountList().add(priceTypeVO);
            }
            log.info("Finished assign apportion state for order " + orderId + ", item " + orderItemId + ", matched = " + matched);
        }
    }
}
