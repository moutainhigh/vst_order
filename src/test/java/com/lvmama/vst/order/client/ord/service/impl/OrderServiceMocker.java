package com.lvmama.vst.order.client.ord.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.datamodel.RawTicketOrderInfo;
import com.lvmama.vst.ticket.vo.PagedTicketOrderInfo;
import com.lvmama.vst.ticket.vo.TicketOrderInfo;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

public class OrderServiceMocker {
	private final Logger LOG = LoggerFactory.getLogger(OrderServiceMocker.class);
	
	private static JsonConfig jsonConfig = new JsonConfig(); 
	static {
		PropertyFilter filter = new PropertyFilter() {
			public boolean apply(Object source, String name, Object value) {
				if (value instanceof JSONNull || value == null || "null".equals(value)) {
					return true;
				}
				return false;
			}
		};
		jsonConfig.setRootClass( HashMap.class );  
		jsonConfig.setJavaPropertyFilter(filter);
		jsonConfig.setJsonPropertyFilter(filter);
	}
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	public ResultHandleT<PagedTicketOrderInfo> getPagedTicketOrderInfoByMobile(String mobile, Integer pageSize,
			Integer start) {
		LOG.info("mobile[" + mobile + "], pageSize[" + pageSize + "], start[" + start + "]");
		ResultHandleT<PagedTicketOrderInfo> result = new ResultHandleT<PagedTicketOrderInfo>();
		if (mobile == null || mobile.trim().equals("")) {
			String msg = "missing mobile";
			LOG.warn(msg);
			result.setMsg(msg);
			return result;
		}
		
		try {
        	String msg = "No order is related to mobile[" + mobile + "]!";
			
	        Integer totalQuantity = getTicketOrderTotalQuantityByMobile(mobile);
	        if (totalQuantity == null || totalQuantity <= 0) {
	        	LOG.debug(msg);
	        	result.setMsg(msg);
	        	return result;
	        }
	        
			List<Long> orderIdL =  getPagedTicketOrderIdByMobile(mobile, pageSize, start, totalQuantity);
			if (orderIdL == null || orderIdL.size() <= 0) {
	        	LOG.debug(msg);
	        	result.setMsg(msg);
	        	return result;
			}
			
			List<TicketOrderInfo> ticketOrderInfoL = getTicketOrderInfoById(orderIdL);
			if (ticketOrderInfoL == null || ticketOrderInfoL.size() <= 0) {
	        	LOG.debug(msg);
				result.setMsg(msg);
				return result;
			}
			
			PagedTicketOrderInfo content = new PagedTicketOrderInfo(totalQuantity, ticketOrderInfoL);
			result.setReturnContent(content);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setMsg(e);
		}
		return result;
	}
	
	private Integer getTicketOrderTotalQuantityByMobile(String mobile) {
		Integer totalQuantity2Return = new Integer(0);
		if (ordOrderDao != null) {
			Integer returnedTotalQuantity = ordOrderDao.getTicketOrderTotalQuantityByMobile(mobile);
			LOG.info("ticketOrderTotalQuantity -> " + returnedTotalQuantity);
			if (returnedTotalQuantity != null && returnedTotalQuantity > 0)
				totalQuantity2Return = returnedTotalQuantity;
		} else {
			LOG.warn("null order DAO");
		}
		return totalQuantity2Return;
	}
	
	private List<Long> getPagedTicketOrderIdByMobile(String mobile, Integer pageSize, Integer start,
			Integer totalQuantity) {
		List<Long> orderIdL = new ArrayList<Long>();
		if (ordOrderDao != null) {
			Integer currentPageSize = pageSize == null ? 10 : pageSize;
			Integer currentStartPageNo = start == null ? 1 : start;
			Page page = Page.page(totalQuantity, currentPageSize, currentStartPageNo);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("mobile", mobile);
			paramsMap.put("_start", page.getStartRows());
			paramsMap.put("_end", page.getEndRows());
			paramsMap.put("_orderby", "oi.visit_time");
			paramsMap.put("_order", "DESC");
			List<RawTicketOrderInfo> rawTicketOrderInfoL = ordOrderDao.getPagedRawTicketOrderInfoByMobile(paramsMap);
			LOG.debug("rawTicketOrderInfoL -> " + JSON.toJSONString(rawTicketOrderInfoL));
			if (rawTicketOrderInfoL != null) {
				for (RawTicketOrderInfo raw : rawTicketOrderInfoL) {
					orderIdL.add(raw.getOrderId());
				}
				LOG.info("order(s) before filtering -> " + orderIdL);
				
				for (RawTicketOrderInfo raw : rawTicketOrderInfoL) {
					if (raw.getVisitTime() != null && raw.getContent() != null) {
						try {
							@SuppressWarnings("unchecked")
							Map<String, Object> contentMap = (Map<String, Object>) JSONObject
									.toBean(JSONObject.fromObject(raw.getContent().trim()), jsonConfig);
							if (contentMap != null) {
								String aperiodicFlag = (String) contentMap.get("aperiodic_flag");
								if (aperiodicFlag.equalsIgnoreCase("Y")) {
									String aperiodicEndDateStr = (String) contentMap.get("aperiodic_end");
									String aperiodicEndDateTimeStr = aperiodicEndDateStr + " 23:59:59";
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									Date aperiodicEndDate = sdf.parse(aperiodicEndDateTimeStr);
									if (aperiodicEndDate.before(new Date())) {
										orderIdL.remove(raw.getOrderId());
									}
								} else if (aperiodicFlag.equalsIgnoreCase("N")) {
									Long certValidDay = Long.valueOf(contentMap.get("cert_valid_day").toString());
									Long expireDateInMillis = raw.getVisitTime().getTime() + certValidDay * 24 * 3600 * 1000;
									Long now = System.currentTimeMillis();
									LOG.debug("expire time: " + expireDateInMillis + ", now: " + now);
									if (expireDateInMillis <= now) {
										orderIdL.remove(raw.getOrderId());
										LOG.debug("expired, remove orderId[" + raw.getOrderId() + "]");
									}
								} else {
									LOG.debug("aperiodic_flag missing, remove orderId[" + raw.getOrderId() + "]");
									orderIdL.remove(raw.getOrderId());
								}
							} 
						} catch (Exception e) {
							LOG.error(e.getMessage(), e);
							LOG.error("error occurred, remove orderId[" + raw.getOrderId() + "]");
							orderIdL.remove(raw.getOrderId());
						}
					} else {
						LOG.debug("visit time or valid period is null, remove orderId[" + raw.getOrderId() + "]");
						orderIdL.remove(raw.getOrderId());
					}
				}
				LOG.info("order(s) after filtering -> " + orderIdL);
			}
		} else {
			LOG.warn("null order DAO");
		}
		return orderIdL;
	}
	
	private List<TicketOrderInfo> getTicketOrderInfoById(List<Long> orderIdL) {
		List<TicketOrderInfo> ticketOrderInfoL = new ArrayList<TicketOrderInfo>();
		if (ordOrderDao != null) {
			ticketOrderInfoL = ordOrderDao.getTicketOrderInfoById(orderIdL);
		} else {
			LOG.warn("null order DAO");
		}
		return ticketOrderInfoL;
	}
	
//	public String resendPassport(String orderId) {
//		LOG.info("resned passport, orderId: " + orderId);
//		String result = "failed";
//		if (passportSendSmsService != null) {
//			try {
//				passportSendSmsService.resendSms(Long.valueOf(orderId));
//				result = "succeeded";
//			} catch (Exception e) {
//				LOG.error(e.getMessage(), e);
//			}
//		} else {
//			LOG.warn("null passport sms service");
//		}
//		return result;
//	}	

//	public String applyForRefund(String orderId) {
//		LOG.info("phone call cancel, orderId: " + orderId);
//		String result = "failed";
//		if (orderId != null && !orderId.trim().equals("")) {
//			try {
//				ResultHandle cancelResult = cancelOrder(Long.getLong(orderId), OrderEnum.ORDER_CANCEL_CODE.PHONE_CALL_CANCEL.name(), "语音退", null, "语音退");
//				if (cancelResult != null && cancelResult.isSuccess())
//					result = "succeeded";
//			} catch (Exception e) {
//				LOG.error(e.getMessage(), e);
//			}
//		}
//		return result;
//	}

	public TicketOrderInfo getSingleTicketOrder(String orderId) {
		LOG.info("get info, orderId: " + orderId);
		TicketOrderInfo toi = null;
		if (orderId == null || orderId.trim().equals(""))
			return toi;
		
		Long orderIdL = new Long(-1);
		try {
			orderIdL = Long.valueOf(orderId);
		} catch (NumberFormatException nfe) {
			LOG.error("fail to turn orderId into LONG format");
		}
		if (orderIdL == null || orderIdL <= 0)
			return toi;
		
		if (ordOrderDao == null)
			return toi;
		
		try {
			List<TicketOrderInfo> ticketOrderInfoL = ordOrderDao.getSingleTicketOrder(orderId);
			if (ticketOrderInfoL != null && ticketOrderInfoL.size() > 0) {
				// 取第一游玩人
				toi = ticketOrderInfoL.get(0);
				LOG.info("get toi -> " + JSON.toJSONString(toi));

			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return toi;
	}
}
