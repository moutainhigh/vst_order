package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSmsSendService;

@Controller
@RequestMapping("/order/ordSmsSend")
public class OrderSmsSendAction extends BaseActionSupport {

	private static final Log log = LogFactory.getLog(OrderSmsSendAction.class);

	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	@Autowired
	private IOrderSmsSendService orderSmsSendService;

	@RequestMapping(value = "/findOrdSmsSendList")
	public String hotelRelation(Model model, Integer page, HttpServletRequest request, HttpServletResponse response) 
			throws BusinessException  {

		String mobile = request.getParameter("mobile");
		String orderId = request.getParameter("orderId");
		String content = request.getParameter("content");
		String operate = request.getParameter("operate");
		String status = request.getParameter("status");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		
		Map<String, Object> params = new HashMap<String, Object>();
		boolean isParamBlank = false;
		//都为空，就不查
		if(StringUtil.isEmptyString(mobile)
			&& StringUtil.isEmptyString(orderId)
			&& StringUtil.isEmptyString(content)
			&& StringUtil.isEmptyString(operate)
			&& StringUtil.isEmptyString(status)
			&& StringUtil.isEmptyString(beginDate)
			&& StringUtil.isEmptyString(endDate)
				){
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.MONTH, -1);
			date = calendar.getTime();
			params.put("beginDate", DateUtil.formatSimpleDate(date));
			beginDate=DateUtil.formatSimpleDate(date);
			isParamBlank = true;
		}
		
		if (!StringUtils.isEmpty(mobile)) {
			params.put("mobile", mobile);
		}
		if (!StringUtils.isEmpty(orderId)) {
			params.put("orderId", orderId);
		}
		if (!StringUtils.isEmpty(content)) {
			params.put("smsContent", content);
		}
		if (!StringUtils.isEmpty(operate)) {
			params.put("operate", operate);
		}
		if (!StringUtils.isEmpty(status)) {
			params.put("status", status);
		}
		if (!StringUtils.isEmpty(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (!StringUtils.isEmpty(endDate)) {
			params.put("endDate", endDate);
		}
		int count = 0;
		if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
			count = orderSmsSendService.findOrdSmsSendCount(params);
		}else{
			if(!isParamBlank){
				count = orderSendSmsService.findOrdSmsSendCount(params);
			}
		}
		
		int pagenum = (page==null) ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(request);
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		params.put("_orderby", "SEND_TIME DESC");
		if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
			pageParam.setItems(orderSmsSendService.findOrdSmsSendList(params));
		}else{
			if(!isParamBlank){
				pageParam.setItems(orderSendSmsService.findOrdSmsSendList(params));
			}
		}
		
		params.clear();
		model.addAttribute("mobile", mobile);
		model.addAttribute("orderId", orderId);
		model.addAttribute("content", content);
		model.addAttribute("operate", operate);
		model.addAttribute("status", status);
		model.addAttribute("beginDate", beginDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("page", pageParam.getPage().toString());
		return "/order/sms/findSmsSendList";
	}

	/** 重发短信 */
	@RequestMapping(value = "/reSendSms")
	@ResponseBody
	public ResultMessage reSendSms(Long smsId) {
		if (smsId==null)
			return ResultMessage.SMS_SEND_FAIL_RESULT;
		log.info("reSendSms, smsId="+smsId);
		try {
			getLoginUserId();
			if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
				orderSmsSendService.reSendSms(smsId, getLoginUserId());
			}else{
				orderSendSmsService.reSendSms(smsId, getLoginUserId());
			}
			
			return ResultMessage.SMS_SEND_SUCCESS_RESULT;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResultMessage.SMS_SEND_FAIL_RESULT;
		}
	}
	
	@RequestMapping(value = "/showOrdSmsList")
	public String showOrdSmsList(Model model, Integer page, HttpServletRequest request) throws BusinessException  {
		String orderId = request.getParameter("orderId");
		List<OrdSmsSend> smsList = new ArrayList<OrdSmsSend>();
		
		if(StringUtil.isNotEmptyString(orderId)){
			Map<String, Object> params = new HashMap<String, Object>();
			params.clear();
			params.put("orderId", orderId);
			int count = orderSendSmsService.findOrdSmsSendCount(params);
			int pagenum = (page==null) ? 1 : page;
			Page pageParam = Page.page(count, 10, pagenum);
			pageParam.buildUrl(request);
			
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());
			params.put("_orderby", "SEND_TIME DESC");
			smsList = orderSendSmsService.findOrdSmsSendList(params);
			pageParam.setItems(smsList);
			model.addAttribute("pageParam", pageParam);
		}else{
			Page pageParam = Page.page(0, 10, 1);
			pageParam.buildUrl(request);
			pageParam.setItems(smsList);
			model.addAttribute("pageParam", pageParam);
		}
		return "/order/sms/showOrdSmsList";
	}
	
	/**
	 * 获取当前日期的上一个月时间
	 * 
	 * @return
	 */
	private String getPreviousMonth(int month) {
		Calendar nowCal = Calendar.getInstance();
		nowCal.add(Calendar.MONTH, -month);
		Date previousDate = nowCal.getTime();
		return DateUtil.formatDate(previousDate, "yyyy-MM-dd HH:mm:ss");
	}
	
}
