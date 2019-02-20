package com.lvmama.vst.order.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.vst.back.client.passport.service.PassportSendSmsService;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.OrderSendSmsVO;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderSmsSendService;
/**
 * 订单详情页短信发送处理
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderSendSmsAction extends BaseActionSupport{
	//日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderSendSmsAction.class);
	//发送短信页面
	private static final String SEND_SMS_PAGE = "/order/sms/orderSendSms";
	//短信发送业务接口
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	@Autowired
	private IOrderSmsSendService orderSmsSendService;
	@Autowired
	PassportSendSmsService  passportSendSmsService;
	/**
	 * 进入短信发送页面
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoSendSmsPage.do")
	public String intoSendSmsPage(Model model,OrderSendSmsVO orderSendSmsVO) throws BusinessException{
		try {
			//短信模板发送节点字典
			Map<String, String> smsTempleMap = new HashMap<String, String>();
			smsTempleMap.put("", "请选择");
			for(OrdSmsTemplate.SEND_NODE sendNode:OrdSmsTemplate.SEND_NODE.values()){
				smsTempleMap.put(sendNode.name(), sendNode.getCnName());
			}
			//保存弹出页面条件
			model.addAttribute("orderSendSmsVO", orderSendSmsVO);
			model.addAttribute("smsTempleMap",smsTempleMap);
			return SEND_SMS_PAGE;
		} catch (BusinessException e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("服务器内部异常", e);
			}
			return null;
		}
	}
	
	/**
	 * 根据订单ID和节点选择短信内容
	 * 
	 * @param model
	 * @param orderSendSmsVO
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/selectSmsContent.do")
	public void selectSmsContent(Model model,OrderSendSmsVO orderSendSmsVO,HttpServletResponse response) throws BusinessException{
		//构造短信发送JSON数据
		JSONObject jsonObject = new JSONObject();
		try {
			//准备查找短信内容参数
			Long orderId = orderSendSmsVO.getOrderId();
			if(null == orderId){
				throw new BusinessException("订单编号不能为空！");
			}
			String sendNode = orderSendSmsVO.getSendNode();
			if(!UtilityTool.isValid(sendNode)){
				throw new BusinessException("短信模板发送节点不能为空！");
			}
			//查找接口已经组合好的短信内容
			String content = null;
			if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
				//String content = orderSmsSendService.getContent(orderId, sendNode);
				content = orderSmsSendService.getContent(orderId, OrdSmsTemplate.SEND_NODE.valueOf(sendNode));
			}else{
				//String content = orderSendSmsService.getContent(orderId, sendNode);
				content = orderSendSmsService.getContent(orderId, OrdSmsTemplate.SEND_NODE.valueOf(sendNode));
			}
			if(!UtilityTool.isValid(content)){
				//throw new BusinessException("没有找到该节点的短信内容,请找人先配置好！");
				throw new BusinessException("没有配置该节点的短信内容或者该节点的短信模板已被禁用，请联系管理员！");
			}
			jsonObject.put("msg", "success");
			jsonObject.put("result", content);
			JSONOutput.writeJSON(response, jsonObject);
		} catch (BusinessException e) {
			jsonObject.put("msg", "failure");
			jsonObject.put("result", e.getMessage());
			JSONOutput.writeJSON(response, jsonObject);
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("服务器内部异常", e);
			}
		}
	}
	
	/**
	 * 发送短信
	 * @param model
	 * @param orderSendSmsVO
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/sendSms.do")
	public void sendSms(Model model,OrderSendSmsVO orderSendSmsVO,HttpServletResponse response) throws BusinessException{
		PermUser user = (PermUser) ServletUtil.getSession(
				HttpServletLocalThread.getRequest(),
				HttpServletLocalThread.getResponse(),
				Constant.SESSION_BACK_USER);
		LOGGER.info("sendSMSOperator:"
				+ (user == null ? "no login!" : user.getUserName())+",orderId:"+orderSendSmsVO.getOrderId());
		if(orderSendSmsVO.getSmsType().equals("ordinarySMS")){
			//构造短信发送JSON数据
			JSONObject jsonObject = new JSONObject();
			try {
				//准备发送短信的接口参数
				Long orderId = orderSendSmsVO.getOrderId();
				if(null == orderId){
					throw new BusinessException("订单编号不能为空!");
				}
				String mobile = orderSendSmsVO.getMobile();
				if(!UtilityTool.isValid(mobile)){
					throw new BusinessException("订单联系人手机号码不能为空!");
				}
				String content = orderSendSmsVO.getContent();
				if(!UtilityTool.isValid(content)){
					throw new BusinessException("短信内容不能为空!");
				}
				String operate = this.getLoginUserId();
				if(!UtilityTool.isValid(operate)){
					throw new BusinessException("当前登录人不能为空!");
				}
				if("old".equals(Constant.getInstance().getProperty("orderSms.version"))){//旧
					orderSmsSendService.sendSmsByCustom(orderId, content, operate, mobile);
				}else{
					orderSendSmsService.sendSmsByCustom(orderId, content, operate, mobile);
				}
				
				jsonObject.put("msg", "success");
				jsonObject.put("result", "短信提交成功!");
				JSONOutput.writeJSON(response, jsonObject);
			} catch (BusinessException e) {
				jsonObject.put("msg", "failure");
				jsonObject.put("result", e.getMessage());
				JSONOutput.writeJSON(response, jsonObject);
				if(LOGGER.isDebugEnabled()){
					LOGGER.error("服务器内部异常", e);
				}
			}
		}else{
		   JSONObject jsonObject = new JSONObject();
            try {
                Long orderId = orderSendSmsVO.getOrderId();
                if(null == orderId){
                    throw new BusinessException("请记录订单号并联系开发人员!");
                }
                passportSendSmsService.resendSms(orderSendSmsVO.getOrderId());
                jsonObject.put("msg", "success");
                jsonObject.put("result", "短信提交成功!");
                JSONOutput.writeJSON(response, jsonObject);
            }catch (BusinessException e){
                jsonObject.put("msg", "failure");
                jsonObject.put("result", e.getMessage());
                JSONOutput.writeJSON(response, jsonObject);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.error("服务器内部异常", e);
                }
            }
		}
	}
}
