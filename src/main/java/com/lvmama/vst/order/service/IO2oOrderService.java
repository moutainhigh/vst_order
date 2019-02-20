package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.O2oOrder;

/**
 * O2o 门店系统， O2o订单Service
 * @date 2016 03 02
 * @author qixiaochen
 *
 */
public interface IO2oOrderService {

	/**
	 * 根据条件查询O2o订单
	 * @param paramsMap
	 * @return
	 */
	public List<O2oOrder> queryO2oOrder(Map<String, Object> paramsMap);
	/**
	 * 根据条件查询O2o订单
	 * @param paramsMap
	 * @return
	 */
	public List<O2oOrder> findO2oOrderList(Map<String, Object> paramsMap);
	
	/**
	 * 根据条件查询O2o订单
	 * @param paramsMap
	 * @return
	 */
	public List<O2oOrder> queryForListForReport(Map<String, Object> paramsMap);
	
	/****
	 *  O2O订单数据
	 * @param oOrder
	 * @return
	 */
	public int insertSelective(O2oOrder oOrder);
	
	/****
	 * 删除O2O订单数据
	 * @param oOrder
	 * @return
	 */
	public int deleteByOrderId(Long oderId);
	
	/**
	 * O2O订单数据条数
	 * @param params
	 * @return
	 */
	public Long getCountByProperty(Map<String, Object> params);
	
	/**
	 * 
	 * @Title: updateO2oOrder 
	 * @Description: TODO
	 * @param o2oOrder
	 * @return
	 * @return: int
	 */
	public int updateO2oOrder(O2oOrder o2oOrder);
}
