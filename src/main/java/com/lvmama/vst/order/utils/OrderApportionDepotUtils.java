package com.lvmama.vst.order.utils;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;
import org.apache.commons.collections.CollectionUtils;

/**
 * Created by zhouyanqun on 2017/6/2.
 */
public class OrderApportionDepotUtils {
    public static boolean checkParam(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO){
        if (!checkFilterParam(orderApportionDepotUpdateVO)) {
            return false;
        }
        //检查将更新的字段是否齐全
        return orderApportionDepotUpdateVO.getApportionStatus() != null
                || orderApportionDepotUpdateVO.getApportionMessage() != null
                || orderApportionDepotUpdateVO.getValidFlag() != null;
    }

    /**
     * 检查筛选条件是否齐全
     * */
    public static boolean checkFilterParam(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO){
        if (orderApportionDepotUpdateVO == null) {
            return false;
        }
        //检查筛选条件是否齐全
        return !(NumberUtils.isNotAboveZero(orderApportionDepotUpdateVO.getOrderId())
                && CollectionUtils.isEmpty(orderApportionDepotUpdateVO.getOrderIdList())
                && CollectionUtils.isEmpty(orderApportionDepotUpdateVO.getOrderApportionIdList()));
    }
}
