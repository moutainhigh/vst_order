package com.lvmama.vst.order.web.insurance.vo;
/**
 * 
 * 订单-保险业务-查询VO
 * @author zhaomingzhu
 * 
 */
public class OrderInsuranceProductVo {
	/*产品编号*/
	private String productId;
	/*产品名称*/
	private String productName;
	/*被保天数*/
	private String daysType;
	/*保险类型*/
    private String insurType;
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getDaysType() {
		return daysType;
	}
	public void setDaysType(String daysType) {
		this.daysType = daysType;
	}
	public String getInsurType() {
		return insurType;
	}
	public void setInsurType(String insurType) {
		this.insurType = insurType;
	}
}
