package com.lvmama.vst.order.snapshot.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.lvmama.order.snapshot.api.service.ISnapshotClientService;
import com.lvmama.order.snapshot.api.service.ISnapshotParseClientService;
import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.order.snapshot.service.IOrderLinerouteSnapshotService;
import com.lvmama.vst.order.snapshot.vo.LineRouteSnapshotVo;

@Component
public class OrderLinerouteSnapshotServiceImpl implements
		IOrderLinerouteSnapshotService {
	
	private static final Log LOG = LogFactory.getLog(OrderLinerouteSnapshotServiceImpl.class);
	
	@Autowired
	private ISnapshotClientService snapshotClientService;
	
	@Autowired
	private ISnapshotParseClientService snapshotParseClientService;

	@Override
	public ProdLineRoute findOneLineRouteSnapShotByOrderId(Long orderId) {
		ProdLineRoute lineRouteResult =null;
		try {
			ResponseBody<String> responseBody = snapshotParseClientService.findOneLineRouteSnapShotByOrderId(orderId);
			if (responseBody == null) {
				LOG.error("OrderLinerouteSnapshotServiceImpl#findOneLineRouteSnapShotByOrderId error orderId = "+orderId+" responseBody = null");
				return null;
			}
			if (responseBody.isFailure()) {
				LOG.error("OrderLinerouteSnapshotServiceImpl#findOneLineRouteSnapShotByOrderId error orderId = "+orderId+" error = l"+
						responseBody.getErrorMessage());
				return null;
			}
			if (responseBody.isSuccess() && null != responseBody.getT()) {
				String jsonStr = responseBody.getT();
				LineRouteSnapshotVo lineRouteSnapshotVo = new Gson().fromJson(jsonStr, LineRouteSnapshotVo.class);
				if (lineRouteSnapshotVo != null && lineRouteSnapshotVo.getProdLineRoute() != null) {
					lineRouteResult = lineRouteSnapshotVo.getProdLineRoute();
				}
			}
		} catch (Exception e) {
			LOG.error("OrderLinerouteSnapshotServiceImpl#findOneLineRouteSnapShotByOrderId error orderId = "+orderId+" messege = "+
					e.getMessage());
			e.printStackTrace();
		}
		return lineRouteResult;
	}

	@Override
	public Boolean insertOneLineRouteSnapshot(Long orderId,ProdLineRoute lineRoute) {
		 try {
			 LineRouteSnapshotVo lineRouteSnapshotVo = new LineRouteSnapshotVo(orderId,lineRoute);
			 String recordStr = new Gson().toJson(lineRouteSnapshotVo);
			ResponseBody<Boolean> responseBody = snapshotClientService.insertOneLineRouteSnapshot(orderId, recordStr);
			if (responseBody == null) {
				LOG.error("OrderLinerouteSnapshotServiceImpl#insertOneLineRouteSnapshot error orderId = "+orderId+" responseBody = null");
				return false;
			}
			if (responseBody.isFailure()) {
				LOG.error("OrderLinerouteSnapshotServiceImpl#insertOneLineRouteSnapshot error orderId = "+orderId+" error = l"+
						responseBody.getErrorMessage());
				return false;
			}
			 if (responseBody!=null && responseBody.getT()!=null) {
				return responseBody.getT();
			}
		} catch (Exception e) {
			LOG.error("OrderLinerouteSnapshotServiceImpl#insertOneLineRouteSnapshot error orderId = "+orderId+" messege = "+
					e.getMessage());
			e.printStackTrace();
		}
		 return false;
	}

}
