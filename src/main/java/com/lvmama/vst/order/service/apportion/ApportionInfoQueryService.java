package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.order.po.OrderApportionInfoPO;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;

/**
 * Created by zhouyanqun on 2017/4/19.
 * 分摊信息查询服务
 */
public interface ApportionInfoQueryService {
    /**
     * 查询订单的分摊信息
     * @param orderApportionInfoQueryVO 查询条件
     * */
    OrderApportionInfoPO calculateOrderApportionInfo(OrderApportionInfoQueryVO orderApportionInfoQueryVO);

    /**
     * 查询单个子单的分摊信息
     * */
    OrderItemApportionInfoPO calcOrderItemApportionInfo(OrderItemApportionInfoQueryVO orderItemApportionInfoQueryVO);
}
