package com.lvmama.vst.order.po;

import java.util.ArrayList;
import java.util.List;

public class OverdueTicketSubOrderStatusPack {
	private Integer status;
	private String desc;

	private List<Long> idList = new ArrayList<Long>();

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<Long> getIdList() {
		return idList;
	}

	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
