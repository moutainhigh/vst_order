package com.lvmama.vst.order.service.apportion.category;

import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/16.
 */
public interface OrderManualChangeApportionPerformer extends OrderAmountApportionPerformer {
    /**
     * 分摊主订单改价
     * */
    void apportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList, Long changeAmount);

    /**
     * 重新分摊所有手工改价信息，手工改价可能有多条，需要逐条分摊
     * */
    void reDoManualChangeApportion(OrdOrder order, List<OrdOrderItem> apportionOrderItemList, List<OrdAmountChange> ordAmountChangeList);
}
