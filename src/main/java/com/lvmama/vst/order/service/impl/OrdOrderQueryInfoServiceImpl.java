package com.lvmama.vst.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.dao.OrdOrderQueryInfoDao;
import com.lvmama.vst.order.service.IOrdOrderQueryInfoService;

@Service
public class OrdOrderQueryInfoServiceImpl implements IOrdOrderQueryInfoService{
	@Autowired
	private OrdOrderQueryInfoDao ordOrderQueryInfoDao;
//
//	@Override
//	public int saveOrderQueryInfo(OrdOrderQueryInfo orderQueryInfo) {
//		return ordOrderQueryInfoDao.insert(orderQueryInfo);
//	}
//
//	@Override
//	public int updateOrderQueryInfo(OrdOrderQueryInfo orderQueryInfo) {
//		return ordOrderQueryInfoDao.updateByPrimaryKey(orderQueryInfo);
//	}
//
//	@Override
//	public int updateQueryInfoByOrderId(OrdOrderQueryInfo orderQueryInfo) {
//		return ordOrderQueryInfoDao.updateByOrderId(orderQueryInfo);
//	}
//
//	@Override
//	public int updateQueryInfoByOrderItemId(OrdOrderQueryInfo orderQueryInfo) {
//		return ordOrderQueryInfoDao.updateByOrderItemId(orderQueryInfo);
//	}
//
//	@Override
//	public List<Long> findOrdQueryInfoHistoryData() {
//		return ordOrderQueryInfoDao.findOrdQueryInfoHistoryData();
//	}
//
//	@Override
//	public int saveOrdQueryInfoHistoryData(List<Long> queryInfoIds) {
//		ordOrderQueryInfoDao.moveOrdQueryInfoHistoryData(queryInfoIds);
//		return ordOrderQueryInfoDao.deleteOrdQueryInfoData(queryInfoIds);
//	}
//	
//	@Override
//	public List<Long> findOrderIdsByCondition(OrderMonitorCnd orderMonitorCnd) {
//		return ordOrderQueryInfoDao.findOrderIdsByCondition(orderMonitorCnd);
//	}
//	
//	@Override
//	public Long findOrderCountByCondition(OrderMonitorCnd orderMonitorCnd) {
//		return ordOrderQueryInfoDao.findOrderCountByCondition(orderMonitorCnd);
//	}
}
