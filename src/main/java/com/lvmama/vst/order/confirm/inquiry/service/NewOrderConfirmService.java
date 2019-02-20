package com.lvmama.vst.order.confirm.inquiry.service;

import java.util.List;
import java.util.Set;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.vo.ConfirmParamVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;
/**
 * 询单系统适配
 * @Author: kemeisong
 * @Date: 2018/4/9 10:33
 */
public interface NewOrderConfirmService {
	/**
	 * 创建通知
	 * @param confirmParamVo
	 * @return
	 * @throws Exception 
	 */
	public ResultHandleT<ComAudit> createConfirmOrder(ConfirmParamVo confirmParamVo) throws Exception;
	
	/**
	 * 已审状态操作(人工),同步供应商凭证信息
	 * @param orderItem
	 * @param newStatus
	 * @param supplierNo 确认号
	 * @param operator
	 * @param linkId
	 * @return
	 */
	public ResultHandle updateSupplierProcess(OrdOrderItem orderItem, CONFIRM_STATUS newStatus
			, String supplierNo, String operator, Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel);

	/**
	 * 根据主订单调询单系统创建供应商订单---走询单系统
	 * @param order
	 * @return
	 */
	public List<SuppOrderResult> createSupplierOrder(OrdOrder order);
}



