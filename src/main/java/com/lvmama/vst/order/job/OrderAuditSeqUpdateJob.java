/**
 * 
 */
package com.lvmama.vst.order.job;

import com.lvmama.dest.api.utils.ZookeeperUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrderAuditService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;

/**
 * 每天更新最近出游的已审活动排序值
 * @author lancey
 *
 */
public class OrderAuditSeqUpdateJob implements Runnable{

	private static Log log = LogFactory.getLog(OrderAuditSeqUpdateJob.class);

	@Resource
	private IOrderAuditService orderAuditService;

	@Override
	public void run() {
		String seqJob = Constant.getInstance().getProperty("seq.job.enabled");
		log.info("OrderAuditSeqUpdateJob seqJob="+seqJob);
		if(Constant.getInstance().isJobRunnable() && Boolean.valueOf(seqJob)){
			log.info("OrderAuditSeqUpdateJob start...");
			orderAuditService.updateOrderAuditSeqByJob(null, null);
		}
	}

}
