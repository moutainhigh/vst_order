package com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain;

import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * Created by dengcheng on 17/4/1.
 * api 订单接口处理链路
 */
public interface IOrderProcessChain {
    public void beforDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,String method);
    public void AfterDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody, OrdOrder order,String method);
}
