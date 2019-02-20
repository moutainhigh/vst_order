package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrdOrderCostSharingItemDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
@Repository
public class OrdOrderCostSharingItemDaoImpl extends MyBatisDao implements OrdOrderCostSharingItemDao {
    public OrdOrderCostSharingItemDaoImpl() {
        super("ORD_ORDER_COST_SHARING_ITEM");
    }

    /**
     * 新建记录
     *
     * @param ordOrderCostSharingItem
     */
    @Override
    public int insert(OrdOrderCostSharingItem ordOrderCostSharingItem) {
        return super.insert("insert", ordOrderCostSharingItem);
    }

    /**
     * 作废原有记录
     *
     * @param orderCostSharingItemQueryVO 查询条件
     */
    @Override
    public int updateRecords4Invalid(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO) {
        return super.update("invalidOrderCostSharingRecords", orderCostSharingItemQueryVO);
    }

    /**
     * 根据参数查询多条记录
     *
     * @param orderCostSharingItemQueryVO
     */
    @Override
    public List<OrdOrderCostSharingItem> queryList(OrderCostSharingItemQueryVO orderCostSharingItemQueryVO) {
        return super.getList("queryResultListByParam", orderCostSharingItemQueryVO);
    }

    /**
     * 根据参数更新分摊金额
     */
    @Override
    public int updateAmountByParam(OrdOrderCostSharingItem orderCostSharingItem) {
        return super.update("updateAmountByParam", orderCostSharingItem);
    }


}
