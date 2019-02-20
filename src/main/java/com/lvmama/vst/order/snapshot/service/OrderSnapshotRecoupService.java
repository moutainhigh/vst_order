package com.lvmama.vst.order.snapshot.service;

import com.lvmama.order.snapshot.api.service.ISnapshotParseClientService;
import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.util.OrdSnapshotJsonUtils;
import com.lvmama.order.snapshot.comm.vo.ProdProductBranchSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.ProdProductSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.SuppGoodsSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 订单快照Service
 */
@Component("orderSnapshotRecoupService")
public class OrderSnapshotRecoupService {
    @Resource
    private IComplexQueryService complexQueryService;
    @Resource
    private IOrderUpdateService orderUpdateService;
    @Resource
    private ISnapshotParseClientService snapshotParseClientService;
    /**
     * showOrderStatusManage
     * @param orderId
     * @param orderItemId
     * @return
     */
    public String showOrderStatusManage(Long orderId, Long orderItemId) throws Exception{
        JSONArray jsonArray =new JSONArray();
        //获取快照对象
        if (orderId != null) {
            OrdOrder ordOrder =complexQueryService.queryOrderByOrderId(orderId);
            OrderParamVo ordOrderParam = SnapshotParamFactory.convertOrderParamVo(ordOrder);
            //get
            ResponseBody<ProdProductSnapshotVo> responseBody =snapshotParseClientService.getProdProductSnapshot(ordOrderParam);
            putJSONArray(jsonArray, "ProdProductSnapshot", responseBody.getT());

        }else if (orderItemId != null) {
            OrdOrderItem ordOrderItem =orderUpdateService.getOrderItem(orderItemId);
            OrderItemParamVo orderItemParamVo =SnapshotParamFactory.convertOrdOrderItem(ordOrderItem);
            //get
            ResponseBody<ProdProductSnapshotVo> responseBody1 =snapshotParseClientService.getProdProductSnapshot(orderItemParamVo);
            ResponseBody<ProdProductBranchSnapshotVo> responseBody2=snapshotParseClientService.getProdProductBranchSnapshot(orderItemParamVo);
            ResponseBody<SuppGoodsSnapshotVo> responseBody3=snapshotParseClientService.getSuppGoodsSnapshot(orderItemParamVo);
            putJSONArray(jsonArray, "ProdProductSnapshot", responseBody1.getT());
            putJSONArray(jsonArray, "ProdProductBranchSnapshot", responseBody1.getT());
            putJSONArray(jsonArray, "SuppGoodsSnapshot", responseBody1.getT());
        }
        return jsonArray.toString();
    }
    /**
     * putJSONArray
     * @param jsonArray
     * @param key
     * @param obj
     */
    private void putJSONArray(JSONArray jsonArray, String key, Object obj){
        JSONObject jsonObject =new JSONObject();
        JSONObject value =JSONObject.fromObject(obj, OrdSnapshotJsonUtils.getJsonConfig());
        //put
        jsonObject.put(key, value.toString());
        jsonArray.add(jsonObject);
    }
}
