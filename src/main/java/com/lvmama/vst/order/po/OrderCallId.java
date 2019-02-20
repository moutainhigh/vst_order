package com.lvmama.vst.order.po;

import java.io.Serializable;
import java.util.Date;

public class OrderCallId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5560851121419043960L;

	private Long id;
	
	private Long orderId;
	
	private String callId;
	
	private String operUserName;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getOperUserName() {
		return operUserName;
	}

	public void setOperUserName(String operUserName) {
		this.operUserName = operUserName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	private Date createTime;
}
