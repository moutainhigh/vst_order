package com.lvmama.vst.order.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.flight.client.order.service.FlightOrderProcessService;

/**
 * 机票子订单,支付通知,重发逻辑
 * @author xuxueli
 */
@Component("flightOrderPaymentNotifyTryExecutor")
public class FlightOrderPaymentNotifyTryExecutor implements ComJobConfigExecutor{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FlightOrderPaymentNotifyTryExecutor.class);
	
	@Autowired
	private FlightOrderProcessService flightOrderProcessServiceRemote;

	@Override
	public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
		ResultHandleTT<ComJobConfig> result = new ResultHandleTT<ComJobConfig>();
		int operateCode = ComJobConfigExecutor.DElETE_COMJOBCONFIG;
		
		if (comJobConfig.getRetryCount() > 0) {
			ResultHandle handle = new ResultHandle();
			try {
				handle = flightOrderProcessServiceRemote.paymentNotify(comJobConfig.getObjectId());
				if (handle.isSuccess()) {
					LOGGER.info("flightOrderPaymentNotifyTryExecutor try success, objectId:{}", comJobConfig.getObjectId());
				} else {
					LOGGER.info("flightOrderPaymentNotifyTryExecutor try fail, objectId:{}, msg:{}", new Object[]{comJobConfig.getObjectId(), handle.getMsg()});
				}
			} catch (Exception e) {
				handle.setMsg(e);
				LOGGER.info("flightOrderPaymentNotifyTryExecutor try exception:{}", e);
			}
			
			comJobConfig.setRetryCount(comJobConfig.getRetryCount() - 1);
			comJobConfig.setPlanTime(DateUtil.getDateAfterMinutes(2));
		}
		
		if (comJobConfig.getRetryCount() > 0) {
			operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
		}
		
		LOGGER.info("flightOrderPaymentNotifyTryExecutor log, operateCode:{}, objectId:{}", new Object[]{operateCode, comJobConfig.getObjectId()});
		result.setCode(operateCode);
		result.setReturnContent(comJobConfig);
		return result;
	}

}
