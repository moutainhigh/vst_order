package com.lvmama.vst.order.web.line;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;

public class LineProdPackageGroupContainer {
	
	
	public final static String LINE_TICKET_single_ticket="LINE_TICKET_single_ticket";
	
	public final static String LINE_TICKET_other_ticket="LINE_TICKET_other_ticket";

	public final static String LINE_TICKET_comb_ticket="LINE_TICKET_comb_ticket";
	
	public final static String LINE_hotelcomb="LINE_hotelcomb";
	
	public final static String LINE_group="LINE_group";
	
	public final static String LINE_freedom="LINE_freedom";
	
	public final static String LINE_local="LINE_local";
	
	
	// 1.供应商打包的跟团游和自由行的（更换酒店和升级）,（更换酒店一个组对应一个行程，一个组下有多个规格)
	private List<ProdPackageGroup> updateProdPackageList = null;
	private List<ProdPackageGroup> changeProdPackageList = null;

	// 2.自主打包产品信息（一个组对应一个行程，一个组下有多个规格)
	// 酒店产品
	private List<ProdPackageGroup> hotelProdPackageList = null;
	// 门票产品
	private List<ProdPackageGroup> ticketProdPackageList = null;
	// 交通产品
	private List<ProdPackageGroup> transprotProdPackageList = null;

	// 线路酒店套餐
	private List<ProdPackageGroup> lineHotelCombPackageList = null;
	// 线路跟团游
	private List<ProdPackageGroup> lineGroupPackageList = null;
	// 线路自由行
	private List<ProdPackageGroup> lineFreedomPackageList = null;
	// 线路当地游
	private List<ProdPackageGroup> lineLocalPackageList = null;
	
	//所有打包信息列表
	private List<ProdPackageGroup> allPackageList=null;
	
	// 是否存在打包信息
	private boolean hasPackage = false;
	
	/**
	 * 产品对应的时间
	 */
	private Map<Long,Date> productTimeMap=new HashMap<Long, Date>();

	public List<ProdPackageGroup> getUpdateProdPackageList() {
		return updateProdPackageList;
	}

	public void setUpdateProdPackageList(
			List<ProdPackageGroup> updateProdPackageList) {
		this.updateProdPackageList = updateProdPackageList;
	}

	public List<ProdPackageGroup> getChangeProdPackageList() {
		return changeProdPackageList;
	}

	public void setChangeProdPackageList(
			List<ProdPackageGroup> changeProdPackageList) {
		this.changeProdPackageList = changeProdPackageList;
	}

	public List<ProdPackageGroup> getHotelProdPackageList() {
		return hotelProdPackageList;
	}

	public void setHotelProdPackageList(
			List<ProdPackageGroup> hotelProdPackageList) {
		this.hotelProdPackageList = hotelProdPackageList;
	}

	public List<ProdPackageGroup> getTicketProdPackageList() {
		return ticketProdPackageList;
	}

	public void setTicketProdPackageList(
			List<ProdPackageGroup> ticketProdPackageList) {
		this.ticketProdPackageList = ticketProdPackageList;
	}

	public List<ProdPackageGroup> getTransprotProdPackageList() {
		return transprotProdPackageList;
	}

	public void setTransprotProdPackageList(
			List<ProdPackageGroup> transprotProdPackageList) {
		this.transprotProdPackageList = transprotProdPackageList;
	}

	public List<ProdPackageGroup> getLineHotelCombPackageList() {
		return lineHotelCombPackageList;
	}

	public void setLineHotelCombPackageList(
			List<ProdPackageGroup> lineHotelCombPackageList) {
		this.lineHotelCombPackageList = lineHotelCombPackageList;
	}

	public List<ProdPackageGroup> getLineGroupPackageList() {
		return lineGroupPackageList;
	}

	public void setLineGroupPackageList(
			List<ProdPackageGroup> lineGroupPackageList) {
		this.lineGroupPackageList = lineGroupPackageList;
	}

	public List<ProdPackageGroup> getLineFreedomPackageList() {
		return lineFreedomPackageList;
	}

	public void setLineFreedomPackageList(
			List<ProdPackageGroup> lineFreedomPackageList) {
		this.lineFreedomPackageList = lineFreedomPackageList;
	}

	public List<ProdPackageGroup> getLineLocalPackageList() {
		return lineLocalPackageList;
	}

	public void setLineLocalPackageList(
			List<ProdPackageGroup> lineLocalPackageList) {
		this.lineLocalPackageList = lineLocalPackageList;
	}	

	public List<ProdPackageGroup> getAllPackageList() {
		return allPackageList;
	}

	public void setAllPackageList(List<ProdPackageGroup> allPackageList) {
		this.allPackageList = allPackageList;
	}

	public boolean isHasPackage() {
		return hasPackage;
	}

	public void setHasPackage(boolean hasPackage) {
		this.hasPackage = hasPackage;
	}
	
	public void addProductTimeMapItem(Long productId,Date date){
		this.productTimeMap.put(productId, date);
	}
	public Date getProductTimeMapItem(Long productId){
		return this.productTimeMap.get(productId);
	}
}
