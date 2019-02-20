package com.lvmama.vst.order.service;

import com.lvmama.comm.pet.po.fin.SettlementItem;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.comm.vo.Constant;
import java.util.Map;
import java.util.List;
import java.util.Map;

/**
 * Created by chenlong on 2016/10/17.
 * 订单结算推送, 调用finance_interface
 */
public interface OrderSettlementService {

    /**
     * 新增或更新订单结算项
     *
     * @param setSettlementItems
     *            订单结算项
     * @param messageType
     *            触发的消息类型
     */
    void insertOrUpdateSettlementItem(List<SettlementItem> setSettlementItems, Constant.EVENT_TYPE messageType);

    int updateSettlementItem(final Long orderId,final Long countSettleAmount);

    List<SetSettlementItem> findSetSettlementItemByParams(final Long orderId, final Long orderItemId);

    /**
     * 新增或修改结算子项信息
     * @param setSettlementItems
     */
    void saveOrUpdateSetSettlementItem(final List<SetSettlementItem> setSettlementItems);

    /**
     * 新增或修改结算子项一条结算子项信息
     * @param setSettlementItem
     */
    void saveSettlementItem(final SettlementItem setSettlementItem);

    /**
     * 根据订单子子项ID查询是否已经进行结算打款
     * @param orderItemMetaId 订单子子项ID
     * @param businessName 业务系统标示
     * @return true 已经结算打款  false 未结算打款
     */
    boolean searchSettlementPayByOrderItemMetaId(Long orderItemMetaId);

    /**
     * 批量插入结算子项信息
     * @param setSettlementItem
     */
    void batchInsertSettlementItem(final List<SettlementItem> setSettlementItems);

    /**
     * 把价格修改记录转变成变价信息，并调用结算系统保存.
     * @param priceRecord
     * @return void
     */
    void insertRecord(OrdSettlementPriceRecord priceRecord);
    /**
     * 动态更新
     * @param  params
     * @return int
     */
    int dynmicUpdateSetSettlementItem(Map<String,Object> params);

}
