package com.lvmama.vst.order.confirm.ord;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;

/**
 * 订单状态同步服务(+流程)
 */
public interface IOrdStatusManageConfirmProcessService {
     /**
     * 更新资源状态(主单/子单)
     * @param objectId
     * @param objectType
     * @param resourceRetentionTime
     * @param assignor
     * @param memo
     * @return
     */
    public ResultHandle executeUpdateOrderResourceStatusAmple(Long objectId, OrderEnum.AUDIT_OBJECT_TYPE objectType
            , String resourceRetentionTime, String assignor, String memo);
    /**
     * 更新资源状态(主单/子单)-O2O
     * @param objectId
     * @param objectType
     * @param resourceRetentionTime
     * @param assignor
     * @param memo
     * @param auditId
     * @return
     * @throws
     */
    public ResultHandleT<ComAudit> executeUpdateOrderResourceStatusAmple_O2O(Long objectId, OrderEnum.AUDIT_OBJECT_TYPE objectType
            , String resourceRetentionTime, String assignor, String memo, Long auditId) throws Exception;

}
