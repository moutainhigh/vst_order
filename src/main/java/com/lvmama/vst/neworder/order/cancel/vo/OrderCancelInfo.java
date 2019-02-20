package com.lvmama.vst.neworder.order.cancel.vo;

import java.io.Serializable;

/**
 * Created by dengcheng on 17/4/12.
 */
public class OrderCancelInfo implements Serializable{
    private Long orderId;
    private String cancelCode;
    private String reason;
    private String operatorId;
    private String memo;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCancelCode() {
        return cancelCode;
    }

    public void setCancelCode(String cancelCode) {
        this.cancelCode = cancelCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
