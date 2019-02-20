package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdFlightTicketInfo;
import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.order.dao.OrdFlightTicketInfoDao;
import com.lvmama.vst.order.service.IOrdFlightTicketInfoService;

@Service
public class OrdFlightTicketInfoServiceImpl implements
		IOrdFlightTicketInfoService {
	
	@Autowired
	private OrdFlightTicketInfoDao ordFlightTicketInfoDao;
	
	@Override
	public OrdFlightTicketInfo findByInfoId(Long infoId) {
		return ordFlightTicketInfoDao.selectByPrimaryKey(infoId);
	}

	@Override
	public List<OrdFlightTicketInfo> findByCondition(Map<String, Object> params) {
		return ordFlightTicketInfoDao.selectByParams(params);
	}

	@Override
	public int addFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo) {
		return ordFlightTicketInfoDao.insert(ordFlightTicketInfo);
	}

	@Override
	public int updateFlightTicketInfo(Map<String, Object> params) {
		return ordFlightTicketInfoDao.update(params);
	}

	@Override
	public int updateFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo) {
		return ordFlightTicketInfoDao.update(ordFlightTicketInfo);
	}

	@Override
	public int saveFlightTicketInfo(OrdFlightTicketInfo ordFlightTicketInfo) {
		if(ordFlightTicketInfo == null) {
			return 0;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", ordFlightTicketInfo.getOrderItemId());
		params.put("passengerName", ordFlightTicketInfo.getPassengerName());
		List<OrdFlightTicketInfo> ordFlightTicketInfoList = this.findByCondition(params);
		if(ordFlightTicketInfoList != null && ordFlightTicketInfoList.size() > 0) {
			ordFlightTicketInfo.setInfoId(ordFlightTicketInfoList.get(0).getInfoId());
		}
		if(ordFlightTicketInfo.getInfoId() != null) {
			return this.updateFlightTicketInfo(ordFlightTicketInfo);
		}
		
		return this.addFlightTicketInfo(ordFlightTicketInfo);
	}

}
