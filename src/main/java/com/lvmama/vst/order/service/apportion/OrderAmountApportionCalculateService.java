package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * Created by zhouyanqun on 2017/4/11.
 * 订单金额分摊服务，用于分摊订单的优惠及改价等信息到每个子订单
 */
public interface OrderAmountApportionCalculateService {

    /**
     * 分摊订单金额
     * 此方法不对子单作过滤，就是订单中的子单必须已经过滤过
     * */
    void apportionOrderAmount(OrdOrder order);

    /**
     * 分摊订单上的手工改价
     * 此方法仅仅生成数据，并不写入数据库
     * */
    OrdOrder apportionOrderManualAmount(Long orderId, OrdAmountChange ordAmountChange);

    /**
     * 分摊订单上所有的手工改价
     * 此方法仅仅生成数据，并不写入数据库
     * */
    OrdOrder apportionFullManualAmount(Long orderId);

    /**
     * 分摊实付金额
     * 此方法仅仅生成数据，并不写入数据库
     * */
    OrdOrder apportionActualPaidAmount(Long orderId);

    /**
     * 分摊实付金额
     * 数据需要已经准备好，子单需要已经过滤
     * */
    void apportionActualPaidAmount(OrdOrder order);
}
