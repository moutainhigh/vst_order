package com.lvmama.vst.order.timeprice.service;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.timeprice.po.OrderItemPricePO;

/**
 * Created  on 2016/11/7.
 */
public interface ItemOrdMulPriceRateService {
    /**
     * 把ApiFlightPricePO中的价格(例如成人价、儿童价，成人结算价、儿童结算价等)计算到OrdOrderItem中
     * 并且在OrdOrderItem的ordMulPriceRateList集合中增加对应的价格，最终这个价格会保存到ord_mul_price_rate表中
     * */
    void calculateOrdMulPriceRate(OrdOrderItem orderItem, OrderItemPricePO orderItemPricePO);
}
