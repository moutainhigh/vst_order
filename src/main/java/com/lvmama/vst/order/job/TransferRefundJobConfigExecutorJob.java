package com.lvmama.vst.order.job;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutorFactory;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleTT;

/**
 * 订单转移退款JOB
 * @author lvpeng
 *
 */
public class TransferRefundJobConfigExecutorJob implements Runnable {
	private static final Log LOG = LogFactory.getLog(TransferRefundJobConfigExecutorJob.class);
	
	@Autowired
	private ComJobConfigService comJobConfigService;
	
	@Autowired
	private ComJobConfigExecutorFactory comJobConfigExecutorFactory;

	@Override
	public void run() {
		   if(Constant.getInstance().isJobRunnable()){
			LOG.info("TransferRefundJobConfigExecutorJob Start");
		    
			List<ComJobConfig> comJobConfigList = comJobConfigService.selectList(ComJobConfig.JOB_TYPE.ORDER_TRANSFER_REFUNDMENT, new Date());
			if (comJobConfigList != null && comJobConfigList.size() > 0) {
				ComJobConfigExecutor executor = null;
				int code = ComJobConfigExecutor.NONE_COMJOBCONFIG;
				for (ComJobConfig comJobConfig : comJobConfigList) {
					if (comJobConfig != null) {
						LOG.info("TransferRefundJobConfigExecutorJob.run:comJobConfig(ID=" + comJobConfig.getComJobConfigId() + ",ObjectId=" + comJobConfig.getObjectId() + ",JobType=" + comJobConfig.getJobType());
					}
					
					executor = comJobConfigExecutorFactory.createTransferRefundJobConfigExecutor(comJobConfig);
					
					if (executor != null) {
						
						ResultHandleTT<ComJobConfig> result= executor.execute(comJobConfig);
						if(comJobConfig != null) {
							LOG.info("TransferRefundJobConfigExecutorJob.run::comJobConfig(ID=" + comJobConfig.getComJobConfigId() + "),code=" + code);
						}
						code = result.getCode();
						if (code == ComJobConfigExecutor.DElETE_COMJOBCONFIG) {
							comJobConfigService.deleteComJobConfig(result.getReturnContent().getComJobConfigId());
						}
						
//						if (code == ComJobConfigExecutor.INSERT_COMJOBCONFIG) {
//							comJobConfigService.saveComJobConfig(result.getReturnContent());
//						}
						
						if (code == ComJobConfigExecutor.UPDATE_COMJOBCONFIG) {
							comJobConfigService.updateComJobConfig(result.getReturnContent());
						}
					} else  {
					}
				}
			}
		  }
	}
}
