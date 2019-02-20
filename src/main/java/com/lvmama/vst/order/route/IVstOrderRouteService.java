package com.lvmama.vst.order.route;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * @author zhouyanqun
 * @ImplementProject vst_order
 * @Description: vst内部订单路由服务
 * @date 2017-10-09
 */
public interface IVstOrderRouteService {
    /**
     * 是否路由到新系统
     *
     * @param orderId 订单号.
     * */
    boolean isOrderRouteToNewSys(Long orderId);

    /**
     * 根据子单号路由
     * */
    boolean isOrderItemRouteToNewSys(Long orderItemId);

    /**
     * 无参数的路由
     * */
    boolean isRequestRouteToNewSys();
    
    /**
     * 判断是否路由到新系统 订单二期
     * */
    boolean isRequestRouteToNewSys4Ord2();

    /**
     * 是否把job路由到新系统
     * */
    boolean isJobRouteToNewSys();

    /**
     * 获取版本号
     * @return
     */
    String getVersion();

    /**
     * 路由到新版工作流
     * @param categoryId
     * @return
     */
    boolean isRouteToNewWorkflow(Long categoryId);

    boolean isRouteToNewWorkflowBySingleHotel(OrdOrder ordOrder);


}
