package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;

import java.util.List;
import java.util.Map;

public interface OrderApportionDepotDao {
    int deleteByPrimaryKey(Long orderApportionId);

    int insert(OrderApportionDepot record);

    int insertSelective(OrderApportionDepot record);

    OrderApportionDepot selectByPrimaryKey(Long orderApportionId);

    int updateByPrimaryKeySelective(OrderApportionDepot record);

    int updateByPrimaryKey(OrderApportionDepot record);

    /**
     * 分页查询记录
     * */
    List<OrderApportionDepot> queryForList(Map<String, Object> paramMap);

    /**
     * 条件查询记录总数
     * */
    Long queryRecordCount(Map<String, Object> paramMap);

    /**
     * 批量更新分摊仓库表记录
     * */
    int updateOrderApportionDepotList(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO);

    /**
     * 移除分摊好的订单
     * */
    int batchDeleteByPrimaryKey(List<Long> orderApportionIdList);

    /**
     * 清除分摊信息，也即解除数据库标识锁
     * */
    void updateOfClearApportionMessage(OrderApportionDepotUpdateVO bookingApportionDepotUpdateVO);

    OrderApportionDepot queryApportionByOrderId(Long orderId);
    /**批量更新下单项分摊完成的数据**/
    int batchUpdateApportionDeportCreateEnd(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO);
    /**批量更新失败的分摊集合**/
    public int batchUpdateApportionDeportFailed(List<OrderApportionDepot> deportList);

    /**
     * 根据订单号删除记录
     * */
    int deleteByOrderId(Long orderId);
}