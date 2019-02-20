package com.lvmama.vst.order.service;

import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.comm.web.BusinessException;

import java.util.List;
import java.util.Map;

public interface OrderConnectsServicePropService {
	
	/**
	 * 获取的OrderConnectsServiceProp列表
	 * @param params
	 */
	List<OrderConnectsServiceProp> findOrderConnectsServicePropList(Map<String, Object> params) throws BusinessException;
	
	
	/**
	 * 获取的OrderConnectsServiceProp
	 * @param orderServiceId
	 */
	OrderConnectsServiceProp findOrderConnectsServicePropById(Long orderServiceId) throws BusinessException;

    /**
     * 保存一个OrderConnectsServiceProp
     * @param orderConnectsServiceProp
     *
     * */
	Integer addOrderConnectsServiceProp(OrderConnectsServiceProp orderConnectsServiceProp) throws BusinessException;


	/**
	 * 删除OrderConnectsServiceProp
	 * @param orderServiceId
	 * @throws BusinessException
	 */
	Integer  deleteOrderConnectsServicePropById(Long orderServiceId)throws BusinessException;

	/**
	 * 修改OrderConnectsServiceProp
	 * @param orderConnectsServiceProp
	 * @return
	 * @throws BusinessException
     */
	Integer updateOrderConnectsServicePropById(OrderConnectsServiceProp orderConnectsServiceProp)throws BusinessException;

	/**
	 * 关联查询属性表
	 * @param params
	 * @return
     */
	List<OrderConnectsServiceProp> queryOrderConnectsPropByParams(Map<String, Object> params);
	
	
	/**
	 * 修改OrderConnectsServiceProp
	 * @param orderConnectsServiceProp
	 * @return
	 * @throws BusinessException
	 * 需要propId,orderId
     */
	Integer updateOrderConnectsServicePropByOrderId(OrderConnectsServiceProp orderConnectsServiceProp)throws BusinessException;
}
