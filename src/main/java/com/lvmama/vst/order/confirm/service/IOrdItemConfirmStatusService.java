package com.lvmama.vst.order.confirm.service;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_CHANNEL_OPERATE;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;

import java.util.Date;
import java.util.List;

/**
 * 子订单确认相关接口
 */
public interface IOrdItemConfirmStatusService {
	/**
	 * 更新子订单确认状态
	 * @param 子订单
	 * @param newStatus
	 * @param operator
	 * @param memo
	 * @return
	 * @throws
	 */
	public ResultHandleT<ComAudit> updateChildConfirmStatus(OrdOrderItem orderItem,
                                                            CONFIRM_STATUS newStatus, String operator, String memo) throws Exception;
	/**
	 * 已审库操作(供应商)
	 * @param order
	 * @param ebkCertif
	 * @return 是否使用新流程确认发送
	 * @throws Exception
	 */
	public ResultHandleT<List<Object[]>> updateInConfirmStatusBySupplier(OrdOrder order, EbkCertif ebkCertif) throws Exception;
	
	/**
	 * 已审库操作(员工)
	 * @param orderItem
	 * @param newStatus
	 * @param supplierNo 确认号
	 * @param operator
	 * @param memo
	 * @param linkId
	 * @return
	 * @throws Exception
	 */
	public ResultHandleT<ComAudit> updateInConfirmStatusByUser(OrdOrderItem orderItem,
                                                               CONFIRM_STATUS newStatus, String supplierNo, String operator, String memo, Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) throws Exception;

	/**
	 * 确认失败库操作(员工)
	 * @param orderItem
	 * @param newStatus
	 * @param operator
	 * @param memo
	 * @return
	 * @throws Exception
	 */
	public ResultHandleT<ComAudit> updateConfirmedStatus(OrdOrderItem orderItem,
                                                         CONFIRM_STATUS newStatus, String operator, String memo) throws Exception;

	/**
	 * 更新子单活动
	 * @param orderItem
	 * @param auditId
	 * @param operator
	 * @param memo
	 * @return
	 * @throws Exception
	 */
	public ResultHandleT<ComAudit> updateOrderConfirmAudit(Long auditId, String operator, String memo) throws Exception;

	/**
	 * 发送通知
	 * @param orderItem
	 * @param operate
	 * @return
	 * @throws
	 */
	public ResultHandle createConfirmOrder(OrdOrderItem orderItem
            , CONFIRM_CHANNEL_OPERATE operate, String operator) throws Exception;
	
	/**
	 * 取消确认
	 * @param auditId 活动ID
	 * @param operator
	 * @return
	 * @throws
	 */
	public ResultHandle cancelConfirm(Long auditId, String operator) throws Exception;
	
	/**
	 * 取消订单
	 * @param orderId
	 * @param cancelCode
	 * @param reason
	 * @param operator
	 * @param memo
	 * @return
	 * @throws Exception
	 * @author majunli
	 * @date 2016-11-10 上午10:57:58
	 */
	public ResultHandle cancelOrder(Long orderId, String cancelCode, String reason
            , String operator, String memo) throws Exception;

    /**
     * 询位处理
     * @param orderItem
     * @param auditId
     * @param operator
     * @param memo
     * @return
     * @throws Exception
     */

	public ResultHandle inquiryConfirm(OrdOrderItem orderItem, Long auditId
			, String operator, String memo,String resourceRetentionTime) throws Exception;

	/**
	 * 新版工作台处理
	 * @param ordOrderItem
	 * @param orderMemo
	 * @param confirmId 确认号
	 * @param status Confirm_Enum.CONFIRM_STATUS
	 * @param operateName
	 * @return
	 * @throws Exception
	 */
	public ResultHandleT<ComAudit> workbenchHandle(OrdOrderItem ordOrderItem, String orderMemo, String confirmId, CONFIRM_STATUS status, String operateName, Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) throws Exception;
	
	/**
	 * 自动关房满房，特殊满房，变价，并禁售当前日期的商品
	 * @param orderItem
	 * @param operator
	 * @param memo
	 * @return
	 * @throws Exception
	 */
	public ResultHandle closeFullhotelAndForbidSale(OrdOrderItem orderItem,String operator,String memo,String sourceType,List<Date> dates,List<Long> suppGoodsId,String orderMemo) throws Exception;
}
