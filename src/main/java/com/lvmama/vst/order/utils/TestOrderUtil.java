package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试单工具类
 * @Author: LuWei
 * @Date: 2018/07/20 13:23
 */
public class TestOrderUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TestOrderUtil.class);

    /**
     * 是否将活动置为无效的订单
     * @param order
     * @return
     */
    public static boolean isTestOrderForMarkTaskValid(OrdOrder order) {
        //TODO storyID=17162
        if(order ==null) return false;

        LOG.info("isTestOrderForMarkTaskValid orderId:"+order.getOrderId()
                +",categoryId:"+order.getCategoryId()
                +", bu:"+order.getBuCode()+", isTestOrder:"+order.getIsTestOrder());

        //测试单，出境BU,跟团游、自由行、酒店套餐、当地游、邮轮、签证
        if (order !=null && 'Y'==order.getIsTestOrder() && CommEnumSet.BU_NAME.OUTBOUND_BU.name().equals(order.getBuCode())
                && (BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_route.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId().equals(order.getCategoryId())
                ||BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId().equals(order.getCategoryId())
                ||BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())
                || BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(order.getCategoryId()))
                ){
            return true;
        }
        return false;
    }

    /**
     * 后台下单游玩人是否前置
     * @return
     */
    public static boolean isTravellerToFirst() {
    	return "ON".equalsIgnoreCase(Constant.getInstance().getProperty("traveller_to_first_on"));
    }
}
