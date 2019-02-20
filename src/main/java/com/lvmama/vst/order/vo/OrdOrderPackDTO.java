package com.lvmama.vst.order.vo;

import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;

public class OrdOrderPackDTO extends OrdOrderPack{

	/**
	 * 
	 */
	private static final long serialVersionUID = 616321373294018635L;

	
	private List<ProdPackageDetail> packageDetailList = new ArrayList<ProdPackageDetail>();

	private List<ProdPackageDetailAddPrice> packageDetailAddPriceList = new ArrayList<ProdPackageDetailAddPrice>();
	
	private com.lvmama.vst.order.vo.OrdOrderDTO order;
	
	public List<ProdPackageDetail> getPackageDetailList() {
		return packageDetailList;
	}

	public void setPackageDetailList(List<ProdPackageDetail> packageDetailList) {
		this.packageDetailList = packageDetailList;
	}


	public List<ProdPackageDetailAddPrice> getPackageDetailAddPriceList() {
		return packageDetailAddPriceList;
	}


	public void setPackageDetailAddPriceList(
			List<ProdPackageDetailAddPrice> packageDetailAddPriceList) {
		this.packageDetailAddPriceList = packageDetailAddPriceList;
	}


	public com.lvmama.vst.order.vo.OrdOrderDTO getOrder() {
		return order;
	}


	public void setOrder(com.lvmama.vst.order.vo.OrdOrderDTO order) {
		this.order = order;
	}

}
