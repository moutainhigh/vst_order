package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.order.dao.OrdOrderDownpayDao;
import com.lvmama.vst.order.service.IOrdOrderDownpayService;

@Service
public class OrdOrderDownpayServiceImpl implements IOrdOrderDownpayService {
	private static Logger logger = LoggerFactory.getLogger(OrdOrderDownpayServiceImpl.class);
	
	@Autowired
	private OrdOrderDownpayDao ordOrderDownpayDao;
	
	@Override
	public int updatePayStatusByOrderId(Long orderId, String payStatus) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orderId", orderId);
		map.put("payStatus", payStatus);
		return ordOrderDownpayDao.updatePayStatusByOrderId(map);
	}

	@Override
	public List<OrdOrderDownpay> selectByOrderId(Long orderId) {
		return ordOrderDownpayDao.selectByOrderId(orderId);
	}

}
