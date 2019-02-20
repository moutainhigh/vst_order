package com.lvmama.vst.order.utils;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import org.apache.commons.collections.CollectionUtils;

/**
 * Created by zhouyanqun on 2017/6/2.
 */
public class OrdOrderCostSharingItemUtils {
    /**
     * 检查查询项是否完成，完整的含义是包含订单号，或者子单号这些信息
     * 如果不包含，在执行查询、修改或者删除时，会造成系统缓慢，甚至破坏掉数据
     * */
    public static boolean checkParam(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO){
        return orderCostSharingItemQueryVO != null
                &&
                (NumberUtils.isAboveZero(orderCostSharingItemQueryVO.getOrderId()) || CollectionUtils.isNotEmpty(orderCostSharingItemQueryVO.getOrderIdList())
                || NumberUtils.isAboveZero(orderCostSharingItemQueryVO.getOrderItemId()) || CollectionUtils.isNotEmpty(orderCostSharingItemQueryVO.getOrderItemIdList()));
    }
}
