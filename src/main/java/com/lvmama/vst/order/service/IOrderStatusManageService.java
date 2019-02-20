package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;

import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;

public interface IOrderStatusManageService {	
	
	
	/**
	 * 更新订单备注
	 * @param order
	 * @return
	 */
	public ResultHandle updateOrderMemo(Long orderId, String memo);
	
	/**
	 * 更新订单资源审核状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateResourceStatus(Long orderId, String newStatus,String resourceRetentionTime,String assignor,String memo);
	
	/**
	 * 更新子订单资源审核状态
	 * @param orderItemId
	 * @param newStatus
	 * @param resourceRetentionTime
	 * @param assignor
	 * @param memo
	 * @param ifEBK 是否EBK发起更新
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildResourceStatus(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo,boolean ifEBK);
	/**
	 * 补偿更新子订单资源审核状态
	 * @param orderId
	 * @param newStatus
	 * @param resourceRetentionTime
	 * @param assignor
	 * @param memo
	 * @param ifEBK 是否EBK发起更新
	 * @return
	 */
	public ResultHandleT<ComAudit> compensateUpdateChildResourceStatus(Long orderId, String newStatus,String resourceRetentionTime,String assignor,String memo,boolean ifEBK);
	
	
	/**
	 * 更新子订单资源审核状态
	 * @param orderItemId
	 * @param newStatus
	 * @param resourceRetentionTime
	 * @param assignor
	 * @param memo
	 * @param suppilerName
	 * @param ifEBK 是否EBK发起更新
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildResourceStatus(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo, String suppilerName, boolean ifEBK);
	
	
	/**
	 * 更新对接机票子订单资源审核状态
	 * @param orderItemId
	 * @param newStatus
	 * @param resourceRetentionTime
	 * @param assignor
	 * @param memo
	 * @param suppilerName
	 * @param ifEBK 是否EBK发起更新
	 * @return
	 */
	public ResultHandleT<ComAudit> updateFlightOrderResourcePass(Long orderItemId, String newStatus,String resourceRetentionTime,String assignor,String memo, String suppilerName, boolean ifEBK);
	
	
	/**
	 * 资源保留时间、最晚无损取消时间中最小的
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	public Date getMinDate(Long orderId, Date lastCancelTime);
	
	/**
	 * 更新订单信息审核状态
	 * @param order
	 * @return
	 */
	public com.lvmama.vst.comm.vo.ResultHandleT<ComAudit> updateInfoStatus(OrdOrder order,String newStatus,String assignor,String memo);
	
	/**
	 * 更新子订单信息审核状态
	 * @param order
	 * @return
	 */
	public com.lvmama.vst.comm.vo.ResultHandleT<ComAudit> updateChildInfoStatus(OrdOrderItem orderItem,String newStatus,String assignor,String memo);
	
	
	/**
	 * 更新订单履行状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateCertificateStatus(OrdOrder order,OrderAttachment orderAttachment,String assignor,String memo);
	
	/**
	 * 更新子订单履行状态
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildCertificateStatus(OrdOrderItem orderItem,OrderAttachment orderAttachment,String assignor,String memo);
	
	
	/**
	 * 更新订单催支付活动，且记录日志
	 * @param order
	 * @param code 活动编码
	 * @return
	 */
	public ResultHandle updatePaymentAudit(OrdOrder order,String assignor,String memo,String code);
	
	
	/**
	 * 更新订单通知出团活动，且记录日志
	 * @param order
	 * @return
	 */
	public ResultHandle updateNoticeRegimentAudit(OrdOrder order,String assignor,String memo);
	
	
	
	/**
	 * 订单取消已确认
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateCancelConfim(OrdOrder order,String assignor,String memo);
	/**
	 * 子订单订单取消已确认
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateChildCancelConfim(OrdOrderItem orderItem,String assignor,String memo);
	
	/**
	 * 订单详情页面发送传真
	 * @param orderId
	 * @param faxReark
	 * @param assignor
	 * @param memo
	 * @return
	 */
	public ResultHandle saveOrderFaxRemark(Long orderId,String faxFlag,String faxRemark,String assignor,String memo);
	/**
	 * 订单详情页面人工发送传真
	 * @param orderId
	 * @param faxReark
	 * @param assignor
	 * @param memo
	 * @return
	 */
	public ResultHandle manualSendOrderFax(OrdOrder order,String toFax,String faxRemark,String assignor, String memo);
	
	/**
	 * 子订单详情页面人工发送传真
	 * @param orderId
	 * @param faxReark
	 * @param assignor
	 * @param memo
	 * @return
	 */
	public ResultHandle manualSendOrderItemFax(OrdOrderItem orderItem,String toFax,String faxRemark,String assignor, String memo);

	public abstract ResultHandleT<ComAudit> updatePretrialAudit(OrdOrder order, String operator,
			String remark);

	/**
	 * 在线退款
	 * @param order
	 * @return
	 */
	public ResultHandleT<ComAudit> updateOnlineRefundConfim(OrdOrder order,String assignor,String memo);
	/**
	 *  更新子订单确认状态
	 * @param orderItemId 子订单
	 * @param newStatus 需要更新的确认状态
	 * @param operator 操作人
	 * @param memo 备注
	 * @return
	 * @throws
	 */
	public ResultHandleT<ComAudit> updateChildConfirmStatus(OrdOrderItem orderItem, CONFIRM_STATUS newStatus
			,String operator,String memo) throws Exception;
	
	/**
	 * @Discribe： 计算门票支付等待时间
	 * 	原因：当日门票的支付时间在资源审核的时候被覆盖，导致等待支付时间错误
	 * @user ZM
	 * @date 2015年5月15日下午4:13:12	
	 * @param order void
	 */
	public void calPaymentWaitTime4Mp(OrdOrder order,List<OrdOrderItem> orderItemList);
	
	/**
	 * 资源保留时间、最晚无损取消时间、支付等待时间中最小的 
	 * 逻辑修改 2015-01-12当 资源审核时间不为空的情况下,取该时间为支付等待时间
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	public Date getMinDate(Long orderId, Date lastCancelTime, Date watiPaymentTime);
	
}
