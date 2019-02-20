package com.lvmama.vst.order.vo;

import java.util.Date;

public class OrdItemShowTicketInfoVO {
    private Long orderItemId;
    private Long priceId;
	private Long price;
    private Long priceType;
    public Long getPriceId() {
		return priceId;
	}
	public void setPriceId(Long priceId) {
		this.priceId = priceId;
	}
	public Long getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}
	
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public Long getPriceType() {
		return priceType;
	}
	public void setPriceType(Long priceType) {
		this.priceType = priceType;
	} 
	
	private Date updateTime;

	 public Date getUpdateTime() {
		 return updateTime;
	}

	 public void setUpdateTime(Date updateTime) {
		 this.updateTime = updateTime;
	}
}
