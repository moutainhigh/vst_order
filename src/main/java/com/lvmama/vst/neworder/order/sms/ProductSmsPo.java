package com.lvmama.vst.neworder.order.sms;

public class ProductSmsPo {
	
	/**
	 * 产品名称
	 */
	private String productName;
	
	/**
	 * 地址
	 */
	private String prodAddress;
	
	/**
	 * 电话
	 */
	private String prodTel;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProdAddress() {
		return prodAddress;
	}

	public void setProdAddress(String prodAddress) {
		this.prodAddress = prodAddress;
	}

	public String getProdTel() {
		return prodTel;
	}

	public void setProdTel(String prodTel) {
		this.prodTel = prodTel;
	}
}
