package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.chainprocessor;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.ICancelChain;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.VstOrderUserRefundServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dengcheng on 17/4/12.
 * 退款工单处理
 */
@Component("cancelForRefundProcessor")
public class RefundTicketProcessorChain extends BaseProcessorChain implements ICancelChain {

    private static final Logger LOG = LoggerFactory.getLogger(RefundTicketProcessorChain.class);

    @Resource
    private IOrderUpdateService orderUpdateService;


    @Resource
    private VstOrderUserRefundServiceAdapter vstOrderRefundServiceRemote;

    @Override
    public void chain(OrdOrder order) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("preRefundStatus", OrderEnum.PRE_REFUND_STATUS.APPLY.name());
        params.put("orderId", order.getOrderId());
        orderUpdateService.updatePreRefundStatus(params);
        // 生成2张售后单(一张售后 一张资审)
        vstOrderRefundServiceRemote.createOrdSaleForPreRufund(
                order.getOrderId(), order.getBackUserId());
    }
}

