package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import scala.actors.threadpool.Arrays;

import com.lvmama.vst.back.client.ord.service.OrderDownpayClientService;
import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrdOrderDownpayService;

@Component("orderDownpayServiceRemote")
public class OrderDownpayClientServiceImpl implements OrderDownpayClientService {
	private static final Log log = LogFactory.getLog(OrderDownpayClientServiceImpl.class);
	
	@Autowired
	private IOrdOrderDownpayService iOrdOrderDownpayService;

	@Override
	public ResultHandleT<List<OrdOrderDownpay>> loadOrderDowpayByOrderId(
			Long orderId) {
		ResultHandleT<List<OrdOrderDownpay>> resultHandleT = new ResultHandleT<List<OrdOrderDownpay>>();
		if(orderId==null){
			resultHandleT.setMsg("orderId is null");
			log.error("orderId is null");
			return resultHandleT;
		}
		try {
			List<OrdOrderDownpay> list = iOrdOrderDownpayService.selectByOrderId(orderId);
			resultHandleT.setReturnContent(list);
		}catch (Exception e){
			resultHandleT.setMsg(e.getMessage());
			log.error("Error occurs while loadOrderDowpayByOrderId", e);
		}
		return resultHandleT;
	}

	@Override
	public ResultHandle updatePayStatus(Long orderId, String payStatus) {
		ResultHandle resultHandle = new ResultHandle();
		String[] payStatusEnums = {OrdOrderDownpay.PAY_STATUS.PAYED.name(),OrdOrderDownpay.PAY_STATUS.UNPAY.name()};
		if(orderId==null||!Arrays.asList(payStatusEnums).contains(payStatus)){
			resultHandle.setMsg("orderId is null or payStatus is illegal");
			log.error("orderId is null or payStatus is illegal：orderId="+orderId+"，payStatus="+payStatus);
			return resultHandle;
		}
		try {
			iOrdOrderDownpayService.updatePayStatusByOrderId(orderId, payStatus);
		}catch (Exception e){
			resultHandle.setMsg(e.getMessage());
			log.error("Error occurs while updatePayStatus", e);
		}
		return resultHandle;
	}

}
