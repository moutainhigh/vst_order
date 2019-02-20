package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdPaymentInfo;
import com.lvmama.vst.order.dao.OrdPaymentInfoDao;
import com.lvmama.vst.order.service.IOrdPaymentInfoService;

@Service("ordPaymentInfoService")
public class OrdPaymentInfoServiceImpl implements IOrdPaymentInfoService {
	@Autowired
	private OrdPaymentInfoDao ordPaymentInfoDao;
	
	@Override
	public int addOrdPaymentInfo(OrdPaymentInfo ordPaymentInfo) {
		return ordPaymentInfoDao.insert(ordPaymentInfo);
	}

	@Override
	public List<OrdPaymentInfo> findOrdPaymentInfoList(
			Map<String, Object> params) {
		return ordPaymentInfoDao.findOrdPaymentInfoList(params);
	}

}
