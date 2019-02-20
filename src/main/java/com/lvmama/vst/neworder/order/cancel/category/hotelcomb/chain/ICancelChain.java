package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain;

import com.lvmama.vst.back.order.po.OrdOrder;

/**
 * Created by dengcheng on 17/4/12.
 * 处理订单取消同步链接接口
 */
public interface ICancelChain {
    void chain(OrdOrder order);
}

