package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;

/**
 * Created by zhouyanqun on 2017/5/19.
 */
public interface ApportionInfoCalculateService {
    /**
     * 根据查询VO，准备必要的数据
     * */
    OrderItemApportionInfoRelatedVO prepareRelatedVO(OrderApportionInfoQueryVO orderApportionInfoQueryVO);

    /**
     * 根据查询VO，准备必要的数据
     * */
    OrderItemApportionInfoRelatedVO prepareRelatedVO(OrderItemApportionInfoQueryVO orderItemApportionInfoQueryVO);
}
