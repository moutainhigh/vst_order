package com.lvmama.vst.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.dao.OrdAuditProcessTaskDao;
import com.lvmama.vst.order.po.OrdAuditProcessTask;
import com.lvmama.vst.order.service.IOrdAuditProcessTaskService;

@Service
public class OrdAuditProcessTaskServiceImpl implements
		IOrdAuditProcessTaskService {
	
	@Autowired
	private OrdAuditProcessTaskDao ordAuditProcessTaskDao;

	@Override
	public int insert(OrdAuditProcessTask record) {
		return ordAuditProcessTaskDao.insert(record);
	}

	@Override
	public List<Long> selectValidOrderIdList() {
		return ordAuditProcessTaskDao.selectValidOrderIdList();
	}

	@Override
	public int addTimes(Long orderId) {
		return ordAuditProcessTaskDao.addTimes(orderId);
	}

	@Override
	public OrdAuditProcessTask selectByPrimaryKey(Long orderId) {
		return ordAuditProcessTaskDao.selectByPrimaryKey(orderId);
	}

	@Override
	public int makeSuccess(Long orderId) {
		return ordAuditProcessTaskDao.makeSuccess(orderId);
	}

	@Override
	public int makeValid(Long orderId) {
		return ordAuditProcessTaskDao.makeValid(orderId);
	}

}
