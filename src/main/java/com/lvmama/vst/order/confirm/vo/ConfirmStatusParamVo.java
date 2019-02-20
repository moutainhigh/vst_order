package com.lvmama.vst.order.confirm.vo;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;

/**
 * 确认状态参数vo
 */
public class ConfirmStatusParamVo {
    /**子订单*/
    private OrdOrderItem orderItem;
    /**确认状态*/
    private Confirm_Enum.CONFIRM_STATUS newStatus;
    /**确认渠道*/
    private EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel;
    private String operator;
    /**备注*/
    private String memo;
    /**支付等待时间*/
    private String resourceRetentionTime;

    /**客服处理*/
    private String supplierNo;/*酒店预定号*/
    private Long linkId;/**关联ID*/

    /*供应商处理*/
    private OrdOrder order;
    private EbkCertif ebkCertif;

    /*取消确认*/
    Long auditId;

    /*新单库*/
    private Confirm_Enum.CONFIRM_CHANNEL_OPERATE operate;

    public OrdOrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrdOrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public Confirm_Enum.CONFIRM_STATUS getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Confirm_Enum.CONFIRM_STATUS newStatus) {
        this.newStatus = newStatus;
    }

    public String getSupplierNo() {
        return supplierNo;
    }

    public void setSupplierNo(String supplierNo) {
        this.supplierNo = supplierNo;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    public String getResourceRetentionTime() {
        return resourceRetentionTime;
    }

    public void setResourceRetentionTime(String resourceRetentionTime) {
        this.resourceRetentionTime = resourceRetentionTime;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public OrdOrder getOrder() {
        return order;
    }

    public void setOrder(OrdOrder order) {
        this.order = order;
    }

    public EbkCertif getEbkCertif() {
        return ebkCertif;
    }

    public void setEbkCertif(EbkCertif ebkCertif) {
        this.ebkCertif = ebkCertif;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Confirm_Enum.CONFIRM_CHANNEL_OPERATE getOperate() {
        return operate;
    }

    public void setOperate(Confirm_Enum.CONFIRM_CHANNEL_OPERATE operate) {
        this.operate = operate;
    }

    public EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL getConfirmChannel() {
        return confirmChannel;
    }

    public void setConfirmChannel(EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) {
        this.confirmChannel = confirmChannel;
    }

    @Override
    public String toString() {
        return "ConfirmStatusParamVo{" +
                "orderItem=" + orderItem +
                ", newStatus=" + newStatus +
                ", operator='" + operator + '\'' +
                ", memo='" + memo + '\'' +
                ", resourceRetentionTime='" + resourceRetentionTime + '\'' +
                ", supplierNo='" + supplierNo + '\'' +
                ", linkId=" + linkId +
                ", order=" + order +
                ", ebkCertif=" + ebkCertif +
                ", auditId=" + auditId +
                ", operate=" + operate +
                '}';
    }
}
