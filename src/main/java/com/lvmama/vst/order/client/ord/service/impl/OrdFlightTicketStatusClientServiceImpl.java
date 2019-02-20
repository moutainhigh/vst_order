package com.lvmama.vst.order.client.ord.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdFlightTicketStatusService;
import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.order.service.IOrdFlightTicketStatusService;

@Component("ordFlightTicketStatusClientServiceRemote")
public class OrdFlightTicketStatusClientServiceImpl implements
		OrdFlightTicketStatusService {
	
	@Autowired
	private IOrdFlightTicketStatusService ordFlightTicketStatusService;

	@Override
	public Long getTotalCount(Map<String, Object> params) {
		return ordFlightTicketStatusService.getTotalCount(params);
	}
	
	@Override
	public int saveFlightTicketStatus(OrdFlightTicketStatus ordFlightTicketStatus){
		return ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
	}

}