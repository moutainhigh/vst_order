package com.lvmama.vst.order.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.RecallOrderWorkflowParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdWorkflowCompensatedService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * 订单工作流补偿
 * @author liuxiaoyong
 *
 */
@Controller
@RequestMapping("/order/activitiTask")
public class OrderActivitiTaskAction extends BaseActionSupport{
	
	private static final String ORDER_ACTIVITI_TASK = "/order/activitiTask/orderActivitiTask";
	private final String ERROR_PAGE="/order/error";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(OrderActivitiTaskAction.class);
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	@Autowired
	private ProcesserClientService processerClientService;
	@Autowired
	private IOrderUpdateService ordOrderService;
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	@Autowired
	private IOrdWorkflowCompensatedService ordWorkflowCompensatedService;
	
	@RequestMapping(value = "/orderActivitiTask")
	public String showOrderActiviti(Model model, RecallOrderWorkflowParam param,HttpServletRequest request){
		if(!checkUrlValid(request.getParameter("code"))){
			model.addAttribute("ERROR","非法链接");
			return ERROR_PAGE;
		}
		String[] authUsers = new String[]{"lv6800","admin"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			model.addAttribute("ERROR","权限不足");
			return ERROR_PAGE;
		}
		// 初始化查询表单,给字典项赋值
		initQueryForm(model, request);
		model.addAttribute("param", param);
		return ORDER_ACTIVITI_TASK;
	}
	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model,HttpServletRequest request) throws BusinessException {
		// 订单状态字典
		Map<String, String> auditTypeMap = new LinkedHashMap<String, String>();
		auditTypeMap.put("", "请选择");
		int i=0;
		for (AUDIT_TYPE item : AUDIT_TYPE.values()) {
			//只处理前四种类型
			if (i < 4) {
				auditTypeMap.put(item.getCode(), item.getCnName());
			}
			i++;
		}
		model.addAttribute("auditTypeMap", auditTypeMap);
	}

	@RequestMapping(value = "/resetOrderWorkflowStatus")
	public String resetOrderWorkflowStatus(Model model, RecallOrderWorkflowParam params, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model,request);

		String result = "订单工作流状态已重置。";

		Long orderId = params.getOrderId();
		String auditType = params.getAuditType();
		if (orderId != null) {
			ResultHandleT<ComAudit> auditHandleT = getOrderComAudit(orderId, auditType);
			if (auditHandleT.getMsg() != null) {
				result = auditHandleT.getMsg();
			} else {
    			OrdOrder order = this.complexQueryService.queryOrderByOrderId(orderId);
   				if (order == null) {
   					result = orderId + "Order对象不存在.";
   				} else {
   	    			if(!ActivitiUtils.hasActivitiOrder(order)){
   	    				result = orderId+"不是工作流订单!";
   	    			} else {
   	    				Map<String, Object> param = new HashMap<String, Object>();
       					param.put("auditId", auditHandleT.getReturnContent().getAuditId());
       					param.put("auditStatus", VstOrderEnum.AUDIT_STATUS.UNPROCESSED.name());
       					int i = this.orderAuditService.updateComAuditStatus(param);
       					LOG.info("main order update com_audit number:"+i);
       					int j = 0;
       					OrdOrder order_ = new OrdOrder();
       					order_.setOrderId(order.getOrderId());
       					if (VstOrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(auditType)) {
       						order_.setInfoStatus(VstOrderEnum.INFO_STATUS.UNVERIFIED.name());
	            			j = this.ordOrderService.updateByPrimaryKeySelective(order_);
       					} else if (VstOrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(auditType)) {
       						order_.setResourceStatus(VstOrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
	            			j = this.ordOrderService.updateByPrimaryKeySelective(order_);
       					} else if (VstOrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equals(auditType)) {
       						order_.setCertConfirmStatus(VstOrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
	            			j = this.ordOrderService.updateByPrimaryKeySelective(order_);
       					}
       					LOG.info("main order update ord_order number:"+j);
   	    			}
				}
			}
		} else {
	        Long orderItemId = params.getOrderItemId();
	        if (orderItemId != null) {
	        	ResultHandleT<ComAudit> auditHandleT = getOrderItemComAudit(orderItemId, auditType);
	        	if (auditHandleT.getMsg() != null) {
	        		result = auditHandleT.getMsg();
	        	} else {
	        		OrdOrderItem ordOrderItem = this.ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
	        		if(ordOrderItem == null){
	        			result = orderItemId+"没有找到对应的子订单!";
	        		} else {
	        			OrdOrder order=this.complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
	        			if(order == null){
	        				result = orderItemId+"没有找到对应的主订单!";
	        			} else if(!ActivitiUtils.hasActivitiOrder(order)){
	        				result = orderItemId+"不是工作流订单!";
	        			} else {
	        				Map<String, Object> param = new HashMap<String, Object>();
	       					param.put("auditId", auditHandleT.getReturnContent().getAuditId());
	       					param.put("auditStatus", VstOrderEnum.AUDIT_STATUS.UNPROCESSED.name());
	       					int i = this.orderAuditService.updateComAuditStatus(param);
	       					LOG.info("orderitem update com_audit number:"+i);
	            			OrdOrderItem en = new OrdOrderItem();
	            			en.setOrderItemId(ordOrderItem.getOrderItemId());
	            			int j = 0;
	       					if (VstOrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(auditType)) {
		            			en.setInfoStatus(VstOrderEnum.INFO_STATUS.UNVERIFIED.name());
		            			j = this.ordOrderService.updateOrderItemByIdSelective(en);
	       					} else if (VstOrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(auditType)) {
		            			en.setResourceStatus(VstOrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
		            			j = this.ordOrderService.updateOrderItemByIdSelective(en);
	       					} else if (VstOrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name().equals(auditType)) {
		            			en.setCertConfirmStatus(VstOrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
		            			j = this.ordOrderService.updateOrderItemByIdSelective(en);
	       					}
	       					LOG.info("orderitem update ord_order_item number:"+j);
	        			}
	        		}
	        	}
	        }
        }
		
		// 存储结果
		model.addAttribute("result", result);

		// 查询条件回显
		model.addAttribute("param", params);		

		return ORDER_ACTIVITI_TASK;
	}
	
	@RequestMapping(value = "/finishWorkflowTask")
	public String finishWorkflowTask(Model model, RecallOrderWorkflowParam params, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model,request);

		String result = "订单工作流已唤醒。";

		if (params.getOrderId() != null) {
			result = finishOrderTask(params.getOrderId(), params.getAuditType());
		} else if (params.getOrderItemId() != null) {
			result = finishOrderItemTask(params.getOrderItemId(), params.getAuditType());
		}
		
		// 存储结果
		model.addAttribute("result", result);

		// 查询条件回显
		model.addAttribute("param", params);
		

		return ORDER_ACTIVITI_TASK;
	}
	
	private String finishOrderTask(Long orderId, String auditType) {
		ResultHandleT<ComAudit> auditHandleT = getOrderComAudit(orderId, auditType);
		if (auditHandleT.getMsg() != null) {
			return auditHandleT.getMsg();
		} else {
    	    ComAudit audit = auditHandleT.getReturnContent();
    		ResultMessage msg = ResultMessage.createResultMessage();
    		try{
    			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
    			if(null!=order){
    				if(ActivitiUtils.hasActivitiOrder(order)){
    					OrdOrderItem mainOrderItem = order.getMainOrderItem();
    					ActivitiKey activitiKey = null;
    					if(OrdOrderUtils.isDestBuFrontOrder(order) 
								&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId() 
								&& mainOrderItem != null && mainOrderItem.hasSupplierApi()) {
							activitiKey = createKeyOfHotelDockByOrder(order, ActivitiUtils.getOrderType(audit));
						} else {
							activitiKey = createKeyByOrder(order);
						}
						this.processerClientService.completeTaskByAudit(activitiKey,audit);
						msg.raise(orderId+"成功执行.");
    				}else{
    					msg.raise(orderId+"不是工作流订单.");
    				}
    			}else{
    				msg.raise(orderId+"Order对象不存在.");
    			}
    		}catch (Exception e){
    			// TODO: handle exception
    			msg.raise("发生异常."+e);
    		}
    		return msg.toString();
		}
    }

	private ResultHandleT<ComAudit> getOrderComAudit(Long orderId, String auditType) {
		ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
	    if(orderId==null||auditType==null){
			result.setMsg("参数为空.");
		}
		
		ComAudit audit = null;
		
		if (("PRETRIAL_AUDIT".equalsIgnoreCase(auditType) || "INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", orderId);
			param.put("auditType", auditType);
			param.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			List<ComAudit> audits = this.orderAuditService.queryAuditListByParam(param);
			if (CollectionUtils.isNotEmpty(audits)) {
				audit = audits.get(0);
			}
			result.setReturnContent(audit);
		}
		if (audit == null) {
			result.setMsg(orderId+"查不到audit信息");
		}
	    return result;
    }
	
	private String finishOrderItemTask(Long orderItemId, String auditType) {
		ResultHandleT<ComAudit> auditHandleT = getOrderItemComAudit(orderItemId, auditType);
		if (auditHandleT.getMsg() != null) {
			return auditHandleT.getMsg();
		} else {
    	    ComAudit audit = auditHandleT.getReturnContent();
    		
    		ResultMessage msg = ResultMessage.createResultMessage();
    		try{
    			OrdOrderItem ordOrderItem = this.ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
    			if(ordOrderItem == null){
    				msg.raise(orderItemId+"没有找到对应的子订单!");
    				return msg.toString();
    			}
    			OrdOrder order=this.complexQueryService.queryOrderByOrderId(ordOrderItem.getOrderId());
    			if(order == null){
    				msg.raise(orderItemId+"没有找到对应的主订单!");
    				return msg.toString();
    			}
    			if(!ActivitiUtils.hasActivitiOrder(order)){
    				msg.raise(orderItemId+"不是工作流订单!");
    				return msg.toString();
    			}
                 ActivitiKey activitiKey = null;
                 if(OrdOrderUtils.isDestBuFrontOrder(order) 
              			&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId() 
              			&& ordOrderItem != null && ordOrderItem.hasSupplierApi()) {
	              	 activitiKey = createKeyOfHotelDockByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(audit));
	               } else {
	              	 activitiKey = createKeyByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(audit));
	               }
	  			this.processerClientService.completeTaskByAudit(activitiKey,audit);
	  			msg.raise(orderItemId+"成功执行.");
    		}catch(Exception e){
    			msg.raise("处理异常:"+e);
    		}
    		return msg.toString();
		}
    }
	
	private ResultHandleT<ComAudit> getOrderItemComAudit(Long orderItemId, String auditType) {
		ResultHandleT<ComAudit> result = new ResultHandleT<ComAudit>();
	    if (!("INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
	    	result.setMsg("audit type非法");
		}
		if(orderItemId == null || auditType == null){
			result.setMsg("orderItemId 和 auditId 为必要参数!");
		}
		
		ComAudit audit = null;
		
		if (("PRETRIAL_AUDIT".equalsIgnoreCase(auditType) || "INFO_AUDIT".equalsIgnoreCase(auditType)
				|| "RESOURCE_AUDIT".equalsIgnoreCase(auditType) || "CERTIFICATE_AUDIT".equalsIgnoreCase(auditType))){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", orderItemId);
			param.put("auditType", auditType);
			param.put("objectType", VstOrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			List<ComAudit> audits = this.orderAuditService.queryAuditListByParam(param);
			if (CollectionUtils.isNotEmpty(audits)) {
				audit = audits.get(0);
			}
			result.setReturnContent(audit);
		}
		if (audit == null) {
			result.setMsg("查不到audit信息");
		}
	    return result;
    }
	
	private ActivitiKey createKeyByOrder(OrdOrder order){
		ComActivitiRelation relation = getRelation(order);
		return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
	}
	/**
	 * 单酒店对接流程key
	 * @param order
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrder(OrdOrder order, String type){
		return new ActivitiKey((String)null, ActivitiUtils.createOrderHotelDockBussinessKey(order, type));
	}
	private ComActivitiRelation getRelation(OrdOrder order){
		try{
			ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
			if(comActiveRelation == null){//补偿机制,通过工作流再次去触发查询
				String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
				if(processId != null){
					comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					comActiveRelation = new ComActivitiRelation();
					comActiveRelation.setObjectId(order.getOrderId());
					comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
					comActiveRelation.setProcessId(processId);
					comActiveRelation.setProcessKey(order.getProcessKey());
				}
			}
			return comActiveRelation;
		}catch(Exception e){
		}
		return null;
	}
	/**
	 * 打包酒店对接流程key
	 * @param item
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrderItem(OrdOrderItem item, String type){
		return new ActivitiKey((String)null, ActivitiUtils.createItemHotelDockBussinessKey(item, type));
	}
	private ActivitiKey createKeyByOrderItem(OrdOrderItem item,String type){
		LOG.info("createKeyByOrderItem item.orderid="+item.getOrderId()+",type="+type);
		return new ActivitiKey((String)null, ActivitiUtils.createOrderBussinessKey(item, type));
	}

	@RequestMapping("/compensatedPayProcess/{orderId}")
	@ResponseBody
	public Object compensatedPayProcess(@PathVariable("orderId")Long orderId,ModelMap map,HttpServletRequest request){
		if(!checkUrlValid(request.getParameter("code"))){
			return "连接非法";
		}
		String[] authUsers = new String[]{"lv6800","admin","lv18200"};
		if(!Arrays.asList(authUsers).contains(getLoginUserId())) {
			LOG.info("权限不足");
			return "权限不足";
		}
		ResultMessage result = ResultMessage.createResultMessage();
		if(orderId == null || orderId.longValue() <= 0){
			result.addObject("order", "orderId不能为空");
		}else{
			boolean isSuccess = ordWorkflowCompensatedService.compensatedOrdPayWorkflow(orderId, false);
			if(!isSuccess){
				result.addObject("success", "补偿失败");
			}
		}
		
		return result;
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
