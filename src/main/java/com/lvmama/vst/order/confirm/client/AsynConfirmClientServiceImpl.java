package com.lvmama.vst.order.confirm.client;

import com.lvmama.vst.back.client.ord.service.AsynConfirmClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.async.AsynConfirmService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("asynConfirmClientServiceRemote")
public class AsynConfirmClientServiceImpl implements AsynConfirmClientService {
	private static final Log LOG =LogFactory.getLog(AsynConfirmClientServiceImpl.class);

	@Resource(name ="inconfirmAsynConfirmService")
	private AsynConfirmService inconfirmAsynConfirmService;
	@Resource(name ="newOrderAsynConfirmService")
	private AsynConfirmService newOrderAsynConfirmService;
	
	@Override
	public void apiAsynConfirmOrder(ResultHandleT<OrdOrderItem> resultHandel) {
		inconfirmAsynConfirmService.apiAsynConfirmOrder(resultHandel);
	}

	@Override
	public void apiAsynConfirmOrder_NewOrder(ResultHandleT<OrdOrderItem> resultHandel) {
		newOrderAsynConfirmService.apiAsynConfirmOrder(resultHandel);
	}

}
