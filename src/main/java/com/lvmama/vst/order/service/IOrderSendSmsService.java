package com.lvmama.vst.order.service;

import com.lvmama.comm.pet.po.sms.SmsMMS;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.web.BusinessException;

import java.util.List;
import java.util.Map;
/**
 * 短信
 * @author zhaomingzhu
 *
 */
public interface IOrderSendSmsService{
	
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
	 * 
	 * @param orderId 订单id
	 * @param orderItemList 订单子项列表
	 * @param map  map<orderItemId,List<Long>> List<Long> 辅助码列表
	 * @return map PHONE 手机号码  SMS 短信内容
	 */
	String getTicketCertContent(Long orderId, List<Long> orderItemList,Map<String,Object> map);
	
	/**
	 * 供应商 长隆 短信内容获取
	 * @param orderId 订单编号
	 * @return
	 */
	public String getMessageContent(Long orderId);
	
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
	 * 发送普通短信
	 */
	void sendSMS(String content,String mobile,Long orderId);
	/**
	 * @author xiexun
	 * 发送普通短信（增加业务类型参数 用于标识调用方）
	 * @param content 短信内容
	 * @param mobile 发送号码
	 * @param bussType 业务类型
	 * @param orderId 订单ID
	 */
	void sendSMS(String content,String mobile,String bussType,Long orderId);
	/**
	 * 发送彩信
	 */
	void sendSMS(SmsMMS sms,String mobile);
	
	/**
	 * 订单凭证短信接口
	 * @param orderId
	 */
	public void sendSms(Long orderId);
	
	/**
	 * 门票订单是否需要发送凭证短信接口
	 * @param orderId
	 * @return
	 */
	public boolean isShouldSendCertOfTicket(Long orderId);

	public Map<String,OrdPerson> getOrdPersons(Long orderId, OrdSmsTemplate.SEND_NODE sendNode);

	public Map<String,ProdProduct> getItemProdProducts(OrdOrder order);

	public Map<String,String> getTicketAddress(OrdOrder order);
	
	/**
	 * 根据模板发送短信
	 * @param orderId
	 * @param sendNode
	 * @param params
	 * @param mobile
	 * @return
	 */
	void  sendSms(Long orderId, String mobile, String  sendNode,Map<String,Object> params);



	public String getExpressContentAndSend(Long orderId, Map<String, Object> map);

	Long sendPreventCheatSms(Long orderId)
			throws BusinessException;
}
