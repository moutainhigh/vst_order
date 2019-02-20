package com.lvmama.vst.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdPayProcessJobService;
import com.lvmama.vst.order.service.IOrdWorkflowCompensatedService;
import com.lvmama.vst.order.service.IOrderLocalService;

@Service
public class OrdWorkflowCompensatedServiceImpl implements
		IOrdWorkflowCompensatedService {
	private static final Logger logger = LoggerFactory.getLogger(OrdWorkflowCompensatedServiceImpl.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrdPayProcessJobService ordPayProcessJobService;

	@Override
	public boolean compensatedOrdPayWorkflow(Long orderId, boolean isJob) {
		logger.info("start compensatedOrdPayWorkflow order:" + orderId);
		try {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			if(order == null) {
				logger.error("can not find the order:" + orderId);
				if(isJob){
					ordPayProcessJobService.makeValid(orderId);
				}
				return false;
			}
			
			if(order.hasCanceled()) {
				logger.error("order has been canceled:" + orderId);
				if(isJob){
					ordPayProcessJobService.makeValid(orderId);
				}
				return false;
			}
			
			if(order.getDistributorId().longValue() == 2) {
				orderLocalService.startBackOrder(orderId, "USER");
			}
			
			if(order.hasNeedPrepaid() && order.hasPayed()){
				ResultHandle resultHandle = orderLocalService.doPaymentSuccessMsg(order);
				if(resultHandle.isSuccess()){
					if(isJob){
						ordPayProcessJobService.makeValid(orderId);
					}
					return true;
				}
				else{
					if(isJob){
						ordPayProcessJobService.addTimes(orderId);
					}
					return false;
				}
			}else if(isJob){
				logger.info("compensatedOrdPayWorkflow 现付订单不启动支付流程, orderId:" + order.getOrderId());
				ordPayProcessJobService.makeValid(orderId);
				return true;
			}
		
		} catch (Exception e) {
			logger.error("compensatedOrdPayWorkflow failed, orderId:" + orderId);
			logger.error("{}", e);
		}
		
		logger.info("end compensatedOrdPayWorkflow order:" + orderId);
		return false;
	}

}
