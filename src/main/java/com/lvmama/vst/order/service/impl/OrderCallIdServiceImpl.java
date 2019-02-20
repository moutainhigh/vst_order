package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.dao.OrderCallIdDao;
import com.lvmama.vst.order.po.OrderCallId;
import com.lvmama.vst.order.service.IOrderCallIdService;

@Service("orderCallIdService")
public class OrderCallIdServiceImpl implements IOrderCallIdService {
	
	private final Logger logger = LoggerFactory.getLogger(OrderCallIdServiceImpl.class);
	
	@Autowired
	private OrderCallIdDao orderCallIdDao;		

	@Override
	public List<OrderCallId> selectByParams(Long orderId, String callId) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("callId", callId);
		return orderCallIdDao.selectByParams(params);		
	}

	@Override
	public long insert(OrderCallId orderCallId) {
		return orderCallIdDao.insert(orderCallId);
	}

}
