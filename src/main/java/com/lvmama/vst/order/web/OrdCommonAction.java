package com.lvmama.vst.order.web;

import com.lvmama.comm.pet.po.perm.PermOrganization;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdApplyInvoiceInfoService;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 人工分单action
 * 
 * @author zhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/ordCommon")
public class OrdCommonAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrdCommonAction.class);
	private static final String REMINDER_PAYMENT="cuizhifu";
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;

	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;

	@Autowired
	private PermOrganizationServiceAdapter permOrganizationService;

	@Autowired
	private DictClientService dictClientService;

	@Autowired
	private IOrderUpdateService ordOrderUpdateService;

	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;
	

	@RequestMapping(value = "/findCascadDepartment")
	public void findCascadDepartment(HttpServletRequest req, HttpServletResponse response) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findCascadDepartment>");
		}
		String depId = req.getParameter("depId");

		String nowLevel = req.getParameter("nowLevel");
		String json = "";
		// Map<String,String> resultMap=new HashMap<String, String>();
		if ("three".equals(nowLevel)) {
			List<PermUser> permUserList = permUserServiceAdapater.findUserByDepId(depId);
			json = JSONArray.fromObject(permUserList).toString();

		} else {
			/*
			 * resultMap=permOrganizationServiceAdapter.getChildOrgList(new
			 * Long(depId)); json=JSONArray.fromObject(resultMap).toString();
			 */

			List<PermOrganization> orgList = permOrganizationServiceAdapter.getChildPermOrgList(new Long(depId));
			PermOrganization permOrganization = new PermOrganization();
			permOrganization.setOrgId(null);
			permOrganization.setDepartmentName("请选择");
			orgList.add(0, permOrganization);

			json = JSONArray.fromObject(orgList).toString();

		}
		this.sendAjaxResultByJson(json, response);
	}

	@RequestMapping(value = "/getSubTypeOfBookingAudit")
	@ResponseBody
	public Map<String, String> getSubTypeOfBookingAudit(HttpServletRequest req, HttpServletResponse response){
		Map<String, String> subTypeMap = new LinkedHashMap<String, String>();
		subTypeMap.put("", "全部");
		for (OrderEnum.AUDIT_SUB_TYPE audit_sub_type : OrderEnum.AUDIT_SUB_TYPE.values()) {
			subTypeMap.put(audit_sub_type.getCode(), audit_sub_type.getCnName());
		}
		return subTypeMap;
	}

	/**
	 * 查询数据字典值
	 * 
	 * @param req
	 * @param response
	 */
	@RequestMapping(value = "/findBizDictData")
	public void findBizDictData(HttpServletRequest req, HttpServletResponse response) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findBizDictData>");
		}
		String json = "";

		String dictDefId = req.getParameter("dictDefId").trim();
		String needSelect = req.getParameter("needSelect");

		List<BizDict> bizDictList = dictClientService.findDictListByDefId(Long.valueOf(dictDefId)).getReturnContent();

		if ("true".equals(needSelect)) {
			BizDict bizDict = new BizDict();
			bizDict.setDictName("请选择");

			bizDictList.add(0, bizDict);
		}

		json = JSONArray.fromObject(bizDictList).toString();

		this.sendAjaxResultByJson(json, response);

	}

	/**
	 * 订单详情页面跳转
	 * 
	 * @param model
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/showOrderDetails")
	public ModelAndView showOrderDetails(Model model, Long orderId, Long objectId, HttpServletRequest req) {

		String objectType = req.getParameter("objectType");
		
		try{
			if(req.getParameter("isReminderPayment")!=null){
				String reminderPayment =new String(req.getParameter("isReminderPayment").getBytes("ISO-8859-1"),"utf-8");
				LOG.info("========orderId:"+objectId+"====isReminderPayment:"+reminderPayment);
				if(StringUtils.isNotEmpty(reminderPayment)){
					if(REMINDER_PAYMENT.equals(reminderPayment)){
						OrdOrder order=ordOrderUpdateService.queryOrdOrderByOrderId(objectId);
						if(order!=null && (order.hasPayed() || order.hasCanceled())){
							Map<String, Object> param=new HashMap<String, Object>();
							param.put("objectId", objectId);
							param.put("objectType", "ORDER");
							param.put("auditType", OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.name());
							List<ComAudit> comauditList=orderAuditService.queryAuditListByParam(param);
							if(!CollectionUtils.isEmpty(comauditList)){
								for (ComAudit comAudit : comauditList) {
									comAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.getCode());
									orderAuditService.updateByPrimaryKey(comAudit);
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			LOG.error("====orderId:"+objectId+"====exception:"+e);
		}

        LOG.info("objectType======" + objectType + ",objectId======" + ",orderId=====" + orderId);
        
        //lvcc座席软件传参
        //add by zjt
        String callId = StringUtils.isEmpty(req.getParameter("callid"))?"":req.getParameter("callid");
        LOG.info("callId======" + callId);
        
		if (StringUtils.isEmpty(objectType)) {// 订单监控 后台计调 跳转
			//modify by zjt
			return getUrl(orderId,callId);

		} else {// 我的工作台 跳转

			objectType = objectType.trim();

			if ("ORDER".equals(objectType)) {
				//modify by zjt
				return getUrl(objectId,callId);

			} else if ("ORDER_ITEM".equals(objectType)) {
				return getChildUrl(objectId);
			}

		}

		throw new BusinessException("未设置跳转地址");

	}

	/**
	 * 油轮 门票 子订单跳转url
	 * 
	 * @param orderId
	 * @return
	 */
	public ModelAndView getChildUrl(Long orderItemId) {

		/*
		 * OrdOrderItem
		 * ordOrderItem=ordOrderUpdateService.getOrderItem(orderItemId);
		 * List<OrdOrderItem> orderItemsList =
		 * ordOrderUpdateService.queryOrderItemByOrderId
		 * (ordOrderItem.getOrderId()); OrdOrder order=new OrdOrder();
		 * order.setOrderItemList(orderItemsList); OrdOrderItem
		 * mainOrdItem=order.getMainOrderItem();
		 */

		OrdOrderItem ordOrderItem = ordOrderUpdateService.getOrderItem(orderItemId);
		OrdOrder order = this.ordOrderUpdateService.queryOrdOrderByOrderId(ordOrderItem.getOrderId());
		// vst组织鉴权
		super.vstOrgAuthentication(LOG, order.getManagerIdPerm());

		// 酒店 主订单
		String hotelUrl = "redirect:/order/orderStatusManage/showOrderStatusManage.do?orderId=" + order.getOrderId();
		// 油轮 子订单
		String shipChildUrl = "redirect:/order/orderShipManage/showChildOrderStatusManage.do?orderItemId="
				+ orderItemId + "&orderType=child";
		// 门票 子订单
		String ticketChildUrl = "redirect:/order/orderManage/showChildOrderStatusManage.do?orderItemId=" + orderItemId
				+ "&orderType=child";

		boolean isHotelUrl = isHotelUrl(order);
		if (isHotelUrl) {
			return new ModelAndView(hotelUrl);
		}

		ResultHandleT<BizCategory> result = categoryClientService
				.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCode());
		BizCategory bizCategoryShip = result.getReturnContent();

		if (bizCategoryShip.getCategoryId().equals(order.getCategoryId())) {
			return new ModelAndView(shipChildUrl);
		}

		return new ModelAndView(ticketChildUrl);

	}

	/**
	 * 酒店 油轮 门票 主订单跳转url
	 * 
	 * @param orderId
	 * @return
	 */
	public ModelAndView getUrl(Long orderId, String callId) {
		/*
		 * List<OrdOrderItem> orderItemsList =
		 * ordOrderUpdateService.queryOrderItemByOrderId(orderId); OrdOrder
		 * order=new OrdOrder(); order.setOrderItemList(orderItemsList);
		 * OrdOrderItem mainOrdItem=order.getMainOrderItem();
		 */
		OrdOrder order = this.ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
		// vst组织鉴权
		LOG.info("order : " + ((order == null) ? "order is null" : "order is not null, Perm:" + order.getManagerIdPerm()) + ", orderID:" + orderId);
		super.vstOrgAuthentication(LOG, order.getManagerIdPerm());

		// 酒店 主订单
		String hotelUrl = "redirect:/order/orderStatusManage/showOrderStatusManage.do?orderId=" + orderId+"&callid="+callId;

		// 油轮 主订单
		String shipUrl = "redirect:/order/orderShipManage/showOrderStatusManage.do?orderType=parent&orderId=" + orderId+"&callid="+callId;

		// 门票 和线路主订单
		String ticketUrl = "redirect:/order/orderManage/showOrderStatusManage.do?orderType=parent&orderId=" + orderId+"&callid="+callId;

		// ResultHandleT<List<BizCategory>>
		// result=categoryClientService.findCategoryByAllValid();
		ResultHandleT<BizCategory> result = categoryClientService
				.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode());
		BizCategory bizCategoryHotel = result.getReturnContent();

		if (bizCategoryHotel.getCategoryId().equals(order.getCategoryId())) {
			return new ModelAndView(hotelUrl);
		}

		boolean isHotelUrl = isHotelUrl(order);
		if (isHotelUrl) {
			return new ModelAndView(hotelUrl);
		}

		result = categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCode());
		BizCategory bizCategoryShip = result.getReturnContent();

		if (bizCategoryShip != null && bizCategoryShip.getCategoryId().equals(order.getCategoryId())) {
			return new ModelAndView(shipUrl);
		}

		result = categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCode());
		BizCategory bizCategorySingleTicket = result.getReturnContent();
		result = categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCode());
		BizCategory bizCategoryOtherTicket = result.getReturnContent();
		result = categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCode());
		BizCategory bizCategoryComTicket = result.getReturnContent();
		if (bizCategorySingleTicket.getCategoryId().equals(order.getCategoryId())
				|| bizCategoryOtherTicket.getCategoryId().equals(order.getCategoryId())
				|| bizCategoryComTicket.getCategoryId().equals(order.getCategoryId())) {

			return new ModelAndView(ticketUrl);
		}

		return new ModelAndView(ticketUrl);

		/*
		 * if (orderItemsList.size()>1) {//油轮 主订单 return new
		 * ModelAndView(shipUrl); }else if(orderItemsList.size()==1){
		 * OrdOrderItem ordItem=orderItemsList.get(0);
		 * 
		 * ResultHandleT<BizCategory>
		 * result=categoryClientService.findCategoryByCode
		 * (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode()); BizCategory
		 * bizCategoryShip=result.getReturnContent(); if
		 * (bizCategoryShip.getCategoryId().equals(ordItem.getCategoryId())) {
		 * return new ModelAndView(shipUrl); }else{ // 酒店 return new
		 * ModelAndView(hotelUrl); } }
		 */

	}

	private boolean isHotelUrl(OrdOrder order) {
		// 三月份的订单key为null，都是酒店订单，走酒店品类的流转页面
		String processKey = order.getProcessKey();
		if (StringUtils.isEmpty(processKey)) {
			return true;
		}

		return BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId());
	}

	/**
	 * 子订单或者子订单聚合详情页面跳转
	 * 
	 * @param model
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/showChildOrderDetails")
	public ModelAndView showChildOrderDetails(Model model, Long orderId, Long orderItemId, HttpServletRequest req) {

		OrdOrderItem orderItem = ordOrderUpdateService.getOrderItem(orderItemId);

		/*
		 * ResultHandleT<BizCategory>
		 * result=categoryClientService.findCategoryByCode
		 * (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode()); BizCategory
		 * bizCategoryShip=result.getReturnContent(); if
		 * (bizCategoryShip.getCategoryId().equals(ordItem.getCategoryId())) {
		 * return new ModelAndView(url); }
		 */

		// 线路-供应商打包，同主订单号、同产品、同供应商、同下单时间，做聚合
		Map<String, Object> params = new HashMap<String, Object>();
		// params.put("categoryId",);
		params.put("orderId", orderId);// 订单号
		params.put("productId", orderItem.getProductId());
		params.put("supplierId", orderItem.getSupplierId());

		int n = ordOrderUpdateService.getOrderItemTotalCount(params);

		if (n > 1) {// 子订单聚合详情 跳转

			return new ModelAndView("redirect:/order/orderManage/showChildMergeOrderStatusManage.do?orderItemId="
					+ orderItemId + "&orderType=childMerge");

		} else {// 子订单详情 跳转

			return new ModelAndView("redirect:/order/orderManage/showChildOrderStatusManage.do?orderItemId="
					+ orderItemId + "&orderType=child");

		}

		// throw new BusinessException("未设置跳转地址");

	}

	
	//根据订单id更新发票信息表申请状态
   	@RequestMapping("/updateInvoiceApplyStatus")
   	@ResponseBody
   	public ResultHandle updateInvoiceApplyStatus(Long id) {
   		ResultHandle resultHandle = new ResultHandle();
   		resultHandle.setMsg("更新发票状态信息成功");
   	    try {
   	   		int updatecount = ordApplyInvoiceInfoService.updateApplyInfoStatus(id); 
   	   		if(updatecount == 0){
   	   		   resultHandle.setMsg("更新发票状态信息失败");
   	   		}
   	 	   LOG.info("---------updateInvoiceApplyStatus-----------------"+updatecount);
   	    }catch (Exception e) {	
   		  LOG.info(e.getMessage());
   		  resultHandle.setMsg("更改发票状态信息异常");
   		 LOG.info("------------updateInvoiceApplyStatus----Faill--------------");
   		}                                             
   		return resultHandle;
   	}
	
}
