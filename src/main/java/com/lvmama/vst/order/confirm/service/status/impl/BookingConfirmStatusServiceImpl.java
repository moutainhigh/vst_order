package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预订通知服务(客服)
 */
@Service("bookingConfirmStatusService")
public class BookingConfirmStatusServiceImpl implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(BookingConfirmStatusServiceImpl.class);
    @Autowired
    private IOrdStatusManageConfirmService ordStatusManageConfirmService;

    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception{
        LOG.info("updateChildConfirmAudit inParams = "+confirmStatusParamVo);
        //加载参数
        String operator =confirmStatusParamVo.getOperator();
        String memo =confirmStatusParamVo.getMemo();
        Long auditId = confirmStatusParamVo.getAuditId();

        ResultHandleT<ComAudit> result =new ResultHandleT<ComAudit>();
        result =ordStatusManageConfirmService.updateOrderConfirmAudit(auditId, operator, memo);

        LOG.info("auditId=" + auditId
                +",isSuccess=" + result.isSuccess());
        return (T)result;
    }

    @Override
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception {
        return null;
    }
}
