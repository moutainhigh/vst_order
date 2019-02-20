package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.chainprocessor;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.ICancelChain;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dengcheng on 17/4/13.
 */
@Component("cancelForWorkflowProcessor")
public class WorkFlowProcessorChain extends BaseProcessorChain implements ICancelChain {


    @Resource
    private ProcesserClientService processerClientService;

    @Resource
    private ComActivitiRelationService comActivitiRelationService;


    private static final Logger LOG = LoggerFactory
            .getLogger(WorkFlowProcessorChain.class);
    @Override
    public void chain(OrdOrder order) {
        Long orderId = order.getOrderId();
        String cancelCode = order.getCancelCode();
        String reason = order.getReason();
        String memo = order.getOrderMemo();
        String operatorId =order.getBackUserId();

        // 存在流程的数据走流程废单
        if (ActivitiUtils.hasActivitiOrder(order)) {
            LOG.info(order.getOrderId() + "在流程废单");
            Map<String, Object> params1 = new HashMap<String, Object>();
            params1.put("cancelCode", cancelCode);
            LOG.info("cancelOrder reason=" + reason);

            if (reason == "" || reason == null || reason == "null") {
                reason = "SYSTEM";
            }

            LOG.info("cancelOrder reason=" + reason);

            params1.put("reason", reason);
            params1.put("operatorId", operatorId);
            params1.put("memo", memo);
            // 取消统一在工作流处理
            //params1.put("ifUpdateOrderCancel", "N");

            ResultHandle handle = processerClientService.cancelOrder(
                    createKeyByOrder(order), params1);

            if (handle.isFail()) {

                Map<String, Object> params2 = new HashMap<String, Object>();
                LOG.info("cancel fail in  activiti,use default cancel orderId:"
                        + orderId);
//                order =  complexQueryService.queryOrderByOrderId(orderId);

                params2.put("orderId", order.getOrderId());
                params2.put("mainOrderItem", order.getMainOrderItem());
                params2.put("order", order);

                String processId = processerClientService
                        .startProcesser(
                                "order_cancel_prepaid",
                                "order_cancel_id:" + order.getOrderId(),
                                params2);
                LOG.info("CANCEL ORDER id:" + order.getOrderId()
                        + " ProcesserId:" + processId);
            } else {
                LOG.info("order id:" + order.getOrderId()
                        + " activiti cancel");
            }

        }
    }

    private ActivitiKey createKeyByOrder(OrdOrder order){
        ComActivitiRelation relation = getRelation(order);
        LOG.info("createKeyByOrder order.orderid="+order.getOrderId());
        return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
    }


    public ComActivitiRelation getRelation(OrdOrder order){
        try{
            ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
            if(comActiveRelation == null){//补偿机制,通过工作流再次去触发查询
                String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
                LOG.info("===processerClientService.queryProcessIdByBusinessKey==orderId:"+order.getOrderId()+"====processId:"+processId);
                if(processId != null){
                    comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
                    comActiveRelation = new ComActivitiRelation();
                    comActiveRelation.setObjectId(order.getOrderId());
                    comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
                    comActiveRelation.setProcessId(processId);
                    comActiveRelation.setProcessKey(order.getProcessKey());
                }
            }
            return comActiveRelation;
        }catch(Exception e){
            LOG.error("ComActivitiRelation getRelation error:"+e);
        }
        return null;
    }

}
