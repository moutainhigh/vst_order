package com.lvmama.vst.neworder.order.cancel;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;

/**
 * Created by dengcheng on 17/2/21.
 */
public interface IOrderCancelService {
    void doOrderCancel(OrderCancelInfo cancelInfo);

    void cancelForWorkFlow(OrderCancelInfo cancelInfo);
}