package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.ComAuditDao;
import com.lvmama.vst.order.dao.OrdFlightTicketStatusDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrderResponsibleService;

public class AutoSaveReservationForFlightTicketJob implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(AutoSaveReservationForFlightTicketJob.class);
	
	@Autowired
	private OrdOrderDao orderDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdFlightTicketStatusDao ordFlightTicketStatusDao;
	
	@Autowired
	private ComAuditDao comAuditDao;
	
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	
	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable());
		if(Constant.getInstance().isJobRunnable()){
			try {
				//主单审核通过、支付成功，对接机票子单在支付完成后30分钟未反馈出票成功。
				Date nowDate = DateUtil.stringToDate(DateUtil.formatDate(new Date(), DateUtil.HHMMSS_DATE_FORMAT), DateUtil.HHMMSS_DATE_FORMAT);
				LOG.info("AutoSaveReservationForFlightTicketJob execute ==========> executeTime:"+nowDate);
				Date endDate = DateUtils.addMinutes(nowDate, -30);
				Date startDate = DateUtils.addHours(nowDate, -8);
				Map<String, Object> paramOrder = new HashMap<String, Object>();
				paramOrder.put("startDate", startDate);
				paramOrder.put("endDate", endDate);
				List<Long> orderIds = orderDao.getOrderIdsForFlightTicket(paramOrder);
				if(null!=orderIds&&orderIds.size()>0){
					List<Long> orderItemIds=new ArrayList<>();
					List<OrdOrderItem> orderItems = ordOrderItemDao.getOrderItemsForFlightTicket(orderIds);
					if(null!=orderItems&&orderItems.size()>0){
						for (OrdOrderItem ordOrderItem : orderItems) {
							if(ordOrderItem.isApiFlightTicket()){
								orderItemIds.add(ordOrderItem.getOrderItemId());
							}
						}
					}
					if(null!=orderItemIds&&orderItemIds.size()>0){
						List<Long> itemIds = ordFlightTicketStatusDao.getOrderItemIdsByTicketSuccess(orderItemIds);
						if(null!=itemIds&&itemIds.size()>0){
							orderItemIds.removeAll(itemIds);
						}
					}
					if(null!=orderItemIds&&orderItemIds.size()>0){
						List<Long> itemIdsHasComAudit = comAuditDao.getOrderItemIdsByFlightTicketFail(orderItemIds);
						if(null!=itemIdsHasComAudit&&itemIdsHasComAudit.size()>0){
							orderItemIds.removeAll(itemIdsHasComAudit);
						}
					}
					if(null!=orderItemIds&&orderItemIds.size()>0){
						LOG.info("AutoSaveReservationForFlightTicketJob execute ==========> orderItemIds:"+orderItemIds.toString());
						for (OrdOrderItem orderItem : orderItems) {
							if(orderItemIds.contains(orderItem.getOrderItemId())){
								//创建预订通知
								saveReservation(
										orderItem.getOrderId(),
										orderItem.getOrderItemId(),
										OrderEnum.AUDIT_SUB_TYPE.FLIGHT_TICKET_FAIL.name(),
										"机票订单["
												+ orderItem.getOrderItemId()
												+ "]出票失败"
												+ "，请及时进行后续人工处理！");
							}
						}
					}
				}
				
			} catch (Exception e) {
				LOG.error("AutoSaveReservationForFlightTicketJob log error:"+e.getMessage(),e);
			}
		}
	}

	/**
	 * 生成出票失败预定通知
	 * @param orderId
	 * @param loginUserId
	 */
	public int saveReservation(Long orderId, Long orderItemId, String subType, String messageContent) {
		//发送给主订单
		String objectType = "ORDER";
		PermUser permUserPrincipal = orderResponsibleService.getOrderPrincipal(objectType, orderId);
		String orderPrincipal = permUserPrincipal.getUserName();
		
		String receiver = null;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver = orderPrincipal;
		}
		
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		
		return comMessageService.saveReservationChildOrder(comMessage, null, subType,
				orderId, orderItemId, "SYSTEM", messageContent);
	}
	
}
