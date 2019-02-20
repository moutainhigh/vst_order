package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.client.ord.service.OrderTravellerConfirmClientService;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2016/5/3.
 * 游玩人确认信息的操作服务的vst内部调用接口
 */
@Component("orderTravellerConfirmClientServiceRemote")
public class OrderTravellerConfirmClientServiceImpl implements OrderTravellerConfirmClientService {
    @Resource(name = "ordOrderTravellerConfirmService")
    private OrdOrderTravellerConfirmService ordOrderTravellerConfirmService;

    /**
     * 插入或者更新，根据OrdOrderTravellerConfirm的orderId判断记录是否存在，如果存在，就更新，如果不存在，就保存
     *
     */
    @Override
    public int saveOrUpdateOrderTravellerConfirmInfo(OrderTravellerOperateDO orderTravellerOperateDO) {
        return ordOrderTravellerConfirmService.saveOrUpdate(orderTravellerOperateDO);
    }

    /**
     * 插入记录
     *
     */
    @Override
    public int saveOrderTravellerConfirmInfo(OrderTravellerOperateDO orderTravellerOperateDO) {
        return ordOrderTravellerConfirmService.save(orderTravellerOperateDO);
    }

    /**
     * 更新记录
     *
     */
    @Override
    public int updateOrderTravellerConfirmInfo(OrderTravellerOperateDO orderTravellerOperateDO) {
        return ordOrderTravellerConfirmService.update(orderTravellerOperateDO);
    }

    /**
     * 根据订单id查询一条记录
     *
     * @param orderId
     */
    @Override
    public OrdOrderTravellerConfirm findOrderTravellerConfirmInfoByOrderId(Long orderId) {
        return ordOrderTravellerConfirmService.selectSingleByOrderId(orderId);
    }
}
