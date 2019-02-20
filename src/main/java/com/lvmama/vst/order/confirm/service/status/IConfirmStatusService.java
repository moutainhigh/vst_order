package com.lvmama.vst.order.confirm.service.status;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;

/**
 * 确认状态服务接口
 */
public interface IConfirmStatusService  {
    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception;

    /**
     * 更新子订单确认状态
     * @param confirmStatusParamVo
     * @return
     * @throws Exception
     */
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception;

}
