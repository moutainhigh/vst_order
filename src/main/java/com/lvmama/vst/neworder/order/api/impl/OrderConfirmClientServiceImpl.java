package com.lvmama.vst.neworder.order.api.impl;

import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.comm.utils.ResponseBodyCreator;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vst.api.common.service.IOrderConfirmClientService;
import com.lvmama.vst.back.client.ord.service.OrderConfirmClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.vo.ConfirmParamVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.utils.EnumUtilsEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("orderConfirmClientWorkflowService")
public class OrderConfirmClientServiceImpl implements IOrderConfirmClientService {

    @Autowired
    private OrderConfirmClientService confirmClientService;

    @Override
    public ResponseBody createConfirmOrder(OrderItemVo vo, String channelOperate, int count) {
        Confirm_Enum.CONFIRM_CHANNEL_OPERATE object_type=EnumUtilsEx.getEnum(Confirm_Enum.CONFIRM_CHANNEL_OPERATE.class,channelOperate);

        OrdOrderItem orderItem = new OrdOrderItem();
        EnhanceBeanUtils.copyProperties(vo, orderItem);
        ConfirmParamVo confirmParamVo =new ConfirmParamVo();
        confirmParamVo.setOrderItem(orderItem);
        confirmParamVo.setChannelOperate(object_type);
        confirmParamVo.setCount(count);
        ResultHandleT<ComAudit> result =confirmClientService.createConfirmOrder(confirmParamVo);
        return ResponseBodyCreator.success(result.isSuccess());
    }

}
