package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated;

public interface ISupplierOrderHandleService {
	public ResultHandle updateAmpleResourceStatus(SuppOrderRelated suppOrderRelated);
	public ResultHandle updateUnperformStatus(SuppOrderRelated suppOrderRelated);
	public ResultHandle updatePerformStatus(SuppOrderRelated suppOrderRelated);
	public ResultHandle updateOrderCancelStatus(SuppOrderRelated suppOrderRelated);
	public ResultHandleT<OrdPassCode> saveOrdTicketPerform(OrdTicketPerform ordTicketPerform);
	public ResultHandleT<List<OrdOrderItem>> checkOrderTicketValid(final String  addCode);
	public void savePassCode(final List<OrdPassCode> list);
	public void updatePassCode(final List<OrdPassCode> list);
	public List<OrdPassCode> getOrdPassCodeByCheckInAndCode(Long checkInId,String addCode);
	public List<OrdPassCode> selectOrdPassCodeByParams(Map<String,Object> params);
	public List<OrdTicketPerform> selectOrdTicketPerforms(Map<String,Object> params);
	public OrdPassCode getOrdPassCodeByOrderItemId(Long orderItemInId);
	public boolean getProductIfYL(Long productId);
}
