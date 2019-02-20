package com.lvmama.vst.order.web.vo;

import java.io.Serializable;

/**
 * Created by zhouyanqun on 2017/6/21.
 */
public class OrderApportionVO implements Serializable {
    /**
     * 订单id
     * */
    private Long orderId;
    /**
     * 是否强制分摊(即不理会其它任务)
     * */
    private String force;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }
}
