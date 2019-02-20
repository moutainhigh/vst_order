package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdFlightTicketInfo;

public interface IOrdFlightTicketInfoService {
	OrdFlightTicketInfo findByInfoId(Long infoId) ;
	
	List<OrdFlightTicketInfo> findByCondition(Map<String, Object> params);
	
	int addFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo);

	int updateFlightTicketInfo(Map<String, Object> params);
	
	int updateFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo);
	
	int saveFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo);
}
