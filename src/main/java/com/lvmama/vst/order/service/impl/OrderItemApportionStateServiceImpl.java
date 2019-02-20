package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.order.dao.OrderItemApportionStateDao;
import com.lvmama.vst.order.service.OrderItemApportionStateService;
import com.lvmama.vst.order.utils.OrderItemApportionStateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
@Service
public class OrderItemApportionStateServiceImpl implements OrderItemApportionStateService {
    @Resource
    private OrderItemApportionStateDao orderItemApportionStateDao;

    /**
     * 保存子单分摊情况
     *
     * @param orderItemApportionState
     * @return 返回保存的实体的id
     */
    @Override
    public Long saveOrderItemApportionState(OrderItemApportionState orderItemApportionState) {
        orderItemApportionStateDao.insertSelective(orderItemApportionState);
        return orderItemApportionState.getOrderItemApportionStateId();
    }

    /**
     * 根据查询条件，查询子单分摊情况的集合
     *
     * @param orderItemApportionStateQueryVO
     */
    @Override
    public List<OrderItemApportionState> queryOrderItemApportionStateList(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO) {
        return orderItemApportionStateDao.selectListByQueryVO(orderItemApportionStateQueryVO);
    }

    /**
     * 根据查询条件，作废原有记录
     *
     * @param orderItemApportionStateQueryVO
     */
    @Override
    public int updateRecords4invalid(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO) {
        if (!OrderItemApportionStateUtils.checkParam(orderItemApportionStateQueryVO)) {
            throw new RuntimeException("作废子单分摊情况表时，必须指定订单id或者子单id");
        }
        return orderItemApportionStateDao.updateByQueryVO4Invalid(orderItemApportionStateQueryVO);
    }

    /**
     * 根据参数更新分摊金额
     * create by liyanpeng on 2017/07/04
     */
    @Override
    public int updateAmountByParam(OrderItemApportionState orderItemApportionState) {
        return orderItemApportionStateDao.updateAmountByParam(orderItemApportionState);
    }
}
