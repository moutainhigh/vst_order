package com.lvmama.vst.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdFunctionService;

/**
 * 功能表action
 * 
 * @author jszhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/ordFunction")
public class OrdFunctionAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrdFunctionAction.class);

	@Autowired
	private IOrdFunctionService ordFunctionservice;

	@RequestMapping(value = "/findOrdFunctionList")
	public String findOrdFunctionList(Model model, Integer page, OrdFunction ordFunction, HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrdFunctionList>");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		String functionName = ordFunction.getFunctionName();
		String functionCode = ordFunction.getFunctionCode();
		if (!StringUtils.isEmpty(functionName)) {
			parameters.put("functionName", functionName);
		}
		if (!StringUtils.isEmpty(functionCode)) {
			parameters.put("functionCode", functionCode);
		}

		int count = ordFunctionservice.findOrdFunctionCount(parameters);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		// parameters.put("_orderby","supp.SUPPLIER_ID desc");

		List<OrdFunction> ordFunctionList = ordFunctionservice.findOrdFunctionList(parameters);
		pageParam.setItems(ordFunctionList);

		model.addAttribute("pageParam", pageParam);
		model.addAttribute("ordFunction", ordFunction);

		// model.addAttribute("ordFunctionList", ordFunctionList);

		return "/order/funcRelation/findOrdFunctionList";

	}
	
	
	@RequestMapping(value = "/findSelectOrdFunctionList")
	public String findSelectOrdFunctionList(Model model, Integer page, OrdFunction ordFunction, HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findSelectOrdFunctionList>");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		String functionName = ordFunction.getFunctionName();
		String functionCode = ordFunction.getFunctionCode();
		if (!StringUtils.isEmpty(functionName)) {
			parameters.put("functionName", functionName);
		}
		if (!StringUtils.isEmpty(functionCode)) {
			parameters.put("functionCode", functionCode);
		}

		int count = ordFunctionservice.findOrdFunctionCount(parameters);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		// parameters.put("_orderby","supp.SUPPLIER_ID desc");

		List<OrdFunction> ordFunctionList = ordFunctionservice.findOrdFunctionList(parameters);
		pageParam.setItems(ordFunctionList);

		model.addAttribute("pageParam", pageParam);
		model.addAttribute("ordFunction", ordFunction);

		// model.addAttribute("ordFunctionList", ordFunctionList);

		return "/order/funcRelation/findSelectOrdFunctionList";


	}

}
