package com.lvmama.vst.order.tnt.utils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: LuWei
 * @Date: 2018/06/26 13:59
 */
public class TntOrderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TntOrderUtils.class);
    /**
     * 是否出境分销淘宝订单合同
     * @param order
     * @return
     */
    public static boolean isDesignatedFreetourOrder(OrdOrder order) {
        boolean isDesignatedFreetourFlag=false;
        OrdOrderItem mainOrderItem = order.getMainOrderItem();
        if(OrderEnum.DISTRIBUTION_CHANNEL.DISTRIBUTOR_TAOBAO.getCode().equals(order.getDistributorCode())
                && BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
                && BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())) {
            if(mainOrderItem != null && ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(mainOrderItem.getProductType())) {
                isDesignatedFreetourFlag = true;
                LOG.info("isDesignatedFreetourOrder continue,orderId=" + order.getOrderId() + " isDesignatedFreetourFlag is true");
            }
        }
        LOG.info("isDesignatedFreetourOrder end,orderId=" + order.getOrderId() + " isDesignatedFreetourFlag=" + isDesignatedFreetourFlag);
        return isDesignatedFreetourFlag;
    }
}
