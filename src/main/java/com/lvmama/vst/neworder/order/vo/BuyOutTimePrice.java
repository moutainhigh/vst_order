package com.lvmama.vst.neworder.order.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.vo.OrdOrderUpdateStockDTO;

public class BuyOutTimePrice extends BaseTimePrice {
	
 //酒套餐时间价格信息
	private List<Map<Long,BuyOutTimePrice>>  buyOutTimePriceList ;
	
	private List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();

	/**
	 * 辅助价格计算
	 */
	private Long bakPrice;

	public List<Map<Long, BuyOutTimePrice>> getBuyOutTimePriceList() {
		return buyOutTimePriceList;
	}

	public void setBuyOutTimePriceList(List<Map<Long, BuyOutTimePrice>> buyOutTimePriceList) {
		this.buyOutTimePriceList = buyOutTimePriceList;
	}

	private Long markerPrice;

	/**
	 * 该买断商品的数量
	 */
	private Long buyoutQuantity;

	private Long buyoutTotalPrice;

	private Long notBuyoutSettleAmout;
	


	/**
	 * 该买断商品的单价
	 */
	private Long buyoutPrice;

	private String buyoutFlag;

	private transient Date aheadTime;
	
	/**
	 * 星云项目ID
	 */
	private Long nebulaProjectId;	

	public Date getAheadTime() {
		return aheadTime;
	}

	public void setAheadTime(Date aheadTime) {
		this.aheadTime = aheadTime;
	}

	public List<OrdOrderStock> getOrderStockList() {
		return orderStockList;
	}

	public void setOrderStockList(List<OrdOrderStock> orderStockList) {
		this.orderStockList = orderStockList;
	}

	public Long getBakPrice() {
		return bakPrice;
	}

	public void setBakPrice(Long bakPrice) {
		this.bakPrice = bakPrice;
	}

	public Long getMarkerPrice() {
		return markerPrice;
	}

	public void setMarkerPrice(Long markerPrice) {
		this.markerPrice = markerPrice;
	}

	public Long getBuyoutQuantity() {
		return buyoutQuantity;
	}

	public void setBuyoutQuantity(Long buyoutQuantity) {
		this.buyoutQuantity = buyoutQuantity;
	}

	public Long getBuyoutTotalPrice() {
		return buyoutTotalPrice;
	}

	public void setBuyoutTotalPrice(Long buyoutTotalPrice) {
		this.buyoutTotalPrice = buyoutTotalPrice;
	}

	public Long getNotBuyoutSettleAmout() {
		return notBuyoutSettleAmout;
	}

	public void setNotBuyoutSettleAmout(Long notBuyoutSettleAmout) {
		this.notBuyoutSettleAmout = notBuyoutSettleAmout;
	}

	public Long getBuyoutPrice() {
		return buyoutPrice;
	}

	public void setBuyoutPrice(Long buyoutPrice) {
		this.buyoutPrice = buyoutPrice;
	}

	public String getBuyoutFlag() {
		return buyoutFlag;
	}

	public void setBuyoutFlag(String buyoutFlag) {
		this.buyoutFlag = buyoutFlag;
	}

	public Long getNebulaProjectId() {
		return nebulaProjectId;
	}

	public void setNebulaProjectId(Long nebulaProjectId) {
		this.nebulaProjectId = nebulaProjectId;
	}
	
}
