package com.lvmama.vst.order.po;

import java.io.Serializable;
import java.util.Date;

public class OrdConfirmProcessJob implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -323854169706689997L;
	
	/**订单ID*/
	private Long orderId;
	/**子订单ID*/
	private Long orderItemId;
	/**活动ID*/
	private Long auditId;
	/**补偿次数*/
	private Long times;
	/**创建时间*/
	private Date createTime;
	/**更新时间*/
	private Date updateTime;
	
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}
	public Long getAuditId() {
		return auditId;
	}
	public void setAuditId(Long auditId) {
		this.auditId = auditId;
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
	
}
