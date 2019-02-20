package com.lvmama.vst.order.dao.datamodel;

import java.io.Serializable;
import java.util.Date;

public class RawTicketOrderInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6867582231514050672L;
	private Long orderId;
	private String scenicSpotName;
	private Date visitTime;
	private String visitorName;
	private String content;

	public RawTicketOrderInfo() {
	}

	public RawTicketOrderInfo(Long orderId, String scenicSpotName, Date visitTime, String visitorName,
			String content) {
		this.orderId = orderId;
		this.scenicSpotName = scenicSpotName;
		this.visitTime = visitTime;
		this.visitorName = visitorName;
		this.content = content;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getScenicSpotName() {
		return scenicSpotName;
	}

	public void setScenicSpotName(String scenicSpotName) {
		this.scenicSpotName = scenicSpotName;
	}

	public Date getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

	public String getVisitorName() {
		return visitorName;
	}

	public void setVisitorName(String visitorName) {
		this.visitorName = visitorName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
