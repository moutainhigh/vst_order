package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * Job每10分钟执行一次，先拿出“目的地BU的已支付的正常订单，两周内支付
  品类自由行，没到出游时间，过了订单提前预定时间的订单”，然后分页（每次传50个单号该处需要门票提供的接口支持，
  查询门票申码结果，返回有申码不成功或者未申码的订单号列表。最后依次取消
 * @author chenpingfan
 *
 */
public class AutoCancelOrderByOutTimePassCodeJob implements Runnable{
	
	private static final Log LOG = LogFactory.getLog(AutoCancelOrderByOutTimePassCodeJob.class);
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private OrdOrderDao orderDao;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	@Autowired
	private IComMessageService comMessageService;
		
	//订单取消原因
	public static String ORDER_CANCEL_REASON = "超过规定申码时间";
	//订单取消操作人ID
	public static String ORDER_CANCEL_OPERATOR_ID = "SYSTEM";
	//预定通知内容
	public static String messageContent="门票子单超过预订时间，请联系客人取消及退款！"; 
	
	public static String objectType = "ORDER";

	@Override
	public void run() {
		
		LOG.info(Constant.getInstance().isJobRunnable());
		

		if(Constant.getInstance().isJobRunnable()){
			Date today = DateUtil.getTodayDate();
			Date payDay = DateUtil.addDays(today, -14);//两周内
			Date nowTime = new Date();
			String lastConfirmTimeStr = DateUtil.formatDate(nowTime, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
			Date lastConfirmTime = DateUtil.stringToDate(lastConfirmTimeStr, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("paymentStatus", "PAYED");
			params.put("orderStatus", "NORMAL");
			params.put("buCode", "DESTINATION_BU");
			params.put("categoryId", 18L);
			params.put("visitTime", today);
			params.put("lastConfirmTime", lastConfirmTime);
			params.put("payTime", payDay);
			params.put("messageContent", messageContent);
			//1.子单凭证存在已经确认的
			List<Long> confirmedOrderIds = orderDao.queryOutBookTimeOrder(params);
			if(CollectionUtils.isNotEmpty(confirmedOrderIds)){
				LOG.info("AutoCancelOrderByOutTimePassCodeJob queryOutBookTimeOrder  size is "+confirmedOrderIds.size());
				
				List<List<Long>> listOrderId = new ArrayList<List<Long>>();
				//防止dubbo超时，50分组
				for(int i = 0;i<confirmedOrderIds.size();i+=50){
					int lastIndex = i+50<confirmedOrderIds.size()?i+50:confirmedOrderIds.size();
					List<Long> oidList = confirmedOrderIds.subList(i, lastIndex);
					listOrderId.add(oidList);
				}
				LOG.info("-----凭证已确认分组完成----");
				if(CollectionUtils.isNotEmpty(listOrderId)){
					for (List<Long> listId : listOrderId) {
						ResultHandleT<List<Long>> resultHandle = orderService.getFailTicketOrderIds(listId);
						//1.1没有申码成功，发送预定通知
						if(null != resultHandle && null != resultHandle.getReturnContent()){
							List<Long> orderIdList = resultHandle.getReturnContent();
							if(CollectionUtils.isNotEmpty(orderIdList)){	
								for(Long orderId : listId){
									if(orderIdList.contains(orderId)){//没有申码成功的订单
										saveTicketTimeOutComMsg(orderId);
									}
								}
							}
						}
					}				
				}else{
					LOG.info("AutoCancelOrderByOutTimePassCodeJob queryOutBookTimeOrder confirmedOrderIds size is 0 ");
				}
			}
			params.put("certConfirmStatus", "UNCONFIRMED");
			//2.子单凭证都为未确认
			List<Long> unconfirmedOrderIds = orderDao.queryOutBookTimeOrder(params);
			if(CollectionUtils.isNotEmpty(unconfirmedOrderIds)){				
				LOG.info("AutoCancelOrderByOutTimePassCodeJob queryOutBookTimeOrder unconfirmedOrderIds size is "+unconfirmedOrderIds.size());
				
				List<List<Long>> listOrderId = new ArrayList<List<Long>>();
				//防止dubbo超时，50分组
				for(int i = 0;i<unconfirmedOrderIds.size();i+=50){
					int lastIndex = i+50<unconfirmedOrderIds.size()?i+50:unconfirmedOrderIds.size();
					List<Long> oidList = unconfirmedOrderIds.subList(i, lastIndex);
					listOrderId.add(oidList);
				}
				LOG.info("-----凭证未确认分组完成----");
				if(CollectionUtils.isNotEmpty(listOrderId)){
					for (List<Long> listId : listOrderId) {
						ResultHandleT<List<Long>> resultHandle = orderService.getFailTicketOrderIds(listId);
						//2.1没有申码成功，取消订单发送短信，发送预定通知
						if(null != resultHandle && null != resultHandle.getReturnContent()){
							List<Long> orderIdList = resultHandle.getReturnContent();
							if(CollectionUtils.isNotEmpty(orderIdList)){	
								for(Long orderId : listId){
									if(orderIdList.contains(orderId)){//没有申码成功的订单
										LOG.info("tFailTicketOrderId="+orderId);
										orderService.cancelOrder(orderId, Long.toString(Constants.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM), ORDER_CANCEL_REASON, ORDER_CANCEL_OPERATOR_ID, null);
									}
									saveTicketTimeOutComMsg(orderId);
								}
							}else{//2.2部分申码成功，发送预定通知
								for(Long orderId : listId){
									saveTicketTimeOutComMsg(orderId);
								}
							}
						}else{//2.2部分申码成功，发送预定通知
							for(Long orderId : listId){
								saveTicketTimeOutComMsg(orderId);
							}
						}
					}
				}				
			}else{
				LOG.info("AutoCancelOrderByOutTimePassCodeJob queryOutBookTimeOrder unconfirmedOrderIds size is 0 ");
			}
		}		
	}

	private void saveTicketTimeOutComMsg(Long orderId){
		PermUser permUserPrincipal = orderResponsibleService.getOrderPrincipal(objectType, orderId);
		String orderPrincipal = permUserPrincipal.getUserName();
		String receiver = null;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver = orderPrincipal;
		}
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		comMessageService.saveReservation(comMessage, null, orderId, "SYSTEM", "门票子单超过预订时间产生预定通知，内容："+messageContent);
	}
}
