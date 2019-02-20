package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.order.po.OrderCallId;

public interface IOrderCallIdService {
	
	public List<OrderCallId> selectByParams(Long orderId, String callId);
	
	public long insert(OrderCallId orderCallId) ;
}
