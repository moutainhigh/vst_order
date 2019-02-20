package com.lvmama.vst.order.confirm.service.status;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 确认状态代理服务
 */
@Service("confirmStatusManagerProxy")
public class ConfirmStatusManagerProxy {
    @Autowired
    private ConfirmStatusManagerService confirmStatusManagerService;

    /**
     * 更新子订单确认状态
     * @param confirmStatusService
     * @param paramVo
     * @return
     * @throws Exception
     */
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE confirmStatusService
            ,ConfirmStatusParamVo paramVo)
            throws Exception{
        return getService(confirmStatusService).updateChildConfirmStatus(paramVo);
    }

    /**
     * 更新子订单确认状态
     * @param confirmStatusService
     * @param paramVo
     * @return
     * @throws Exception
     */
    public ResultHandleT<ComAudit> updateOrderConfirmAudit(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE confirmStatusService, ConfirmStatusParamVo paramVo) throws Exception{
        return getService(confirmStatusService).handle(paramVo);
    }

    /**
     * 工作台处理
     * @param confirmStatusService
     * @param paramVo
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE confirmStatusService
            ,ConfirmStatusParamVo paramVo)throws Exception{
        return getService(confirmStatusService).handle(paramVo);
    }

    //initParam

    /**
     * 客服处理
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(OrdOrderItem orderItem
            , Confirm_Enum.CONFIRM_STATUS newStatus, String operator, String memo){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setOrderItem(orderItem);
        paramVo.setNewStatus(newStatus);
        paramVo.setOperator(operator);
        paramVo.setMemo(memo);

        return paramVo;
    }
    /**
     * 供应商处理
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(OrdOrder order, EbkCertif ebkCertif){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setOrder(order);
        paramVo.setEbkCertif(ebkCertif);
        return paramVo;
    }
    /**
     * 新单库
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(OrdOrderItem orderItem
            , Confirm_Enum.CONFIRM_CHANNEL_OPERATE operate, String operator){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setOrderItem(orderItem);
        paramVo.setOperate(operate);
        paramVo.setOperator(operator);
        return paramVo;
    }
    /**
     * 取消确认
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(Long auditId, String operator){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setAuditId(auditId);
        paramVo.setOperator(operator);

        return paramVo;
    }


    /**
     * 询位处理
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(OrdOrderItem orderItem
            , Long auditId, String operator, String memo){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setOrderItem(orderItem);
        paramVo.setAuditId(auditId);
        paramVo.setOperator(operator);
        paramVo.setMemo(memo);


        return paramVo;
    }
    /**
     * 询位处理
     * @return ConfirmStatusParamVo
     */
    public ConfirmStatusParamVo initParam(OrdOrderItem orderItem
            , Long auditId, String operator, String memo,String resourceRetentionTime){
        ConfirmStatusParamVo paramVo =new ConfirmStatusParamVo();
        paramVo.setOrderItem(orderItem);
        paramVo.setAuditId(auditId);
        paramVo.setOperator(operator);
        paramVo.setMemo(memo);
        paramVo.setResourceRetentionTime(resourceRetentionTime);

        return paramVo;
    }
    /**
     * 获取服务
     * @param confirmStatusService
     * @return
     * @throws RuntimeException
     */
    private IConfirmStatusService getService(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE confirmStatusService)
            throws RuntimeException{
        IConfirmStatusService service =confirmStatusManagerService.getService(confirmStatusService);
        if(service ==null){
            throw new RuntimeException("getService() null");
        }
        return service;
    }

}
