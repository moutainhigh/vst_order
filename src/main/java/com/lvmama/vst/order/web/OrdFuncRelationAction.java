package com.lvmama.vst.order.web;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.order.po.OrdFuncRelation;
import com.lvmama.vst.back.order.po.OrdStatusGroup;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.OrdFuncRelationVO;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdFuncRelationService;
import com.lvmama.vst.order.service.IOrdStatusGroupService;

/**
 * 订单功能关系表维护action
 * 
 * @author jszhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/ordFuncRelation")
public class OrdFuncRelationAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrdFuncRelationAction.class);

	@Autowired
	private IOrdStatusGroupService ordStatusGroupService;

	@Autowired
	private IOrdFuncRelationService ordFuncRelationService;

	@RequestMapping(value = "/showAddOrdFuncRelation")
	public String showAddOrdFuncRelation(Model model, HttpServletRequest req)
			throws BusinessException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddOrdFuncRelation>");
		}

		return "/order/funcRelation/addOrdFuncRelation";
	}

	@RequestMapping(value = "/addOrdFuncRelation")
	@ResponseBody
	public Object addOrdFuncRelation(OrdFuncRelation ordFuncRelation) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<addOrdFuncRelation>");
		}

		ordFuncRelationService.insertOrdFuncRelation(ordFuncRelation);
		// suppSupplierService.addSuppSupplier(suppSupplier);
		return ResultMessage.ADD_SUCCESS_RESULT;
	}

	@RequestMapping(value = "/showUpdateOrdFuncRelation")
	public String showUpdateOrdFuncRelation(@RequestParam("ordFunctionId") String ordFunctionId, @RequestParam("functionName") String functionName, Model model) throws BusinessException,
			IllegalAccessException, InvocationTargetException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateOrdFuncRelation>");
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ordFunctionId", ordFunctionId);
		parameters.put("_orderby", "CATEGORY_ID desc");
		List<OrdFuncRelation> ordFuncRelationList = ordFuncRelationService.findOrdFuncRelationList(parameters);
		List<OrdFuncRelationVO> ordFuncRelationPOList = new ArrayList<OrdFuncRelationVO>();
		for (OrdFuncRelation ordFuncRelation : ordFuncRelationList) {

			OrdFuncRelationVO ordFuncRelationPO = new OrdFuncRelationVO();
			BeanUtils.copyProperties(ordFuncRelationPO, ordFuncRelation);
			OrdStatusGroup ordStatusGroup = ordStatusGroupService.findOrdStatusGroupById(ordFuncRelation.getStatusGroupId());

			ordFuncRelationPO.setFileds(ordStatusGroup.getFileds());

			ordFuncRelationPOList.add(ordFuncRelationPO);

		}
		model.addAttribute("ordFuncRelationPOList", ordFuncRelationPOList);
		model.addAttribute("ordFunctionId", ordFunctionId);
		try {
			model.addAttribute("functionName", new String(functionName.getBytes("ISO-8859-1"),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}

		return "/order/funcRelation/updateOrdFuncRelation";
	}

	/**
	 * 
	 * @param supplier
	 * @return
	 */
	@RequestMapping(value = "/updateOrdFuncRelationList")
	@ResponseBody
	public Object updateOrdFuncRelationList(OrdFuncRelationVO ordFuncRelationPO) throws BusinessException {
		if (log.isDebugEnabled()) {
			log.debug("start method<addOrdFuncRelationList>");
		}
		List<OrdFuncRelation> ordFuncRelationList = ordFuncRelationPO.getOrdFuncRelationList();

		ordFuncRelationPO.getOrdStatusGroupList();

		/*
		 * for (OrdStatusGroup ordStatusGroup : ordStatusGroupList) {
		 * ordStatusGroupService.updateOrdStatusGroup(ordStatusGroup); }
		 */
		for (OrdFuncRelation ordFuncRelation : ordFuncRelationList) {
			ordFuncRelationService.updateOrdFuncRelation(ordFuncRelation);
		}
		// ordFuncRelationService.addOrdFuncRelation(ordFuncRelation);
		// suppSupplierService.addSuppSupplier(suppSupplier);
		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}

}
