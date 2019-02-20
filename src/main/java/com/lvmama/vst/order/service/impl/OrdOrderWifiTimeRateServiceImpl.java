package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;
import com.lvmama.vst.order.dao.OrdOrderWifiTimeRateDao;
import com.lvmama.vst.order.service.IOrdOrderWifiTimeRateService;

@Service
public class OrdOrderWifiTimeRateServiceImpl implements IOrdOrderWifiTimeRateService {

	private static final Log LOG = LogFactory.getLog(OrdOrderWifiTimeRateServiceImpl.class);
	@Autowired
	private OrdOrderWifiTimeRateDao ordOrderWifiTimeRateDao;
	@Override
	public int addOrdOrderWifiTimeRate(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return ordOrderWifiTimeRateDao.insert(ordOrderWifiTimeRate);
	}
	@Override
	public OrdOrderWifiTimeRate findOrdOrderWifiTimeRateById(Long id) {
		return ordOrderWifiTimeRateDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdOrderWifiTimeRate> findOrdOrderWifiTimeRateList(
			Map<String, Object> params) {
		
		return ordOrderWifiTimeRateDao.selectByParam(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return ordOrderWifiTimeRateDao.updateByPrimaryKey(ordOrderWifiTimeRate);
	}


}
