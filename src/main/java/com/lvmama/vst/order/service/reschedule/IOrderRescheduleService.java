package com.lvmama.vst.order.service.reschedule;

import com.lvmama.vst.back.goods.vo.SuppGoodsRescheduleVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderRescheduleInfo;
import com.lvmama.vst.comm.vo.RescheduleTimePrice;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface IOrderRescheduleService {

     String rescheduleFlag(OrdOrder order);
    
     String rescheduleFlag(Long orderId);
     
     String rescheduleMsg(OrdOrder order);
    
     boolean isEbkAndEnterInTime(OrdOrderItem ordOrderItem);
    
     ResultHandle rescheduleCheckStock(OrdOrder order);
    
     void deductStock(OrdOrder order);
    
     Long getSuppChangeCount(OrdOrder order);
    
     Long getOrdChangeCount(OrdOrder order);
    
     ResultHandleT<List<SuppGoodsRescheduleVO.Item>> updateOrderItemVisitTime(Long orderId, List<SuppGoodsRescheduleVO.Item> list);
    
     ResultHandle  rescheduleCheck(Long orderId,List<SuppGoodsRescheduleVO.Item> list);

     SuppGoodsRescheduleVO.OrdRescheduleStatus getOrderRescheduleStatus(OrdOrder ordOrder);
    
     void addRescheduleLog(ResultHandleT<List<SuppGoodsRescheduleVO.Item>> resultHandleT,Long orderId,Map<String,Object> map);
    
    OrderRescheduleInfo queryOrderRescheduleInfo(Long orderId, Map<String, Object> map);

    Boolean isMerge(OrdOrder ordOrder);

    List<RescheduleTimePrice> getRescheduleTimePriceList(Long suppGoodsId,Long productId,Long distributorId,Map<String,Object> map);
    
}
