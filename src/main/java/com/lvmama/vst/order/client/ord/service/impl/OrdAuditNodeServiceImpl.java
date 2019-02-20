package com.lvmama.vst.order.client.ord.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdAuditNodeService;
import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.order.service.IOrdFunctionService;
@Component("ordAuditNodeServiceRemote")
public class OrdAuditNodeServiceImpl implements OrdAuditNodeService {

	@Autowired
	private IOrdFunctionService ordFunctionService;
	
	@Override
	public List<OrdFunction> findOrdFunctionList(Map<String, Object> params) {
		List<OrdFunction> ordFunctions = ordFunctionService.findOrdFunctionList(params);
		if(ordFunctions == null){
			ordFunctions = new ArrayList<OrdFunction>();
		}
		return ordFunctions;
	}

}
