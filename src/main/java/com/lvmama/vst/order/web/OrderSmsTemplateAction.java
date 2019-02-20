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

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderSmsNotSendRuleService;
import com.lvmama.vst.order.service.IOrderSmsTemplateService;

@Controller
@RequestMapping("/order/ordSmsTemplate")
public class OrderSmsTemplateAction {

private static final Log LOG = LogFactory.getLog(OrderSmsTemplateAction.class);
	
	@Autowired 
	private IOrderSmsTemplateService smsTemplateService ;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private DistributorClientService distributorClientService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private IOrderSmsNotSendRuleService orderSmsNotSendRuleService;
	
	@RequestMapping(value = "/findOrdSmsTemplateList")
	public String findOrdSmsTemplateList(Model model,OrdSmsTemplate ordSmsTemplate , Integer page, HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrdSmsTemplateList>");
		}
		//获取可选的品类和分销商
		Map<String, Object> distributorParam = new HashMap<String, Object>();
		distributorParam.put("cancelFlag", "Y");
		List<Distributor> distributorlList = distributorClientService.findDistributorList(distributorParam).getReturnContent();
		
		ResultHandleT<List<BizCategory>>  bizCategoryListResult = categoryClientService.findCategoryByAllValid();
		List<BizCategory> bizCategoryList = bizCategoryListResult.getReturnContent();
		
		if (bizCategoryList.size() > 0) {
			model.addAttribute("bizCategoryList", bizCategoryList);
		}
		if (distributorlList.size() > 0) {
			model.addAttribute("distributorlList", distributorlList);
		}
		model.addAttribute("orderTimeList", OrdSmsTemplate.ORDER_TIME.values());
		model.addAttribute("sendNodeList", OrdSmsTemplate.getShowSendNodes());
		Map<String, Object> paramOrdSmsTemplate = new HashMap<String, Object>();
		paramOrdSmsTemplate.clear();
		if (null != ordSmsTemplate.getCategoryId()) {
			paramOrdSmsTemplate.put("categoryId", ordSmsTemplate.getCategoryId());
		}
		if (null != ordSmsTemplate.getSendNode() && !"".equals(ordSmsTemplate.getSendNode())) {
			paramOrdSmsTemplate.put("sendNode", ordSmsTemplate.getSendNode());
		}
		if (null != ordSmsTemplate.getSuplierId()) {
			paramOrdSmsTemplate.put("suplierId", ordSmsTemplate.getSuplierId());
		}
		if (null !=ordSmsTemplate.getDistributorId()) {
			paramOrdSmsTemplate.put("distributorId", ordSmsTemplate.getDistributorId());
		}
		if (null != ordSmsTemplate.getOrderTime() && !"".equals(ordSmsTemplate.getOrderTime()) ) {
			paramOrdSmsTemplate.put("orderTime", ordSmsTemplate.getOrderTime());
		}
		
		
		//选择展示模板
		String rule = req.getParameter("rule");
		if ("Y".equals(rule)) {
			if (null !=ordSmsTemplate.getTemplateName() && !"".equals(ordSmsTemplate.getTemplateName())) {
				paramOrdSmsTemplate.put("templateName",ordSmsTemplate.getTemplateName());
			}
			int count = smsTemplateService.findOrdSmsTemplateCount(paramOrdSmsTemplate);
			int pagenum = page == null ? 1 : page;
			Page pageParam = Page.page(count, 10, pagenum);
			pageParam.buildUrl(req);
			paramOrdSmsTemplate.put("_start", pageParam.getStartRows());
			paramOrdSmsTemplate.put("_end", pageParam.getEndRows());
			paramOrdSmsTemplate.put("_orderby", "CREATA_TIME DESC");
			List<OrdSmsTemplate> ordSmsTemplateList = smsTemplateService.findOrdSmsTemplateList(paramOrdSmsTemplate);
			if (ordSmsTemplateList.size() > 0) {
				for (OrdSmsTemplate ordSmsTemplate2 : ordSmsTemplateList) {
					if (null != ordSmsTemplate2.getCategoryId()) {
						ResultHandleT<BizCategory> bizCategoryResult = categoryClientService.findCategoryById(ordSmsTemplate2.getCategoryId());
						ordSmsTemplate2.setBizCategory(bizCategoryResult.getReturnContent());
					}
				}
			}
			pageParam.setItems(ordSmsTemplateList);
			model.addAttribute("pageParam", pageParam);
		} else if("N".equals(rule)){

			if (null !=ordSmsTemplate.getTemplateName() && !"".equals(ordSmsTemplate.getTemplateName())) {
				paramOrdSmsTemplate.put("ruleName",ordSmsTemplate.getTemplateName());
			}
			int count = orderSmsNotSendRuleService.findOrdSmsNotSendRuleCount(paramOrdSmsTemplate);
			int pagenum = page == null ? 1 : page;
			Page pageParam = Page.page(count, 10, pagenum);
			pageParam.buildUrl(req);
			paramOrdSmsTemplate.put("_start", pageParam.getStartRows());
			paramOrdSmsTemplate.put("_end", pageParam.getEndRows());
			paramOrdSmsTemplate.put("_orderby", "CREATA_TIME DESC");
			List<OrdSmsNotSendRule> ordSmsTemplateList = orderSmsNotSendRuleService.findOrdSmsNotSendRuleList(paramOrdSmsTemplate);
			if (ordSmsTemplateList.size() > 0) {
				for (OrdSmsNotSendRule ordSmsNotSendRule : ordSmsTemplateList) {
					if (null != ordSmsNotSendRule.getCategoryId()) {
						ResultHandleT<BizCategory> bizCategoryResult = categoryClientService.findCategoryById(ordSmsNotSendRule.getCategoryId());
						ordSmsNotSendRule.setBizCategory(bizCategoryResult.getReturnContent());
					}
				}
			}
			pageParam.setItems(ordSmsTemplateList);
			model.addAttribute("pageParam", pageParam);
		}
		model.addAttribute("ordSmsTemplate", ordSmsTemplate);
		model.addAttribute("rule",rule );
		return "/order/sms/findSmsTemplateList";
	}
	
	
	
	/**
	 * 跳转到预览
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value = "/showOrdSmsTemplate")
	public String showOrdSmsTemplate(Model model,Long templateId , HttpServletRequest req) throws BusinessException{
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrdSmsTemplate>");
		}
        String rule = req.getParameter("rule");
        if ("Y".equals(rule)) {
			OrdSmsTemplate ordSmsTemplate = smsTemplateService.findOrdSmsTemplateById(templateId);
			model.addAttribute("ordSmsTemplate", ordSmsTemplate);
			String categoryName = categoryClientService.findCategoryById(ordSmsTemplate.getCategoryId()).getReturnContent().getCategoryName();
			model.addAttribute("categoryName", categoryName);
			if (null != ordSmsTemplate.getSuplierId() ) {
				String supplierName = getSupplierNameBySupplierId(ordSmsTemplate.getSuplierId());
				model.addAttribute("supplierName", supplierName);
			}
			if (null != ordSmsTemplate.getDistributorId()&& ordSmsTemplate.getDistributorId()!=-1L) {
				String distributorName = distributorClientService.findDistributorById(ordSmsTemplate.getDistributorId()).getReturnContent().getDistributorName();
				model.addAttribute("distributorName", distributorName);
			}else if (-1L == ordSmsTemplate.getDistributorId()) {
				model.addAttribute("distributorName", "全部");
			}
		}else if ("N".equals(rule)) {
			OrdSmsNotSendRule ordSmsNotSendRule = orderSmsNotSendRuleService.findOrdSmsNotSendRuleById(templateId);
			model.addAttribute("ordSmsTemplate", ordSmsNotSendRule);
			String categoryName = categoryClientService.findCategoryById(ordSmsNotSendRule.getCategoryId()).getReturnContent().getCategoryName();
			model.addAttribute("categoryName", categoryName);
			if (null != ordSmsNotSendRule.getSuplierId() ) {
				String supplierName = getSupplierNameBySupplierId(ordSmsNotSendRule.getSuplierId());
				model.addAttribute("supplierName", supplierName);
			}
			if (null != ordSmsNotSendRule.getDistributorId()&& ordSmsNotSendRule.getDistributorId()!=-1L) {
				String distributorName = distributorClientService.findDistributorById(ordSmsNotSendRule.getDistributorId()).getReturnContent().getDistributorName();
				model.addAttribute("distributorName", distributorName);
			}else if (-1L == ordSmsNotSendRule.getDistributorId()) {
				model.addAttribute("distributorName", "全部");
			}
		} 
		model.addAttribute("orderTimeList", OrdSmsTemplate.ORDER_TIME.values());
		model.addAttribute("sendNodeList", OrdSmsTemplate.getShowSendNodes());
		model.addAttribute("rule", rule);
		return "/order/sms/showSmsTemplate";
	}
	
	
	
	/**
	 * 跳转到创建短信模板
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value = "/showAddOrdSmsTemplate")
	public String showAddOrdSmsTemplate(Model model, Long templateId, OrdSmsTemplate ordSmsTemplate, HttpServletRequest req ) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddOrdSmsTemplate>");
		}
		Map<String, Object> distributorParam = new HashMap<String, Object>();
		distributorParam.put("cancelFlag", "Y");
		List<Distributor> distributorlList = distributorClientService.findDistributorList(distributorParam).getReturnContent();
		List<BizCategory> bizCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		if (bizCategoryList.size() > 0) {
			model.addAttribute("bizCategoryList", bizCategoryList);
		}
		if (distributorlList.size() > 0) {
			model.addAttribute("distributorlList", distributorlList);
		}
		OrdSmsNotSendRule ordSmsNotSendRule = new OrdSmsNotSendRule();
		ordSmsNotSendRule = orderSmsNotSendRuleService.findOrdSmsNotSendRuleById(templateId);
		if (null != ordSmsNotSendRule && null != ordSmsNotSendRule.getSuplierId()) {
			String supplierName = getSupplierNameBySupplierId(ordSmsNotSendRule.getSuplierId());
			model.addAttribute("supplierName", supplierName);
		}
		model.addAttribute("ordSmsNotSendRule",ordSmsNotSendRule );
		model.addAttribute("orderTimeList", OrdSmsTemplate.ORDER_TIME.values());
		model.addAttribute("sendNodeList", OrdSmsTemplate.getShowSendNodes());
		model.addAttribute("fieldList", OrdSmsTemplate.FIELD.values());
		model.addAttribute("ruleId", templateId);
		return "/order/sms/showAddSmsTemplate";
	}
	

	/**
	 * 跳转到修改短信模板页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/showUpdateOrdSmsTemplate")
	public String showUpdateOrdSmsTemplate(Model model, Long templateId) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateOrdSmsTemplate>");
		}
		Map<String, Object> distributorParam = new HashMap<String, Object>();
		distributorParam.put("cancelFlag", "Y");
		List<Distributor> distributorlList = distributorClientService.findDistributorList(distributorParam).getReturnContent();
		List<BizCategory> bizCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		if (bizCategoryList.size() > 0) {
			model.addAttribute("bizCategoryList", bizCategoryList);
		}
		if (distributorlList.size() > 0) {
			model.addAttribute("distributorlList", distributorlList);
		}
		OrdSmsTemplate ordSmsTemplate = new OrdSmsTemplate();
		ordSmsTemplate = smsTemplateService.findOrdSmsTemplateById(templateId);
		if (null != ordSmsTemplate && null != ordSmsTemplate.getSuplierId()) {
			String supplierName = getSupplierNameBySupplierId(ordSmsTemplate.getSuplierId());
			model.addAttribute("supplierName", supplierName);
		}
		model.addAttribute("ordSmsTemplate",ordSmsTemplate );
		model.addAttribute("orderTimeList", OrdSmsTemplate.ORDER_TIME.values());
		model.addAttribute("sendNodeList", OrdSmsTemplate.getShowSendNodes());
		model.addAttribute("fieldList", OrdSmsTemplate.FIELD.values());
		return "/order/sms/showUpdateSmsTemplate";
	}

	
	/**
	 * 创建短信模板
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value = "/addOrdSmsTemplate")
	@ResponseBody
	public Object addOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate , HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<addOrdSmsTemplate>");
		}
		String rule = req.getParameter("rule");
		if (ordSmsTemplate!=null && "Y".equals(rule)) {
			ordSmsTemplate.setCreataTime(new Date());
			ordSmsTemplate.setValid("N");
			smsTemplateService.addOrdSmsTemplate(ordSmsTemplate);	
		}else if (ordSmsTemplate!=null && "N".equals(rule)) {
			OrdSmsNotSendRule ordSmsNotSendRule = new OrdSmsNotSendRule();
			ordSmsNotSendRule.setRuleName(ordSmsTemplate.getTemplateName());
			ordSmsNotSendRule.setCategoryId(ordSmsTemplate.getCategoryId());
			ordSmsNotSendRule.setDistributorId(ordSmsTemplate.getDistributorId());
			
			ordSmsNotSendRule.setSendNode(ordSmsTemplate.getSendNode());
			ordSmsNotSendRule.setSuplierId(ordSmsTemplate.getSuplierId());
			ordSmsNotSendRule.setCreataTime(new Date());
			orderSmsNotSendRuleService.addOrdSmsNotSendRule(ordSmsNotSendRule);
		}
		return ResultMessage.ADD_SUCCESS_RESULT;
	}
	
	/**
	 * 更新短信模板
	 */
	@RequestMapping(value = "/updateOrdSmsTemplate")
	@ResponseBody
	public Object updateOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateOrdSmsTemplate>");
		}
		smsTemplateService.updateOrdSmsTemplate(ordSmsTemplate);
		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}
	
	/**
	 * 更新短信模板
	 */
	@RequestMapping(value = "/updateOrdSmsNotSendRule")
	@ResponseBody
	public Object updateOrdSmsNotSendRule(OrdSmsNotSendRule ordSmsNotSendRule) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateOrdSmsTemplate>");
		}
		orderSmsNotSendRuleService.updateOrdSmsNotSendRule(ordSmsNotSendRule);
		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}

	/**
	 * 禁用/启用
	 * 
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/editFlag")
	@ResponseBody
	public Object editFlag(Long templateId, String valid) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<editFlag>");
		}
		smsTemplateService.editFlag(templateId, valid);
		return ResultMessage.SET_SUCCESS_RESULT;
	}
	
	/**
	 * 删除
	 * 
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/deleteOrdSmsTemplate")
	@ResponseBody
	public Object deleteOrdSmsTemplate(Long templateId , HttpServletRequest request ) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<deleteOrdSmsTemplate>");
		}
		String rule = request.getParameter("rule");
		if ("Y".equals(rule)) {
			smsTemplateService.deleteOrdSmsTemplate(templateId);
		}else if ("N".equals(rule)) {
			orderSmsNotSendRuleService.deleteOrdSmsNotSendRule(templateId);
		}
		
		return ResultMessage.DELETE_SUCCESS_RESULT;
	}
	
	private String getSupplierNameBySupplierId(Long supplierId) {
		String supplierName = null;
		if (supplierId != null) {
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(supplierId);
			if (resultHandleSuppSupplier.isSuccess()) {
				if (resultHandleSuppSupplier.getReturnContent() != null) {
					supplierName = resultHandleSuppSupplier.getReturnContent().getSupplierName();
				} else {
					LOG.info("method getSupplierNameBySupplierId:Supplier(ID=" + supplierId + ") == null");
				}
			} else {
				LOG.info("method getSupplierNameBySupplierId:resultHandleSuppSupplier.isFail,msg=" + resultHandleSuppSupplier.getMsg());
			}
		}
		return supplierName;
	}
	
}
