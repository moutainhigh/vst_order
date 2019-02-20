package com.lvmama.vst.order.web;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdDepositRefundAudit;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORD_DEPOSIT_REFUND_AUDIT;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.OrdDepositRefundAuditService;
import com.lvmama.vst.order.service.OrdPayPromotionService;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 订单定金退改
 */
@Controller
@RequestMapping("order/depositRefund")
public class OrderDepositRefundAction extends BaseActionSupport {

    /**
     * -    订单金额： 	ord_order.ought_amount (应付金额) ;
     * -	已支付  ： 	ord_order.actual_amount(实付金额) ;
     * - 	已退金额： 	ord_order.refunded_amount(退款金额) ;
     * -	剩余可退：	计算所得（实付金额 - 退款金额）;
     * -	损失金额：	产品经理填写（审核表中，一条申请一条记录）;
     * -	可退金额：	剩余可退 - 损失金额 ;
     */


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDepositRefundAction.class);

    @Autowired
    private OrdDepositRefundAuditService ordDepositRefundAuditService;

    @Autowired
    private IComplexQueryService complexQueryService;

    @Autowired
    private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;

    @Autowired
    private OrdPayPromotionService ordPayPromotionService;


    /**
     * 申请显示
     */
    @RequestMapping("/showAudit/{applyType}/{orderId}")
    public String showDepositRefund(@PathVariable String applyType, @PathVariable Long orderId, Model model) {

        List<OrdDepositRefundAudit> refundAuditList;
        if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
            refundAuditList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER);
        } else {
            refundAuditList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES);
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            model.addAttribute("order", order);
        }

        model.addAttribute("orderId", orderId);
        //申请类型
        model.addAttribute("applyType", applyType);
        model.addAttribute("refundAuditList", refundAuditList);

        return "/order/orderDepositRefund/showAudit";
    }

    /**
     * 产品经理审核显示
     */
    @RequestMapping("showProcessAudit/{applyType}/{orderId}")
    public String showProcessAudit(@PathVariable String applyType, @PathVariable Long orderId, Model model) {

        //历史记录
        List<OrdDepositRefundAudit> historyList;
        //需要审核的记录
        OrdDepositRefundAudit processAudit = null;

        if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
            historyList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER);
        } else {
            historyList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES);
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            model.addAttribute("order", order);
            try {
                //剩余可退：	计算所得（实付金额 - 退款金额）
                Long actualAmount = order.getActualAmount() == null ? 0 : order.getActualAmount();
                Long refundedAmount = order.getRefundedAmount() == null ? 0 : order.getRefundedAmount();
                model.addAttribute("canRundAmount", actualAmount - refundedAmount);
            } catch (Exception e) {
                LOGGER.error(String.format("计算可退金额错误, 实付:%s ; 已退:%s", order.getActualAmount(), order.getRefundedAmount()));
            }
        }

        //剔除处理中的审核记录并赋值给需要审核的记录
        Iterator<OrdDepositRefundAudit> iterator = historyList.iterator();
        while (iterator.hasNext()) {
            OrdDepositRefundAudit audit = iterator.next();
            if (ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name().equals(audit.getAduitStatus())) {
                processAudit = audit;
            }
        }

        //申请类型
        model.addAttribute("applyType", applyType);
        //历史
        model.addAttribute("refundAuditList", historyList);
        //当前需要审核的记录
        model.addAttribute("processAudit", processAudit);

        return "/order/orderDepositRefund/showProcessAudit";
    }

    /**
     * 提交申请校验
     */
    @RequestMapping("/checkCommit/{applyType}/{orderId}")
    @ResponseBody
    public Object checkCommit(@PathVariable String applyType, @PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>(1);
        result.put("code", 200);

        /*
         * 资产转移校验
         */
        if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            //资金已转移
            if (VstOrderEnum.PAYMENT_STATUS.TRANSFERRED.name().equals(order.getPaymentStatus())) {
                result.put("code", 500);
                result.put("msg", "此单支付金额已转移，无法再次申请转移。");
                return result;
            } else {
                //(未审核 || 审核通过) && 资产未转移
                int processingCount = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name(), ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name()));
                int passCount = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name(), ORD_DEPOSIT_REFUND_AUDIT.PASS.name()));
                if ((processingCount + passCount) > 0) {
                    result.put("code", 501);
                    result.put("msg", "此单已有未处理完的资金转移申请，是否需要撤销已有记录？");
                    return result;
                }
            }
        }

        /*
         * 定金核损校验
         */
        if (ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name().equals(applyType)) {

            //校验是否有正在处理中的退款
            if (!CollectionUtils.isEmpty(orderRefundmentServiceAdapter.findRefundOrdRefundmentByOrderId(orderId))) {
                result.put("code", 500);
                result.put("msg", "此单已有退款正在处理中，请等待处理完成后再提交申请!");
                return result;
            }

            //正在处理的审核
            int processingCount = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name(), ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name()));
            //审核通过未退款
            int passCount = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name(), ORD_DEPOSIT_REFUND_AUDIT.PASS.name(), "N"));

            if ((processingCount + passCount) > 0) {
                result.put("code", 501);
                result.put("msg", "此单已有未处理完的核损申请，是否需要撤销已有记录？");
                return result;
            }

        }

        return result;
    }

    /**
     * 提交申请
     */
    @RequestMapping("commitAudit/{applyType}")
    @ResponseBody
    public Object commitAudit(@PathVariable String applyType, OrdDepositRefundAudit audit) {
        Map<String, Object> result = new HashMap<>(1);
        result.put("code", 200);

        try {
            audit.setApplicantName(this.getLoginUser().getRealName());
            //申请类型
            if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
                audit.setApplyType(ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name());
            } else {
                audit.setApplyType(ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name());
            }

            this.checkCommit(applyType, audit);
            ordDepositRefundAuditService.commitAudit(audit);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            LOGGER.error("commit audit error :", e);
            return result;
        }

        return result;
    }


    /**
     * 校验产品经理登录是否显示
     *
     * @param managerId 产品表中的产品经理
     */
    @RequestMapping("checkShowProcessAudit/{applyType}/{managerId}/{orderId}")
    @ResponseBody
    public Object checkShowProcessAudit(@PathVariable String applyType, @PathVariable Long managerId, @PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>(1);
        result.put("code", 500);

        try {
            if (!Objects.equals(managerId, this.getLoginUser().getUserId())) {
                return result;
            }

            //检查该订单是否有未处理申请
            int num;
            if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
                num = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name(), ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name()));
            } else {
                num = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name(), ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name()));
            }

            if (num > 0) {
                result.put("code", 200);
            }

        } catch (Exception e) {
            result.put("code", 500);
            LOGGER.error("checkShowProcessAudit error : ", e.getMessage());
        }

        return result;
    }

    /**
     * 产品经理处理审核
     */
    @RequestMapping(value = "processAudit", method = RequestMethod.POST)
    @ResponseBody
    public Object processAudit(OrdDepositRefundAudit audit) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);

        //计算可退金额
        if (ORD_DEPOSIT_REFUND_AUDIT.LOSSES.name().equals(audit.getApplyType())
                && ORD_DEPOSIT_REFUND_AUDIT.PASS.name().equals(audit.getAduitStatus())) {
            OrdOrder order = this.complexQueryService.queryOrderByOrderId(audit.getOrderId());
            try {
                Long actualAmount = order.getActualAmount() == null ? 0 : order.getActualAmount();
                Long refundedAmount = order.getRefundedAmount() == null ? 0 : order.getRefundedAmount();
                //剩余可退
                Long canRundAmount = actualAmount - refundedAmount;
                //可退金额 = 剩余可退 - 损失金额
                Long retreatAmount = canRundAmount - audit.getLossAmount();
                audit.setRetreatAmount(retreatAmount);
            } catch (Exception e) {
                LOGGER.error(String.format("计算可退金额异常,实付:%s ; 已退:%s ;", order.getActualAmount(), order.getRefundedAmount()));
                result.put("code", 500);
                result.put("msg", e.getMessage());
                return result;
            }
        }

        try {
            this.checkCommit(audit.getApplyType(), audit);
            ordDepositRefundAuditService.updateAudit(audit);
        } catch (Exception e) {
            LOGGER.error("processAudit error : ", e);
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }

        return result;
    }

    @RequestMapping("revertBeforeApply/{applyType}/{orderId}")
    @ResponseBody
    public Object revertApply(@PathVariable Long orderId, @PathVariable String applyType) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);

        List<OrdDepositRefundAudit> refundAuditList;
        if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.toString().equals(applyType)) {
            refundAuditList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER);
        } else {
            refundAuditList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.LOSSES);
        }

        try {
            for (OrdDepositRefundAudit audit : refundAuditList) {
                if (isCanReply(audit)) {
                    audit.setAduitStatus(ORD_DEPOSIT_REFUND_AUDIT.REJECT.name());
                    audit.setAduitReply("自主撤销");
                    ordDepositRefundAuditService.updateAudit(audit);
                }
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 是否可自主撤销
     *
     * @param audit
     * @return
     */
    private boolean isCanReply(OrdDepositRefundAudit audit) {
//        驳回已有 【审核中】 或 【审核通过且未处理】的记录

        //已退款
        if ("Y".equals(audit.getRefundFlag())) {
            return false;
        }

        return ORD_DEPOSIT_REFUND_AUDIT.PROCESSING.name().equals(audit.getAduitStatus())
                || ORD_DEPOSIT_REFUND_AUDIT.PASS.name().equals(audit.getAduitStatus());
    }


    @RequestMapping("checkTransferOrder/{orderId}")
    @ResponseBody
    public Object checkTransferOrder(@PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);

        try {

            List<OrdOrderDownpay> ordList = ordPayPromotionService.queryOrderDownpayByOrderId(orderId);

            if (CollectionUtils.isEmpty(ordList)) {
                return result;
            }

            OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
            if (!this.showLosses(order, ordList.get(0))) {
                return result;
            }

            List<OrdDepositRefundAudit> refundAuditList = ordDepositRefundAuditService.findListByOrderId(orderId, ORD_DEPOSIT_REFUND_AUDIT.TRANSFER);

            if (CollectionUtils.isEmpty(refundAuditList)) {
                result.put("code", 500);
                result.put("msg", "该订单[" + orderId + "]还未申请资金转移");
                return result;
            }

            for (OrdDepositRefundAudit audit : refundAuditList) {
                //有审核通过的订单
                if (ORD_DEPOSIT_REFUND_AUDIT.PASS.toString().equals(audit.getAduitStatus())) {
                    result.put("code", 502);
                    result.put("msg", audit.getTransferOrderId());
                    return result;
                }
            }

            result.put("code", 500);
            result.put("msg", "该订单[" + orderId + "]还有正在审核或审核驳回的申请");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "其他异常");
            LOGGER.error("checkTransferOrder error " + e.getMessage());
        }

        return result;
    }

    /**
     * 校验提交审核信息
     *
     * @param audit
     */
    private void checkCommit(String applyType, OrdDepositRefundAudit audit) throws IllegalAccessException {

        //申请
        if (audit.getAduitStatus() == null) {
            if (ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name().equals(applyType)) {
                if (audit.getTransferOrderId() == null) {
                    throw new IllegalAccessException("转移目标订单不能为空");
                }
            }

            if (StringUtils.isBlank(audit.getApplyInfo())) {
                throw new IllegalAccessException("申请原因不能为空");
            }
        } else {//审批
            if (StringUtils.isBlank(audit.getAduitReply())) {
                throw new IllegalAccessException("审批理由不能为空");
            }
        }

    }


    /**
     * 是否显示定金审核
     */
    private boolean showLosses(OrdOrder order, OrdOrderDownpay orderDownpay) {
        /*
		1.订单支付方式为定金支付。
		2.品类为出境线路和邮轮订单。
		3.支付状态为部分支付。
		4.订单定金已支付。
		5.订单取消状态。
		 */

        if (orderDownpay == null) {
            return false;
        }


        //定金支付
        if (OrdOrderDownpay.PAY_TYPE.FULL.toString().equals(orderDownpay.getPayType())) {
            return false;
        }

        //出境
        if (!Constant.BU_NAME.OUTBOUND_BU.toString().equals(order.getBuCode())) {
            return false;
        }

        // 线路
        if (!(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())
        )) {
            return false;
        }

        //订单部分支付
        if (!OrderEnum.PAYMENT_STATUS.PART_PAY.toString().equals(order.getPaymentStatus())) {
            return false;
        }

        //定金已支付
        if (!OrderEnum.PAYMENT_STATUS.PAYED.toString().equals(orderDownpay.getPayStatus())) {
            return false;
        }

        //订单已取消
        if (!OrderEnum.ORDER_STATUS.CANCEL.toString().equals(order.getOrderStatus())) {
            return false;
        }

        //废单重下
        if (!OrderEnum.ORDER_CANCEL_TYPE_ABANDON_ORDER_REPEAT.toString().equals(order.getCancelCode())) {
            return false;
        }

        return true;
    }


}
