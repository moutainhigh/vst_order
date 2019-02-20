package com.lvmama.vst.order.confirm.web;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 确认节点卡单手动补偿action
 */
@Controller
@RequestMapping("/ord/order/confirm/")
public class OrderConfirmTaskFinishAction extends BaseActionSupport {
    private static final Log LOG = LogFactory.getLog(OrderConfirmTaskFinishAction.class);
    @Autowired
    protected IComplexQueryService complexQueryService;
    @Autowired
    private IOrderUpdateService orderUpdateService;
    @Autowired
    private IOrdItemConfirmProcessService ordItemConfirmProcessService;
    @Autowired
    private IOrderAuditService orderAuditService;
    @Autowired
    private ProcesserClientService processerClientService;
    /**
     * 确认流程手动补偿-audit
     */
    @RequestMapping(value = "/completeAudit")
    @ResponseBody
    public Object completeAudit(HttpServletRequest request, Long orderItemId, Long auditId) {
        ResultHandle resultHandle =checkAudit(request, auditId);
        if(resultHandle.isFail()){
            return resultHandle.getMsg();
        }
        ComAudit audit = orderAuditService.queryAuditById(auditId);
        if (orderItemId != null) {
            OrdOrderItem mainOrderItem = orderUpdateService.getOrderItem(orderItemId);
            ordItemConfirmProcessService.completeAuditTaskByConfirm(mainOrderItem, audit);
        }
        return "successful";
    }
    /**
     * 确认流程手动补偿-userTask
     */
    @RequestMapping(value = "/completeUserTask")
    @ResponseBody
    public Object completeUserTask(HttpServletRequest request, Long orderId, Long orderItemId, String userTask) {
        ResultHandle resultHandle =checkUser(request);
        if(resultHandle.isFail()){
            return resultHandle.getMsg();
        }
        String operateName = getLoginUserId();
        if (orderItemId != null) {
            OrdOrderItem mainOrderItem = orderUpdateService.getOrderItem(orderItemId);
            ordItemConfirmProcessService.completeUserTaskByConfirm(mainOrderItem, userTask, operateName);

        }
        return "successful";
    }
    /**
     * 确认流程手动补偿-audit-o2o
     */
    @RequestMapping(value = "/o2o/completeAudit")
    @ResponseBody
    public Object completeAudit_o2o(HttpServletRequest request, Long orderItemId, Long auditId) {
        ResultHandle resultHandle =checkAudit(request, auditId);
        if(resultHandle.isFail()){
            return resultHandle.getMsg();
        }
        ComAudit audit = orderAuditService.queryAuditById(auditId);
        if (orderItemId != null) {
            OrdOrderItem mainOrderItem = orderUpdateService.getOrderItem(orderItemId);
            ordItemConfirmProcessService.completeAuditTask_O2O(mainOrderItem, audit);
        }
        return "successful";
    }
    /**
     * 确认流程手动补偿-userTask-o2o
     */
    @RequestMapping(value = "/o2o/completeUserTask")
    @ResponseBody
    public Object completeUserTask_o2o(HttpServletRequest request, Long orderId, Long orderItemId, String userTask) {
        ResultHandle resultHandle =checkUser(request);
        if(resultHandle.isFail()){
            return resultHandle.getMsg();
        }
        String operateName = getLoginUserId();
        if (orderId != null) {
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            if(OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)){
                ordItemConfirmProcessService.completeUserTask_O2O(order, userTask, operateName);
            }else {
                return "it does not have a workflow.";
            }
        }
        return "successful";
    }
    /**
     * checkAudit
     * @param request
     * @param auditId
     * @return
     */
    private ResultHandle checkAudit(HttpServletRequest request, Long auditId){
        ResultHandle resultHandle =checkUser(request);
        if(resultHandle.isFail()){
            return resultHandle;
        }
        if(auditId == null) {
            resultHandle.setMsg("start complete task, auditId:" + auditId);
            return resultHandle;
        }
        return resultHandle;
    }
    /**
     * checkUser
     * @param request
     * @return
     */
    private ResultHandle checkUser(HttpServletRequest request){
        ResultHandle resultHandle =new ResultHandle();
        if(!checkUrlValid(request.getParameter("code"))){
            resultHandle.setMsg("连接非法");
            return resultHandle;
        }
//        String[] authUsers = new String[]{"lv6800", "admin"};
//        if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
//            resultHandle.setMsg("权限不足");
//            return resultHandle;
//        }
        return resultHandle;
    }
    private boolean checkUrlValid(String code){
        if(code == null){
            return false;
        }

        try{
            code = DESCoder.decrypt(code);
        }catch(Exception e){
            log.info(e);
        }

        String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());

        if(today.equalsIgnoreCase(code)){
            return true;
        }
        return false;
    }
}
