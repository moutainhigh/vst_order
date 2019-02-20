package com.lvmama.vst.order.job.overdue.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.comm.TaskServiceInterface;
import com.lvmama.comm.pet.po.pub.TaskResult;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OverdueTicketSubOrder;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdExpiredRefundDao;
import com.lvmama.vst.order.job.overdue.service.RefundProcessedOverdueTicketSettlementPriceWrapperService;
import com.lvmama.vst.supp.client.service.OverdueRefundService;
/**
 * 过期退同步订单废码状态JOB
 */
public class OverdueTicketSynStateTaskServiceImpl implements TaskServiceInterface, Serializable{
	private static final long serialVersionUID = -1895753109015191124L;
	private static final Logger LOGGER =  LoggerFactory.getLogger(OverdueTicketSynStateTaskServiceImpl.class);
	private static final int MAX_NUM = 10;//审核中订单，最大处理次数
	private static final int MAX_PAGE = 1000;//每次查询条数
	
	@Resource
	private OrdExpiredRefundDao ordExpiredRefundDao;
	@Resource
	private OverdueTicketProcessingServiceImpl overdueTicketProcessingService;
	
	@Resource
	private OverdueRefundService overdueRefundService;
	
	@Resource
	private RefundProcessedOverdueTicketSettlementPriceWrapperService refundProcessedOverdueTicketSettlementPriceWrapperService;
	
	@Override
	public TaskResult execute(Long logId, String parameter) throws Exception {
		int totalResult=0,auditingNums=0,successNums=0,failedNums = 0, endedNums = 0;
		String contentText = "无";
		
		TaskResult taskResult = new TaskResult();
		try {
			LOGGER.info("System SynState BEGIN");
			int limitnum = MAX_NUM;
			if(parameter!=null){
				try{
					JSONObject jo = JSONObject.fromObject(parameter);
					if(jo.has("limitnum"))
						limitnum = jo.getInt("limitnum");
				}catch(Exception e){
					LOGGER.error("System SynState no limitnum", e);
				}
			}
			LOGGER.info("System SynState BEGIN limitnum:"+limitnum);
			Long minId = 0L;//sql查询id列大于minId的数据
			int listSize=0;
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("processStatus", OrderEnum.ExpiredRefundState.AUDITING.getCode());
			params.put("updateTime", getBefore3Month());
			params.put("maxPage", MAX_PAGE);//限制每次查询条数
			do {
				params.put("id", minId);
				List<OrdExpiredRefund> expiredRefundList = ordExpiredRefundDao.queryAuditingByMinId(params);
				listSize = expiredRefundList==null ? 0 : expiredRefundList.size();
				totalResult+=listSize;
				if(expiredRefundList!=null && !expiredRefundList.isEmpty()){
			        LOGGER.info("order SynState: first OrderItemId="+expiredRefundList.get(0).getOrderItemId());
					for(OrdExpiredRefund expiredRefund : expiredRefundList){
						try {
							Long expId = expiredRefund.getId();
							if(expId>minId)
								minId = expId;//下次查询数据的id列应该大于本次查询的id最大值
							Long orderItemId = expiredRefund.getOrderItemId();
							ResultHandleT<Integer> destroyStatusResult = overdueRefundService.queryDestroyStatus(orderItemId);
							Integer destroyStatus = destroyStatusResult.getReturnContent();
							LOGGER.info("SynState OrderItemId="+orderItemId+",result:"+destroyStatus);
							Integer processNum = expiredRefund.getProcessNum();
							if(processNum==null) processNum = 0;
							if(processNum>=limitnum){
								expiredRefund.setProcessStatus(OrderEnum.ExpiredRefundState.OVERLIMIT.getCode());
								expiredRefund.setProcessDesc(OrderEnum.ExpiredRefundState.OVERLIMIT.getDesc());
								endedNums++;
							}else{
								if(destroyStatus!=null){
									switch(destroyStatus){
									case 1://废码成功
										successNums++;
										overdueTicketProcessingService.updateOverdueTicketSubOrderInOneShot(orderItemId);//更新子订单标识
//										setSettlementPrice(expiredRefund, orderItemId);//结算价推送
										break;
									case 2://废码失败
										expiredRefund.setProcessStatus(OrderEnum.ExpiredRefundState.FAILURE.getCode());
										expiredRefund.setProcessDesc(OrderEnum.ExpiredRefundState.FAILURE.getDesc());
										failedNums++;
										break;
									default:
										expiredRefund.setProcessNum(processNum+1);
										auditingNums++;
										break;
									}
								}else{
									expiredRefund.setProcessNum(processNum+1);
								}
							}
							if(destroyStatus==null||destroyStatus!=1)
								ordExpiredRefundDao.update(expiredRefund);
						}catch (Exception e) {
							LOGGER.error("auto SynState failed.", e);
						}
					}
				}
			} while (listSize==MAX_PAGE);
		} catch (Exception e) {
			taskResult.setRunStatus(TaskResult.RUN_STATUS.FAILED);
            taskResult.setResult(e.getMessage());
			LOGGER.error("", e);
			contentText = e.getMessage();
		}
		LOGGER.info("System SynState END");
		taskResult.setRunStatus(TaskResult.RUN_STATUS.SUCCESS);
    	taskResult.setResult("共有" + totalResult + "条，仍然是废码审核中"+auditingNums+"条，已废码成功" + successNums + "条，已废码失败"
                + failedNums + "条，已终止"+endedNums+"条"
        		+"\n异常信息："+contentText);
		return taskResult;
	}

	private void setSettlementPrice(OrdExpiredRefund expiredRefund, Long orderItemId) {
		OverdueTicketSubOrder subOrder = new OverdueTicketSubOrder();
		subOrder.setOrderId(expiredRefund.getOrderId());
		subOrder.setOrderItemId(orderItemId);
		ResultHandleT<String> resultHandleT = refundProcessedOverdueTicketSettlementPriceWrapperService.setSettlementPriceToZero(subOrder);
		if(resultHandleT!=null){
			LOGGER.info("System SynState Settlement result:"+resultHandleT.getReturnContent());
		}
	}
	
	public void doRefreshState(Date startDate) {
		LOGGER.info("RefreshState BEGIN");
		
		try {
			Long minId = 0L;//sql查询id列大于minId的数据
			int listSize=0;
			Map<String,Object> params = new HashMap<String,Object>();
			List<Integer> staList = new ArrayList<Integer>();
			staList.add(OrderEnum.ExpiredRefundState.AUDITING.getCode());
			staList.add(OrderEnum.ExpiredRefundState.FAILURE.getCode());
			staList.add(OrderEnum.ExpiredRefundState.OVERLIMIT.getCode());
			params.put("list", staList);
			params.put("maxPage", MAX_PAGE);//限制每次查询条数
			if(startDate==null){
				startDate = getBefore3Month();
			}
			params.put("updateTime", startDate);
			
			do {
				params.put("id", minId);
				List<OrdExpiredRefund> expiredRefundList = ordExpiredRefundDao.queryAuditingByMinId(params);
				listSize = expiredRefundList==null ? 0 : expiredRefundList.size();

				if(expiredRefundList!=null && !expiredRefundList.isEmpty()){
			        LOGGER.info("order RefreshState: first OrderItemId="+expiredRefundList.get(0).getOrderItemId());
					for(OrdExpiredRefund expiredRefund : expiredRefundList){
						try {
							Long expId = expiredRefund.getId();
							if(expId>minId)
								minId = expId;//下次查询数据的id列应该大于本次查询的id最大值
							Long orderItemId = expiredRefund.getOrderItemId();
							ResultHandleT<Integer> destroyStatusResult = overdueRefundService.queryDestroyStatus(orderItemId);
							Integer destroyStatus = destroyStatusResult.getReturnContent();
							LOGGER.info("RefreshState OrderItemId="+orderItemId+",result:"+destroyStatus);
							if(destroyStatus!=null){
								switch(destroyStatus){
								case 1://废码成功
									overdueTicketProcessingService.updateOverdueTicketSubOrderInOneShot(orderItemId);//更新子订单标识
//									setSettlementPrice(expiredRefund, orderItemId);//结算价推送
									break;
								case 2://废码失败
									expiredRefund.setProcessStatus(OrderEnum.ExpiredRefundState.FAILURE.getCode());
									expiredRefund.setProcessDesc(OrderEnum.ExpiredRefundState.FAILURE.getDesc());
									ordExpiredRefundDao.update(expiredRefund);
									break;
								default:
									break;
								}
							}
						}catch (Exception e) {
							LOGGER.error("auto RefreshState failed.", e);
						}
					}
				}
			} while (listSize==MAX_PAGE);
		} catch (Exception e) {
			LOGGER.error("RefreshState Exception", e);
		}
		LOGGER.info("RefreshState END");
	}

	private Date getBefore3Month() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -3);
		return calendar.getTime();
	}

}
