package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * 彩虹会员工单服务层
 */
public interface RainbowVIPWorkOrderService {

    /**
     * 下单完成之后,推送工单提醒
     */
    void pushWorkOrderRemindAfterOrder(OrdOrder ordOrder);

    /**
     * 推送消息
     */
    void push(OrdOrder ordOrder) throws Exception;
}
