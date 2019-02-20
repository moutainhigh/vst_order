package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdDepositRefundAudit;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 定金退改DAO
 * @author Administrator
 */
@Repository
public class OrdDepositRefundAuditDao extends MyBatisDao {

    public OrdDepositRefundAuditDao() {
        super("ORD_DEPOSIT_REFUND_AUDIT");
    }


    public List<OrdDepositRefundAudit> findList(OrdDepositRefundAudit param) {
        return super.queryForList("findList" , param);
    }

    public int findCount(OrdDepositRefundAudit param) {
        return super.get("findCount" , param);
    }

    public void save(OrdDepositRefundAudit audit) {
        super.insert("save" , audit);
    }

    public void update(OrdDepositRefundAudit audit) {
        super.update("update" , audit);
    }

    public void updateRefundFlag(Long orderId) {
        super.update("updateRefundFlag" , orderId);
    }

    public Long findRetreatAmountByOrderId(Long orderId) {
        return super.get("findRetreatAmountByOrderId" , orderId);
    }
}
