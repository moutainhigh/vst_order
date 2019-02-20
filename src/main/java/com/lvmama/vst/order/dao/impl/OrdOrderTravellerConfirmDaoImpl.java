package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrdOrderTravellerConfirmDao;
import org.springframework.stereotype.Repository;

/**
 * Created by zhouyanqun on 2016/4/29.
 */
@Repository("orderTravellerConfirmDao")
public class OrdOrderTravellerConfirmDaoImpl extends MyBatisDao implements OrdOrderTravellerConfirmDao {
    public OrdOrderTravellerConfirmDaoImpl() {
        super("ORD_ORDER_TRAVELLER_CONFIRM");
    }

    /**
     * 更新记录根据订单orderTravellerConfirm的orderId字段定位记录
     * @param orderTravellerConfirm
     */
    @Override
    public int merge(OrdOrderTravellerConfirm orderTravellerConfirm) {
        return super.update("updateByOrderId", orderTravellerConfirm);
    }

    /**
     * 插入记录
     *
     * @param orderTravellerConfirm
     */
    @Override
    public int persist(OrdOrderTravellerConfirm orderTravellerConfirm) {
        return super.insert("insert", orderTravellerConfirm);
    }

    /**
     * 根据订单id查询一条记录
     *
     * @param orderId
     */
    @Override
    public OrdOrderTravellerConfirm selectSingleByOrderId(Long orderId) {
        return super.get("selectByOrderId", orderId);
    }
}
