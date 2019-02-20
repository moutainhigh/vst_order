package com.lvmama.vst.order.job.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderService;

public class AutoQueryOrderForDelWorkflowJob implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(AutoQueryOrderForDelWorkflowJob.class);

	@Resource(name="orderIdForWorkflowMessageProducer")
	private TopicMessageProducer orderIdForWorkflowMessageProducer;
	
	@Autowired
	private IOrdOrderService ordOrderService;

	@Override
	public void run() {
		logger.info("AutoQueryOrderForDelWorkflowJob starts and isClearActHisJobRunnable:" + Constant.getInstance().isClearActHisJobRunnable());
		if (Constant.getInstance().isClearActHisJobRunnable()) {
			int startBeforeDays = Constant.getInstance().getActHisJobStartBeforeDays();
			List<Long> orderIdList = null;
			try {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("intervalDays", startBeforeDays);
				orderIdList = ordOrderService.queryOrderForDelWorkflowByParams(params);
			} catch (Exception e) {
				logger.error("获取待删除ordrId操作报错:" + e.getMessage(), e);
			}
			if (orderIdList == null || orderIdList.size() == 0) {
				logger.warn("查询未获取到ordrId, 请确认是否合理!");
				return;// 没有数据直接结束查询
			}
			int success = 0;
			for (Long orderId : orderIdList) {
				try {
					orderIdForWorkflowMessageProducer.sendMsg(MessageFactory.newOrderForDelWorkflowMessage(orderId));
				} catch (Exception e) {
					logger.error("发送ordrId(" + orderId + ")到消息队列操作报错:" + e.getMessage(), e);
				}
				success++;
			}
			logger.info("本次查询获取到ordrId数量：" + orderIdList.size() + ", 成功发送到消息队列数量：" + success);
		}
	}
}
