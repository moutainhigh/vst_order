package com.lvmama.vst.order.web.vo;

import java.io.Serializable;

/**
 * Created by zhouyanqun on 2017/7/27.
 */
public class OrderApportionMsgVO implements Serializable {
    /**
     * 订单id
     * */
    private Long orderId;
    /**
     * 消息类型
     * */
    private String eventType;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
