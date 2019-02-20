package com.lvmama.vst.order.confirm.ord.impl;

import com.lvmama.vst.back.order.po.Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmService;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderStatusManageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 订单状态同步服务实现local
 */
@Service("orderStatusManageConfirmService")
public class OrdStatusManageConfirmServiceImpl implements IOrdStatusManageConfirmService {
    private static final Logger LOG = LoggerFactory.getLogger(OrdStatusManageConfirmServiceImpl.class);
    @Autowired
    private IOrderAuditService orderAuditService;
    @Autowired
    private IOrderStatusManageService orderStatusManageService;
    @Autowired
    private OrdOrderItemDao ordOrderItemDao;
    @Autowired
    private OrdOrderDao ordOrderDao;
    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Override
    public ResultHandle updateChildResourceStatus(Long orderItemId, String newStatus
            , String resourceRetentionTime, String assignor, String memo) {
        return orderStatusManageService.updateChildResourceStatus(orderItemId, newStatus
                , resourceRetentionTime, assignor, memo, null, false);
    }
    @Override
    public ResultHandle updateResourceStatus(Long orderId, String newStatus
            , String resourceRetentionTime, String assignor, String memo) {
        return orderStatusManageService.updateResourceStatus(orderId, newStatus
                , resourceRetentionTime, assignor, memo);
    }

    @Override
    public ResultHandleT<ComAudit> updateChildConfirmStatusByAudit(OrdOrderItem orderItem, Confirm_Enum.CONFIRM_STATUS newStatus
            ,String operator,String memo){
        ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
        //更新子订单确认状态
        OrdOrderItem orderItemObj=new OrdOrderItem();
        orderItemObj.setOrderItemId(orderItem.getOrderItemId());
        orderItemObj.setConfirmStatus(newStatus.name());
        orderItemObj.setOrderMemo(memo);
        int n=ordOrderItemDao.updateByPrimaryKeySelective(orderItemObj);
        if (n!=1 ) {
            result.setMsg("子订单确认状态更新失败");
            return result;
        }
        //更新子订单确认活动
        result =updateChildConfirmAudit(orderItemObj, operator, memo);
        //插入日志
        if (result.isSuccess()) {
            insertChildConfirmStatusLog(orderItem.getOrderId(),orderItem.getOrderItemId()
                    , newStatus.name(), operator,memo);
        }else{
            result.setMsg("子订单活动更新失败");
        }
        return result;
    }
    @Override
    public ResultHandleT<ComAudit> updateChildConfirmAudit(Long auditId,String operator) {
        ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
        ComAudit audit = orderAuditService.queryAuditById(auditId);
        if(audit ==null || OrderEnum.AUDIT_STATUS.PROCESSED.name().equalsIgnoreCase(audit.getAuditStatus())){
            result.setMsg("audit ==null or audit is processed,auditId:" +auditId);
            return result;
        }
        result.setReturnContent(audit);

        //更新子订单确认活动
        audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
        audit.setUpdateTime(new Date());
        if(StringUtils.isEmpty(audit.getOperatorName())){
            audit.setOperatorName(operator);
        }
        audit.setAuditFlag("SYSTEM");// 标记为系统自动过
        int rs=orderAuditService.updateByPrimaryKey(audit);
        if(rs >0){
            //插入日志
            ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
            if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
                objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
            }
            lvmmLogClientService.sendLog(objectType,
                    audit.getObjectId(),
                    audit.getObjectId(),
                    operator,
                    "将编号为[" +audit.getObjectId() +"]的订单活动变更[" + Confirm_Enum.CONFIRM_AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName() +"通过]",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+ Confirm_Enum.CONFIRM_AUDIT_TYPE.valueOf(audit.getAuditType()).getCnName() +"通过]",
                    "");
        }else{
            result.setMsg("子订单确认活动更新失败");
        }
        return result;
    }

    @Override
    public ResultHandleT<ComAudit> updateOrderConfirmAudit(Long auditId,String operator, String memo) {
        ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
        ComAudit audit = orderAuditService.queryAuditById(auditId);
        if(audit ==null || OrderEnum.AUDIT_STATUS.PROCESSED.name().equalsIgnoreCase(audit.getAuditStatus())){
            result.setMsg("audit ==null or audit is processed,auditId:" +auditId);
            return result;
        }
        result.setReturnContent(audit);

        updateOrderMemo(memo, audit);

        //更新子订单确认活动为已处理
        audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
        audit.setUpdateTime(new Date());
        if(StringUtils.isEmpty(audit.getOperatorName())){
            audit.setOperatorName(operator);
        }
        audit.setAuditFlag("SYSTEM");// 标记为系统自动过
        int rs=orderAuditService.updateByPrimaryKey(audit);
        if(rs >0){
            //插入日志
            Long parentId = audit.getObjectId();
            ComLog.COM_LOG_OBJECT_TYPE objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
            if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
                objectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
                OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(audit.getObjectId());
                if (orderItem != null) {
                    parentId = orderItem.getOrderId();
                }
            }
            lvmmLogClientService.sendLog(objectType,
                    parentId,
                    audit.getObjectId(),
                    operator,
                    "将编号为[" +audit.getObjectId() +"]的订单活动变更[" + ConfirmEnumUtils.getCnName(audit.getAuditType()) +"通过]",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+ ConfirmEnumUtils.getCnName(audit.getAuditType()) +"通过]",
                    "");
        }else{
            result.setMsg("子订单确认活动更新失败");
        }
        return result;
    }

    private void updateOrderMemo(String memo, ComAudit audit) {
        if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(audit.getObjectType())) {
            //更新子订单确认状态
            OrdOrderItem orderItemObj=new OrdOrderItem();
            orderItemObj.setOrderItemId(audit.getObjectId());
            orderItemObj.setOrderMemo(memo);
            int n=ordOrderItemDao.updateByPrimaryKeySelective(orderItemObj);
            if (n!=1 ) {
                LOG.info("子订单备注更新失败，orderItemId="+audit.getObjectId());
            }
        } else {
            //更新主单备注信息
            OrdOrder orderObj = new OrdOrder();
            orderObj.setOrderId(audit.getObjectId());
            orderObj.setOrderMemo(memo);
            int n = ordOrderDao.updateByPrimaryKeySelective(orderObj);
            if (n!=1 ) {
                LOG.info("主单备注更新失败，orderId="+audit.getObjectId());
            }
        }
    }

    /**
     * 更新子订单确认活动
     * @param orderItem
     * @param operator
     * @param memo
     * @return
     */
    private ResultHandleT<ComAudit> updateChildConfirmAudit(OrdOrderItem orderItem
            ,String operator,String memo) {
        ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
        //更新子订单确认活动
        Map<String,Object> params= new HashMap<String, Object>();
        params.put("objectId", orderItem.getOrderItemId());
        params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
        params.put("auditStatusArray", Arrays.asList(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()
                ,OrderEnum.AUDIT_STATUS.POOL.name()));
        List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
        if(!auditList.isEmpty()){
            for(ComAudit audit : auditList){
                //过滤预定通知,避免获取非新版流程节点活动,造成卡单
                if(OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equalsIgnoreCase(audit.getAuditType())
                		||CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name().equals(audit.getAuditType())){
                    continue;
                }
                if("SYSTEM".equals(operator) && !StringUtils.isEmpty(audit.getOperatorName())){
                    audit.setOperatorName(audit.getOperatorName());
                }else{
                    audit.setOperatorName(operator);
                }
                audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
                audit.setUpdateTime(new Date());
                orderAuditService.updateByPrimaryKey(audit);
                result.setReturnContent(audit);
            }
            
        }
        //插入日志
        if (result.isSuccess()
                && result.getReturnContent() !=null) {
            insertChildConfirmAuditTypeLog(orderItem.getOrderId(),orderItem.getOrderItemId()
                    , result.getReturnContent().getAuditType(), operator,memo);
        }
        return result;
    }
    /**
     * 插入日志
     * @param orderId
     * @param orderItemId
     * @param confirmStatus
     * @param assignor
     * @param memo
     */
    private void insertChildConfirmStatusLog(final Long orderId,final Long orderItemId,String confirmStatus,String assignor,String memo){
        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                orderId,
                orderItemId,
                assignor,
                "将编号为["+orderItemId+"]的子订单确认状态变更["+ Confirm_Enum.CONFIRM_STATUS.getCnName(confirmStatus)+"]",
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
                        +"["+ Confirm_Enum.CONFIRM_STATUS.getCnName(confirmStatus)+"更改]",
                memo);
    }

    private void insertChildConfirmAuditTypeLog(final Long orderId,final Long orderItemId,String auditType,String assignor,String memo){
        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                orderId,
                orderItemId,
                assignor,
                "将编号为["+orderItemId+"]的子订单活动变更["
                        +Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(auditType)+"通过]",
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
                        +"["+Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(auditType)+"通过]",
                memo);
    }
}
