package com.lvmama.vst.order.vo;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.supp.po.SuppOrderResult;

public class OrderSupplierOperateResult {
	private boolean isSuccess = true;
	
	private String errMsg = null;
	
	private boolean isRetry = false;
	
	private OrdOrder vstOrder = null;

	private List<SuppOrderResult> suppOrderResultList;
	
	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.isSuccess = false;
		this.errMsg = errMsg;
	}

	public boolean isRetry() {
		return isRetry;
	}

	public void setRetry(boolean isRetry) {
		this.isRetry = isRetry;
	}

	public OrdOrder getVstOrder() {
		return vstOrder;
	}

	public void setVstOrder(OrdOrder vstOrder) {
		this.vstOrder = vstOrder;
	}

	public List<SuppOrderResult> getSuppOrderResultList() {
		return suppOrderResultList;
	}

	public void setSuppOrderResultList(List<SuppOrderResult> suppOrderResultList) {
		this.suppOrderResultList = suppOrderResultList;
	}
	
}
