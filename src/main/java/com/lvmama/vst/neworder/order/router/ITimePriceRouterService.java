package com.lvmama.vst.neworder.order.router;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;

import java.util.Date;

/**
 * Created by dengcheng on 17/4/26.
 *
 */
public interface ITimePriceRouterService {
    BaseTimePrice findTimePrice(SuppGoods goods,OrderHotelCombBuyInfo.GoodsItem goodItem,OrderHotelCombBuyInfo.Item item);
}
