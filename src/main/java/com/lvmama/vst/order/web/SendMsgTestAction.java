package com.lvmama.vst.order.web;
import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
@Controller
@RequestMapping("/ord/sendMsg")
public class SendMsgTestAction extends BaseActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	private static final Log LOG = LogFactory.getLog(SendMsgTestAction.class);

	@RequestMapping(value = "/sendMsgTest")
	public void sendMsg(Model model, String sql, HttpServletRequest req){
		LOG.debug("test send msg start--------------");
		
		String strOrdId =  req.getParameter("orderId");//订单号
		Long longOrdId = Long.valueOf(strOrdId);
		LOG.debug("test send msg ordid="+strOrdId);
		
		String passWord = req.getParameter("passWord");
		LOG.debug("test send msg passWord="+passWord);
		
		String sendNode = req.getParameter("sendNode");//发送节点
		LOG.debug("test send msg sendNode="+sendNode);
		
		if("111111".equals(passWord)){//
			//orderSendSmsService.sendSms(longOrdId, OrdSmsTemplate.SEND_NODE.valueOf("ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND"));
			orderSendSmsService.sendSms(longOrdId, OrdSmsTemplate.SEND_NODE.valueOf(sendNode));
		}else{
			LOG.debug("test send msg passWord is error");
		}
		
	}
	
	@RequestMapping(value = "/memConfig")
	@ResponseBody
	public Object sendMsg2(Model model, String sql, HttpServletRequest req){
		String result = ZooKeeperConfigProperties.getProperties(sql);
		return result;
	}
	

}
