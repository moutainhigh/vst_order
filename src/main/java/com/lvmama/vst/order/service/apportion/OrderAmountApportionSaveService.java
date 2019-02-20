package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * Created by zhouyanqun on 2017/4/17.
 * 订单分摊数据保存的服务，计算得到的分摊数据往往存在于订单项中，需要把这些分摊数据保存起来
 */
public interface OrderAmountApportionSaveService {
    /**
     * 保存所有分摊信息
     * */
    void saveAllApportion(OrdOrder order);
    /**
     * 仅仅保存实付分摊信息
     * */
    void savePaymentApportion(OrdOrder order);
}
