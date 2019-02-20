package com.lvmama.vst.order.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.order.dao.OrdOrderWifiPickingPointDao;
import com.lvmama.vst.order.service.IOrdOrderWifiPickingPointService;

@Service
public class OrdOrderWifiPickingPointServiceImpl implements IOrdOrderWifiPickingPointService{
	private static final Log LOG = LogFactory.getLog(OrdOrderWifiPickingPointServiceImpl.class);
	
	@Autowired
	private OrdOrderWifiPickingPointDao orderWifiPickingPointDao;
	
	@Override
	public int insertOrdOrderWifiPickingPoint(
			OrdOrderWifiPickingPoint ordOrderWifiPickingPoint) {
		return orderWifiPickingPointDao.insertSelective(ordOrderWifiPickingPoint);
	}


	@Override
	public OrdOrderWifiPickingPoint getOrderWifiPickingPointById(Long ordOrderId) {
		return orderWifiPickingPointDao.selectByPrimaryKey(ordOrderId);
	}

	@Override
	public int deleteOrdOrderWifiPickingPointById(Long ordOrderId) {
		return orderWifiPickingPointDao.deleteByPrimaryKey(ordOrderId);
	}


	@Override
	public int updateOrdOrderWifiPickingPoint(
			OrdOrderWifiPickingPoint ordOrderWifiPickingPoint) {
		return orderWifiPickingPointDao.updateByPrimaryKeySelective(ordOrderWifiPickingPoint);
	}

}
