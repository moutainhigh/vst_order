package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.order.dao.OrdFlightTicketStatusDao;
import com.lvmama.vst.order.service.IOrdFlightTicketStatusService;

@Service
public class OrdFlightTicketStatusServiceImpl implements
		IOrdFlightTicketStatusService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrdFlightTicketStatusServiceImpl.class);
	
	@Autowired
	private OrdFlightTicketStatusDao ordFlightTicketStatusDao;
	
	@Override
	public OrdFlightTicketStatus findByStatusId(Long statusId) {
		return ordFlightTicketStatusDao.selectByPrimaryKey(statusId);
	}

	@Override
	public List<OrdFlightTicketStatus> findByCondition(
			Map<String, Object> params) {
		return ordFlightTicketStatusDao.selectByParams(params);
	}

	@Override
	public int addFlightTicketStatus(OrdFlightTicketStatus ordFlightTicketStatus) {
		return ordFlightTicketStatusDao.insert(ordFlightTicketStatus);
	}

	@Override
	public int updateFlightTicketStatus(Map<String, Object> params) {
		return ordFlightTicketStatusDao.update(params);
	}
	
	@Override
	public int updateFlightTicketStatus(
			OrdFlightTicketStatus ordFlightTicketStatus) {
		return ordFlightTicketStatusDao.update(ordFlightTicketStatus);
	}
	
	@Override
	public int saveFlightTicketStatus(
			OrdFlightTicketStatus ordFlightTicketStatus) {
		if(ordFlightTicketStatus == null) {
			return 0;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", ordFlightTicketStatus.getOrderItemId());
		List<OrdFlightTicketStatus> ordFlightTicketStatusList = this.findByCondition(params);
		if(ordFlightTicketStatusList != null && ordFlightTicketStatusList.size() > 0) {
			ordFlightTicketStatus.setStatusId(ordFlightTicketStatusList.get(0).getStatusId());
		}
		LOGGER.info("OrdFlightTicketStatusServiceImpl::saveFlightTicketStatus_orderItemId="
				+ ordFlightTicketStatus.getOrderItemId()
				+ " ticketStatus="
				+ ordFlightTicketStatus.getStatusCode()
				+ " statusId="
				+ ordFlightTicketStatus.getStatusId());
		if(ordFlightTicketStatus.getStatusId() != null) {
			return this.updateFlightTicketStatus(ordFlightTicketStatus);
		}
		
		return this.addFlightTicketStatus(ordFlightTicketStatus);
	}
	
	@Override
	public Long getTotalCount(Map<String, Object> params){
		return ordFlightTicketStatusDao.getTotalCount(params);
	}
}
