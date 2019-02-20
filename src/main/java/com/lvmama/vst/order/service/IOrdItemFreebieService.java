package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdItemFreebiesRelation;
import com.lvmama.vst.back.order.po.OrdOrderItem;

public interface IOrdItemFreebieService {

	int batchInsertOrdItemFreebie(List<OrdItemFreebiesRelation> ordItemfreebies);
	
	
	int batchUpdateOrdItemFreebie(List<OrdItemFreebiesRelation> ordItemfreebies);
	
	
	public void cancelFreebie(List<OrdOrderItem> orderItemlist);
	
	
	int insert(OrdItemFreebiesRelation ordItemfreebie);
	
	List<OrdItemFreebiesRelation> queryFreebieListByItem(Map<String, Object> params);
}
