package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.order.service.OrderConnectsServicePropClientService;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.order.service.OrderConnectsServicePropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("orderConnectsServicePropClientServiceRemote")
public class OrderConnectsServicePropClientServiceImpl implements OrderConnectsServicePropClientService {

	private final Logger logger = LoggerFactory.getLogger(OrderConnectsServicePropClientServiceImpl.class);

	@Autowired
	private OrderConnectsServicePropService orderConnectsServicePropService;

	@Override
	public List<OrderConnectsServiceProp> queryOrderConnectsPropByParams(Map<String, Object> params) {
		return this.orderConnectsServicePropService.queryOrderConnectsPropByParams(params);
	}
}
