package com.lvmama.vst.order.job;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.ComActivitiRelationService;

@Service
public class AutoClearActivitiDataJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AutoClearActivitiDataJob.class);
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	
	@Autowired
	private ProcesserClientService processerClientService;
	
	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			try {
				Map<String, Object> params = new HashMap<String, Object>();
				//读取配置数据
				String activitiClearCreateTime = Constant.getInstance().getActivitiClearCreateTime();
				String activitiClearVisitTime = Constant.getInstance().getActivitiClearVisitTime();
				String activitiClearOrderStatus = Constant.getInstance().getActivitiClearOrderStatus();
				String activitiClearPaymentStatus = Constant.getInstance().getActivitiClearPaymentStatus();
				String activitiClearPaymentTarget = Constant.getInstance().getActivitiClearPaymentTarget();
				String activitiClearCount = Constant.getInstance().getActivitiClearCount();
				String activitiClearExitTime = Constant.getInstance().getActivitiClearExitTime();
				Date currDate = new Date();
				
				//下单时间必须配置，防止因漏配导致工作流数据被误删
				if (activitiClearCreateTime != null
						&& StringUtils.isNotBlank(activitiClearCreateTime.trim())) {
					if(Integer.parseInt(activitiClearCreateTime.trim()) < 60) {
						throw new Exception("下单时间天数配置不能小于60");
					}
					params.put("createTime", DateUtil.getDateAfterDays(currDate, 0 - Integer.parseInt(activitiClearCreateTime.trim())));
				} else {
					throw new Exception("找不到下单时间配置");
				}
				
				//游玩时间必须配置，防止因漏配导致工作流数据被误删
				if (activitiClearVisitTime != null
						&& StringUtils.isNotBlank(activitiClearVisitTime.trim())) {
					if(Integer.parseInt(activitiClearVisitTime.trim()) < 60) {
						throw new Exception("游玩时间天数配置不能小于60");
					}
					params.put("visitTime", DateUtil.getDateAfterDays(currDate, 0 - Integer.parseInt(activitiClearVisitTime.trim())));
				} else {
					throw new Exception("找不到游玩时间配置");
				}
				
				//订单状态
				if (activitiClearOrderStatus != null
						&& StringUtils.isNotBlank(activitiClearOrderStatus.trim())) {
					params.put("orderStatus", activitiClearOrderStatus.trim());
				}
				
				//支付状态
				if (activitiClearPaymentStatus != null
						&& StringUtils.isNotBlank(activitiClearPaymentStatus.trim())) {
					params.put("paymentStatus", activitiClearPaymentStatus.trim());
				}
				
				//支付方式
				if (activitiClearPaymentTarget != null
						&& StringUtils.isNotBlank(activitiClearPaymentTarget.trim())) {
					params.put("paymentTarget", activitiClearPaymentTarget.trim());
				}
				
				int limitCount = Integer.MAX_VALUE;
				//每次删除的数据量限制
				if (activitiClearCount != null
						&& StringUtils.isNotBlank(activitiClearCount.trim())) {
					params.put("limitCount", Integer.parseInt(activitiClearCount.trim()));
					limitCount = Integer.parseInt(activitiClearCount.trim());
				}
				
				int deletedCount = 0;
				boolean isExit = false;
				while(deletedCount < limitCount && !isExit) {
					//查询符合删除条件的流程ID
					List<Long> processIdList = comActivitiRelationService.queryClearProcessByCondition(params);
					if(processIdList == null || processIdList.size() <= 0) {
						break;
					}
					for(Long processId : processIdList) {
						String currProcessStatus = "DELETED";
						try {
							processerClientService.deleteHistoricProcess(String.valueOf(processId), null);
							logger.info("删除流程历史记录成功，流程ID：" + processId);
							deletedCount ++;
						} catch (Exception ex) {
							logger.error("删除流程历史记录失败，流程ID：" + processId + "，异常信息：" + ex.getMessage());
							currProcessStatus = "DELETE_FAIL";
						}
						
						//更新状态
						comActivitiRelationService.updateProcessStatus(String.valueOf(processId), currProcessStatus);
						
						//根据配置，如果当前时间超过强制退出时间，则跳出循环，自动结束当前JOB
						if(!checkExitTime(activitiClearExitTime)) {
							logger.info("时间超过" + activitiClearExitTime + "点，job自动停止运行");
							isExit = true;
							break;
						}
						
						//每删除500条，休息2秒
						if(deletedCount % 500 == 0) {
							Thread.sleep(2000l);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	//校验当前时间是否已过强制退出时间
	private boolean checkExitTime(String exitTime) {
		if(exitTime == null || StringUtils.isBlank(exitTime.trim())) {
			return true;
		}
		
		int exitHour = Integer.parseInt(exitTime.trim());
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if(calendar.get(Calendar.HOUR_OF_DAY) >= exitHour && calendar.get(Calendar.HOUR_OF_DAY) < exitHour + 1) {
			return false;
		}
		
		return true;
	}
}
