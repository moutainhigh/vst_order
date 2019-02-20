package com.lvmama.vst.order.contract.vo;

import java.io.Serializable;
import java.util.List;

import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdLineRoute;

public class TeamWithInTerritoryContractDataVO implements Serializable{

	private static final long serialVersionUID = -7560745946748976850L;
	
	//补充条款
	private String supplementaryTerms;
	
	//购物说明
	List<ProdContractDetail> shopingDetailList;
	
	//推荐项目
	List<ProdContractDetail> recommendDetailList;
	
	//行程信息
	private LineRoute shipLineRoute;
	
	private ProdLineRoute lineRoute;

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
