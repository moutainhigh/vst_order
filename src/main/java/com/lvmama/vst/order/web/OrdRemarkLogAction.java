package com.lvmama.vst.order.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.order.po.OrdRemarkLog;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdRemarkLogService;

@Controller
public class OrdRemarkLogAction extends BaseActionSupport {

	private static final Log Log = LogFactory.getLog(OrdRemarkLogAction.class);
	
	@Autowired
	private IOrdRemarkLogService ordRemarkLogService;
	
	@RequestMapping(value = "/ord/ordRemarkLog/showOrdRemarkLogList")
	public String showOrdRemarkLogList(Model model, Integer page, Integer pageSize, HttpServletRequest request) throws BusinessException  {
		String orderId = request.getParameter("orderId");
		String flag = request.getParameter("flag");
		if(pageSize != null){
			flag = "N";
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		int count = ordRemarkLogService.findOrdRemarkLogCount(params);
		
		Integer currentPage = (page == null ? 1 : page);
		Integer currentPageSize = (pageSize == null ? 10 : pageSize);
		Page pageParam = Page.page(count, currentPageSize, currentPage);
		
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		params.put("_orderby", "CREATED_TIME DESC");
		List<OrdRemarkLog> ordRemarkLogList = ordRemarkLogService.findOrdRemarkLogList(params);
		
		pageParam.setItems(ordRemarkLogList);
		pageParam.buildUrl(request);
		
		model.addAttribute("orderId", orderId);
		model.addAttribute("pageParam", pageParam);
		if("Y".equals(flag)){
			return "/order/orderStatusManage/ordRemarkLogList";
		}else{
			return "/order/orderStatusManage/showOrdRemarkLogList";
		}
	}
	
	@RequestMapping(value = "/ord/ordRemarkLog/saveOrdRemarkLog")
	@ResponseBody
	public Object saveOrdRemarkLog(OrdRemarkLog ordRemarkLog, Model model) throws BusinessException {
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 if(null == ordRemarkLog){
				 throw new IllegalArgumentException("对象为空");
			 }
			 if(null == ordRemarkLog.getOrderId()){
				 throw new IllegalArgumentException("订单ID为空");
			 }
			 ordRemarkLog.setCreatedTime(new Date());
			 ordRemarkLog.setCreatedUser(this.getLoginUserId());
			 ordRemarkLogService.addOrdRemarkLog(ordRemarkLog);
			 msg.setCode("success");
			 msg.setMessage("保存成功");
		 } catch (Exception e) {
			 msg.setCode("error");
			 msg.setMessage("保存失败:"+e.getMessage());
			 log.error(e);
		}
		 return msg;
	}
}
