package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 拒单库服务(客服)
 */
@Service("rejectStatusService")
public class RejectStatusServiceImpl extends DefaultConfirmStatusService implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(RejectStatusServiceImpl.class);
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
        ResultHandleT<ComAudit> result =new ResultHandleT<ComAudit>();
        //加载参数
        OrdOrderItem orderItem =confirmStatusParamVo.getOrderItem();
        Confirm_Enum.CONFIRM_STATUS newStatus =confirmStatusParamVo.getNewStatus();
        String operator =confirmStatusParamVo.getOperator();
        if(orderItem ==null){
            result.setMsg("orderItem is null");
            return (T)result;
        }
        //成功
        if(Confirm_Enum.CONFIRM_STATUS.SUCCESS.name().equals(newStatus.name())){
            //更新子订单确认状态
            result = updateChildConfirmStatus(confirmStatusParamVo);
        }else{//拒绝
            //更新子订单确认状态
            result = updateChildConfirmStatus(confirmStatusParamVo);
            //创建活动
            if(result.isSuccess()){
                Confirm_Enum.CONFIRM_AUDIT_TYPE type = destOrderWorkflowService
                        .getAuditTypeByConfirmStatus(newStatus.name());
                Long auditId = destOrderWorkflowService.createTask(orderItem.getOrderId(), orderItem.getOrderItemId(), type, operator);

                LOG.info("orderItemId=" + orderItem.getOrderItemId()
                        + ",auditId=" +auditId);
            }
        }
        if (result.isFail()) {
            throw new BusinessException(result.getMsg());
        }
        return (T)result;
    }
}
