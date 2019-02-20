package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdDepositRefundAudit;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_DEPOSIT_REFUND_AUDIT;
import com.lvmama.vst.order.dao.OrdDepositRefundAuditDao;
import com.lvmama.vst.order.service.OrdDepositRefundAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定金退改
 */
@Service
public class OrdDepositRefundAuditServiceImpl implements OrdDepositRefundAuditService {

    @Autowired
    private OrdDepositRefundAuditDao ordDepositRefundAuditDao;

    /**
     * 根据订单号申请类型获取所有申请
     *
     * @param orderId
     * @param applyType
     * @return
     */
    @Override
    public List<OrdDepositRefundAudit> findListByOrderId(Long orderId, ORD_DEPOSIT_REFUND_AUDIT applyType) {
        OrdDepositRefundAudit param = new OrdDepositRefundAudit();
        param.setOrderId(orderId);
        param.setApplyType(applyType.name());
        return ordDepositRefundAuditDao.findList(param);
    }

    /**
     * 条件统计
     *
     * @param audit
     * @return
     */
    @Override
    public int findCount(OrdDepositRefundAudit audit) {
        return ordDepositRefundAuditDao.findCount(audit);
    }

    /**
     * 提交申请
     *
     * @param audit
     */
    @Override
    public void commitAudit(OrdDepositRefundAudit audit) {
        ordDepositRefundAuditDao.save(audit);
    }

    /**
     * 更新审核
     *
     * @param audit
     */
    @Override
    public void updateAudit(OrdDepositRefundAudit audit) {
        ordDepositRefundAuditDao.update(audit);
    }

    /**
     * 更新退款标识(已审核通过的记录)
     *
     * @param orderId
     */
    @Override
    public void updateRefundFlag(Long orderId) {
        ordDepositRefundAuditDao.updateRefundFlag(orderId);
    }

    /**
     * 根据订单号获取可退金额
     * -- 查询可退金额条件
     * -- 1、审核状态通过（PASS）
     * -- 2、还未处理退款的（REFUND_FLAG = 'N'）
     *
     * @param orderId
     */
    @Override
    public Long findRetreatAmountByOrderId(Long orderId) {
        return ordDepositRefundAuditDao.findRetreatAmountByOrderId(orderId);
    }
}
