package com.lvmama.vst.order.vo;

import java.io.Serializable;

import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

/**
 * 
 * @author sunjian
 *
 */
public class OrdOrderUpdateStockDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	private Long timePriceId;
	
	private Long updateStock;
	
	private SuppGoodsBaseTimePrice timePrice;
	
	//解决bug80192，为了不序列化OrderTimePriceService，加上transient
	private transient OrderTimePriceService orderTimePriceService;

	public Long getTimePriceId() {
		return timePriceId;
	}

	public void setTimePriceId(Long timePriceId) {
		this.timePriceId = timePriceId;
	}

	public Long getUpdateStock() {
		return updateStock;
	}

	public void setUpdateStock(Long updateStock) {
		this.updateStock = updateStock;
	}

	public OrderTimePriceService getOrderTimePriceService() {
		return orderTimePriceService;
	}

	public void setOrderTimePriceService(OrderTimePriceService orderTimePriceService) {
		this.orderTimePriceService = orderTimePriceService;
	}

	public SuppGoodsBaseTimePrice getTimePrice() {
		return timePrice;
	}

	public void setTimePrice(SuppGoodsBaseTimePrice timePrice) {
		this.timePrice = timePrice;
	}
	
	public void addUpdateStock(Long updateStock) {
		if (updateStock != null) {
			if (this.updateStock == null) {
				this.updateStock = updateStock;
			} else {
				this.updateStock = this.updateStock + updateStock;
			}
		}
	}
}
