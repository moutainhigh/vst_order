package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
public interface OrderItemApportionStateService {
    /**
     * 保存子单分摊情况
     * @return 返回保存的实体的id
     * */
    Long saveOrderItemApportionState(OrderItemApportionState orderItemApportionState);

    /**
     * 根据查询条件，查询子单分摊情况的集合
     * */
    List<OrderItemApportionState> queryOrderItemApportionStateList(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO);

    /**
     * 根据查询条件，作废原有记录
     * */
    int updateRecords4invalid(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO);

    /**
     * 根据参数更新分摊金额
     * create by liyanpeng on 2017/07/04
     */
    int updateAmountByParam(OrderItemApportionState orderItemApportionState);
}
