package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
public interface OrdOrderCostSharingItemService {
    /**
     * 插入新记录
     * */
    int saveOrdOrderCostSharingItem(OrdOrderCostSharingItem orderCostSharingItem);

    /**
     * 作废原有记录
     * */
    int updateRecords4Invalid(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO);

    /**
     * 作废原订单的分摊记录
     * */
    int updateOrderApportionRecords4Invalid(Long orderId);

    /**
     * 根据参数查询记录
     * */
    List<OrdOrderCostSharingItem> queryOrdOrderCostSharingItemList(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO);

    /**
     * 更新分销(门票)子单的分摊金额
     * create by liyanpeng on 2017/7/4
     */
    int updateAmountByParam(OrdOrderCostSharingItem orderCostSharingItem);
}
