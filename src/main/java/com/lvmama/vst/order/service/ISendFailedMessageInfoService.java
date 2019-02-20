package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.SendFailedMessaeInfo;

import java.util.List;
import java.util.Map;

public interface ISendFailedMessageInfoService {

	/**
	 * 通过主键删除消息信息
	 * @param failedMessageId
	 * @return
	 */
	public int deleteByFailedMessageId(Long failedMessageId);

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
	 * @param sendFailedMessaeInfo
	 * @return
	 */
	public int saveSendFailedMessaeInfo(SendFailedMessaeInfo sendFailedMessaeInfo);


	public int saveSendFailedMessaeInfoSelective(SendFailedMessaeInfo sendFailedMessaeInfo);

	/**
	 * 根据消息ID 获取消息
	 * @param failedMessageId
	 * @return
	 */
	public SendFailedMessaeInfo selectByFailedMessageId(Long failedMessageId);

	/**
	 * 条件查询 消息
	 * @param params
	 * @return
	 */
	public List<SendFailedMessaeInfo> selectByParams(Map<String, Object> params);

	/**
	 * 更新消息
	 * 慎用
	 * @param sendFailedMessaeInfo
	 * @return
	 */
	public int updateSendFailedMessaeInfo(SendFailedMessaeInfo sendFailedMessaeInfo);

	/**
	 * 更新消息
	 * @param sendFailedMessaeInfo
	 * @return
	 */
	public int updateSendFailedMessaeInfoSelective(SendFailedMessaeInfo sendFailedMessaeInfo);

	/**
	 * 根据订单ID查询 消息集合
	 * @param orderId
	 * @return
	 */
	public List<SendFailedMessaeInfo> selectByOrderId(Long orderId);

	/**
	 * 根据子订单ID查询 消息集合
	 * @param orderItemId
	 * @return
	 */
	public List<SendFailedMessaeInfo> selectByOrderItemId(Long orderItemId);

}
