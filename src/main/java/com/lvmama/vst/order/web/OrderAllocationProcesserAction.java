package com.lvmama.vst.order.web;

import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 活动分单补偿action
 *
 */
@Controller
@RequestMapping("/order/allocation")
public class OrderAllocationProcesserAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrderAllocationProcesserAction.class);

	@Autowired
	private IOrderDistributionBusiness distributionBusiness;

	//订单活动审核业务
	@Autowired
	private IOrderAuditService orderAuditService;

	@Resource(name="allocationMessageProducer")
	private TopicMessageProducer allocationMessageProducer;

	@RequestMapping(value = "/makeup")
	@ResponseBody
	public Object makeup(HttpServletRequest request, String auditIds, String addition) {
		if(!checkUrlValid(request.getParameter("code"))){
			return ("连接非法");
		}
		LOG.info("makeup auditId:" + auditIds);
		if(auditIds == null) {
			return "auditIds is null";
		}
		for(String auditId :auditIds.split(",")){
			if (StringUtil.isEmptyString(auditId)) {
				continue;
			}
			Message message =MessageFactory.newOrderAllocationMessage(Long.valueOf(auditId), addition);
			allocationMessageProducer.sendMsg(message);
		}
		return "successful";
	}


	private boolean checkUrlValid(String code){
		if(code == null){
			return false;
		}

		try{
			code = DESCoder.decrypt(code);
		}catch(Exception e){
			log.info(e);
		}

		String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());

		if(today.equalsIgnoreCase(code)){
			return true;
		}
		return false;
	}
}
