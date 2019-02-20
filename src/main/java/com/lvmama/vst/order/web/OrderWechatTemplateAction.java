package com.lvmama.vst.order.web;

import com.lvmama.vst.back.order.po.OrdWechatTemplate;
import com.lvmama.vst.back.order.po.OrdWechatTemplate.WechatInfo;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrdWechatTemplateService;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 
 * 订单微信消息模板action
 * @author zhaomingzhu
 *
 */
@Controller
public class OrderWechatTemplateAction extends BaseActionSupport {

	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 525427079629392136L;

	/**
	 * 日志
	 */
	private static final Log LOG = LogFactory.getLog(OrderWechatTemplateAction.class);
	
	@Autowired
	private IOrdWechatTemplateService ordWechatTemplateService;
	
	@RequestMapping(value="/ord/ordWechatTemplate/findOrdWechatTemplateList.do")
	public String findOrdWechatTemplateList(Model model, Integer page, OrdWechatTemplate ordWechatTemplate, HttpServletRequest request){

		LOG.info("OrderWechatTemplateAction >>> findOrdWechatTemplateList");
		
		//1.取到发送节点列表
		model.addAttribute("nodeList", OrdWechatTemplate.SendNode.values());
		//2.取到消息类型列表
		model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.values());
		//3.返回查询条件
		model.addAttribute("ordWechatTemplate", ordWechatTemplate);
		//4.查询模板
		Map<String, Object> params = new HashMap<String, Object>();
		if(StringUtil.isNotEmptyString(ordWechatTemplate.getSendNode())){
			params.put("sendNode", ordWechatTemplate.getSendNode());
		}
		if(StringUtil.isNotEmptyString(ordWechatTemplate.getMessageCode())){
			params.put("messageCode", ordWechatTemplate.getMessageCode());
		}
		if(StringUtil.isNotEmptyString(ordWechatTemplate.getState())){
			params.put("state", ordWechatTemplate.getState());
		}		
		//5.数量
		int count = ordWechatTemplateService.findOrdWechatTemplateCount(params);
		//6.分页设置
		int currPage = (page == null ? 1 : page);
		Page pageData = Page.page(count, 10, currPage);
		pageData.buildUrl(request);
		params.put("_start", pageData.getStartRows());
		params.put("_end", pageData.getEndRows());
		params.put("_orderby", "UPDATED_TIME");
		params.put("_order", "DESC");
		//7.列表
		List<OrdWechatTemplate> ordWechatTemplates= ordWechatTemplateService.findOrdWechatTemplateList(params);
		pageData.setItems(ordWechatTemplates);
		
		model.addAttribute("pageData", pageData);
		return "/order/sms/findOrdWechatTemplateList";
	}
	
	@RequestMapping(value="/ord/ordWechatTemplate/showAddOrdWechatTemplate.do")
	public String showAddOrdWechatTemplate(Model model,String messageType){
		LOG.info("OrderWechatTemplateAction >>> showAddOrdWechatTemplate");
		//查询已经有的模板
		Set<String> sendNodeSet = new HashSet<String>();
		List<OrdWechatTemplate.SendNode> sendNodeList = new ArrayList<OrdWechatTemplate.SendNode>();
		Map<String, Object> params = new HashMap<String, Object>();
		List<OrdWechatTemplate> ordWechatTemplates= ordWechatTemplateService.findOrdWechatTemplateList(params);
		if(ordWechatTemplates != null && ordWechatTemplates.size() > 0){
			for(OrdWechatTemplate template : ordWechatTemplates){
				if(template.getSendNode() != null){
					sendNodeSet.add(template.getSendNode());
				}
			}
		}
		for(OrdWechatTemplate.SendNode node : OrdWechatTemplate.SendNode.values()){
			if(!sendNodeSet.contains(node.getCode())){
				sendNodeList.add(node);
			}
		}
		//1.取到发送节点列表
		model.addAttribute("nodeList", sendNodeList);
/*		//2.取到消息类型列表
		model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.values());*/
		//3.默认选择消息类型ORDER_STATUS_CHANGE的变量集合展示到页面
		List<WechatInfo.InfoField> infoFields = OrdWechatTemplate.WechatInfoType.getInfoVars("ORDER_STATUS_CHANGE");

		LOG.info("messageType--------------"+messageType);
		
		if(OrdWechatTemplate.SendNode.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name().equals(messageType)){
			List<WechatInfo.InfoField> infoFields2 = OrdWechatTemplate.WechatInfoType.getInfoVars("HOTEL_TRAVEL_REMIND");
			model.addAttribute("infoFields", infoFields2);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.HOTEL_TRAVEL_REMIND);
		}
		else if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name().equals(messageType)) {
			List<WechatInfo.InfoField> infoFields2 = OrdWechatTemplate.WechatInfoType.getInfoVars("TICKETS_TRAVEL_REMIND");
			model.addAttribute("infoFields", infoFields2);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.TICKETS_TRAVEL_REMIND);
		}else if(OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.VERIFIED_PAY_APERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_APERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name().equals(messageType)||
				 OrdWechatTemplate.SendNode.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name().equals(messageType)){
			List<WechatInfo.InfoField> electronicInfoFields = OrdWechatTemplate.WechatInfoType.getInfoVars("ELECTRONIC_TICKETS_REMIND");
			model.addAttribute("infoFields", electronicInfoFields);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.ELECTRONIC_TICKETS_REMIND);
		}else if(OrdWechatTemplate.SendNode.ORDER_NORMAL_REFUND.name().equals(messageType)||
				OrdWechatTemplate.SendNode.ORDER_REFUND_APPLY.name().equals(messageType)||
				OrdWechatTemplate.SendNode.CANCEL_NO_REFUND.name().equals(messageType)||
				OrdWechatTemplate.SendNode.CANCEL_REFUND_FIRST_BACK.name().equals(messageType)||
				OrdWechatTemplate.SendNode.CANCEL_REFUND_UNFIRST_BACK.name().equals(messageType)){
			List<WechatInfo.InfoField> refundInfoFields = OrdWechatTemplate.WechatInfoType.getInfoVars("REFUND_REMIND");
			model.addAttribute("infoFields", refundInfoFields);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.REFUND_REMIND); 
		
		}else if (messageType == null) {
			model.addAttribute("infoFields", infoFields);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.ORDER_STATUS_CHANGE);
		}else{
			model.addAttribute("infoFields", infoFields);
			model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.ORDER_STATUS_CHANGE);
		}
		model.addAttribute("messageType",messageType);
		return "/order/sms/showAddOrdWechatTemplate";
	}
	
	@RequestMapping(value="/ord/ordWechatTemplate/addOrdWechatTemplate.do")
	@ResponseBody
	public Object addOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate,String messageType){
		LOG.info("OrderWechatTemplateAction >>> addOrdWechatTemplate");
		if(ordWechatTemplate != null){
			try{
				ordWechatTemplate.setCreatedTime(new Date());
				ordWechatTemplate.setUpdatedTime(new Date());
				ordWechatTemplate.setCreatedUser(this.getLoginUserId());
				ordWechatTemplate.setUpdatedUser(this.getLoginUserId());
				ordWechatTemplate.setState("Y");
				ordWechatTemplate.setMessageName(ordWechatTemplate.getMessageCode());
				ordWechatTemplate.setName(ordWechatTemplate.getSendNode());
				
				OrdWechatTemplate.WechatInfo wechatInfo = ordWechatTemplate.getWechatInfo();
				ordWechatTemplate.setWechatInfo(wechatInfo);
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("touser", "${" + OrdWechatTemplate.WechatInfo.InfoField.WECHAT_ID.nameToLower() + "}");
				jsonObject.put("template_id", ordWechatTemplate.getTemplateId());
				
				//电子票提醒、退款提醒
				String mssageCode = ordWechatTemplate.getMessageCode();
				LOG.info("mssageCode----------------------"+mssageCode);
				if(OrdWechatTemplate.WechatInfoType.ELECTRONIC_TICKETS_REMIND.getCode().equals(mssageCode)||(OrdWechatTemplate.WechatInfoType.REFUND_REMIND.getCode().equals(mssageCode))){
					jsonObject.put("url", OrdWechatTemplate.URL1 + "?orderType=BIZ_VST&productType=hotel&orderId=${" + OrdWechatTemplate.WechatInfo.InfoField.ORDER_ID.nameToLower() + "}");
				}else{
					jsonObject.put("url", OrdWechatTemplate.URL + "?orderType=BIZ_VST&productType=hotel&orderId=${" + OrdWechatTemplate.WechatInfo.InfoField.ORDER_ID.nameToLower() + "}");
				}
				
				jsonObject.put("topcolor", "#FF0000");
				//取到变量
				Map<String,OrdWechatTemplate.FieldDetail>  fieldDetailMap= new HashMap<String, OrdWechatTemplate.FieldDetail>();
				for(Map<String, String> map : wechatInfo.getInfoVars()){
					for(String key : map.keySet()){
						String value = map.get(key);
						OrdWechatTemplate.FieldDetail fieldDetail = new OrdWechatTemplate.FieldDetail();
						if(StringUtil.isNotEmptyString(value)){
							fieldDetail.setValue(value);
							fieldDetail.setColor("#000000");
						}else{
							fieldDetail.setValue("");
							fieldDetail.setColor("#000000");						
						}

						LOG.info("messageType#############"+mssageCode);
						if(OrdWechatTemplate.SendNode.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name().equals(messageType)){
							OrdWechatTemplate.WechatInfo.HotelInfoField  field = OrdWechatTemplate.WechatInfo.HotelInfoField.getInfoFieldObj(key);
							fieldDetailMap.put(field.nameToLower(), fieldDetail);
						}
						else if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name().equals(messageType)) {
							OrdWechatTemplate.WechatInfo.TicketsInfoField  field = OrdWechatTemplate.WechatInfo.TicketsInfoField.getInfoFieldObj(key);
							fieldDetailMap.put(field.nameToLower(), fieldDetail);
						}else if(OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.VERIFIED_PAY_APERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_APERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name().equals(messageType)||
								 OrdWechatTemplate.SendNode.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name().equals(messageType)){
							OrdWechatTemplate.WechatInfo.ElectronicInfoField  field = OrdWechatTemplate.WechatInfo.ElectronicInfoField.getInfoFieldObj(key);
							fieldDetailMap.put(field.nameToLower(), fieldDetail);
						}else if(OrdWechatTemplate.SendNode.ORDER_NORMAL_REFUND.name().equals(messageType)||
								OrdWechatTemplate.SendNode.ORDER_REFUND_APPLY.name().equals(messageType)||
								OrdWechatTemplate.SendNode.CANCEL_NO_REFUND.name().equals(messageType)||
								OrdWechatTemplate.SendNode.CANCEL_REFUND_FIRST_BACK.name().equals(messageType)||
								OrdWechatTemplate.SendNode.CANCEL_REFUND_UNFIRST_BACK.name().equals(messageType)){
							OrdWechatTemplate.WechatInfo.RefundInfoField  field = OrdWechatTemplate.WechatInfo.RefundInfoField.getInfoFieldObj(key);
							fieldDetailMap.put(field.nameToLower(), fieldDetail);
						}else{
							OrdWechatTemplate.WechatInfo.InfoField  field = OrdWechatTemplate.WechatInfo.InfoField.getInfoFieldObj(key);
							fieldDetailMap.put(field.nameToLower(), fieldDetail);
						}

					}
				}
				jsonObject.put("data", fieldDetailMap);
				ordWechatTemplate.setMessageContent(jsonObject.toString());
				ordWechatTemplateService.addOrdWechatTemplate(ordWechatTemplate);
				return ResultMessage.ADD_SUCCESS_RESULT;
			}catch(Exception e){
				return ResultMessage.ADD_FAIL_RESULT;
			}
		}
		return ResultMessage.ADD_FAIL_RESULT;
	}
	@RequestMapping(value="/ord/ordWechatTemplate/showEditOrdWechatTemplate.do")
	public String showEditOrdWechatTemplate(Model model, Long id){
		LOG.info("OrderWechatTemplateAction >>> showEditOrdWechatTemplate");
		//1.取到发送节点列表
		model.addAttribute("nodeList", OrdWechatTemplate.SendNode.values());
		//2.取到消息类型列表
		model.addAttribute("wechatInfoTypeList", OrdWechatTemplate.WechatInfoType.values());		
		//3.查询微信模板
		OrdWechatTemplate ordWechatTemplate = ordWechatTemplateService.findOrdWechatTemplateById(id);
		
		JSONObject jsonObject = JSONObject.fromObject(ordWechatTemplate.getMessageContent());
		
		Map<String,String> map = new HashMap<String, String>();
		String data = (String)jsonObject.get("data").toString();
		JSONObject jsonObj1 = JSONObject.fromObject(data);
		Iterator<String> keys = jsonObj1.keys();
		OrdWechatTemplate.FieldDetail fieldDetail=null;
		String key =null;
		while(keys.hasNext()) {
			key = keys.next();
			String value = jsonObj1.get(key).toString();

			JSONObject jsonObj2 = JSONObject.fromObject(value);
			fieldDetail = new OrdWechatTemplate.FieldDetail();
			fieldDetail = (OrdWechatTemplate.FieldDetail) jsonObj2.toBean(jsonObj2, OrdWechatTemplate.FieldDetail.class);


			if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name().equals(ordWechatTemplate.getSendNode())) {
				OrdWechatTemplate.WechatInfo.HotelInfoField infoField = OrdWechatTemplate.WechatInfo.HotelInfoField.getInfoFieldObj(OrdWechatTemplate.WechatInfo.HotelInfoField.nameAddUnderline(key));
				if (infoField != null) {
					map.put(infoField.getCode(), fieldDetail.getValue());
				}
			} else if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name().equals(ordWechatTemplate.getSendNode())) {
				OrdWechatTemplate.WechatInfo.TicketsInfoField infoField = OrdWechatTemplate.WechatInfo.TicketsInfoField.getInfoFieldObj(OrdWechatTemplate.WechatInfo.TicketsInfoField.nameAddUnderline(key));
				if (infoField != null) {
					map.put(infoField.getCode(), fieldDetail.getValue());
				}
			}else if(OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.VERIFIED_PAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name().equals(ordWechatTemplate.getSendNode())||
					 OrdWechatTemplate.SendNode.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name().equals(ordWechatTemplate.getSendNode())){
				OrdWechatTemplate.WechatInfo.ElectronicInfoField infoField = OrdWechatTemplate.WechatInfo.ElectronicInfoField.getInfoFieldObj(OrdWechatTemplate.WechatInfo.ElectronicInfoField.nameAddUnderline(key));
				if (infoField != null) {
					map.put(infoField.getCode(), fieldDetail.getValue());
				}
			}else if(OrdWechatTemplate.SendNode.ORDER_NORMAL_REFUND.name().equals(ordWechatTemplate.getSendNode())||
					OrdWechatTemplate.SendNode.ORDER_REFUND_APPLY.name().equals(ordWechatTemplate.getSendNode())||
					OrdWechatTemplate.SendNode.CANCEL_NO_REFUND.name().equals(ordWechatTemplate.getSendNode())||
					OrdWechatTemplate.SendNode.CANCEL_REFUND_FIRST_BACK.name().equals(ordWechatTemplate.getSendNode())||
					OrdWechatTemplate.SendNode.CANCEL_REFUND_UNFIRST_BACK.name().equals(ordWechatTemplate.getSendNode())){
				OrdWechatTemplate.WechatInfo.RefundInfoField infoField = OrdWechatTemplate.WechatInfo.RefundInfoField.getInfoFieldObj(OrdWechatTemplate.WechatInfo.RefundInfoField.nameAddUnderline(key));
				if (infoField != null) {
					map.put(infoField.getCode(), fieldDetail.getValue());
				}
			}else {
				OrdWechatTemplate.WechatInfo.InfoField infoField = OrdWechatTemplate.WechatInfo.InfoField.getInfoFieldObj(OrdWechatTemplate.WechatInfo.InfoField.nameAddUnderline(key));
				if (infoField != null) {
					map.put(infoField.getCode(), fieldDetail.getValue());
				}
			}
		}
		List<WechatInfo.InfoField> infoFields = OrdWechatTemplate.WechatInfoType.getInfoVars(ordWechatTemplate.getMessageCode());
		model.addAttribute("infoFields", infoFields);
		model.addAttribute("fieldDetailMap", map);
		model.addAttribute("ordWechatTemplate", ordWechatTemplate);
		
		return "/order/sms/showEditOrdWechatTemplate";
	}
	
	@RequestMapping(value="/ord/ordWechatTemplate/editOrdWechatTemplate.do")
	@ResponseBody
	public Object editOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate){
		LOG.info("OrderWechatTemplateAction >>> editOrdWechatTemplate");
		if(ordWechatTemplate != null){
			try{
				OrdWechatTemplate ordWechat = ordWechatTemplateService.findOrdWechatTemplateById(ordWechatTemplate.getId());
				ordWechat.setMessageCode(ordWechatTemplate.getMessageCode());
				ordWechat.setMessageName(ordWechatTemplate.getMessageCode());
				ordWechat.setName(ordWechatTemplate.getSendNode());
				ordWechat.setSendNode(ordWechatTemplate.getSendNode());
				ordWechat.setTemplateId(ordWechatTemplate.getTemplateId());
				ordWechat.setUpdatedTime(new Date());
				ordWechat.setUpdatedUser(this.getLoginUserId());
				OrdWechatTemplate.WechatInfo wechatInfo = ordWechatTemplate.getWechatInfo();
				ordWechat.setWechatInfo(wechatInfo);
				
				JSONObject jsonObj = JSONObject.fromObject(ordWechat.getMessageContent());
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("touser", jsonObj.get("touser"));
				jsonObject.put("url", jsonObj.get("url"));
				jsonObject.put("topcolor", jsonObj.get("topcolor"));			
				jsonObject.put("template_id", ordWechatTemplate.getTemplateId());
				//取到变量
				Map<String,OrdWechatTemplate.FieldDetail>  fieldDetailMap= new HashMap<String, OrdWechatTemplate.FieldDetail>();
				for(Map<String, String> map : wechatInfo.getInfoVars()){
					for(String key : map.keySet()){
						String value = map.get(key);
						OrdWechatTemplate.FieldDetail fieldDetail = new OrdWechatTemplate.FieldDetail();
						if(StringUtil.isNotEmptyString(value)){
							fieldDetail.setValue(value);
							fieldDetail.setColor("#000000");
						}else{
							fieldDetail.setValue("");
							fieldDetail.setColor("#000000");						
						}

						if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_HOTEL_DAY_BEFORE_REMIND.name().equals(ordWechatTemplate.getSendNode())) {
							OrdWechatTemplate.WechatInfo.HotelInfoField infoField = OrdWechatTemplate.WechatInfo.HotelInfoField.getInfoFieldObj(key);
							if (infoField != null) {
								fieldDetailMap.put(infoField.nameToLower(), fieldDetail);
							}
						} else if (OrdWechatTemplate.SendNode.ORDER_TRAVEL_TICKET_DAY_BEFORE_REMIND.name().equals(ordWechatTemplate.getSendNode())) {
							OrdWechatTemplate.WechatInfo.TicketsInfoField infoField = OrdWechatTemplate.WechatInfo.TicketsInfoField.getInfoFieldObj(key);
							if (infoField != null) {
								fieldDetailMap.put(infoField.nameToLower(), fieldDetail);
							}
						}else if(OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_LV.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.ORDER_CREATE_VERIFIED_PAY_UNAPERIODIC_QRCODE_PRO.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.VERIFIED_PAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.VERIFIED_PAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_APERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_UNAPERIODIC_QRCODE.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_BJ.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.PAYMENT_PREPAY_QRCODE_SUPPLIER_GZ.name().equals(ordWechatTemplate.getSendNode())||
								 OrdWechatTemplate.SendNode.PAYMENT_QRCODE_NOTIFY_SEND_CODE_TIME.name().equals(ordWechatTemplate.getSendNode())){
							OrdWechatTemplate.WechatInfo.ElectronicInfoField infoField = OrdWechatTemplate.WechatInfo.ElectronicInfoField.getInfoFieldObj(key);
							if (infoField != null) {
								fieldDetailMap.put(infoField.nameToLower(), fieldDetail);
							}
						}else if(OrdWechatTemplate.SendNode.ORDER_NORMAL_REFUND.name().equals(ordWechatTemplate.getSendNode())||
								OrdWechatTemplate.SendNode.ORDER_REFUND_APPLY.name().equals(ordWechatTemplate.getSendNode())||
								OrdWechatTemplate.SendNode.CANCEL_NO_REFUND.name().equals(ordWechatTemplate.getSendNode())||
								OrdWechatTemplate.SendNode.CANCEL_REFUND_FIRST_BACK.name().equals(ordWechatTemplate.getSendNode())||
								OrdWechatTemplate.SendNode.CANCEL_REFUND_UNFIRST_BACK.name().equals(ordWechatTemplate.getSendNode())){
							OrdWechatTemplate.WechatInfo.RefundInfoField infoField = OrdWechatTemplate.WechatInfo.RefundInfoField.getInfoFieldObj(key);
							if (infoField != null) {
								fieldDetailMap.put(infoField.nameToLower(), fieldDetail);
							}
						}
						else {
							OrdWechatTemplate.WechatInfo.InfoField field = OrdWechatTemplate.WechatInfo.InfoField.getInfoFieldObj(key);
							if (field != null) {
								fieldDetailMap.put(field.nameToLower(), fieldDetail);
							}
						}


					}
				}
				jsonObject.put("data", fieldDetailMap);
				ordWechat.setMessageContent(jsonObject.toString());
				
				ordWechatTemplateService.updateOrdWechatTemplate(ordWechat);
				return ResultMessage.UPDATE_SUCCESS_RESULT;
			}catch(Exception e){
				return ResultMessage.UPDATE_FAIL_RESULT;
			}
		}
		return ResultMessage.UPDATE_FAIL_RESULT;
	}
	
	@RequestMapping(value="/ord/ordWechatTemplate/updateOrdWechatTemplate.do")
	@ResponseBody
	public Object updateOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate){
		LOG.info("OrderWechatTemplateAction >>> updateOrdWechatTemplate");
		if(ordWechatTemplate != null){
			try{
				ordWechatTemplateService.updateOrdWechatTemplateStatus(ordWechatTemplate);
				return ResultMessage.UPDATE_SUCCESS_RESULT;
			}catch(Exception e){
				return ResultMessage.UPDATE_FAIL_RESULT;
			}
		}
		return ResultMessage.UPDATE_FAIL_RESULT;
	}
}
