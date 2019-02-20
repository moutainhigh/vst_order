package com.lvmama.vst.order.dao.impl;

import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.OrderApportionDepotDao;
import com.lvmama.vst.order.vo.OrderApportionDepotUpdateVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/5/18.
 */
@Repository
public class OrderApportionDepotDaoImpl extends MyBatisDao implements OrderApportionDepotDao {
    public OrderApportionDepotDaoImpl() {
        super("ORDER_APPORTION_DEPOT");
    }

    @Override
    public int deleteByPrimaryKey(Long orderApportionId) {
        return super.delete("deleteByPrimaryKey", orderApportionId);
    }

    @Override
    public int insert(OrderApportionDepot record) {
        return super.insert("insert", record);
    }

    @Override
    public int insertSelective(OrderApportionDepot record) {
        return super.insert("insertSelective", record);
    }

    @Override
    public OrderApportionDepot selectByPrimaryKey(Long orderApportionId) {
        return super.get("selectByPrimaryKey", orderApportionId);
    }

    @Override
    public int updateByPrimaryKeySelective(OrderApportionDepot record) {
        return super.update("updateByPrimaryKeySelective", record);
    }

    @Override
    public int updateByPrimaryKey(OrderApportionDepot record) {
        return super.update("updateByPrimaryKey", record);
    }


    /**
     * 分页查询记录
     *
     * @param paramMap 查询参数
     */
    @Override
    public List<OrderApportionDepot> queryForList(Map<String, Object> paramMap) {
        return super.queryForList("queryListByParam", paramMap);
    }

    /**
     * 条件查询记录总数
     *
     * @param paramMap
     */
    @Override
    public Long queryRecordCount(Map<String, Object> paramMap) {
        return super.get("getRecordCount", paramMap);
    }

    /**
     * 批量更新分摊仓库表记录
     *
     * @param orderApportionDepotUpdateVO
     */
    @Override
    public int updateOrderApportionDepotList(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO) {
        return super.update("updateOrderApportionDepotList", orderApportionDepotUpdateVO);
    }

    /**
     * 移除分摊好的订单
     *
     * @param orderIdList
     */
    @Override
    public int batchDeleteByPrimaryKey(List<Long> orderApportionIdList) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderApportionIdList", orderApportionIdList);
        return super.deleteAll("batchDeleteByPrimaryKey", paramMap);
    }

    @Override
    public void updateOfClearApportionMessage(OrderApportionDepotUpdateVO bookingApportionDepotUpdateVO) {
        super.update("clearApportionMessage", bookingApportionDepotUpdateVO);
    }
    
    /**
     *根据订单查询分摊 
     */
	@Override
	public OrderApportionDepot queryApportionByOrderId(Long orderId) {
	    return super.getSqlSession().selectOne("queryApportionByOrderId", orderId);
	}
	
	/**
	 * 批量更新下单项分摊完成的数据（mgs=null,apportionStatus=apportion_status_booking_completed）
	 * @param orderApportionIdList
	 * @return
	 */
	public  int batchUpdateApportionDeportCreateEnd(OrderApportionDepotUpdateVO orderApportionDepotUpdateVO){
        return super.update("batchUpdateApportionDeportCreateEnd", orderApportionDepotUpdateVO);
    }
	
	/**
	 * 批量更新失败的分摊集合
	 * @param deportList
	 * @return
	 */
	public int batchUpdateApportionDeportFailed(List<OrderApportionDepot> deportList){
		return super.update("batchUpdateApportionDeportFailed", deportList);
	}

    /**
     * 根据订单号删除记录
     *
     * @param orderId
     */
    @Override
    public int deleteByOrderId(Long orderId) {
        return super.delete("deleteByOrderId", orderId);
    }
}
