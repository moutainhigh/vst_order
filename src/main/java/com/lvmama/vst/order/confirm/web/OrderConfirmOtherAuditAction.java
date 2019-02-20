package com.lvmama.vst.order.confirm.web;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 新版工作台-其它预订通知-action
 */
@Controller
@RequestMapping("/ord/order/confirm/")
public class OrderConfirmOtherAuditAction extends BaseActionSupport {

    private static final Log LOG = LogFactory.getLog(OrderConfirmOtherAuditAction.class);
    //日志记录器
    private static final Log LOGGER = LogFactory.getLog(OrderConfirmOtherAuditAction.class);

    @Autowired
    private IOrderAuditService orderAuditService;

    @Autowired
    private IOrdItemConfirmStatusService ordItemConfirmStatusService;

    @Autowired
    private IComMessageService comMessageService;

    @Autowired
    private IOrdOrderService ordOrderService;

    @Autowired
    private IOrdOrderItemService ordOrderItemService;

    //订单综合查询
    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private LvmmLogClientService lvmmLogClientService;
    
    /**
     * 其它预订通知-审核通过
     * @param request
     * @param orderItemId
     * @param auditId
     * @return
     */
    @RequestMapping(value = "/orderPassOtherAudit")
    @ResponseBody
    public Object orderPassOtherAudit(HttpServletRequest request, Long orderItemId, Long auditId, String orderMemo) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit orderItemId:" + orderItemId + ", orderMemo="+orderMemo);
            if (orderItemId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit orderItemId or confirmStatus is null");
                return msg;
            }
            LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit orderItemId:" + orderItemId + ", auditId="+auditId);
            if (auditId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit auditId is null");
                return msg;
            }
            OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
            if (ordOrderItem == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit ordOrderItem is null orderItemId:" + orderItemId);
                return msg;
            }
            LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
            if (StringUtil.isEmptyString(ordOrderItem.getConfirmStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录中状态为空");
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit OrdOrderItem ConfirmStatus is null orderItemId:" + orderItemId);
                return msg;
            }
            ComAudit audit = orderAuditService.queryAuditById(auditId);
            if (audit == null || OrderEnum.AUDIT_STATUS.PROCESSED.name().equalsIgnoreCase(audit.getAuditStatus())) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在或已审核");
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit audit is null :" + auditId);
                return msg;
            }

            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = " ";
            }
            ResultHandleT<ComAudit> comAuditResultHandleT = ordItemConfirmStatusService.updateOrderConfirmAudit(auditId, operateName, orderMemo);
            if (comAuditResultHandleT.isFail()) {
                LOGGER.info("OrderConfirmOtherAuditAction orderPassOtherAudit update audit error auditId:" + auditId);
            }

            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    ordOrderItem.getOrderId(),
                    ordOrderItem.getOrderItemId(),
                    operateName,
                    "将编号为[" + ordOrderItem.getOrderItemId() + "]的子订单，更新订单备注",
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                    orderMemo);

            msg.setMessage("保存成功");
            return msg;
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderConfirmOtherAuditAction orderPassOtherAudit error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

}
