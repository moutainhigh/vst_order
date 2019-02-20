package com.lvmama.vst.order.web;

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

import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.back.order.po.OrdStatusGroup;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdStatusGroupService;

/**
 * 功能表action
 * 
 * @author jszhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/ordStatusGroup")
public class OrdStatusGroupAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrdStatusGroupAction.class);

	@Autowired
	private IOrdStatusGroupService ordStatusGroupService;

	@RequestMapping(value = "/findOrdStatusGroupList")
	public String findOrdStatusGroupList(Model model, Integer page, OrdFunction ordFunction, HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrdStatusGroupList>");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		int count = ordStatusGroupService.findOrdStatusGroupCount(parameters);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		parameters.put("_orderby", " STATUS_GROUP_ID desc");

		List<OrdStatusGroup> ordStatusGroupList = ordStatusGroupService.findOrdStatusGroupList(parameters);

		model.addAttribute("ordStatusGroupList", ordStatusGroupList);

		model.addAttribute("queryOper", "false");
		model.addAttribute("pageParam", pageParam);

		return "/order/funcRelation/findOrdStatusGroupList";

	}

	@RequestMapping(value = "/queyOrdStatusGroupList")
	public String queyOrdStatusGroupList(Model model, Integer page, OrdStatusGroup ordStatusGroup, HttpServletRequest req) throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<queyOrdStatusGroupList>");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("fileds", ordStatusGroup.getFileds());

		int count = ordStatusGroupService.findOrdStatusGroupCount(parameters);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		parameters.put("_orderby", " STATUS_GROUP_ID desc");

		List<OrdStatusGroup> ordStatusGroupList = ordStatusGroupService.findOrdStatusGroupList(parameters);

		model.addAttribute("ordStatusGroupList", ordStatusGroupList);
		model.addAttribute("queryOper", "true");

		model.addAttribute("pageParam", pageParam);

		return "/order/funcRelation/queyOrdStatusGroupList";

	}

	@RequestMapping(value = "/showAddordStatusGroup")
	public String showAddordStatusGroup(Model model, HttpServletRequest req) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddordStatusGroup>");
		}

		model.addAttribute("updateOp", "false");
		return "/order/funcRelation/addOrdStatusGroup";
	}

	@RequestMapping(value = "/showUpdateOrdStatusGroup")
	public String showUpdateOrdStatusGroup(OrdStatusGroup ordStatusGroup, Model model, HttpServletRequest req) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateOrdStatusGroup>");
		}
		model.addAttribute("statusGroupId", req.getParameter("statusGroupId"));
		model.addAttribute("fileds", req.getParameter("fileds"));

		model.addAttribute("updateOp", "true");
		return "/order/funcRelation/addOrdStatusGroup";
	}

	@RequestMapping(value = "/addOrdStatusGroup")
	@ResponseBody
	public Object operateOrdStatusGroup(OrdStatusGroup ordStatusGroup, HttpServletRequest req) {
		if (log.isDebugEnabled()) {
			log.debug("start method<addOrdStatusGroup>");
		}
		String updateOp = req.getParameter("updateOp");
		if ("true".equals(updateOp)) {
			ordStatusGroupService.updateOrdStatusGroup(ordStatusGroup);
		}
		if ("false".equals(updateOp)) {
			ordStatusGroupService.insertOrdStatusGroup(ordStatusGroup);
		}

		return ResultMessage.ADD_SUCCESS_RESULT;
	}

}
