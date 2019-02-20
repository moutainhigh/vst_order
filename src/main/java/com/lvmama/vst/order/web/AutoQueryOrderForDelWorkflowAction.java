package com.lvmama.vst.order.web;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderService;

@Controller
public class AutoQueryOrderForDelWorkflowAction {
	private static final Logger logger = LoggerFactory.getLogger(AutoQueryOrderForDelWorkflowAction.class);

	@Resource(name="orderIdForWorkflowMessageProducer")
	private TopicMessageProducer orderIdForWorkflowMessageProducer;
	
	@Resource
	private IOrdOrderService ordOrderService;

	@RequestMapping("/job/queryOrderIdsForDelWorkflow")
	@ResponseBody
	public String run(String parameter) {
		int startBeforeDays = 0;
		if(!StringUtil.isBlank(parameter)) {
			startBeforeDays = Integer.valueOf(parameter);
		} else {
			startBeforeDays = Constant.getInstance().getActHisJobStartBeforeDays();
		}
		if(startBeforeDays < 1) {
			startBeforeDays = 30;
		}
		logger.info("AutoQueryOrderForDelWorkflowAction starts. startBeforeDays" + startBeforeDays);
		Calendar start = Calendar.getInstance();
		List<Long> orderIdList = null;
		StringBuilder sb = new StringBuilder();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("intervalDays", startBeforeDays);
			orderIdList = ordOrderService.queryOrderForDelWorkflowByParams(params);
		} catch (Exception e) {
			logger.error(sb.append("获取待删除ordrId操作报错:").append(e.getMessage()).toString(), e);
			return "{\"runStatus\": \"FAILED\", \"result\": \"获取待删除ordrId操作报错:" + e.getMessage() + "\"}";
		}
		
		Calendar mid = Calendar.getInstance();
		logger.info("查询订单IDs消耗时间：" + (mid.getTimeInMillis() - start.getTimeInMillis()));
		if (orderIdList == null || orderIdList.size() == 0) {
			logger.warn("查询未获取到ordrId, 请确认是否合理!");
			return "{\"runStatus\": \"SUCCESS\", \"result\": \"查询未获取到ordrId, 请确认是否合理!\"}";// 没有数据直接结束查询
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
		logger.info(sb.append("本次查询获取到ordrId数量：").append(orderIdList.size()).append(", 成功发送到消息队列数量：").append(success).toString());
		return "{\"runStatus\": \"SUCCESS\", \"result\": \"" + sb.toString() + "\"}";
	}
}
