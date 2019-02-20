package com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.impl;

import com.google.common.eventbus.Subscribe;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import org.elasticsearch.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dengcheng on 17/2/21.
 */
@Component("orderWorkFlowMessageChain")
public class OrderWorkFlowMessageChain implements IOrderProcessChain {

    private static final Logger LOG = LoggerFactory
            .getLogger(OrderWorkFlowMessageChain.class);

    @Resource
    private ProcesserClientService processerClientService;


    @Autowired
    private ComActivitiRelationService comActivitiRelationService;


    @Resource
    IOrderCancelService orderCancelService;

    @Override
    public void beforDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,String method) {

    }

    @Override
    public void AfterDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody, OrdOrder order,String method) {
    	LOG.info("OrderWorkFlowMessageChain----AfterDoFilter--start");
        if(method.indexOf("submitOrder") != -1) {
            try {

                Map<String, Object> params = new HashMap<String, Object>();

                params.put("orderId", order.getOrderId());
                params.put("mainOrderItem", order.getMainOrderItem());
                params.put("order", order);

                LOG.info("-------------------@---order.getDistributorId()"
                        + order.getDistributorId()
                        + "----getProcessKey:"
                        + order.getProcessKey()
                        + "--"
                        + ActivitiUtils.createOrderBussinessKey(order));

                long startTime = System.currentTimeMillis();
                String processId = processerClientService
                        .startProcesser(order
                                .getProcessKey(), ActivitiUtils
                                .createOrderBussinessKey(order), params);

                LOG.info(ComLogUtil.printTraceInfo("OrderWorkFlowMessageNotifier#process",
                        "启动订单流传流程",
                        "processerClientService.startProcesser",
                        System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();

                comActivitiRelationService.saveRelation(order.getProcessKey(), processId,
                        order.getOrderId(),
                        ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);

                LOG.info(ComLogUtil.printTraceInfo("OrderWorkFlowMessageNotifier#process",
                        "保存工作流信息",
                        "comActivitiRelationService.saveRelation",
                        System.currentTimeMillis() - startTime));

                LOG.info("order.hasNeedPrepaid="
                        + order.hasNeedPrepaid());
            } catch (Exception ex) {
        
                
                OrderCancelInfo info = new OrderCancelInfo();
                info.setOrderId(order.getOrderId());
                info.setCancelCode(OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name());
                info.setReason("订单创建启动工作流异常");
                info.setOperatorId("System");
                orderCancelService.doOrderCancel(info);
                LOG.error("OrderWorkFlowMessageChain----AfterDoFilter---工作流异常"+ex);
                ex.printStackTrace();
               
                Throwables.propagate(ex);
            }
         	LOG.info("OrderWorkFlowMessageChain----AfterDoFilter--end");
        }
    }
}
