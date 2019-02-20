package com.lvmama.vst.neworder.order.sms;

public class GoodsSmsPo {
	
	private String goodsName;
	
	/**
	 * 最晚预定
	 */
	private String lastTime;
	
	/**
	 * 担保时间
	 */
	private String guaranteeTime;

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getLastTime() {
		return lastTime;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	public String getGuaranteeTime() {
		return guaranteeTime;
	}

	public void setGuaranteeTime(String guaranteeTime) {
		this.guaranteeTime = guaranteeTime;
	}
}
