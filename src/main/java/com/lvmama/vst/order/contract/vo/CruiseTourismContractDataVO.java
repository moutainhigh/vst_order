package com.lvmama.vst.order.contract.vo;

import java.io.Serializable;
import java.util.List;

import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdLineRoute;

public class CruiseTourismContractDataVO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String productName; //产品名称

	private String lineShipDesc;//停靠港口

	private String  departurePlace;//出发地点

	private String  returnPlace;//返回地点

	private String  minPersonCountOfGroup;//最低成团人数

	private String supplementaryTerms;//产品信息描述

	List<ProdContractDetail> shopingDetailList;//购物说明

	List<ProdContractDetail> recommendDetailList;//推荐项目
	
	private LineRoute shipLineRoute;//行程信息
	
	private ProdLineRoute lineRoute;//行程信息

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLineShipDesc() {
		return lineShipDesc;
	}

	public void setLineShipDesc(String lineShipDesc) {
		this.lineShipDesc = lineShipDesc;
	}

	public String getDeparturePlace() {
		return departurePlace;
	}

	public void setDeparturePlace(String departurePlace) {
		this.departurePlace = departurePlace;
	}

	public String getReturnPlace() {
		return returnPlace;
	}

	public void setReturnPlace(String returnPlace) {
		this.returnPlace = returnPlace;
	}

	public String getMinPersonCountOfGroup() {
		return minPersonCountOfGroup;
	}

	public void setMinPersonCountOfGroup(String minPersonCountOfGroup) {
		this.minPersonCountOfGroup = minPersonCountOfGroup;
	}

	public String getSupplementaryTerms() {
		return supplementaryTerms;
	}

	public void setSupplementaryTerms(String supplementaryTerms) {
		this.supplementaryTerms = supplementaryTerms;
	}

	public List<ProdContractDetail> getShopingDetailList() {
		return shopingDetailList;
	}

	public void setShopingDetailList(List<ProdContractDetail> shopingDetailList) {
		this.shopingDetailList = shopingDetailList;
	}

	public List<ProdContractDetail> getRecommendDetailList() {
		return recommendDetailList;
	}

	public void setRecommendDetailList(List<ProdContractDetail> recommendDetailList) {
		this.recommendDetailList = recommendDetailList;
	}

	public LineRoute getShipLineRoute() {
		return shipLineRoute;
	}

	public void setShipLineRoute(LineRoute shipLineRoute) {
		this.shipLineRoute = shipLineRoute;
	}

	public ProdLineRoute getLineRoute() {
		return lineRoute;
	}

	public void setLineRoute(ProdLineRoute lineRoute) {
		this.lineRoute = lineRoute;
	}
}
