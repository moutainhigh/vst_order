package com.lvmama.vst.order.web.service;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/6/12.
 * 邮轮订单详情分摊信息展示相关服务
 */
public interface OrderShipDetailApportionService {
    /**
     * 计算订单详情中，子单的分摊信息
     * 目前订单详情中，子单的信息只有一列：实付金额
     * */
    void calcOrderDetailItemApportion(Long orderId, List<OrderMonitorRst> orderMonitorRstList);
}
