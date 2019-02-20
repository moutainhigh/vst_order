package com.lvmama.vst.order.po;

import java.util.Date;

public class OrdAuditProcessTask {
	
	/**
	 * 订单号
	 */
	private Long orderId;
	/**
	 * 执行状态，Y:成功，N:失败
	 */
	private String status;
	/**
	 * 补偿次数
	 */
	private Long times;
	/**
	 * 记录时间
	 */
	private Date createTime;
	
	/**更新时间*/
	private Date updateTime;
	
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getTimes() {
		return times;
	}

	public void setTimes(Long times) {
		this.times = times;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public OrdAuditProcessTask() {
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
