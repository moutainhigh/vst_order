package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.Date;

import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.order.po.OrdOrderItem;

public class OrderItemDecorator extends OrdOrderItem implements Serializable {
	private static final long serialVersionUID = -3652392776609328276L;

	private Long distributorId;

	private Long supplierId;

	private Long goodsId;

	private Date visitTime;

	private OrdOrderItem orderItem;

	private TimePrice timePrice;

	public Long getDistributorId() {
		return distributorId;
	}

	public void setDistributorId(Long distributorId) {
		this.distributorId = distributorId;
	}

	public Long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	@Override
	public Date getVisitTime() {
		return visitTime;
	}

	@Override
	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

	public OrdOrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrdOrderItem orderItem) {
		this.orderItem = orderItem;
	}

	public TimePrice getTimePrice() {
		return timePrice;
	}

	public void setTimePrice(TimePrice timePrice) {
		this.timePrice = timePrice;
	}
}
