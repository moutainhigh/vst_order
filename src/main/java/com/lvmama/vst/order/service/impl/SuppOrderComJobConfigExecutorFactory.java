package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutorFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 
 * @author sunjian
 *
 */
@Component
public class SuppOrderComJobConfigExecutorFactory implements ComJobConfigExecutorFactory {
	@Resource(name="supplierOrderCreateExecutor")
	private ComJobConfigExecutor supplierOrderCreateExecutor;
	
	@Resource(name="supplierOrderCancelExecutor")
	private ComJobConfigExecutor supplierOrderCancelExecutor;
	
	@Resource(name="orderCancelBySupplierExecutor")
	private ComJobConfigExecutor orderCancelBySupplierExecutor;
	
	@Resource(name="orderTransferRefundmentExecutor")
	private ComJobConfigExecutor orderTransferRefundmentExecutor;
	
	@Resource(name="flightOrderPaymentNotifyTryExecutor")
	private ComJobConfigExecutor flightOrderPaymentNotifyTryExecutor;

	@Resource(name="orderSettlementExecutor")
	private ComJobConfigExecutor orderSettlementExecutor;

	@Override
	public ComJobConfigExecutor createComJobConfigExecutor(ComJobConfig comJobConfig) {
		ComJobConfigExecutor executor = null;
		if (comJobConfig != null) {
			if (ComJobConfig.JOB_TYPE.SUPP_ORDER_CREATE.name().equals(comJobConfig.getJobType())) {
				executor = supplierOrderCreateExecutor;
			} else if (ComJobConfig.JOB_TYPE.SUPP_ORDER_CANCEL.name().equals(comJobConfig.getJobType())) {
				executor = supplierOrderCancelExecutor;
			} else if (ComJobConfig.JOB_TYPE.ORDER_CANCEL_BY_SUPP.name().equals(comJobConfig.getJobType())){
				executor = orderCancelBySupplierExecutor;
			} else if(ComJobConfig.JOB_TYPE.FLIGHT_ORDER_PAYMENT_NOTIFY.name().equals(comJobConfig.getJobType())){
				executor = flightOrderPaymentNotifyTryExecutor;
			} else if (ComJobConfig.JOB_TYPE.ORDER_SETTLEMENT.name().equals(comJobConfig.getJobType())) {
				executor = orderSettlementExecutor;
			}
		}
		
		return executor;
	}
	
	@Override
	public ComJobConfigExecutor createTransferRefundJobConfigExecutor(ComJobConfig comJobConfig) {
		ComJobConfigExecutor executor = null;
		if (comJobConfig != null) {
			if (ComJobConfig.JOB_TYPE.ORDER_TRANSFER_REFUNDMENT.name().equals(comJobConfig.getJobType())){
				executor = orderTransferRefundmentExecutor;
			}  
		}
		
		return executor;
	}

}
