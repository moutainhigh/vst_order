package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderAmountChangeClientService;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.order.service.IOrderAmountChangeService;

@Component("orderAmountChangeServiceRemote")
public class OrderAmountChangeClientServiceImpl implements OrderAmountChangeClientService {
	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;
	
	@Override
	public List<OrdAmountChange> findOrdAmountChangeList(HashMap<String, Object> params) {
		return orderAmountChangeService.findOrdAmountChangeList(params);
	}

	@Override
	public Integer findOrdAmountChangeCounts(HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return orderAmountChangeService.findOrdAmountChangeCounts(params);
	}

}
