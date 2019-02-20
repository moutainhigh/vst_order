package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;

import java.util.List;

public interface OrderItemApportionStateDao {
    int deleteByPrimaryKey(Long orderItemApportionStateId);

    int insert(OrderItemApportionState record);

    int insertSelective(OrderItemApportionState record);

    OrderItemApportionState selectByPrimaryKey(Long orderItemApportionStateId);

    /**
     * 根据条件查询结果集
     * */
    List<OrderItemApportionState> selectListByQueryVO(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO);

    /**
     * 根据条件作废记录
     * */
    int updateByQueryVO4Invalid(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO);

    int updateByPrimaryKeySelective(OrderItemApportionState record);

    int updateByPrimaryKey(OrderItemApportionState record);

    /**
     * 根据参数更新分摊金额
     * create by liyanpeng on 2017/07/04
     */
    int updateAmountByParam(OrderItemApportionState orderItemApportionState);
}