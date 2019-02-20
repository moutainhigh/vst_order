package com.lvmama.vst.order.confirm.ord.impl;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmProcessService;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单状态同步服务实现remote
 */
@Component("ordStatusManageConfirmProcessService")
public class OrdStatusManageConfirmProcessServiceImpl implements IOrdStatusManageConfirmProcessService {
    private static final Logger LOG = LoggerFactory.getLogger(OrdStatusManageConfirmProcessServiceImpl.class);
    @Autowired
    private IOrdStatusManageConfirmService ordStatusManageConfirmService;
    @Autowired
    protected IOrderUpdateService orderUpdateService;
    @Autowired
    private IOrderLocalService orderLocalService;
    @Autowired
    private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;
    @Autowired
    private IOrdItemConfirmProcessService ordItemConfirmProcessService;

    @Override
    public ResultHandle executeUpdateOrderResourceStatusAmple(Long objectId, OrderEnum.AUDIT_OBJECT_TYPE objectType
            , String resourceRetentionTime, String assignor, String memo) {
        ResultHandle resultHandle = null;
        if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
            //更新主订单资审状态
            resultHandle = ordStatusManageConfirmService.updateResourceStatus(objectId, OrderEnum.RESOURCE_STATUS.AMPLE.name()
                    , resourceRetentionTime, assignor, memo);
            if (resultHandle.isSuccess()) {
                prepayAmpleVstByOrder(objectId);
            }
        }else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
            //更新子订单资审状态
            resultHandle = ordStatusManageConfirmService.updateChildResourceStatus(objectId
                    , OrderEnum.RESOURCE_STATUS.AMPLE.name(), resourceRetentionTime, assignor, memo);
            if(resultHandle.isSuccess()){
                doAmpleVstByItem(objectId);
            }

        }
        return resultHandle;
    }
    @Override
    public ResultHandleT<ComAudit>  executeUpdateOrderResourceStatusAmple_O2O(Long objectId, OrderEnum.AUDIT_OBJECT_TYPE objectType
            , String resourceRetentionTime, String assignor, String memo,Long auditId) throws Exception{
        ResultHandleT<ComAudit> resultHandle = new ResultHandleT<ComAudit>();

        if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
            resultHandle.setMsg("only executeUpdateOrderResourceStatusAmple_O2O by item");
            return resultHandle;

        }else if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
            //更新
            resultHandle =updateItemdResource(objectId, resourceRetentionTime, assignor, memo,auditId);
            if(resultHandle.isFail()){
                return resultHandle;
            }
            //触发流程
            if (resultHandle.isSuccess() && resultHandle.getReturnContent() !=null) {
                OrdOrderItem ordOrderItem = orderLocalService.getOrderItem(objectId);
                ordItemConfirmProcessService.completeAuditTask_O2O(ordOrderItem, resultHandle.getReturnContent());
            }
            doAmpleVstByItem(objectId);
        }
        return resultHandle;


    }
    /**
     * 更新子单资审状态
     * @return
     * @throws Exception
     */
    private ResultHandleT<ComAudit> updateItemdResource(Long objectId, String resourceRetentionTime
            , String assignor, String memo,Long auditId) throws Exception{
        ResultHandleT<ComAudit> resultHandle = new ResultHandleT<ComAudit>();
        //更新子单资审状态
        ResultHandle result = ordStatusManageConfirmService.updateChildResourceStatus(objectId
                , OrderEnum.RESOURCE_STATUS.AMPLE.name(), resourceRetentionTime, assignor, memo);
        if(result.isFail()){
            resultHandle.setMsg(result.getMsg());
            return resultHandle;
        }
        //更新活动
        resultHandle =ordStatusManageConfirmService.updateChildConfirmAudit(auditId, assignor);
        if(resultHandle.isFail()){
            throw new Exception(resultHandle.getMsg());
        }
        return resultHandle;
    }
    /**
     * 发送消息
     * @param orderId
     * @param order
     */
    private void prepayAmpleVst(Long orderId, OrdOrder order) {
        if(order.hasNeedPrepaid()){
            if(order.getOughtAmount()==0){
                ordPrePayServiceAdapter.vstOrder0YuanPayMsg(orderId);
            }else if(order.hasPayed()){
                ordPrePayServiceAdapter.resourceAmpleVst(orderId);
            }
        }
    }
    private ActivitiKey createKeyByOrder(OrdOrder order){
        ComActivitiRelation relation = orderLocalService.getRelation(order);
        LOG.info("createKeyByOrder order.orderid="+order.getOrderId());
        return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
    }
    /**
     * 资审通过同步vst(order)
     * @param objectId
     */
    private void prepayAmpleVstByOrder(Long objectId) {
        LOG.info("resource ample, order id:" + objectId);
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(objectId);
        //发送资源审核消息
        orderLocalService.sendResourceStatusAmpleMsg(objectId);
        //扣款申请
        prepayAmpleVst(objectId, order);
    }
    /**
     * 资审通过同步vst(item)
     * @param objectId
     */
    private void doAmpleVstByItem(Long objectId) {
        OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(objectId);
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
        LOG.info("resource ample, order id:" + order.getOrderId()
                +",hasResourceAmple:"+order.hasResourceAmple()+",objectId"+objectId);

        if (order.hasResourceAmple()) {
            //发送资源审核消息
            orderLocalService.sendResourceStatusAmpleMsg(order.getOrderId());
            //扣款申请
            prepayAmpleVst(order.getOrderId(), order);
        }

    }

}
