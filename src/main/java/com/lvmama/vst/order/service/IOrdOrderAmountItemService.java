package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrderAmountItem;

/**
 * ord order amount item interface
 * @author qixiaochen
 *
 */
public interface IOrdOrderAmountItemService {
	
	public List<OrdOrderAmountItem> findOrderAmountItemList(Map<String, Object> params);
	
	/**
	 * insert ordOrderAmountItem to database
	 * @param orderAmountItem
	 * @return
	 */
	public int insertOrderAmountItem(OrdOrderAmountItem orderAmountItem);
	
}
