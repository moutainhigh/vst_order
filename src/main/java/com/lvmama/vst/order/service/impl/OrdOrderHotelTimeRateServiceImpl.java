package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.order.dao.OrdOrderHotelTimeRateDao;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrdOrderItemService;

@Service
public class OrdOrderHotelTimeRateServiceImpl implements IOrdOrderHotelTimeRateService {

	private static final Log LOG = LogFactory
			.getLog(OrdOrderHotelTimeRateServiceImpl.class);
	@Autowired
	private OrdOrderHotelTimeRateDao ordOrderHotelTimeRateDao;
	
	@Override
	public int addOrdOrderHotelTimeRate(
			OrdOrderHotelTimeRate OrdOrderHotelTimeRate) {
		// TODO Auto-generated method stub
		return ordOrderHotelTimeRateDao.insert(OrdOrderHotelTimeRate);
	}
	@Override
	public OrdOrderHotelTimeRate findOrdOrderHotelTimeRateById(Long id) {
		// TODO Auto-generated method stub
		return ordOrderHotelTimeRateDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdOrderHotelTimeRate> findOrdOrderHotelTimeRateList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordOrderHotelTimeRateDao.selectByParam(params);
	}
	@Override
	public List<OrdOrderHotelTimeRate> findOrdOrderHotelTimeRateListByParams(
			Map<String, Object> params) {
		return ordOrderHotelTimeRateDao.selectListByParam(params);
	}
	@Override
	public int updateByPrimaryKeySelective(
			OrdOrderHotelTimeRate OrdOrderHotelTimeRate) {
		// TODO Auto-generated method stub
		return ordOrderHotelTimeRateDao.updateByPrimaryKeySelective(OrdOrderHotelTimeRate);
	}
	@Override
	public List<Date> findOrdOrderItemHotelLastLeaveTimeByItemId(Long ordorderItemId) {
		return ordOrderHotelTimeRateDao.findOrdOrderItemHotelLastLeaveTimeByItemId(ordorderItemId);
	}
	
	

}
