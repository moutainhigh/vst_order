package com.lvmama.vst.order.service;

/**
 * Created by zhouyanqun on 2016/5/7.
 * 订单游玩人相关的操作接口
 * 目前只有锁定游玩人功能
 */
public interface OrdOrderTravellerService {
    /**
     *锁定订单游玩人
     * */
    public int updateOrderLockTraveller(Long orderId);
}
