package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.ComMessageClientService;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.order.service.IComMessageService;
@Component("comMessageClientServiceRemote")
public class ComMessageClientServiceImpl implements ComMessageClientService {
	
	@Autowired
	private IComMessageService comMessageService;
	
	@Override
	public Long addComMessage(ComMessage comMessage) {
		comMessageService.addComMessage(comMessage);
		return comMessage.getMessageId();
	}

	@Override
	public int updateComMessage(ComMessage comMessage) {
		return comMessageService.updateComMessage(comMessage);
	}

	@Override
	public List<ComMessage> findComMessageList(Map<String, Object> params) {
		return comMessageService.findComMessageList(params);
	}

}
