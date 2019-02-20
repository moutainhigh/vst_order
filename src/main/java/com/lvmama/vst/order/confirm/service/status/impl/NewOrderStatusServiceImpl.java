package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.vo.AuditActiviTask;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.vo.ConfirmParamVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.client.service.ConfirmAdapterClientService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;

import com.lvmama.vst.order.service.IOrdOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 新单库服务
 */
@Service("newOrderStatusService")
public class NewOrderStatusServiceImpl implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(NewOrderStatusServiceImpl.class);
    @Autowired
    private IOrdItemConfirmProcessService ordItemConfirmProcessService;
	@Autowired
	private ConfirmAdapterClientService confirmAdapterServiceRemote;
    @Autowired
    private IOrdOrderService ordOrderService;
    @Autowired
    private IApiOrderWorkflowService apiOrderWorkflowService;

    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo) throws Exception{
        ResultHandle result =new ResultHandle();
        //加载参数
        OrdOrderItem orderItem = confirmStatusParamVo.getOrderItem();

        //构造参数
        ConfirmParamVo confirmParamVo =new ConfirmParamVo();
        confirmParamVo.setOrderItem(orderItem);
        confirmParamVo.setChannelOperate(confirmStatusParamVo.getOperate());
        confirmParamVo.setCount(1);//确认1次
        //发送通知确认
        ResultHandleT<ComAudit> resultHandle =confirmAdapterServiceRemote.createConfirmOrder(confirmParamVo);
        LOG.info("orderItemId=" +orderItem.getOrderItemId()
                +",isSuccess=" +resultHandle.isSuccess()
                +",msg=" +resultHandle.getMsg());

        if(resultHandle.isSuccess()){
            if(resultHandle.getReturnContent() !=null){
                //触发流程
                Long orderId=orderItem.getOrderId();
                OrdOrder ordOrder=ordOrderService.findByOrderId(orderId);
                Long categoryId=ordOrder.getCategoryId()==null?-1L:ordOrder.getCategoryId();
                String newWorkFlowFlag=ordOrder.getNewWorkflowFlag();
                LOG.info("workbenchHandle|orderItemId="+orderItem.getOrderItemId()+",newWorkFlowFlag="+newWorkFlowFlag+",categoryId="+categoryId);
                //调用新版工作流
                if(("Y".equalsIgnoreCase(newWorkFlowFlag)||"S".equalsIgnoreCase(newWorkFlowFlag))&&categoryId.equals(1L)){
                    ComAudit comAudit=resultHandle.getReturnContent();
                    com.lvmama.order.api.base.vo.RequestBody<AuditActiviTask> request=new com.lvmama.order.api.base.vo.RequestBody<>();
                    AuditActiviTask auditActiviTask=new AuditActiviTask();
                    EnhanceBeanUtils.copyProperties(comAudit, auditActiviTask);
                    auditActiviTask.setOrderId(orderId);
                    request.setT(auditActiviTask);
                    com.lvmama.order.api.base.vo.ResponseBody<String> responseBody= apiOrderWorkflowService.completeTaskByAudit(request);
                    if(null!=responseBody&&responseBody.isSuccess()){
                        LOG.info("老工单完成新工作流成功:orderId={},orderItemId={}",orderId,orderItem);
                    }else{
                        LOG.info("老工单完成新工作流失败:orderId={},orderItemId={},msg={}",orderId,orderItem,responseBody.getErrorMessage());
                    }
                }else {
                    ordItemConfirmProcessService.completeTaskByAuditHasCompensated(orderItem, resultHandle.getReturnContent());
                }
            }
        }else{
            throw new BusinessException(result.getMsg());
        }
        return (T)result;
    }

    @Override
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception {
        return null;
    }
}
