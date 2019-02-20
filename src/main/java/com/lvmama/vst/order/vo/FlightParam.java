package com.lvmama.vst.order.vo;

/**
 * 后台 更换交通 机票参数
 */
public class FlightParam{
	
	private Long price;

	private Long adultPrice;
	
	private Long childPrice;
	
	private Long packageGroupId;
	
	private Long packageProductId;
	
	private Long packageProductBranchId;
	
	private Long selectedSuppGoodsId;

	public Long getPackageGroupId() {
		return packageGroupId;
	}

	public void setPackageGroupId(Long packageGroupId) {
		this.packageGroupId = packageGroupId;
	}

	public Long getPackageProductId() {
		return packageProductId;
	}

	public void setPackageProductId(Long packageProductId) {
		this.packageProductId = packageProductId;
	}

	public Long getPackageProductBranchId() {
		return packageProductBranchId;
	}

	public void setPackageProductBranchId(Long packageProductBranchId) {
		this.packageProductBranchId = packageProductBranchId;
	}

	public Long getSelectedSuppGoodsId() {
		return selectedSuppGoodsId;
	}

	public void setSelectedSuppGoodsId(Long selectedSuppGoodsId) {
		this.selectedSuppGoodsId = selectedSuppGoodsId;
	}

	public Long getAdultPrice() {
		return adultPrice;
	}

	public void setAdultPrice(Long adultPrice) {
		this.adultPrice = adultPrice==null?0L:adultPrice;
	}

	public Long getChildPrice() {
		return childPrice;
	}

	public void setChildPrice(Long childPrice) {
		this.childPrice = childPrice==null?0L:childPrice;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price==null?0L:price;
	}

	@Override
	public String toString() {
		return "FlightParam [price=" + price + ", adultPrice=" + adultPrice
				+ ", childPrice=" + childPrice + ", packageGroupId="
				+ packageGroupId + ", packageProductId=" + packageProductId
				+ ", packageProductBranchId=" + packageProductBranchId
				+ ", selectedSuppGoodsId=" + selectedSuppGoodsId + "]";
	}
}
