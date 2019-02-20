package com.lvmama.vst.order.confirm.vo;

import java.util.Date;
import java.util.List;

import com.lvmama.vst.back.goods.po.SuppGoods;

public class ComfirmSuppGoodsSpecDateParamVo {
	private Long suppGoodsId;
	private List<Date> specDateList;
	private SuppGoods suppGoods;
	public Long getSuppGoodsId() {
		return suppGoodsId;
	}
	public void setSuppGoodsId(Long suppGoodsId) {
		this.suppGoodsId = suppGoodsId;
	}
	public List<Date> getSpecDateList() {
		return specDateList;
	}
	public void setSpecDateList(List<Date> specDateList) {
		this.specDateList = specDateList;
	}
	public SuppGoods getSuppGoods() {
		return suppGoods;
	}
	public void setSuppGoods(SuppGoods suppGoods) {
		this.suppGoods = suppGoods;
	}
	
}
