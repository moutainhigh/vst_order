package com.lvmama.vst.order.vo;

import java.io.Serializable;

import com.lvmama.vst.comm.utils.DateUtil;

public class OrdOrderMarkVo implements Serializable {
    private static final long serialVersionUID = 1872322941169979530L;
    /**
     * 订单验颜色标记(1：已搬单；2：未搬单)
     */
    private Integer markFlag;
    /**
     * 订单来源,下单渠道
     */
    private Long distributorId;
    /**
     * 订单来源,下单渠道
     */
    private String distributorName;
    /**
     * 订单号
     */
    private Long orderId;
    /**
     * 品类ID
     */
    private Long categoryId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 支付方式
     */
    private String payTarget;
    /**
     * 购买数量
     */
    private Integer buyCount;
    /**
     * 下单时间
     */
    private String createTime;
    /**
     * 入住时间，时间区段
     */
    private String visitTime;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 当前状态
     */
    private String currentStatus;
    /**
     * 所属BU
     */
    private String buCode;
    /**
     * 产品经理ID
     */
    private Long managerId;
    /**
     * 下单时间开始
     */
    private String createTimeBegin;
    /**
     * 下单时间结束
     */
    private String createTimeEnd;
    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 支付状态
     */
    private String paymentStatus;
    /**
     * 支付方式
     */
    private String paymentTarget;

    /**
     * 信息状态
     */
    private String infoStatus;
    /**
     * 资源状态
     */
    private String resourceStatus;
    /**
     * 凭证确认状态
     */
    private String certConfirmStatus;
    /**
     * 出团通知书状态
     */
    private String noticeRegimentStatus;
    /**
     * 是否需要担保
     */
    private String guarantee;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPayTarget() {
        return payTarget;
    }

    public void setPayTarget(String payTarget) {
        this.payTarget = payTarget;
    }

    public Integer getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(Integer buyCount) {
        this.buyCount = buyCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getMarkFlag() {
        return markFlag;
    }

    public void setMarkFlag(Integer markFlag) {
        this.markFlag = markFlag;
    }

    public String getBuCode() {
        return buCode;
    }

    public void setBuCode(String buCode) {
        this.buCode = buCode;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getCreateTimeBegin() {
        return createTimeBegin;
    }
    
    public void setCreateTimeBegin(String createTimeBegin) {
        this.createTimeBegin = createTimeBegin;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }
    
    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentTarget() {
        return paymentTarget;
    }

    public void setPaymentTarget(String paymentTarget) {
        this.paymentTarget = paymentTarget;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getInfoStatus() {
        return infoStatus;
    }

    public void setInfoStatus(String infoStatus) {
        this.infoStatus = infoStatus;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getCertConfirmStatus() {
        return certConfirmStatus;
    }

    public void setCertConfirmStatus(String certConfirmStatus) {
        this.certConfirmStatus = certConfirmStatus;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getNoticeRegimentStatus() {
        return noticeRegimentStatus;
    }

    public void setNoticeRegimentStatus(String noticeRegimentStatus) {
        this.noticeRegimentStatus = noticeRegimentStatus;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public String getGuarantee() {
        return guarantee;
    }

    public void setGuarantee(String guarantee) {
        this.guarantee = guarantee;
    }
}
