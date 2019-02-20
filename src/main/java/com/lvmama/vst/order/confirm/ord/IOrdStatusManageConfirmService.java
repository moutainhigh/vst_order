package com.lvmama.vst.order.confirm.ord;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;

/**
 * 订单状态同步服务
 */
public interface IOrdStatusManageConfirmService {
    /**
     * 子订单状态(老版工作流)
     */
    /**
     * 更新资源状态(子单)
     * @param orderItemId
     * @param newStatus
     * @param resourceRetentionTime
     * @param assignor
     * @param memo
     * @return
     */
    public ResultHandle updateChildResourceStatus(Long orderItemId, String newStatus, String resourceRetentionTime, String assignor, String memo);

    /**
     * 更新资源状态(主单)
     * @param orderId
     * @param newStatus
     * @param resourceRetentionTime
     * @param assignor
     * @param memo
     * @return
     */
    public ResultHandle updateResourceStatus(Long orderId, String newStatus, String resourceRetentionTime, String assignor, String memo);

    /**
     * 子订单状态(新版工作流)
     */
    /**
     * 更新子订单确认状态&活动
     * @param orderItem 子订单
     * @param newStatus 需要更新的确认状态
     * @param operator 操作人
     * @param memo 备注
     * @return ComAudit
     */
    public ResultHandleT<ComAudit> updateChildConfirmStatusByAudit(OrdOrderItem orderItem, Confirm_Enum.CONFIRM_STATUS newStatus
            , String operator, String memo);

    /**
     * 更新子订单确认活动
     * @param auditId
     * @param operator
     * @return
     */
    public ResultHandleT<ComAudit> updateChildConfirmAudit(Long auditId, String operator);

    /**
     * 更新子订单确认活动
     * @param auditId
     * @param operator
     * @return
     */
    public ResultHandleT<ComAudit> updateOrderConfirmAudit(Long auditId, String operator, String memo);

}
