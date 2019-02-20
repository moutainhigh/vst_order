package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;

/**
 * Created by zhouyanqun on 2016/5/3.
 */
public interface OrdOrderTravellerConfirmService {
    /**
     * 插入或者更新，根据OrdOrderTravellerConfirm的orderId判断记录是否存在，如果存在，就更新，如果不存在，就保存
     * */
    int saveOrUpdate(OrderTravellerOperateDO orderTravellerOperateDO);

    /**
     *插入记录
     * */
    int save(OrderTravellerOperateDO orderTravellerOperateDO);

    /**
     *更新记录
     * */
    int update(OrderTravellerOperateDO orderTravellerOperateDO);

    /**
     * 根据订单id查询一条记录
     * */
    OrdOrderTravellerConfirm selectSingleByOrderId(Long orderId);
}
