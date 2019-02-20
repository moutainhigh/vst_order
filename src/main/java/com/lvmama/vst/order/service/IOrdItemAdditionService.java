package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdItemAddition;

import java.util.List;
import java.util.Map;


/**
 *
 */
public interface IOrdItemAdditionService {

	
	public int addOrdItemAddition(OrdItemAddition ordAdditionStatus);
	
	public OrdItemAddition findOrdItemAdditionById(Long id);
	
	public List<OrdItemAddition> findOrdItemAdditionList(Map<String, Object> params);

	public int updateByPrimaryKeySelective(OrdItemAddition ordAdditionStatus);
}
