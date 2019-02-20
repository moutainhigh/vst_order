package com.lvmama.vst.order.route.po;

import java.io.Serializable;
import java.util.Date;

public class OrderRouteRelationInfo implements Serializable {
    private Long orderRouteRelationId;

    private Long orderId;

    private Long orderItemId;

    private Long orderCategoryId;

    private Long orderItemCategoryId;

    private Long distributorId;

    private String distributorCode;

    private char isTestOrder;

    private char validFlag;

    private Date createTime;

    private Date updateTime;

    public OrderRouteRelationInfo() {
    }

    public OrderRouteRelationInfo(Long orderId, Long orderItemId, Long orderCategoryId, Long orderItemCategoryId, Long distributorId, String distributorCode, char isTestOrder, char validFlag, Date createTime, Date updateTime) {
        this.orderRouteRelationId = orderRouteRelationId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.orderCategoryId = orderCategoryId;
        this.orderItemCategoryId = orderItemCategoryId;
        this.distributorId = distributorId;
        this.distributorCode = distributorCode;
        this.isTestOrder = isTestOrder;
        this.validFlag = validFlag;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getOrderRouteRelationId() {
        return orderRouteRelationId;
    }

    public void setOrderRouteRelationId(Long orderRouteRelationId) {
        this.orderRouteRelationId = orderRouteRelationId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderCategoryId() {
        return orderCategoryId;
    }

    public void setOrderCategoryId(Long orderCategoryId) {
        this.orderCategoryId = orderCategoryId;
    }

    public Long getOrderItemCategoryId() {
        return orderItemCategoryId;
    }

    public void setOrderItemCategoryId(Long orderItemCategoryId) {
        this.orderItemCategoryId = orderItemCategoryId;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorCode() {
        return distributorCode;
    }

    public void setDistributorCode(String distributorCode) {
        this.distributorCode = distributorCode;
    }

    public char getIsTestOrder() {
        return isTestOrder;
    }

    public void setIsTestOrder(char isTestOrder) {
        this.isTestOrder = isTestOrder;
    }

    public char getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(char validFlag) {
        this.validFlag = validFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
