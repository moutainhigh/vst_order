package com.lvmama.vst.order.web.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 一键下单VO
 * @author zhangdewen
 */
@SuppressWarnings("serial")
public class LineBackOneKeyOrderVO implements Serializable {

	private Long productId;
	private String specDate;
	private Long distributionId;
	private String userId;
	private Long startDistrictId;
	private Long originalOrderId;
	private String orderCreatingManner;
	
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getSpecDate() {
		return specDate;
	}
	public void setSpecDate(String specDate) {
		this.specDate = specDate;
	}
	public Long getDistributionId() {
		return distributionId;
	}
	public void setDistributionId(Long distributionId) {
		this.distributionId = distributionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getStartDistrictId() {
		return startDistrictId;
	}
	public void setStartDistrictId(Long startDistrictId) {
		this.startDistrictId = startDistrictId;
	}
	public Long getOriginalOrderId() {
		return originalOrderId;
	}
	public void setOriginalOrderId(Long originalOrderId) {
		this.originalOrderId = originalOrderId;
	}
	public String getOrderCreatingManner() {
		return orderCreatingManner;
	}
	public void setOrderCreatingManner(String orderCreatingManner) {
		this.orderCreatingManner = orderCreatingManner;
	}	
}
