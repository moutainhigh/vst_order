package com.lvmama.vst.order.confirm.service.impl;

import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.comm.utils.ResponseBodyCreator;
import com.lvmama.order.enums.ApiEnum;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vst.api.common.service.IApiOrdItemConfirmStatusService;
import com.lvmama.order.vst.api.common.vo.request.OrdItemConfirmVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 子订单确认状态变更桥接服务
 *
 * @author houjian
 * @date 2018/5/22.
 */
@Service("apiOrdItemConfirmStatusService")
public class ApiOrdItemConfirmStatusServiceImpl implements IApiOrdItemConfirmStatusService {
    @Autowired
    private IOrdItemConfirmStatusService ordItemConfirmStatusService;
    @Autowired
    private IOrdItemConfirmProcessService ordItemConfirmProcessService;
    @Autowired
    private IOrdOrderService ordOrderService;

    @Override
    public ResponseBody closeFullhotelAndForbidSale(OrderItemVo orderItemVo, String operator, String memo, String sourceType) {
        OrdOrderItem ordOrderItem = new OrdOrderItem();
        EnhanceBeanUtils.copyProperties(orderItemVo, ordOrderItem);
        try {
            ResultHandle resultHandle = this.ordItemConfirmStatusService.closeFullhotelAndForbidSale(ordOrderItem, operator, memo, sourceType, null, null, null);
            if (resultHandle.isFail())
                return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), String.valueOf(orderItemVo.getOrderItemId()), "自动关房失败");
        } catch (Exception e) {
            return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "自动关房失败", e, String.valueOf(orderItemVo.getOrderItemId()), ApiEnum.BUSSINESS_TAG.ORD_ORDER_ITEM.name());
        }
        return new ResponseBody();
    }

    @Override
    public ResponseBody completeUserTaskByConfirm(ResponseBody<OrdItemConfirmVo> responseBody) {
        OrdItemConfirmVo confirmVo = responseBody.getT();
        if (confirmVo == null) {
            return null;
        }
        try {
            String operator = confirmVo.getOperatorName();
            Long orderId = confirmVo.getOrderId();
            String taskKey = confirmVo.getTaskKey();
            OrdOrder order = ordOrderService.loadOrderWithItemByOrderId(orderId);
            OrdOrderItem ordOrderItem = order.getMainOrderItem();
            ordItemConfirmProcessService.completeUserTaskByConfirm(ordOrderItem, taskKey, operator);
        } catch (Exception e) {
            return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.COMPLETE_AUDIT_TASK_ERROR.getCode(), null, "完成审核任务失败", e, confirmVo.getOrderItemId(), ApiEnum.BUSSINESS_TAG.ORD_ORDER_ITEM.name());
        }
        return new ResponseBody();
    }
}
