/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsStock;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;

/**
 * 内容使用的无事务接口
 * @author lancey
 *
 */
public interface IOrderLocalService extends OrderService{
	/**
	 * 更新订单信息审核状态
	 * @param order
	 * @return
	 */
	public ResultHandle executeUpdateInfoStatus(OrdOrder order,String newStatus,String assignor,String memo);
	
	/**
	 * 更新订单信息审核状态
	 * @param order
	 * @return
	 */
	public ResultHandle executeUpdateChildInfoStatus(OrdOrderItem orderItem,String newStatus,String assignor,String memo);
	
	/**
	 * 资源审核成功后发送消息
	 * @param order
	 * @return
	 */
	public void sendResourceStatusAmpleMsg(Long orderId);
	
	/**
	 * 支付成功后发送消息
	 * @param order
	 * @return
	 */
	public void sendPayedMsg(OrdOrder order);
	
	/**
	 * 订单应付金额修改成功后发送消息
	 * @param order
	 * @return
	 */
	public void sendOrdSettlementPriceChangeMsg(Long orderItemId,String addition);

	/**
	 * 子订单价格确认状态变动发消息
	 * @param  orderItemId
	 * @param  addition
	 */
	public void sendOrdItemPriceConfirmChangeMsg(Long orderItemId,String addition);
	
	
	/**
	 * 订单详情发送传真
	 * @param order
	 * @return
	 */
	public void sendOrderSendFaxMsg(OrdOrder order,String addition);

	/**
	 * 订单详情发送传真
	 * @param orderItemId
	 * @return
	 */
	public void sendOrderSendTwiceFaxMsg(Long orderItemId, String addition);

	/**
	 * 子订单详情发送传真
	 * @param order
	 * @return
	 */
	public void sendOrderItemSendFaxMsg(OrdOrderItem orderItem,String addition);
	
	/**
	 * 子订单手动生成结算单消息
	 * @param order
	 * @return
	 */
	public void sendManualSettlmenteMsg(Long  orderItemId,String addition);
	
	/**
	 * 只cancel ord_order,and jms
	 * @param orderId
	 * @param cancelCode
	 * @param reason
	 * @param operatorId
	 * @param memo
	 * @return
	 */
	ResultHandle cancelOrderLocal(Long orderId, String cancelCode,String reason, String operatorId, String memo);
	
	/**
	 * 批量人工分单
	 * @param auditIdStatusList
	 * @param assignor
	 * @param orgIds
	 * @param isForce
	 * @return
	 */
	Map<String, Object> makeOrderAuditForManualAudit(final List<String> auditIdStatusList, final String assignor, final List<Long> orgIds,final String targetOperatorUser,final boolean isForce);
	
	/**
	 * 系统自动分单
	 * @param audit
	 */
	void makeOrderAuditForSystem(ComAudit audit);
	
	/**
	 * 凭证确认操作（无需凭证确认,系统自动完成）
	 * @param order
	 * @param orderAttachment
	 * @param assignor
	 * @param memo
	 * @return
	 */
	ResultHandle updateCertificateStatus(OrdOrder order,String assignor,String memo);
	
	/**
	 * 子订单凭证确认操作
	 * @param order
	 * @param orderAttachment
	 * @param assignor
	 * @param memo
	 * @return
	 */
	ResultHandle updateChildCertificateStatus(OrdOrderItem orderItem,OrderAttachment orderAttachment,String assignor,String memo);
	
	
	/**
	 * 取消凭证确认
	 * @param order
	 * @param assignor
	 * @param memo
	 * @return
	 */
	ResultHandle updateCancelConfim(OrdOrder order,String assignor,String memo);
	
	/**
	 * 子订单取消凭证确认
	 * @param order
	 * @param assignor
	 * @param memo
	 * @return
	 */
	ResultHandleT<ComAudit> updateChildCancelConfim(OrdOrderItem orderItem,String assignor,String memo);
	
	
	/**
	 * 后台订单人工开启流程流转
	 * @param orderId
	 * @param operatorId
	 * @return
	 */
	ResultHandle startBackOrder(final Long orderId,String operatorId);
	
	
	/**
	 * 保存一个订单的
	 * @param orderId
	 * @param buyInfo
	 * @return
	 */
	ResultHandle saveOrderPerson(final Long orderId,BuyInfo buyInfo, String operatorId);
	

	
	/**
	 * 
	 * @param order
	 * @param operator
	 * @param remark
	 * @return
	 */
	ResultHandle updatePretrialAudit(final OrdOrder order,String operator,String remark);
	
	/** 订单下的买断子订单返还资源到库
	 * @param orderId
	 */
	boolean updateResBackToPrecontrol(Long orderId);
	
	
	
	ResultHandle travellerLockAudit(Long orderId, String operatorId);
	
	/**
	 * 启动支付工作流，成功后发送支付消息
	 * @param order
	 */
	ResultHandle doPaymentSuccessMsg(OrdOrder order);
	
	/**
	 * 在线退款
	 * @param order
	 * @param assignor
	 * @param memo
	 */
	ResultHandle updateOnlineRefundConfim(OrdOrder order,String assignor,String memo);

	/**
	 * 定金支付
	 * @param orderId
	 * @param addtion
	 */
	void newOrdOrderDownpayMessage(Long orderId,String addtion);
	
	boolean handelAuditReceiveTask(OrdOrder order);

	/**
	 * 出境BU发送信息安全卡邮件
	 * @param order
	 */
	void sendSafetyInfoEmail(OrdOrder order);
	
	public void sendExpiredOrderItemRefundedMsgForEbk(Long orderItemId);

	/**
	 * 保存线路后台下一个订单的
	 * @param orderId
	 * @param buyInfo
	 * @return
	 */
	ResultHandle saveNewOrderPerson(final Long orderId,BuyInfo buyInfo, String operatorId);
}
