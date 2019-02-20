package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrderNotice;

public interface IOrdProductNotice {
	
	public int insert(OrdOrderNotice record);


	public List<OrdOrderNotice> findOrdNoticeList(Map<String, Object> params);
	
    public List<OrdOrderNotice> findOrdNoticeList_notice(Map<String, Object> params);
	
}
