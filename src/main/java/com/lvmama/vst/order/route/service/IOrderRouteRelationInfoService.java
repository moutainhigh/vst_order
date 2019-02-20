package com.lvmama.vst.order.route.service;

import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;

import java.util.Map;

/**
 * @author zhouyanqun
 * @ImplementProject: vst_order
 * @Description: 订单路由关联表的服务
 * @date 2017-09-21
 */
public interface IOrderRouteRelationInfoService {
    int insert(OrderRouteRelationInfo orderRouteRelationInfo);

    /**
     * 根据条件查询订单路由表记录
     * */
    OrderRouteRelationInfo queryOrderRouteRelationInfo(Map<String, Object> paramMap);
}
