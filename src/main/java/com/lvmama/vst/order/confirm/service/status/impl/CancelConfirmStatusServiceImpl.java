package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 取消确认库服务
 */
@Service("cancelConfirmStatusService")
public class CancelConfirmStatusServiceImpl implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(CancelConfirmStatusServiceImpl.class);
    @Autowired
    private DestOrderWorkflowService destOrderWorkflowService;

    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo) throws Exception{
        ResultHandle result =new ResultHandle();
        Long auditId =confirmStatusParamVo.getAuditId();
        String operator =confirmStatusParamVo.getOperator();
        try {
            destOrderWorkflowService.completeTask(auditId, operator);

        } catch (Exception ex) {
            LOG.error("auditId=" + auditId
                    + ",Exception:" +ex);
            throw new BusinessException(ex.getMessage());
        }
        return (T)result;
    }

    @Override
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception {
        return null;
    }
}
