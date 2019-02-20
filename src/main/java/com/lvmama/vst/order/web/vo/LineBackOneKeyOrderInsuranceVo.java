package com.lvmama.vst.order.web.vo;

import java.io.Serializable;

public class LineBackOneKeyOrderInsuranceVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7859791349467294328L;

	private Long suppGoodsId;
	
	private Long quantity;

	public Long getSuppGoodsId() {
		return suppGoodsId;
	}

	public void setSuppGoodsId(Long suppGoodsId) {
		this.suppGoodsId = suppGoodsId;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}
	
}
