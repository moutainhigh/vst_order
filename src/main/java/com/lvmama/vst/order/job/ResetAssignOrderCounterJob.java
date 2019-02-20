/**
 * 
 */
package com.lvmama.vst.order.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrderResponsibleService;

/**
 * 分单计数器，清空计数数据
 * @author lancey
 *
 */
public class ResetAssignOrderCounterJob implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(ResetAssignOrderCounterJob.class);
	
	@Autowired
	private IOrderResponsibleService orderCounterService;
	
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			try{
				logger.info("start reset user counter.");
				orderCounterService.deleteCounterAll();
				logger.info("end reset user counter.");
			}catch(Exception ex){
				logger.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
	}

}
