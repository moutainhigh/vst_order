package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.dao.OrdPayProcessJobDao;
import com.lvmama.vst.order.po.OrdPayProcessJob;
import com.lvmama.vst.order.service.IOrdPayProcessJobService;

@Service
public class OrdPayProcessJobServiceImpl implements IOrdPayProcessJobService {
	@Autowired
	private OrdPayProcessJobDao ordPayProcessJobDao;
	
	public OrdPayProcessJobServiceImpl() {
	}

	@Override
	public int insert(OrdPayProcessJob record) {
		return ordPayProcessJobDao.insert(record);
	}

	@Override
	public List<Long> selectValidOrderIdList() {
		return ordPayProcessJobDao.selectValidOrderIdList();
	}

	@Override
	public int addTimes(Long orderId) {
		return ordPayProcessJobDao.addTimes(orderId);
	}
	
	@Override
	public OrdPayProcessJob selectByPrimaryKey(Long orderId){
		return ordPayProcessJobDao.selectByPrimaryKey(orderId);
	}
	
	@Override
	public int makeValid(Long orderId){
		return ordPayProcessJobDao.makeValid(orderId);
	}

}
