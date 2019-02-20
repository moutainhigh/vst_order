package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderPersonRelationService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;

@Component("orderPersonRelationServiceRemote")
public class OrderPersonRelationServiceImpl implements OrderPersonRelationService {

	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Override
	public List<OrdItemPersonRelation> findOrdItemPersonRelationList(Long orderId) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId); 
		return ordItemPersonRelationService.findOrdItemPersonRelationList(params);
	}

}
