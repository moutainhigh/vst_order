package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrderTracking;

public interface IOrdOrderTrackingService {

	public int updateByPrimaryKeySelective(OrdOrderTracking ordOrderTracking);
	
	public int insert(OrdOrderTracking ordOrderTracking);
	
	public int updateOrderStatusByOrderIdAndStatus(Map<String,Object> paramsMap);
	
	public List<OrdOrderTracking> selectByOrderIdAndStatus(Map<String,Object> paramsMap);
	
	public int  deleteByPrimaryKey(Long trackingId);
	
	/**
	 * 根据订单ID查询当前订单状态轨迹
	 * @param orderId
	 * @return
	 */
	public List<OrdOrderTracking> findNowOrderStatusByOrderId(Long orderId);
	
	/**
	 * 保存订单跟踪信息
	 * @param ordOrderTracking
	 */
	public void saveOrderTracking(OrdOrderTracking ordOrderTracking);
}
