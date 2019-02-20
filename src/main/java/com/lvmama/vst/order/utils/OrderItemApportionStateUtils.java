package com.lvmama.vst.order.utils;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import org.apache.commons.collections.CollectionUtils;

/**
 * Created by zhouyanqun on 2017/6/2.
 */
public class OrderItemApportionStateUtils {
    /**
     * 检查查询项是否完成，完整的含义是包含订单号，或者子单号这些信息
     * 如果不包含，在执行查询、修改或者删除时，会造成系统缓慢，甚至破坏掉数据
     * */
    public static boolean checkParam(OrderItemApportionStateQueryVO orderItemApportionStateQueryVO){
        return orderItemApportionStateQueryVO != null
                && (NumberUtils.isAboveZero(orderItemApportionStateQueryVO.getOrderId())
                    || NumberUtils.isAboveZero(orderItemApportionStateQueryVO.getOrderItemId())
                    || CollectionUtils.isNotEmpty(orderItemApportionStateQueryVO.getOrderIdList())
                    || CollectionUtils.isNotEmpty(orderItemApportionStateQueryVO.getOrderItemIdList()));
    }
}
