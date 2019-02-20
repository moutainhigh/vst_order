package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdItemPersonRelationClientService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;

@Component("ordItemPersonRelationServiceRemote")
public class OrdItemPersonRelationClientServiceImpl implements OrdItemPersonRelationClientService {
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Override
	public OrdItemPersonRelation findOrdItemPersonRelationById(Long id) {
		return ordItemPersonRelationService.findOrdItemPersonRelationById(id);
	}

	@Override
	public List<OrdItemPersonRelation> findOrdItemPersonRelationList(Map<String, Object> params) {
		return ordItemPersonRelationService.findOrdItemPersonRelationList(params);
	}

}
