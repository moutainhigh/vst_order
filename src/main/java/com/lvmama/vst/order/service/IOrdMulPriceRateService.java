package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;


/**
 * @author 张伟
 *
 */
public interface IOrdMulPriceRateService {

	
	public int addOrdMulPriceRate(OrdMulPriceRate ordMulPriceRate);
	
	public OrdMulPriceRate findOrdMulPriceRateById(Long id);
	
	public List<OrdMulPriceRate> findOrdMulPriceRateList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdMulPriceRate ordMulPriceRate);


	
}
