package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;

/**
 * Created by zhouyanqun on 2016/4/29.
 * 游玩人确认信息3条的操作服务
 * 对应的实体是com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm
 */
public interface OrdOrderTravellerConfirmDao {
    /**
     * 更新记录
     * */
    int merge(OrdOrderTravellerConfirm orderTravellerConfirm);

    /**
     *插入记录
     * */
    int persist(OrdOrderTravellerConfirm orderTravellerConfirm);

    /**
     * 根据订单id查询一条记录
     * */
    OrdOrderTravellerConfirm selectSingleByOrderId(Long orderId);
}
