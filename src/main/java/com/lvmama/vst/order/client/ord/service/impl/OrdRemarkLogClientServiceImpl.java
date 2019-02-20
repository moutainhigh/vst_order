package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdRemarkLogClientService;
import com.lvmama.vst.back.order.po.OrdRemarkLog;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdRemarkLogService;

@Component("ordRemarkLogServiceRemote")
public class OrdRemarkLogClientServiceImpl implements OrdRemarkLogClientService {

	@Autowired
	private IOrdRemarkLogService ordRemarkLogService;
	
	@Override
	public int findOrdRemarkLogCount(Map<String, Object> params)
			throws BusinessException {
		return ordRemarkLogService.findOrdRemarkLogCount(params);
	}

	@Override
	public List<OrdRemarkLog> findOrdRemarkLogList(Map<String, Object> params)
			throws BusinessException {
		return ordRemarkLogService.findOrdRemarkLogList(params);
	}

	@Override
	public OrdRemarkLog findOrdRemarkLogById(Long logId)
			throws BusinessException {
		return ordRemarkLogService.findOrdRemarkLogById(logId);
	}

	@Override
	public int addOrdRemarkLog(OrdRemarkLog ordRemarkLog)
			throws BusinessException {
		return ordRemarkLogService.addOrdRemarkLog(ordRemarkLog);
	}

	@Override
	public int updateOrdRemarkLog(OrdRemarkLog ordRemarkLog)
			throws BusinessException {
		return ordRemarkLogService.updateOrdRemarkLog(ordRemarkLog);
	}

	@Override
	public int updateOrdRemarkLogStatus(OrdRemarkLog ordRemarkLog)
			throws BusinessException {
		return ordRemarkLogService.updateOrdRemarkLogStatus(ordRemarkLog);
	}

	@Override
	public int deleteOrdRemarkLog(Long logId) throws BusinessException {
		return ordRemarkLogService.deleteOrdRemarkLog(logId);
	}

}
