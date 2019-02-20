package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdFunction;

public interface IOrdFunctionService {

	
	public int insertOrdFunction(OrdFunction ordFunction);
	
	public OrdFunction findOrdFunctionById(Long id);
	
	public List<OrdFunction> findOrdFunctionList(Map<String, Object> params);

	public int findOrdFunctionCount(Map<String, Object> params);

	public int updateOrdFunction(OrdFunction ordFunction);


	
}
