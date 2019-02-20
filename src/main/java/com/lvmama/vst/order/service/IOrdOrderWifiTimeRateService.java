package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;


public interface IOrdOrderWifiTimeRateService {

	
	public int addOrdOrderWifiTimeRate(OrdOrderWifiTimeRate ordOrderWifiTimeRate);
	
	public OrdOrderWifiTimeRate findOrdOrderWifiTimeRateById(Long id);
	
	public List<OrdOrderWifiTimeRate> findOrdOrderWifiTimeRateList(Map<String, Object> params);

	public int updateByPrimaryKeySelective(OrdOrderWifiTimeRate ordOrderWifiTimeRate);
	
	
}
