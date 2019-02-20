package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;

public interface IOrdFlightTicketStatusService {
	OrdFlightTicketStatus findByStatusId(Long statusId);
	
	List<OrdFlightTicketStatus> findByCondition(Map<String, Object> params);
	
	int addFlightTicketStatus(OrdFlightTicketStatus ordFlightTicketStatus);

	int updateFlightTicketStatus(Map<String, Object> params);
	
	int updateFlightTicketStatus(OrdFlightTicketStatus ordFlightTicketStatus);
	
	int saveFlightTicketStatus(OrdFlightTicketStatus ordFlightTicketStatus);
	
	public Long getTotalCount(Map<String, Object> params);
}
