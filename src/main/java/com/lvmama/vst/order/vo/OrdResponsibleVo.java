/**
 * 
 */
package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.Date;

import com.lvmama.vst.comm.vo.order.OrderMonitorRst;

/**
 * @author pengyayun
 *
 */
public class OrdResponsibleVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4752604720690177227L;
 
	private Long objectId;
	
	private String distributorName;//订单来源
	
	private Long orderId;//订单号
	
	private Long orderItemId;//子单号
	
	private Long productId;//产品编号
	
	private String productName;//产品名称
	
	private String productType;//产品类型
	
	private String supplierName;//供应商名称
	
	private String createTime;//下单时间
	
	private String visitTime;//入住时间，时间区段
	
	private String contactName;//联系人
	
	private String contactMobile;//联系人手机
	
	private String currentStatus;//订单组合状态
	
	private String principal;//负责人
	
	private String department;//订单负责人所从属的组

	public String getDistributorName() {
		return distributorName;
	}

	public void setDistributorName(String distributorName) {
		this.distributorName = distributorName;
	}

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

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(String visitTime) {
		this.visitTime = visitTime;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
}
