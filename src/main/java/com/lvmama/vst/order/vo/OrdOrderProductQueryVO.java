package com.lvmama.vst.order.vo;

import java.io.Serializable;
import java.util.Date;

import com.lvmama.vst.comm.utils.DateUtil;

/***
 * 订单商品PO
 * 
 * @author mayonghua
 * 
 */
public class OrdOrderProductQueryVO implements Serializable {
	
	private Long productId;//酒店ID

	private String productName;//酒店名称

	private String startDate;//入住日期

	private String endDate;//离店日期

	private Integer days;// 入住天数

	private boolean establishmentDateFlag;

	private boolean renovationDateFlag;

	private int renovationInnerDate;

	private String bedType;//床型要求,目前变为单选
	
	private String[] bedTypes;//床型要求数组

	private String starRate;//酒店星级

	private String[] starRates;//酒店星级数组

	private boolean smokelessRoomFlag;

	private boolean facilitiesForSwimPoorFlag;

	private String[] productIds;

	private String priceRange;//价格区间

	private String[] priceRanges;//价格区间数组

	private boolean bookLimitTypeFlag;

	private boolean internetFlag;

	private Long districtId;//行政区域ID
	
	private String districtName;

	private Long suppGoodsId;

	private String[] propCodes;
	
	private Integer recommendLevel;//推荐级别
	
	private String payTarget;//支付方式
	
	private String[] payTargets;//支付方式数组
	
	private String facilities;//设施列表
	
	private String[] facilitieses;//设施列表数组
	
	private Integer priceBegin;//自定义价格区间开始
	
	private Integer priceEnd;//自定义价格区间结束
	
	private String[] categoryIds;//品类id
	
	private String subCategoryId;
	
	private String keyWords;//关键字

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public String[] getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(String[] categoryIds) {
		this.categoryIds = categoryIds;
	}

	public Integer getPriceBegin() {
		return priceBegin;
	}

	public void setPriceBegin(Integer priceBegin) {
		this.priceBegin = priceBegin;
	}

	public Integer getPriceEnd() {
		return priceEnd;
	}

	public void setPriceEnd(Integer priceEnd) {
		this.priceEnd = priceEnd;
	}

	public String getPayTarget() {
		return payTarget;
	}

	public void setPayTarget(String payTarget) {
		this.payTarget = payTarget;
	}

	public Integer getRecommendLevel() {
		return recommendLevel;
	}

	public void setRecommendLevel(Integer recommendLevel) {
		this.recommendLevel = recommendLevel;
	}

	public Long getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Long districtId) {
		this.districtId = districtId;
	}

	public String[] getProductIds() {
		return productIds;
	}

	public void setProductIds(String[] productIds) {
		this.productIds = productIds;
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

	public boolean getEstablishmentDateFlag() {
		return establishmentDateFlag;
	}

	public void setEstablishmentDateFlag(boolean establishmentDateFlag) {
		this.establishmentDateFlag = establishmentDateFlag;
	}

	public boolean getRenovationDateFlag() {
		return renovationDateFlag;
	}

	public void setRenovationDateFlag(boolean renovationDateFlag) {
		this.renovationDateFlag = renovationDateFlag;
	}

	public String[] getStarRates() {
		return starRates;
	}

	public void setStarRates(String[] starRates) {
		this.starRates = starRates;
	}

	public boolean isInternetFlag() {
		return internetFlag;
	}

	public void setInternetFlag(boolean internetFlag) {
		this.internetFlag = internetFlag;
	}

	public boolean isSmokelessRoomFlag() {
		return smokelessRoomFlag;
	}

	public void setSmokelessRoomFlag(boolean smokelessRoomFlag) {
		this.smokelessRoomFlag = smokelessRoomFlag;
	}

	public boolean isFacilitiesForSwimPoorFlag() {
		return facilitiesForSwimPoorFlag;
	}

	public void setFacilitiesForSwimPoorFlag(boolean facilitiesForSwimPoorFlag) {
		this.facilitiesForSwimPoorFlag = facilitiesForSwimPoorFlag;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Date getEndDateDate() {
		return DateUtil.toDate(endDate, "yyyy-MM-dd");
	}

	public Date getStartDateDate() {
		return DateUtil.toDate(startDate, "yyyy-MM-dd");
	}

	public int getRenovationInnerDate() {
		return renovationInnerDate;
	}

	public void setRenovationInnerDate(int renovationInnerDate) {
		this.renovationInnerDate = renovationInnerDate;
	}

	public String getStarRate() {
		return starRate;
	}

	public void setStarRate(String starRate) {
		this.starRate = starRate;
	}

	public String getPriceRange() {
		return priceRange;
	}

	public void setPriceRange(String priceRange) {
		this.priceRange = priceRange;
	}

	public String[] getPriceRanges() {
		return priceRanges;
	}

	public void setPriceRanges(String[] priceRanges) {
		this.priceRanges = priceRanges;
	}

	public boolean isBookLimitTypeFlag() {
		return bookLimitTypeFlag;
	}

	public void setBookLimitTypeFlag(boolean bookLimitTypeFlag) {
		this.bookLimitTypeFlag = bookLimitTypeFlag;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	public Long getSuppGoodsId() {
		return suppGoodsId;
	}

	public void setSuppGoodsId(Long suppGoodsId) {
		this.suppGoodsId = suppGoodsId;
	}

	public String[] getPropCodes() {
		return propCodes;
	}

	public void setPropCodes(String[] propCodes) {
		this.propCodes = propCodes;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}
	
	public String[] getPayTargets() {
		if(payTargets!=null && payTargets.length==0){
			return null;
		}
		return payTargets;
	}

	public void setPayTargets(String[] payTargets) {
		this.payTargets = payTargets;
	}

	public String getBedType() {
		return bedType;
	}

	public void setBedType(String bedType) {
		this.bedType = bedType;
	}

	public String[] getBedTypes() {
		if(bedTypes!=null && bedTypes.length==0){
			return null;
		}
		return bedTypes;
	}

	public void setBedTypes(String[] bedTypes) {
		this.bedTypes = bedTypes;
	}

	public String getFacilities() {
		return facilities;
	}

	public void setFacilities(String facilities) {
		this.facilities = facilities;
	}

	public String[] getFacilitieses() {
		return facilitieses;
	}

	/**
	 * @param facilitieses
	 */
	public void setFacilitieses(String[] facilitieses) {
		this.facilitieses = facilitieses;
	}

	/**
	 * @return the subCategoryId
	 */
	public String getSubCategoryId() {
		return subCategoryId;
	}

	/**
	 * @param subCategoryId the subCategoryId to set
	 */
	public void setSubCategoryId(String subCategoryId) {
		this.subCategoryId = subCategoryId;
	}


}
