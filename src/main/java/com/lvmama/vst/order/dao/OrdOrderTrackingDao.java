package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
@Repository
public class OrdOrderTrackingDao extends MyBatisDao {
	
	public OrdOrderTrackingDao() {
		super("ORD_ORDER_TRACKING");
	}
	
	public int deleteByPrimaryKey(Long trackingId) {
		return super.delete("deleteByPrimaryKey", trackingId);
	}

	public int insert(OrdOrderTracking ordOrderTracking) {
		return super.insert("insert", ordOrderTracking);
	}

	public OrdOrderTracking selectByPrimaryKey(Long trackingId) {
		return super.get("selectByPrimaryKey", trackingId);
	}
	
	public List<OrdOrderTracking> selectNowOrderStatusByOrderId(Long orderId) {
		return super.getList("selectNowOrderStatusByOrderId", orderId);
	}

	public int updateByPrimaryKeySelective(OrdOrderTracking ordOrderTracking) {
		return super.update("updateByPrimaryKeySelective", ordOrderTracking);
	}
	
	/**
	 * 根据订单ID与订单状态更新订单追踪信息
	 * @param paramsMap
	 * @return
	 */
	public int updateOrderStatusByOrderIdAndStatus(Map<String,Object> paramsMap) {
		return super.update("updateOrderStatusByOrderIdAndStatus", paramsMap);
	}
	
	/**
	 * 根据订单ID和订单状态查询订单状态信息
	 * @param paramsMap
	 * @return
	 */
	public List<OrdOrderTracking> selectByOrderIdAndStatus(Map<String,Object> paramsMap) {
		return super.queryForList("selectByOrderIdAndStatus", paramsMap);
	}

}
