package com.lvmama.vst.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.comm.bee.po.ord.OrdRefundmentLog;
import com.lvmama.vst.order.dao.impl.OrdRefundmentLogDao;
import com.lvmama.vst.order.service.IOrdRefundmentLogService;
@Service
public class OrdRefundLogServiceImpl implements IOrdRefundmentLogService {

	
	@Autowired
	private OrdRefundmentLogDao ordRefundmentLogDao;
	
	
	@Override
	public int addOrdRefundmentLog(OrdRefundmentLog ordRefundmentLog) {
		return ordRefundmentLogDao.insertSelective(ordRefundmentLog);
	}

}
