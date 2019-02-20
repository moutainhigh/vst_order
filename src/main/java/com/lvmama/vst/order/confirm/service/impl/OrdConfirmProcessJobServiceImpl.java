package com.lvmama.vst.order.confirm.service.impl;

import com.lvmama.vst.order.confirm.service.IOrdConfirmProcessJobService;
import com.lvmama.vst.order.dao.OrdConfirmProcessJobDao;
import com.lvmama.vst.order.po.OrdConfirmProcessJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdConfirmProcessJobServiceImpl implements IOrdConfirmProcessJobService {
	@Autowired
	private OrdConfirmProcessJobDao ordConfirmProcessJobDao;
	
	public OrdConfirmProcessJobServiceImpl() {
	}

	@Override
	public int insert(OrdConfirmProcessJob record) {
		return ordConfirmProcessJobDao.insert(record);
	}

	@Override
	public List<OrdConfirmProcessJob> selectValidOrdConfirmProcessJobList() {
		return ordConfirmProcessJobDao.selectValidOrdConfirmProcessJobList();
	}

	@Override
	public int addTimes(Long orderItemId) {
		return ordConfirmProcessJobDao.addTimes(orderItemId);
	}
	
	@Override
	public OrdConfirmProcessJob selectByPrimaryKey(Long orderItemId){
		return ordConfirmProcessJobDao.selectByPrimaryKey(orderItemId);
	}
	
	@Override
	public int makeValid(Long orderItemId){
		return ordConfirmProcessJobDao.makeValid(orderItemId);
	}

}
