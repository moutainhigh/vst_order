package com.lvmama.vst.order.service.apportion;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/17.
 * 准备订单数据的服务
 */
public interface ApportionOrderDataPrepareService {
    /**
     * 为实付金额分摊准备数据
     * 需要的数据为 订单、子订单、多价格、入住记录(如果有)
     * */
    OrdOrder prepareOrderDataForPayment(Long orderId);

    /**
     * 为重新分摊准备订单数据，需要做两件事
     * 1. 查询出子订单、多价格、入住记录(如果有)、其它金额的分摊明细
     * 2. 补全优惠、促销、渠道优惠3种信息
     * */
    OrdOrder prepareApportionDataForBookingApportion(Long orderId);

    /**
     * 批次准备数据，对每个订单号，需要做两件事
     * 1. 查询出子订单、多价格、入住记录(如果有)、其它金额的分摊明细
     * 2. 补全优惠、促销、渠道优惠3种信息
     * 为了提升性能，需要先把所有数据查询出来，然后关联到每个订单上
     * */
    List<OrdOrder> prepareApportionDataForBookingApportion(List<Long> orderIdList);

    /**
     * 为分摊手工改价准备订单数据，需要查询出子订单、多价格、入住记录
     * */
    OrdOrder prepareOrderDataForManualChange(Long orderId);
}
