package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrdOrderTrackingService;
import com.lvmama.vst.order.service.OrdOrderTrackingClientService;

@Component("ordOrderTrackingClientService")
public class OrdOrderTrackingClientServiceImpl implements OrdOrderTrackingClientService {

	@Autowired
	private IOrdOrderTrackingService orderTrackingService;
	@Override
	public ResultHandleT<List<OrdOrderTracking>> findNowOrderStatusByOrderId(Long orderId) {
		List<OrdOrderTracking> orderTrackingList = orderTrackingService.findNowOrderStatusByOrderId(orderId);
		ResultHandleT<List<OrdOrderTracking>> returnHandler = new ResultHandleT<List<OrdOrderTracking>>();
		if(!CollectionUtils.isEmpty(orderTrackingList)){
			returnHandler.setReturnContent(orderTrackingList);
		}else{
			returnHandler.setMsg("没有查询到订单："+orderId+"跟踪信息");
		}
		return returnHandler;
	}

	@Override
	public void saveOrderTracking(OrdOrderTracking ordOrderTracking) {
		orderTrackingService.saveOrderTracking(ordOrderTracking);
	}

}
