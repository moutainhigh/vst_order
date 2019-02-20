package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
public interface OrdOrderCostSharingItemDao {
    /**
     * 新建记录
     * */
    int insert(OrdOrderCostSharingItem ordOrderCostSharingItem);

    /**
     * 作废原有记录
     * */
    int updateRecords4Invalid(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO);

    /**
     * 根据参数查询多条记录
     * */
    List<OrdOrderCostSharingItem> queryList(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO);

    /**
     * 根据参数更新分摊金额
     */
    int updateAmountByParam(OrdOrderCostSharingItem orderCostSharingItem);
}
