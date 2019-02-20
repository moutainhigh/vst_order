package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdDepositRefundAudit;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_DEPOSIT_REFUND_AUDIT;

import java.util.List;

/**
 * 定金退改
 * @author Administrator
 */
public interface OrdDepositRefundAuditService {


    /**
     * 根据订单号申请类型获取所有申请
     * @param orderId
     * @param applyType
     * @return
     */
    List<OrdDepositRefundAudit> findListByOrderId(Long orderId, ORD_DEPOSIT_REFUND_AUDIT applyType);

    /**
     * 条件统计
     * @param audit
     * @return
     */
    int findCount(OrdDepositRefundAudit audit);

    /**
     * 提交申请
     * @param audit
     */
    void commitAudit(OrdDepositRefundAudit audit);

    /**
     * 更新审核
     * @param audit
     */
    void updateAudit(OrdDepositRefundAudit audit);


    /**
     * 更新退款标识(已审核通过的记录)
     * @param orderId
     */
    void updateRefundFlag(Long orderId);


    /**
     * 根据订单号获取可退金额
     * -- 查询可退金额条件
     *   -- 1、审核状态通过（PASS）
     *   -- 2、还未处理退款的（REFUND_FLAG = 'N'）
     */
    Long findRetreatAmountByOrderId(Long orderId);

}
