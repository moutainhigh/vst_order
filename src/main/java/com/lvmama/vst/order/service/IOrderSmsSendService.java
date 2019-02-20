package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.web.BusinessException;
/**
 * 短信
 * @author chenkeke
 *
 */
public interface IOrderSmsSendService {
	/**
	 * 发送短信
	 * @param orderId
	 * @param sendNode
	 * @throws Exception 
	 */
	Long sendSms(Long orderId,OrdSmsTemplate.SEND_NODE sendNode,String operate) throws BusinessException;
	/**
	 * 发送短信
	 * @param orderId
	 * @param sendNode
	 * @throws Exception 
	 */
	Long sendSms(Long orderId,OrdSmsTemplate.SEND_NODE sendNode) throws BusinessException;
	/**
	 * 自定义发送短信
	 * @param orderId
	 * @param content
	 * @param operate
	 * @param mobile
	 * @throws BusinessException
	 */
	void sendSmsByCustom(Long orderId,String content,String operate,String mobile) throws BusinessException;
	/**
	 * 重发
	 * @param smsId
	 * @throws BusinessException
	 */
	void reSendSms(Long smsId,String operate)throws BusinessException;
	/**
	 * 获取短信内容
	 * @param orderId
	 * @param sendNode
	 * @return
	 */
	String getContent(Long orderId, OrdSmsTemplate.SEND_NODE sendNode);
	/**
	 * 获取短信模板
	 * @param orderId
	 * @param sendNode
	 * @return
	 */
	OrdSmsTemplate getOrdSmsTemplate(Long orderId, OrdSmsTemplate.SEND_NODE sendNode);

	/**
	 * 获取已发送短信记录数
	 */
	int findOrdSmsSendCount(Map<String, Object> params) throws BusinessException;
	
	/**
	 * 获取已发送短信详情
	 */
	List<OrdSmsSend> findOrdSmsSendList(Map<String, Object> params) throws BusinessException;
	
	/**
	 * 更新快递单号成功  需要 发送短信
	 * @param templateId
	 * @param parameters
	 * @return
	 */
	String getSmsContent(String templateId, Map<String, Object> parameters);
}
