package com.lvmama.vst.order.service.book;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdProduct;

public class CreateOrdTravelContractData {
	private String EcontractTemplate;
	private List<OrdOrderItem> orderItemList;
	private ProdProduct parentProduct;
	
	public List<OrdOrderItem> getOrderItemList() {
		return orderItemList;
	}
	public void setOrderItemList(List<OrdOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}
	public ProdProduct getParentProduct() {
		return parentProduct;
	}
	public void setParentProduct(ProdProduct parentProduct) {
		this.parentProduct = parentProduct;
	}
	public String getEcontractTemplate() {
		return EcontractTemplate;
	}
	public void setEcontractTemplate(String econtractTemplate) {
		EcontractTemplate = econtractTemplate;
	}
	
	
	
}
