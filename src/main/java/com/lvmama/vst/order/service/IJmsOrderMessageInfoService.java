package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.JmsOrderMessageInfo;

import java.util.List;
import java.util.Map;

public interface IJmsOrderMessageInfoService {

	/**
	 * 通过主键删除消息信息
	 * @param messageInfoId
	 * @return
	 */
	public int deleteByMessageInfoId(Long messageInfoId);

	/**
	 * 通过订单ID 删除消息信息
	 * @param orderId
	 * @return
	 */
	public int deleteByOrderId(Long orderId);

	/**
	 * 通过子订单ID 删除消息信息
	 * @param orderItemId
	 * @return
	 */
	public int deleteByOrderItemId(Long orderItemId);

	/**
	 * 保存订单消息
	 * @param jmsOrderMessageInfo
	 * @return
	 */
	public int saveJmsOrderMessaeInfo(JmsOrderMessageInfo jmsOrderMessageInfo);

	public int saveJmsOrderMessaeInfoSelective(JmsOrderMessageInfo jmsOrderMessageInfo);

	/**
	 * 根据消息ID 获取消息
	 * @param messageInfoId
	 * @return
	 */
	public JmsOrderMessageInfo selectByMessageInfoId(Long messageInfoId);

	/**
	 * 条件查询 消息
	 * @param params
	 * @return
	 */
	public List<JmsOrderMessageInfo> selectByParams(Map<String, Object> params);

	/**
	 * 更新消息
	 * @param JmsOrderMessageInfo
	 * @return
	 */
	public int updateJmsOrderMessaeInfo(JmsOrderMessageInfo jmsOrderMessageInfo);

	public int updateJmsOrderMessaeInfoSelective(JmsOrderMessageInfo jmsOrderMessageInfo);

	/**
	 * 根据订单ID查询 消息集合
	 * @param orderId
	 * @return
	 */
	public List<JmsOrderMessageInfo> selectByOrderId(Long orderId);

	/**
	 * 根据子订单ID查询 消息集合
	 * @param orderId
	 * @return
	 */
	public List<JmsOrderMessageInfo> selectByOrderItemId(Long orderItemId);

}
