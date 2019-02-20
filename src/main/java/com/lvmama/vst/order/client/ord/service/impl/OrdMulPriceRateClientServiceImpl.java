package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdMulPriceRateClientService;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;

@Component("ordMulPriceRateServiceRemote")
public class OrdMulPriceRateClientServiceImpl implements OrdMulPriceRateClientService {
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	
	@Override
	public int addOrdMulPriceRate(OrdMulPriceRate ordMulPriceRate) {
		return ordMulPriceRateService.addOrdMulPriceRate(ordMulPriceRate);
	}
	@Override
	public OrdMulPriceRate findOrdMulPriceRateById(Long id) {
		return ordMulPriceRateService.findOrdMulPriceRateById(id);
	}
	@Override
	public List<OrdMulPriceRate> findOrdMulPriceRateList(Map<String, Object> params) {
		return ordMulPriceRateService.findOrdMulPriceRateList(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdMulPriceRate ordMulPriceRate) {
		return ordMulPriceRateService.updateByPrimaryKeySelective(ordMulPriceRate);
	}
}
