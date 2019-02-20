package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.BatchApportionOutcome;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
public interface OrderApportionDepotService {
    /**
     * 添加订单记录,返回添加的记录id
     * */
    Long addOrderApportionDepot(OrderApportionDepot orderApportionDepot);
    /**
     * 添加订单记录,返回添加的记录id
     * */
    Long addOrderApportionDepot(Long orderId);

    /**
     * 分页查询分摊记录
     * */
    List<OrderApportionDepot> queryOrderApportionDepotList(Map<String, Object> paramMap);

    /**
     * 根据条件查询记录总数
     * */
    Long queryRecordCount(Map<String, Object> paramMap);

    /**
     * 批量更新分摊仓库表记录，此方法中一定要做参数检查，避免更新了额外的记录
     * */
    int updateOrderApportionDepotList(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO);

    /**
     * 移除分摊好的订单
     * */
    void removeCompletedOrder(List<Long> orderIdList);

    /**
     * 批量更新订单仓库信息
     * */
    void updateOrderApportionDepotList(List<OrderApportionDepot> orderApportionDepotList);

    /**
     * 单条更新订单仓库信息(无选择性，即一个字段如果是null，数据库中的记录也会随着变成null)
     *
     * */
    int updateOrderApportionDepot(OrderApportionDepot orderApportionDepot);

    /**
     * 单条更新订单仓库信息(有选择性，即一个字段不是null，才会更新)
     *
     * */
    int updateOrderApportionDepotSelective(OrderApportionDepot orderApportionDepot);

    /**
     * 批量清除分摊信息
     * */
    void updateOfClearApportionMessage(OrderApportionDepotUpdateVO bookingApportionDepotUpdateVO);

    
    /**
     * 根据订单号查询分摊信息
     * @param orderId
     * @return
     */
    OrderApportionDepot queryApportionByOrderId(Long orderId);
    
    /**
     * 删除分摊
     * @param orderApportionId
     * @return
     */
    int deleteByPrimaryKey(Long orderApportionId);
    
    /**
     * 批量处理分摊相关数据
     * @param batchApportionOutcome
     */
    void batchUpdateApportionOutcome(BatchApportionOutcome batchApportionOutcome);

    /**
     * 根据订单号删除
     * */
    int deleteByOrderId(Long orderId);
}
