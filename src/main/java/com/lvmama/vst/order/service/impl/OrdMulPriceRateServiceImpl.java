package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;

@Service
public class OrdMulPriceRateServiceImpl implements IOrdMulPriceRateService {

	private static final Log LOG = LogFactory
			.getLog(OrdMulPriceRateServiceImpl.class);
	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDao;
	@Override
	public int addOrdMulPriceRate(OrdMulPriceRate ordMulPriceRate) {
		// TODO Auto-generated method stub
		return ordMulPriceRateDao.insert(ordMulPriceRate);
	}
	@Override
	public OrdMulPriceRate findOrdMulPriceRateById(Long id) {
		// TODO Auto-generated method stub
		return ordMulPriceRateDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdMulPriceRate> findOrdMulPriceRateList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordMulPriceRateDao.selectByParams(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdMulPriceRate ordMulPriceRate) {
		// TODO Auto-generated method stub
		return ordMulPriceRateDao.updateByPrimaryKeySelective(ordMulPriceRate);
	}
	

	

	
	
	
}
