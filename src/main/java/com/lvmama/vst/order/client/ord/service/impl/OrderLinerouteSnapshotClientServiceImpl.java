package com.lvmama.vst.order.client.ord.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderLinerouteSnapshotClientService;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.snapshot.service.IOrderLinerouteSnapshotService;

@Component("orderLinerouteSnapshotClientRemote")
public class OrderLinerouteSnapshotClientServiceImpl implements OrderLinerouteSnapshotClientService {

	@Autowired
	private IOrderLinerouteSnapshotService orderLinerouteSnapshotService;
	
	@Override
	public ResultHandleT<ProdLineRoute> findOneLineRouteSnapShotByOrderId(
			Long orderId) {
		ResultHandleT<ProdLineRoute> resultHandleT = new ResultHandleT<ProdLineRoute>();
		if (orderId == null ) {
			resultHandleT.setMsg("paramter order is null");
			return resultHandleT;
		}
		ProdLineRoute ProdLineRoute = orderLinerouteSnapshotService.findOneLineRouteSnapShotByOrderId(orderId);
		resultHandleT.setReturnContent(ProdLineRoute);
		return resultHandleT;
	}

}
