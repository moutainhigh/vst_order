package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.O2oOrder;
import com.lvmama.vst.order.dao.O2oOrderDao;
import com.lvmama.vst.order.service.IO2oOrderService;

/**
 * O2o 门店系统， O2o订单Service Impl
 * @date 2016 03 02
 * @author qixiaochen
 *
 */
@Service("o2oOrderService")
public class O2oOrderServiceImpl implements IO2oOrderService {

	@Autowired
	private O2oOrderDao o2oOrderDao;
	
	@Override
	public List<O2oOrder> queryO2oOrder(Map<String, Object> paramsMap) {
		return o2oOrderDao.queryO2oOrder(paramsMap);
	}
	@Override
	public List<O2oOrder> findO2oOrderList(Map paramsMap) {
		return o2oOrderDao.findO2oOrderList(paramsMap);
	}
	
	
	@Override
	public List<O2oOrder> queryForListForReport(Map<String, Object> paramsMap) {
		return o2oOrderDao.queryForListForReport(paramsMap);
	}
	
	@Override
	public int insertSelective(O2oOrder oOrder) {
		if (oOrder==null) {
			return 0;
		}
		
		return o2oOrderDao.insertSelective(oOrder);
	}
	@Override
	public int deleteByOrderId(Long oderId) {
		if (oderId==null) {
			return 0;
		}
		return o2oOrderDao.deleteByOrderId(oderId);
	}
	
	@Override
	public Long getCountByProperty(Map<String, Object> params){
		return o2oOrderDao.getCountByProperty(params);
	}

	public int updateO2oOrder(O2oOrder o2oOrder){
		return o2oOrderDao.updateO2oOrder(o2oOrder);
	}
}
