package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.back.order.po.BatchApportionOutcome;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderApportionDepot;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/25.
 * 分摊相关服务，实际是一个外观模式下的分摊组件
 */
public interface OrderAmountApportionService {
    /**
     * 计算并且保存订单的分摊信息，
     * 此方法用于已经生成的订单，目前分摊优惠、促销、渠道优惠信息
     * */
    void calcAndSaveBookingApportionAmount(Long orderId);
    /**
     * 计算并且保存订单的分摊信息，
     * 数据需要已经准备好
     * */
    void calcAndSaveBookingApportionAmount(OrdOrder order);

    /**
     * 分摊并保存订单的实付金额
     * */
    void apportionAndSaveActualPaidAmount(Long orderId);

    /**
     * 分摊并保存订单的实付金额
     * 数据需要已经准备好
     * */
    void apportionAndSaveActualPaidAmount(OrdOrder order);

    /**
     * 分摊并且保存订单的手工改价信息
     * */
    void apportionAndSaveManualChangeAmount(Long orderId, OrdAmountChange ordAmountChange);

    /**
     * 分摊并且保存订单所有的手工改价信息
     * */
    void apportionAndSaveFullManualChangeAmount(Long orderId);

    /**
     * 添加订单到分摊仓库表，等待分摊
     * */
    void addToOrderApportionDepot(Long orderId);
}
