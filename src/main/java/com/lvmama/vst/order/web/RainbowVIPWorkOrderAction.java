package com.lvmama.vst.order.web;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.order.job.OrderRequestPaymentJob;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.RainbowVIPWorkOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rainbowVIP/workOrder")
public class RainbowVIPWorkOrderAction {

    @Autowired
    private IOrderUpdateService ordOrderUpdateService;
    @Autowired
    private RainbowVIPWorkOrderService rainbowVIPWorkOrderService;
    @Autowired
    private OrderRequestPaymentJob orderRequestPaymentJob;

    @ResponseBody
    @RequestMapping(value = "/pushWorkOrderRemind")
    public Object pushWorkOrderRemind(Long orderId){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            //获取订单
            OrdOrder ordOrder = this.ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
            //推送工单
            rainbowVIPWorkOrderService.push(ordOrder);
            resultMap.put("status","SUCCESS");
        }catch (Exception e){
            resultMap.put("status","FAILURE");
            resultMap.put("msg", ExceptionFormatUtil.getTrace(e));
        }
        return resultMap;
    }

    @ResponseBody
    @RequestMapping(value = "/runJob")
    public Object runJob(){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            orderRequestPaymentJob.run();
            resultMap.put("status","SUCCESS");
        }catch (Exception e){
            resultMap.put("status","FAILURE");
            resultMap.put("msg", ExceptionFormatUtil.getTrace(e));
        }
        return resultMap;
    }

}
