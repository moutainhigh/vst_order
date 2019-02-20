package com.lvmama.vst.order.confirm.web;

import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.utils.ConfirmEnumUtils;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.processer.OrderComMessageProcesser;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * 主单预订通知action
 */
@Controller
@RequestMapping("/ord/order/confirm/")
public class OrderConfirmMainAuditAction extends BaseActionSupport {

    //日志记录器
    private static final Log LOGGER = LogFactory.getLog(OrderConfirmMainAuditAction.class);

    @Autowired
    private IOrderAuditService orderAuditService;

    @Autowired
    private IOrdItemConfirmStatusService ordItemConfirmStatusService;

    @Autowired
    private IOrdOrderService ordOrderService;

    //订单综合查询
    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private LvmmLogClientService lvmmLogClientService;

    @Autowired
    private OrderComMessageProcesser orderComMessageProcesser;

    @Autowired
    private IOrderDistributionBusiness distributionBusiness;


    /**
     * 主单预订通知-审核通过
     * @param request
     * @param orderItemId
     * @param auditId
     * @return
     */
    @RequestMapping(value = "/orderPassMainAudit")
    @ResponseBody
    public Object orderPassMainAudit(HttpServletRequest request, Long orderId, Long auditId, String orderMemo) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit orderId:" + orderId + ", orderMemo="+orderMemo);
            if (orderId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit orderId is null");
                return msg;
            }
            if (auditId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit auditId is null");
                return msg;
            }
            OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
            if (ordOrder == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit ordOrder is null orderId:" + orderId);
                return msg;
            }
            LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit orderId:" + orderId + ", auditId="+auditId);
            ComAudit audit = orderAuditService.queryAuditById(auditId);
            if (audit == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderConfirmMainAuditAction orderPassMainAudit audit is null :" + auditId);
                return msg;
            }

            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = " ";
            }
            ResultHandleT<ComAudit> comAuditResultHandleT = ordItemConfirmStatusService.updateOrderConfirmAudit(auditId, operateName, orderMemo);
            if (comAuditResultHandleT.isFail()) {
                LOGGER.info("OrderConfirmMainAuditAction orderPassOtherAudit update audit error auditId:" + auditId);
            }
            try {
                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                        ordOrder.getOrderId(),
                        ordOrder.getOrderId(),
                        operateName,
                        "将编号为[" + ordOrder.getOrderId() + "]的订单，更新订单备注",
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                        orderMemo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            msg.setMessage("保存成功");
            return msg;
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderConfirmMainAuditAction orderPassMainAudit error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 修改订单备注，不更新状态
     * @param model
     * @param orderItemId
     * @param orderMemo
     * @param request
     * @return
     */
    @RequestMapping("/updateMainOrderMemo.do")
    @ResponseBody
    public Object updateMainOrderMemo(Model model, Long orderId, String orderMemo, HttpServletRequest request) {
        ResultMessage msg = ResultMessage.createResultMessage();
        msg.setCode(ResultMessage.SUCCESS);
        try {
            LOGGER.info("OrderConfirmMainAuditAction updateMainOrderMemo orderItemId:" + orderId + ", orderMemo="+orderMemo);
            if (orderId == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("参数不能为空");
                LOGGER.info("OrderConfirmMainAuditAction updateMainOrderMemo orderId or confirmStatus is null");
                return msg;
            }
            OrdOrder ordOrder = ordOrderService.findByOrderId(orderId);
            if (ordOrder == null) {
                msg.setCode(ResultMessage.ERROR);
                msg.setMessage("记录不存在");
                LOGGER.info("OrderConfirmMainAuditAction updateMainOrderMemo ordOrder is null orderId:" + orderId);
                return msg;
            }

            String operateName = getLoginUserId();
            if (StringUtil.isEmptyString(orderMemo)) {
                orderMemo = " ";
            }
            ordOrder.setOrderMemo(orderMemo);
            int result = ordOrderService.updateOrderMemo(ordOrder);

            try {
                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                        ordOrder.getOrderId(),
                        ordOrder.getOrderId(),
                        operateName,
                        "将编号为[" + ordOrder.getOrderId() + "]的订单，更新订单备注",
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
                        ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新订单备注",
                        orderMemo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result != 1) {
                LOGGER.info("OrderConfirmMainAuditAction updateMainOrderMemo update orderMemo error orderId:" + orderId);
            }
            return msg;
        } catch (Exception e) {
            msg.setCode(ResultMessage.ERROR);
            msg.setMessage("运行出现异常"+e);
            LOGGER.error("OrderConfirmMainAuditAction updateMainOrderMemo error,msg:" + e.getMessage());
            e.printStackTrace();
        }
        return msg;
    }


    /**
     * 主单预订通知-补偿支付消息
     * @param request
     * @param orderItemId
     * @param auditId
     * @return
     */
    @RequestMapping(value = "/orderPaymentMsg")
    @ResponseBody
    public Object orderPaymentMsg(HttpServletRequest request, Long orderId) {
        String memo = "支付超时取消且支付成功";
        saveComMessageByConfirm(orderId, Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_CANCEL_ORDER_PAY_SUCCESS.name(), memo, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE);


        return "success";
    }

    private void saveComMessageByConfirm(Long orderId, String auditSubType, String memo, ComLog.COM_LOG_LOG_TYPE logType) {
        try {
            LOGGER.info("start method saveAuditMessage");
            ComAudit audit = new ComAudit();
            audit.setObjectId(orderId);
            audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
            audit.setAuditType(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
            audit.setAuditSubtype(auditSubType);
            audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
            audit.setCreateTime(Calendar.getInstance().getTime());
            int i = orderAuditService.saveAudit(audit);
            LOGGER.info("saveAuditMessage auditId="+audit.getAuditId());
            if (i == 1) {
                insertOrderLog(orderId, auditSubType);
                ComAudit comAudit = distributionBusiness.makeOrderAudit(audit);
            } else {
                LOGGER.error("orderId="+orderId+", saveAudit error.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(ExceptionFormatUtil.getTrace(e));

            if (Constants.NO_PERSON.equals(e.getMessage())) {

                lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                        orderId,
                        orderId,
                        "system",
                        memo,
                        logType.name(),
                        logType.getCnName()+"["+ memo +"]",
                        memo);
            }else{
                LOGGER.info(" saveAuditMessage has no person exception");
            }
        }
    }

    /**
     *
     * 保存日志
     *
     */
    private void insertOrderLog(final Long orderId,String auditType){
        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                orderId,
                orderId,
                Constants.SYSTEM,
                "编号为["+orderId+"]的订单,系统自动创建订单活动["+ ConfirmEnumUtils.getCnName(auditType)+"]",
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(),
                ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()+"["+ ConfirmEnumUtils.getCnName(auditType)+"]",
                null);
    }
}
