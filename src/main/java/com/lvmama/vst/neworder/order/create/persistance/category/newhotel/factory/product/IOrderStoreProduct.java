package com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory.product;

import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/2/23.
 */
public interface IOrderStoreProduct {
    /**
     * 订单投持久化
     * @param order
     */
    void orderHeaderDbStore(OrdOrderDTO order);

    /**
     * 订单子项持久化
     * @param order
     */
    void orderItemDbStore(OrdOrderDTO order);


    /**
     * 订单游客项持久化
     * @param order
     */
    void orderItemTravelDbStore(OrdOrderDTO order);

    /**
     * 订单金额流水项持久化
     * @param order
     */
    void orderAmountTravelDbStore(OrdOrderDTO order);

//    /**
//     * 订单扣减流水项持久化
//     * @param order
//     */
//    void orderSaleDbStore(OrdOrderDTO order);

    /**
     * 促销优惠持久化
     * @param order
     */
    void ordePromotionDbStore(OrdOrderDTO order);

    /**
     * 合同项持久化
     * @param order
     */
    void ordTravelContractDbStore(OrdOrderDTO order);
}
