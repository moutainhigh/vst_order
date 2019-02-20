package com.lvmama.vst.order.service.apportion.category;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/16.
 */
public interface OrderInternalApportionPerformer extends OrderAmountApportionPerformer {
    void doApportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList);
}
