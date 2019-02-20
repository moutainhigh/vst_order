package com.lvmama.vst.order.lvcc;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.redis.JedisTemplate2;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import com.lvmama.vst.pet.adapter.impl.UserUserProxyAdapterImpl;
import com.lvmama.vst.ticket.vo.PagedTicketOrderInfo;
import com.lvmama.vst.ticket.vo.TicketOrderInfo;


@Controller
public class OrderLvccAction extends BaseActionSupport {
	
	private final static Integer IVR_ORDER_PAGE_SIZE = 20;
	
	private final static String IVR_KEY_HEAD = "IVR_HEAD_";
	
	private final static Integer IVR_KEY_EXPIRE = (IVR_ORDER_PAGE_SIZE + 1) * 60;
	
	private final static String IVR_ORDER_VOICE_FORMAT = "订单号%s,%s出游,%s,出游人%s";
	
	private static final Log LOG = LogFactory.getLog(OrderLvccAction.class);
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	private OrderService orderServiceRemote;

	
	@RequestMapping(value = "/callCenter/ivrQueryOrderIds")
	public String ivrQueryOrderIds(Model model, HttpServletRequest req){
		LOG.info("ivrQueryOrderIds start");
		Integer ivrOrderTotalCount = 0;
		String ivrOrders = "";
		String callerNo = req.getParameter("callerNo");
		String pageSizeStr = req.getParameter("pagesize");
		String startPageNum = req.getParameter("startpagenum");
		
		
		System.out.println("callerNo:" + callerNo);
		System.out.println("pageSizeStr:" + pageSizeStr);
		System.out.println("startPageNum:" + startPageNum);
		
		LOG.info("ivrQueryOrderIds callerNo:" + callerNo + " pageSizeStr:" + pageSizeStr + " startPageNum:" + startPageNum);
		
		Integer pageSize =  StringUtils.isEmpty(pageSizeStr) ? IVR_ORDER_PAGE_SIZE : Integer.valueOf(pageSizeStr); 
		Integer startPage = StringUtils.isEmpty(startPageNum) ? 1 : Integer.valueOf(startPageNum);
		
		if (StringUtils.isNotEmpty(callerNo) && orderServiceRemote != null){
			try{
				ResultHandleT<PagedTicketOrderInfo> result =
						orderServiceRemote.getPagedTicketOrderInfoByMobile(callerNo, pageSize, startPage);
				
				if (result != null && result.getReturnContent() != null && result.getReturnContent().getTotalTicketOrderQuantity() > 0){
					PagedTicketOrderInfo pagedTicketOrderInfo = result.getReturnContent();
					
					LOG.info("ivrQueryOrderIds PagedTicketOrderInfo:" + pagedTicketOrderInfo);
					
					StringBuilder sbTicketOrderInfos = new StringBuilder();
					//JedisTemplate2 writeJedis = JedisTemplate2.getWriterInstance();
					JedisTemplate2 writeJedis = JedisTemplate2.getReaderInstance();//经测Reader可写,而Writer反而不可写(配置反了?)
					boolean exists = false;
					String key;
					String contentFormatStr;
					for(TicketOrderInfo item : pagedTicketOrderInfo.getTicketOrderInfoL()){
						sbTicketOrderInfos.append(item.getOrderId());
						sbTicketOrderInfos.append(",");
						//缓存
						if (writeJedis != null){
							key = IVR_KEY_HEAD + item.getOrderId();
							exists = writeJedis.exists(key);
							if (exists){
								writeJedis.expire(key, IVR_KEY_EXPIRE);
							}
							else{
								contentFormatStr = formatTicketOrderInfo(item);
								writeJedis.set(key, contentFormatStr, IVR_KEY_EXPIRE);
							}
						}
					}
					sbTicketOrderInfos.deleteCharAt(sbTicketOrderInfos.length() - 1);
					
					ivrOrders = sbTicketOrderInfos.toString();
					ivrOrderTotalCount = pagedTicketOrderInfo.getTotalTicketOrderQuantity();
					
				}else{
					LOG.info("ivrQueryOrderIds result is null or getTotalTicketOrderQuantity<0");
				}
					
			}
			catch(Exception e){
				LOG.error("ivrQueryOrderIds Error："+e.getMessage(), e);
			}
			
		}
		

		LOG.info("ivrQueryOrderIds ivrOrders:" + ivrOrders + "ivrOrderTotalCount:" + ivrOrderTotalCount);
		
		model.addAttribute("ivrOrders", ivrOrders);
		model.addAttribute("ivrOrderTotalCount", ivrOrderTotalCount);
		
		LOG.info("ivrQueryOrderIds end");

		return "/order/orderLvcc/ivr_getOrderIds";
	}	

	@RequestMapping(value = "/callCenter/ivrQueryOrderInfo")
	public String ivrQueryOrderInfo(Model model, HttpServletRequest req) throws UnsupportedEncodingException{
		String ivrOrderDescription = "";
		LOG.info("ivrQueryOrderInfo start:");
		try{
			String orderId = req.getParameter("orderId");
			
			LOG.info("ivrQueryOrderInfo parameter orderId:" + orderId);
			
			if (StringUtils.isNotEmpty(orderId)){
				//JedisTemplate2 writeJedis = JedisTemplate2.getWriterInstance();
				JedisTemplate2 writeJedis = JedisTemplate2.getReaderInstance();//配置反了?
				String key = IVR_KEY_HEAD + orderId;
				TicketOrderInfo ticketOrderInfo = null;
				
				if (writeJedis != null){
					LOG.info("ivrQueryOrderInfo from redis orderId:" + orderId);
					ivrOrderDescription = TrimQuotes(writeJedis.get(key));
					
					if (StringUtils.isNotBlank(ivrOrderDescription)){
						writeJedis.del(key);
					}
				}
				
				if (StringUtils.isBlank(ivrOrderDescription) && orderServiceRemote != null){
						ticketOrderInfo = orderServiceRemote.getSingleTicketOrder(orderId);
						if (ticketOrderInfo != null){
							ivrOrderDescription = formatTicketOrderInfo(ticketOrderInfo);					
					}
				}
				
				
			}else{
				LOG.info("orderId is null");
			}
		}
		catch(Exception e){
			LOG.error("ivrQueryOrderInfo Error："+e.getMessage(), e);
		}
		
		ivrOrderDescription = StringUtils.isBlank(ivrOrderDescription) ? "" : ivrOrderDescription;

		LOG.info("ivrQueryOrderInfo ivrOrderDescription:" + ivrOrderDescription);
		
		model.addAttribute("orderDescription", ivrOrderDescription);
		
		
		LOG.info("ivrQueryOrderInfo end");
		return "/order/orderLvcc/ivr_getOrderInfo";	
	}
	
	
	@RequestMapping(value = "/callCenter/ivrDoReSend")
	public String ivrDoReSend(Model model, HttpServletRequest req){
		
		LOG.info("ivrDoReSend start");
		
		String ivrOrderDescription = "N";
		
		try{
			String orderId = req.getParameter("orderId");
			
			LOG.info("ivrDoReSend orderId:" + orderId);
			
			if (StringUtils.isNotEmpty(orderId)){
				String result = orderServiceRemote.resendPassport(orderId);
				
				ivrOrderDescription = "succeeded".equalsIgnoreCase(result) ? "Y" : "N";
				
				if ("N".equals(ivrOrderDescription)){
					LOG.info("ivrDoReSend Error,Result:"+result);
				}else{
					LOG.info("ivrDoReSend OK,Result:"+result);
				}
				
			}else{
				LOG.info("ivrDoReSend orderId is null");
			}
		}
		catch(Exception e){
			LOG.error("ivrDoReSend Exception", e);
		}
		
		ivrOrderDescription = StringUtils.isBlank(ivrOrderDescription) ? "N" : ivrOrderDescription;
		
		LOG.info("ivrDoReSend:ivrOrderDescription:" + ivrOrderDescription);
		
		model.addAttribute("orderDescription", ivrOrderDescription);
		
		LOG.info("ivrDoReSend end");
		
		return "/order/orderLvcc/ivr_getOrderInfo";	
	}
	

	@RequestMapping(value = "/callCenter/ivrCheckVip")
	public String ivrCheckVip(Model model, HttpServletRequest req){
		String ivrOrderDescription = "N";
		LOG.info("ivrCheckVip start");
		try{
			String callerNo = req.getParameter("callerNo");
			
			LOG.info("ivrCheckVip callerNo:" + callerNo);
			
			if (userUserProxyAdapter != null && StringUtils.isNotBlank(callerNo)) {
				ResultHandleT<UserUser> result = userUserProxyAdapter.getUserByMobile(callerNo);
				
				if (result.isSuccess() && result.getReturnContent() != null){
					ivrOrderDescription = "Y";			
				}
			}else{
				LOG.info("ivrCheckVip userUserProxyAdapter is null");
			}
		}
		catch(Exception e){
			LOG.error("ivrCheckVip Exception", e);
		}
		
		ivrOrderDescription = StringUtils.isBlank(ivrOrderDescription) ? "N" : ivrOrderDescription;
		
		LOG.info("ivrCheckVip ivrOrderDescription:" + ivrOrderDescription);
		
		model.addAttribute("orderDescription", ivrOrderDescription);
		
		LOG.info("ivrCheckVip end");
		return "/order/orderLvcc/ivr_getOrderInfo";	
	}
	

	@RequestMapping(value = "/callCenter/ivrDoCancelOrder")
	public String ivrDoCancelOrder(Model model, HttpServletRequest req){
		LOG.info("ivrDoCancelOrder start");
		String ivrOrderDescription = "N";
		try{
			String orderId = getRequest().getParameter("orderId");
			
			
			LOG.info("ivrDoCancelOrder orderId:" + orderId);
			
			if (StringUtils.isNotEmpty(orderId) && orderServiceRemote != null){
				String result = orderServiceRemote.applyForRefund(orderId);
				

				ivrOrderDescription = "succeeded".equalsIgnoreCase(result) ? "Y" : "N";
				
				if ("N".equalsIgnoreCase(ivrOrderDescription)){
					LOG.error("ivrDoCancelOrder result Error reason："+result);
				}
			}else{
				LOG.info("ivrDoCancelOrder orderServiceRemote is null");
			}
		}
		catch(Exception e){
			LOG.error("ivrDoCancelOrder Exception", e);
		}
		
		ivrOrderDescription = StringUtils.isBlank(ivrOrderDescription) ? "N" : ivrOrderDescription;

		LOG.info("ivrDoCancelOrder:ivrOrderDescription:" + ivrOrderDescription);
		
		model.addAttribute("orderDescription", ivrOrderDescription);
		
		LOG.info("ivrDoCancelOrder end");
		
		return "/order/orderLvcc/ivr_getOrderInfo";	
	}
	
	
	@RequestMapping(value = "/callCenter/ivrSendSmsCode")
	public String ivrSendSmsCode(Model model, HttpServletRequest req){
		LOG.info("ivrSendSmsCode start");
		//规定ivrOrderDescription的值为短信验证码
		String ivrOrderDescription = "N";
		
		try{
			String mobileNum = req.getParameter("inputRegMobileNum");
			
			LOG.info("ivrSendSmsCode inputRegMobileNum:" + mobileNum);
			
			if (StringUtils.isNotBlank(mobileNum) && userUserProxyAdapter != null){
				UserUser userUser = new UserUser();
				userUser.setMobileNumber(mobileNum);
				String code = userUserProxyAdapter.sendAuthenticationCode(UserUserProxyAdapter.USER_IDENTITY_TYPE.MOBILE, userUser, Constant.SMS_SSO_TEMPLATE.SMS_MOBILE_AUTHENTICATION_CODE.name());
				ivrOrderDescription = StringUtils.isBlank(code) ? "N" : code;
			}else{
				LOG.info("ivrSendSmsCode userUserProxyAdapter is null");
			}
		}
		catch(Exception e){
			LOG.error("ivrSendSmsCode Exception", e);
		}
		
		ivrOrderDescription = StringUtils.isBlank(ivrOrderDescription) ? "N" : ivrOrderDescription;
		
		LOG.info("ivrSendSmsCode ivrOrderDescription:" + ivrOrderDescription);
		
		model.addAttribute("orderDescription", ivrOrderDescription);
		
		LOG.info("ivrSendSmsCode end");
		
		return "/order/orderLvcc/ivr_getOrderInfo";
	}
	
	
	private String formatTicketOrderInfo(TicketOrderInfo ticketOrderInfo){
		String visitTime = new SimpleDateFormat("yyyy年MM月dd日").format(ticketOrderInfo.getVisitTime());
		
		return String.format(IVR_ORDER_VOICE_FORMAT, ticketOrderInfo.getOrderId(),
				visitTime, ticketOrderInfo.getScenicSpotName(), ticketOrderInfo.getVisitorName());	
	}
	
	private String TrimQuotes(String content){
		String result = content;
		if (StringUtils.isNotBlank(content)){
			if (content.startsWith("\"")){
				result = content.substring(1);
			}
			if (result.endsWith("\"")){
				result = result.substring(0, result.length()-1);
			}
			
			return result;
		}
		else{
			return content;
		}	
	}
	
	
/*	private String formatKey(String callerNo, String orderId){
		return IVR_KEY_HEAD + callerNo.trim() + "_" + orderId;
	}
	*/
@RequestMapping(value = "/callCenter/ivrQueryOrderIdsTest")
@ResponseBody
public Object ivrQueryOrderIdsTest(Model model, HttpServletRequest req){
	LOG.info("ivrQueryOrderIds start");
	Integer ivrOrderTotalCount = 0;
	String ivrOrders = "";
	String callerNo = req.getParameter("callerNo");
	String pageSizeStr = req.getParameter("pagesize");
	String startPageNum = req.getParameter("startpagenum");


	System.out.println("callerNo:" + callerNo);
	System.out.println("pageSizeStr:" + pageSizeStr);
	System.out.println("startPageNum:" + startPageNum);

	LOG.info("ivrQueryOrderIds callerNo:" + callerNo + " pageSizeStr:" + pageSizeStr + " startPageNum:" + startPageNum);

	Integer pageSize =  StringUtils.isEmpty(pageSizeStr) ? IVR_ORDER_PAGE_SIZE : Integer.valueOf(pageSizeStr);
	Integer startPage = StringUtils.isEmpty(startPageNum) ? 1 : Integer.valueOf(startPageNum);

	if (StringUtils.isNotEmpty(callerNo) && orderServiceRemote != null){
		try{
			ResultHandleT<PagedTicketOrderInfo> result =
					orderServiceRemote.getPagedTicketOrderInfoByMobile(callerNo, pageSize, startPage);

			if (result != null && result.getReturnContent() != null && result.getReturnContent().getTotalTicketOrderQuantity() > 0){
				PagedTicketOrderInfo pagedTicketOrderInfo = result.getReturnContent();

				LOG.info("ivrQueryOrderIds PagedTicketOrderInfo:" + pagedTicketOrderInfo);

				StringBuilder sbTicketOrderInfos = new StringBuilder();
				//JedisTemplate2 writeJedis = JedisTemplate2.getWriterInstance();
				JedisTemplate2 writeJedis = JedisTemplate2.getReaderInstance();//经测Reader可写,而Writer反而不可写(配置反了?)
				boolean exists = false;
				String key;
				String contentFormatStr;
				for(TicketOrderInfo item : pagedTicketOrderInfo.getTicketOrderInfoL()){
					sbTicketOrderInfos.append(item.getOrderId());
					sbTicketOrderInfos.append(",");
					//缓存
					if (writeJedis != null){
						key = IVR_KEY_HEAD + item.getOrderId();
						exists = writeJedis.exists(key);
						if (exists){
							writeJedis.expire(key, IVR_KEY_EXPIRE);
						}
						else{
							contentFormatStr = formatTicketOrderInfo(item);
							writeJedis.set(key, contentFormatStr, IVR_KEY_EXPIRE);
						}
					}
				}
				sbTicketOrderInfos.deleteCharAt(sbTicketOrderInfos.length() - 1);

				ivrOrders = sbTicketOrderInfos.toString();
				ivrOrderTotalCount = pagedTicketOrderInfo.getTotalTicketOrderQuantity();

			}else{
				LOG.info("ivrQueryOrderIds result is null or getTotalTicketOrderQuantity<0");
			}

		}
		catch(Exception e){
			LOG.error("ivrQueryOrderIds Error："+e.getMessage(), e);
		}

	}


	LOG.info("ivrQueryOrderIds ivrOrders:" + ivrOrders + "ivrOrderTotalCount:" + ivrOrderTotalCount);
	LOG.info("ivrQueryOrderIds end");
	List<Object> returnList = new ArrayList<>();
	returnList.add(ivrOrders);
	returnList.add(ivrOrderTotalCount);
	JSONArray jsonArray = null;
	try {
		JsonConfig jc = new JsonConfig();
		jsonArray = JSONArray.fromObject(returnList,jc);
	} catch (Exception e) {
		LOG.error("失败:"+e.toString());
	}
	return jsonArray;
}
}
