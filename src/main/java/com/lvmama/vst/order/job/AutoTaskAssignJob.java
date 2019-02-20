package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderResponsibleService;

/**
 * 订单自动分单,作为辅助人工分单的功能
 * 
 * 1,非取消确认的分单任务必须是订单未取消状态 2,取消确认的分单任务必须是订单取消状态 3,凭证确认的分单任务不考虑订单的状态
 * 4,针对一个任务取一定数量的订单，比如20个
 * 
 * @author wenzhengtao
 * 
 */

public class AutoTaskAssignJob implements Runnable {
	/**
	 * 日志记录器
	 */
	private static final Log LOGGER = LogFactory.getLog(AutoTaskAssignJob.class);
	/**
	 * 每调度一次取的任务数量
	 */
	@SuppressWarnings("unused")
	private static final int RECORD_NUM = 20;
	/**
	 * 订单分单-要分什么单
	 */
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	

	/**
	 * 订单分单-具体分给谁
	 */
	@Autowired
	private IOrderLocalService orderLocalService;

	@Override
	public void run() {
		if (Constant.getInstance().isJobRunnable()) {
			doTaskAssignHandle();
			doTaskAssignHandle_Confirm();
		}
	}
	private void doTaskAssignHandle() {
		long start = System.currentTimeMillis();
		// 遍历当前系统中所有的分单任务,各自分开来做，防止任务积压
		for (OrderEnum.AUDIT_TYPE auditTypeEnum : OrderEnum.AUDIT_TYPE.values()) {
			List<ComAudit> auditList = this.queryComAuditListByPool(auditTypeEnum.name());
			if (null != auditList && !auditList.isEmpty()) {
				for (ComAudit audit : auditList) {
					try {
						LOGGER.info("doTaskAssignHandle-audit:"+audit.getAuditId()+",order:"+audit.getObjectId());
						orderLocalService.makeOrderAuditForSystem(audit);
						//this.writeOrderDistributionLog(audit,resultAudit);
					} catch (Exception e) {
						// 某一个分单任务出错了
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("分单逻辑发生异常");
						}
						LOGGER.error(ExceptionFormatUtil.getTrace(e));
					}
				}
			}
		} 
		long end = System.currentTimeMillis();
		LOGGER.info("本次分单所用时间为：" + (start-end) + "毫秒");
	}
	private void doTaskAssignHandle_Confirm() {
		long start = System.currentTimeMillis();
		// 遍历当前系统中所有的分单任务,各自分开来做，防止任务积压
		for (Confirm_Enum.CONFIRM_AUDIT_TYPE auditTypeEnum : Confirm_Enum.CONFIRM_AUDIT_TYPE.values()) {
			List<ComAudit> auditList = this.queryComAuditListByPool(auditTypeEnum.name());
			if (null != auditList && !auditList.isEmpty()) {
				for (ComAudit audit : auditList) {
					try {
						LOGGER.info("doTaskAssignHandle_Confirm-audit:"+audit.getAuditId()+",order:"+audit.getObjectId());
						orderLocalService.makeOrderAuditForSystem(audit);
						//this.writeOrderDistributionLog(audit,resultAudit);
					} catch (Exception e) {
						// 某一个分单任务出错了
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("分单逻辑发生异常");
						}
						LOGGER.error(ExceptionFormatUtil.getTrace(e));
					}
				}
			}
		}
		List<ComAudit> auditList = this.queryComAuditListByPool(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
		if (null != auditList && !auditList.isEmpty()) {
			for (ComAudit audit : auditList) {
				try {
					LOGGER.info("doTaskAssignHandle_Confirm-audit:"+audit.getAuditId()+",order:"+audit.getObjectId());
					orderLocalService.makeOrderAuditForSystem(audit);
				} catch (Exception e) {
					// 某一个分单任务出错了
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("分单逻辑发生异常");
					}
					LOGGER.error(ExceptionFormatUtil.getTrace(e));
				}
			}
		}
		long end = System.currentTimeMillis();
		LOGGER.info("本次分单所用时间为：" + (start-end) + "毫秒");
	}
	
	/**
	 * 查找待分单的数据
	 * 
	 * @return
	 */
	private List<ComAudit> queryComAuditListByPool(String auditType) {
		// 封装查询条件
		Map<String, Object> param = new HashMap<String, Object>();

		// 订单活动条件
		param.put("auditType", auditType);

		// 查询所有待分配的任务
		List<ComAudit> comAuditList = orderAuditService.queryComAuditListByPool(param);
		
		// 查找自动过未分配到人的任务集合
		Long poolCount = Long.valueOf(comAuditList.size());
		param.put("poolCount", poolCount);
		List<ComAudit> comAuditProcessedList = orderAuditService.queryComAuditListByProcessed(param);
		
		comAuditList.addAll(comAuditProcessedList);
		return comAuditList;
	}
	
}
