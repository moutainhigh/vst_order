package com.lvmama.vst.order.service.apportion.assist;

import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.vo.ApportionQueryVO;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/5/15.
 */
public interface ApportionDataAssist {

    /**
     * 批量补全下单金额信息，包括优惠、促销、渠道优惠信息
     * */
    void assignBookingAmount(List<OrdOrder> orderList);

    /**
     * 批量补全下单金额信息，包括优惠、促销、渠道优惠信息
     * */
    void assignBookingAmount(List<OrdOrder> orderList, List<Long> orderIdList);

    /**
     * 批量查询手工改价的金额信息
     * */
    Map<Long, List<OrdAmountChange>> catchPriceChangeAmountMap(List<OrdOrder> priceChangeOrderList, List<Long> priceChangeOrderIdList);

    /**
     * 批量分配除实付金额外的其它金额的分摊信息
     * @param orderIdList 订单id集合，可以为空，为空则从订单集合中获取
     * */
    void assignApportionItemExceptActualPaid(List<OrdOrder> orderList, List<Long> orderIdList);

    /**
     * 批量分配手工改价的分摊金额到各个订单的子单，用于下单项金额已经分配，而且需要计算实付分摊的场景
     * */
    void assignApportionItemOfManualChange(List<OrdOrder> orderList, List<Long> orderIdList);

    /**
     * 根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
     * */
    List<OrdOrder> findOrderListWithPercentCases(List<Long> orderIdList);

    /**
     * 根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
     * */
    OrdOrder findOrderListWithPercentCases(Long orderId);

    /**
     * 分配分摊比例的数据（也就是多价格和入住记录）
     * */
    void assignPercentCases(List<OrdOrder> orderList, List<Long> orderIdList);

    /**
     * 过滤得到需要参与分摊的子单集合
     * 目前过滤快递和保险
     * */
    List<OrdOrderItem> filterOrderItems(List<OrdOrderItem> orderItemList);

    /**
     * 根据条件作废以前的分摊记录
     * */
    void invalidOrderApportionData(ApportionQueryVO apportionQueryVO);
}
