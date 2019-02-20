package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import org.apache.commons.collections.CollectionUtils;

/**
 * Created by zhouyanqun on 2017/4/12.
 */
public class OrdOrderUtils {
    /**
     * 获取订单的产品id，先直接从订单从取，如果取不到，遍历order的packList，直接取出一个有效的productId
     * 如果依然取不到，返回 null
     * */
    public static Long getOrderProductId(OrdOrder order){
        if(order == null) {
            return null;
        }

        if(order.getProductId() != null && order.getProductId() > 0) {
            return order.getProductId();
        }

        if(CollectionUtils.isEmpty(order.getOrderPackList())) {
            return null;
        }

        for (OrdOrderPack ordOrderPack : order.getOrderPackList()) {
            if(ordOrderPack.getProductId() != null && ordOrderPack.getProductId() > 0) {
                return ordOrderPack.getProductId();
            }
        }

        return null;
    }
}
