/**
 * 
 */
package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermOrganization;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PAYMENT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrdAuditConfigInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.vo.OrdResponsibleVo;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 订单负责人分单
 * @author lancey
 *
 */
@Controller
public class OrdResponsibleAction extends BaseActionSupport{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4337589080436638684L;
	
	
	private static final Log LOG = LogFactory.getLog(OrdResponsibleAction.class);
	
	private final String RESPONSIBLE_PAGE="/order/ordManualDistOrder/manualResponsible";
	
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10; 
	
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private CategoryClientService  categoryClientService;
	
	/**
	 * 员工业务
	 */
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;
	
	@RequestMapping("/order/ordManualDistOrder/responsible.do")
	public String index(Model model,HttpServletRequest request) throws BusinessException{
		
		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		
		Map<String,String> initMap=new HashMap<String, String>();
		initMap.put("", "请选择");
		
		/*Map<String,String> groupMemberMap=new HashMap<String, String>();
		groupMemberMap.put("", "请选择");*/
		
		
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", initMap);
		//model.addAttribute("groupMemberMap", groupMemberMap);
		
		//select默认选中值
		OrdAuditConfigInfo ordAuditConfigInfo=new OrdAuditConfigInfo();
		ordAuditConfigInfo.setFirstDepartment(Constants.ORG_ID_CTI+"");//默认呼叫中心 55 orgid
		
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		
		initQueryForm(model);
		
		OrderMonitorCnd monitorCnd=new OrderMonitorCnd();
		model.addAttribute("monitorCnd", monitorCnd);
		return RESPONSIBLE_PAGE;
	}
	
	/**
	 * 
	 * @param ordAuditConfigInfo
	 * @param monitorCnd
	 * @param page
	 * @param pageSize
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/order/ordManualDistOrder/query.do")
	public String query(OrdAuditConfigInfo ordAuditConfigInfo,OrderMonitorCnd monitorCnd,Integer page,Integer pageSize,Model model, HttpServletRequest request) throws BusinessException{
		
		 
		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
//		String threeDepartment=ordAuditConfigInfo.getThreeDepartment();
		
		Map<String, Object> params=initParam(ordAuditConfigInfo, monitorCnd);
		
		
		
		Integer auditTotalCount=orderResponsibleService.selectResponsibleCount(params);
		
		
		
		
		int currentPage = page == null ? 1 : page;
		int currentPageSize = pageSize == null?DEFAULT_PAGE_SIZE:pageSize;
		
		Page<OrdResponsibleVo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
		pageParam.buildUrl(request);
		
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		
		List<Map> list=orderResponsibleService.queryResponsibleListByCondition(params);
		
		
		Set<Long> orderIds=null;
		
		Set<Long> orderItemIds=null;
		
		if("ORDER_ITEM".equals(monitorCnd.getOrderType())){
			orderItemIds=new TreeSet<Long>();
		}else{
			orderIds=new TreeSet<Long>();
		}
		for (Map map : list) {
			Long orderId=0L;
			Long orderItemId=0L;
			if("ORDER_ITEM".equals(monitorCnd.getOrderType())){
				orderItemId=Long.valueOf(map.get("ORDER_ITEM_ID").toString());
				orderItemIds.add(orderItemId);
			}else{
				orderId=Long.valueOf(map.get("ORDER_ID").toString());
				orderIds.add(orderId);
			}
		}
		//将订单转化为map,方便数据整合
		Map<Long, OrdResponsibleVo> orderResultMap = null;
		
		//将订单负责人转化为map,方便数据整合
		Map<Long, OrdResponsible> responsibleListMap = null;
		
		if(null!=auditTotalCount&&auditTotalCount.intValue()>0){
		
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition condition = buildQueryConditionForResponsible(page, pageSize, orderIds,orderItemIds);
			// 根据条件获取订单集合
			List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
			
			if(log.isDebugEnabled()){
				if(null != orderList && orderList.size()>0){
					log.debug("orderList=="+ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE));
				}
			}
			
			// 根据页面展示特色组装其想要的结果
			List<OrdResponsibleVo> orderResultList = buildQueryResult(monitorCnd.getOrderType(),orderList,orderItemIds);
			
			orderResultMap=new HashMap<Long, OrdResponsibleVo>(orderResultList.size());
			
			for(OrdResponsibleVo ordResponsibleVo:orderResultList){
				if("ORDER_ITEM".equals(monitorCnd.getOrderType())){
					orderResultMap.put(ordResponsibleVo.getOrderItemId(), ordResponsibleVo);
				}else{
					orderResultMap.put(ordResponsibleVo.getOrderId(), ordResponsibleVo);
				}
			}
			Map<String, Object> ordResParams=new HashMap<String, Object>();
			if("ORDER_ITEM".equals(monitorCnd.getOrderType())){
				ordResParams.put("objectIds",orderItemIds);
			}else{
				ordResParams.put("objectIds", orderIds);
			}
			ordResParams.put("objectType", monitorCnd.getOrderType());
			List<OrdResponsible> responsibleList=orderResponsibleService.findOrdResponsibleList(ordResParams);
			
			responsibleListMap=new HashMap<Long, OrdResponsible>(responsibleList.size());
			for(OrdResponsible ordResponsible:responsibleList){
				responsibleListMap.put(ordResponsible.getObjectId(), ordResponsible);
			}
		}
		
		
		List<OrdResponsibleVo> resultList=new ArrayList<OrdResponsibleVo>();
		for (Map map : list) {
			OrdResponsibleVo item=new OrdResponsibleVo();
			Long orderId=0L;
			Long orderItemId=0L;
			OrdResponsible ordResponsible=new OrdResponsible();
			if("ORDER_ITEM".equals(monitorCnd.getOrderType())){
				orderItemId=Long.valueOf(map.get("ORDER_ITEM_ID").toString());
				if(null!=orderResultMap){
					item= orderResultMap.get(orderItemId);
				}
				if(null!=responsibleListMap){
					ordResponsible=responsibleListMap.get(orderItemId);
				}
				if(null!=item){
					item.setObjectId(orderItemId);
				}
			}else{
				orderId=Long.valueOf(map.get("ORDER_ID").toString());
				if(null!=orderResultMap){
					item= orderResultMap.get(orderId);
				}
				if(null!=responsibleListMap){
					ordResponsible=responsibleListMap.get(orderId);
				}
				if(null!=item){
					item.setObjectId(orderId);
				}
			}
			if(null!=ordResponsible){
				item.setPrincipal(ordResponsible.getOperatorName());
				//得到组的部门层级
				List<Map<String, Object>> orgList=permOrganizationServiceAdapter.getOrganizationByChildId(ordResponsible.getOrgId());
				if(CollectionUtils.isNotEmpty(orgList)){
					for(Map<String, Object> orgMap : orgList){
						Long level = Long.valueOf(String.valueOf(orgMap.get("permLevel")));
						String name = String.valueOf(orgMap.get("departmentName"));
						if(null != level){
							if(level.intValue() == 3){
								item.setDepartment(name);
							}
						}
					}
				}
			}
			resultList.add(item);
		}
		pageParam.setItems(resultList);
		
		// 存储分页结果
		model.addAttribute("resultPage", pageParam);

		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();
		//Map<String,String> groupMemberMap=Collections.emptyMap();
		
		if (!StringUtils.isEmpty(firstDepartment)) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(firstDepartment));
		}
		if (!StringUtils.isEmpty(secondDepartment)) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(secondDepartment));
		}else{
			threeDepMap = new HashMap<String, String>();
			threeDepMap.put("", "请选择");
		}
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		
		initQueryForm(model);
		
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		model.addAttribute("ordAuditConfigInfo", ordAuditConfigInfo);
		return RESPONSIBLE_PAGE;
	}
	
	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model) throws BusinessException {
		
		// 订单状态字典
		Map<String, String> orderStatusMap = new LinkedHashMap<String, String>();
		orderStatusMap.put("", "全部");
		for (ORDER_STATUS item : ORDER_STATUS.values()) {
			orderStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("orderStatusMap", orderStatusMap);

		// 信息状态字典
		Map<String, String> infoStatusMap = new LinkedHashMap<String, String>();
		infoStatusMap.put("", "全部");
		for (INFO_STATUS item : INFO_STATUS.values()) {
			infoStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("infoStatusMap", infoStatusMap);

		// 资源状态字典
		Map<String, String> resourceStatusMap = new LinkedHashMap<String, String>();
		resourceStatusMap.put("", "全部");
		for (RESOURCE_STATUS item : RESOURCE_STATUS.values()) {
			resourceStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("resourceStatusMap", resourceStatusMap);

		// 支付状态字典	
		Map<String, String> paymentStatusMap = new LinkedHashMap<String, String>();
		paymentStatusMap.put("", "全部");
		for (PAYMENT_STATUS item : PAYMENT_STATUS.values()) {
			paymentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("paymentStatusMap", paymentStatusMap);
		
		//确认单凭证
		Map<String, String> certConfirmStatusMap = new LinkedHashMap<String, String>();
		certConfirmStatusMap.put("", "全部");
		for(OrderEnum.CERT_CONFIRM_STATUS certConfirmStatus:OrderEnum.CERT_CONFIRM_STATUS.values()){
			certConfirmStatusMap.put(certConfirmStatus.name(), certConfirmStatus.getCnName());
		}
		model.addAttribute("certConfirmStatusMap", certConfirmStatusMap);
		
		// 订单类型字典	
		Map<String, String> orderTypeMap = new LinkedHashMap<String, String>();
		orderTypeMap.put("ORDER", "主订单");
		orderTypeMap.put("ORDER_ITEM", "子订单");
		model.addAttribute("orderTypeMap", orderTypeMap);
	}
	
	/**
	 * 初始化查询参数
	 * 
	 * @return
	 */
	private Map<String, Object> initParam(OrdAuditConfigInfo ordAuditConfigInfo,OrderMonitorCnd monitorCnd) {
		
		Map<String, Object> params=new HashMap<String, Object>();
		
		if(null!=monitorCnd.getOrderId()){
			params.put("orderId", monitorCnd.getOrderId());
		}
		if(StringUtils.isNotEmpty(monitorCnd.getOperatorName())){
			params.put("operatorName", monitorCnd.getOperatorName());
		}
		 
		if (!StringUtils.isEmpty(ordAuditConfigInfo.getThreeDepartment())) { //选择指定组的情况
			Long[] orgIdArray=new Long[]{Long.valueOf(ordAuditConfigInfo.getThreeDepartment())};
			params.put("orgIds", orgIdArray);
			
		}else if(!StringUtils.isEmpty(ordAuditConfigInfo.getSecondDepartment())){ //选择指定二级部门的情况
			Long num = NumberUtils.toLong(ordAuditConfigInfo.getSecondDepartment());
			if(num>0){
				List<PermOrganization> treeDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(num);
				
				if(null!=treeDepartmentList&&treeDepartmentList.size()>0){
					
					Long[] orgIdArray=new Long[treeDepartmentList.size()+1];
					for (int i = 0; i < treeDepartmentList.size(); i++) {
						orgIdArray[i]=treeDepartmentList.get(i).getOrgId();
					}
					orgIdArray[treeDepartmentList.size()]=num;
					params.put("orgIds", orgIdArray);
				}
			}
			
		}else if (!StringUtils.isEmpty(ordAuditConfigInfo.getFirstDepartment())) {  //选择指定一级部门的情况
			
			List<PermOrganization> secondDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(new Long(ordAuditConfigInfo.getFirstDepartment()));
			List<Long> orgIdArray=new ArrayList<Long>();
			orgIdArray.add(NumberUtils.toLong(ordAuditConfigInfo.getFirstDepartment()));
			for (PermOrganization second : secondDepartmentList) {
				orgIdArray.add(second.getOrgId());
				List<PermOrganization> treeDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(second.getOrgId());
				if(null!=treeDepartmentList&&treeDepartmentList.size()>0){
					for (int i = 0; i < treeDepartmentList.size(); i++) {
						orgIdArray.add(treeDepartmentList.get(i).getOrgId());
					}
				}
			}
			Long[] orgIds= orgIdArray.toArray(new Long[orgIdArray.size()]);
			params.put("orgIds",orgIds);
			
		}
		
		if(params.containsKey("orgIds")){
			Long[] orgIds = (Long[])params.get("orgIds");
			if(ArrayUtils.isEmpty(orgIds)){
				params.remove("orgIds");
			}
		}
		 
		//params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		//订单类型
		if(!StringUtils.isEmpty(monitorCnd.getOrderType())){
			params.put("objectType",monitorCnd.getOrderType());
		}
		//下单时间开始
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
			params.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
		}
		//下单时间结束
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
			params.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
		}
		
		//游玩时间开始
		Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
		if (visitTimeBegin!=null) {
			params.put("visitTimeBegin",visitTimeBegin);
		}
		//游玩时间结束
		Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
		if (visitTimeEnd!=null) {
			params.put("visitTimeEnd",visitTimeEnd);
		}
		
		if(StringUtils.isNotEmpty(monitorCnd.getOrderStatus())){
			params.put("orderStatus",monitorCnd.getOrderStatus());
		}
		
		if(StringUtils.isNotEmpty(monitorCnd.getInfoStatus())){
			params.put("infoStatus",monitorCnd.getInfoStatus());
		}
		
		if(StringUtils.isNotEmpty(monitorCnd.getResourceStatus())){
			params.put("resourceStatus",monitorCnd.getResourceStatus());
		}
		
		if(StringUtils.isNotEmpty(monitorCnd.getPaymentStatus())){
			params.put("paymentStatus",monitorCnd.getPaymentStatus());
		}
		
		if(StringUtils.isNotEmpty(monitorCnd.getCertConfirmStatus())){
			params.put("certificateStatus",monitorCnd.getCertConfirmStatus());
		}
		
		if(null!=monitorCnd.getProductId()){
			params.put("productId",monitorCnd.getProductId());
		}
		if(null!=monitorCnd.getSupplierId()){
			params.put("supplierId",monitorCnd.getSupplierId());
		}
		if(StringUtils.isNotEmpty(monitorCnd.getContactName())){
			params.put("contactName",monitorCnd.getContactName());
		}
		if(StringUtils.isNotEmpty(monitorCnd.getContactMobile())){
			params.put("contactMobile",monitorCnd.getContactMobile());
		}
		return params;
	}
	
	/**
	 * 人工分单查询条件封装
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param monitorCnd
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryConditionForResponsible(Integer currentPage, Integer pageSize,Set<Long> orderIds,Set<Long> orderItemIds) {
		//保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		//联系人姓名  下单时间  游玩/入住时间      联系人姓名  联系人手机    供应商名称    供应商名称
		//组装订单内容类条件
		//condition.getOrderContentParam().setOperatorName(monitorCnd.getOperatorName());
		/*condition.getOrderContentParam().setContactName(monitorCnd.getContactName());
		condition.getOrderContentParam().setContactMobile(monitorCnd.getContactMobile());
		condition.getOrderContentParam().setProductId(monitorCnd.getProductId());
		condition.getOrderContentParam().setSupplierId(monitorCnd.getSupplierId());*/
		 
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(false);//需要分页

		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
		
		//组装订单活动类条件
		/*condition.getOrderActivityParam().setActivityName(monitorCnd.getActivityName());
		condition.getOrderActivityParam().setActivityDetail(monitorCnd.getActivityDetail());
		condition.getOrderActivityParam().setActivityStatus(monitorCnd.getActivityStatus());
		 */
		//组装订单时间类条件
		/*if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
			condition.getOrderTimeRangeParam().setCreateTimeBegin(DateUtil.toDate(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
			condition.getOrderTimeRangeParam().setCreateTimeEnd(DateUtil.toDate(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
		}
		condition.getOrderTimeRangeParam().setVisitTimeBegin(monitorCnd.getVisitTimeBegin());
		condition.getOrderTimeRangeParam().setVisitTimeEnd(monitorCnd.getVisitTimeEnd());*/
		//组装订单ID类条件
		condition.getOrderIndentityParam().setOrderIds(orderIds);
		condition.getOrderIndentityParam().setOrderItems(orderItemIds);
		return condition;
	}
	
	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrdResponsibleVo> buildQueryResult(String orderType,List<OrdOrder> orderList,Set<Long> orderItemIds) {
		List<OrdResponsibleVo> resultList = new ArrayList<OrdResponsibleVo>();
		//下单渠道
		List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>()).getReturnContent();
		Map<String, String> distributorMap = new HashMap<String, String>();
		for(Distributor distributor:distributorList){
			distributorMap.put(distributor.getDistributorId()+"", distributor.getDistributorName());
		}
		for (OrdOrder order : orderList) {
			if("ORDER_ITEM".equals(orderType)){
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if(!orderItemIds.isEmpty()&&orderItemIds.contains(orderItem.getOrderItemId())){
						OrdResponsibleVo ordResponsibleVo=this.doOrdResponsibleVoHandle(order,orderItem,distributorMap);
						resultList.add(ordResponsibleVo);
					}
				}
			}else{
				OrdOrderItem orderItem=order.getMainOrderItem();
				OrdResponsibleVo ordResponsibleVo=this.doOrdResponsibleVoHandle(order,orderItem,distributorMap);
				resultList.add(ordResponsibleVo);
			}
		}
		return resultList;
	}
	
	private OrdResponsibleVo doOrdResponsibleVoHandle(OrdOrder order,OrdOrderItem orderItem,Map<String, String> distributorMap){
		OrdResponsibleVo  ordResponsibleVo=new OrdResponsibleVo();
		
		//将订单来源转化为名称显示
		if(null != distributorMap){
			if(distributorMap.containsKey(order.getDistributorId()+"")){
				ordResponsibleVo.setDistributorName(distributorMap.get(order.getDistributorId()+""));
			}else{
				ordResponsibleVo.setDistributorName(order.getDistributorId()+"");
			}
		}else{
			ordResponsibleVo.setDistributorName(order.getDistributorId()+"");
		}
		
		ordResponsibleVo.setOrderId(order.getOrderId());
		ordResponsibleVo.setProductName(this.buildProductName(order));
		
		ordResponsibleVo.setOrderItemId(orderItem.getOrderItemId());
		ordResponsibleVo.setProductId(orderItem.getProductId());
		
		BizCategory bizCategory=categoryClientService.findCategoryById(orderItem.getCategoryId()).getReturnContent();
		if(null!=bizCategory){
			ordResponsibleVo.setProductType(bizCategory.getCategoryName());
		}

		ordResponsibleVo.setCreateTime(this.buildCreateTime(order));
		ordResponsibleVo.setVisitTime(this.buildVisitTime(order));
		ordResponsibleVo.setContactName(this.buildContactName(order));
		OrdPerson orderPerson = order.getContactPerson();
		if(null != orderPerson){
			ordResponsibleVo.setContactMobile(orderPerson.getMobile());
		}
		ordResponsibleVo.setCurrentStatus(this.buildCurrentStatus(order));
		
		/*查询供应商 start*/
		ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
		if (resultHandleSuppSupplier.isSuccess()) {
			SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
			if(null!=suppSupplier&&!StringUtils.isEmpty(suppSupplier.getSupplierName())){
				ordResponsibleVo.setSupplierName(suppSupplier.getSupplierName());
			}
		} else {
			log.info("method:orderQueryList,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
		}
		/*查询供应商 end*/
		
			return ordResponsibleVo;
	}
	
	/**
	 * 根据订单和订单子项一对多的关系构建多个商品名称
	 * 
	 * @param orderList
	 * @return
	 */
	private String buildProductName(OrdOrder order) {
		String productName = "未知产品名称";
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
			productName = orderItem.getProductName()+orderItem.getSuppGoodsName();
		}
		return productName;
	}
	
	/**
	 * 处理下单时间
	 * 
	 * @param order
	 * @return
	 */
	private String buildCreateTime(OrdOrder order) {
		String createTimeStr = "未知下单时间";
		Date createTime = order.getCreateTime();
		if (null != createTime) {
			// 保留年月日时分
			createTimeStr = DateUtil.formatDate(createTime, "yyyy-MM-dd HH:mm");
		}
		return createTimeStr;
	}

	/**
	 * 根据订单和订单子项一对多的关系构建多条游玩和入住时间
	 * 
	 * @param order
	 * @return
	 */
	private String buildVisitTime(OrdOrder order) {
		String visitTime = "未知日期";
		OrdOrderItem orderItem = order.getMainOrderItem();
		if(null != orderItem){
			List<OrdOrderHotelTimeRate> orderHotelTimeRate = orderItem.getOrderHotelTimeRateList();
			String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
			visitTime = firstDay;
			if(null != orderHotelTimeRate && orderHotelTimeRate.size()>0){
				String lastDay = DateUtil.formatDate(DateUtil.dsDay_Date(orderItem.getVisitTime(), orderHotelTimeRate.size()),"yyyy-MM-dd");
				//visitTime = firstDay+"/"+lastDay;
				visitTime += "<br>"+lastDay;
			}
		}
		return visitTime;
	}

	/**
	 * 处理联系人
	 * 
	 * @param order
	 * @return
	 */
	private String buildContactName(OrdOrder order) {
		String contactPerson = "";
		OrdPerson orderPerson = order.getContactPerson();
		if(null != orderPerson){
			if(UtilityTool.isValid(orderPerson.getFullName()) && !orderPerson.getFullName().contains("null")){
				contactPerson = orderPerson.getFullName();
			}else{
				//如果没有显示手机号
				contactPerson = orderPerson.getMobile();
			}
		}
		return contactPerson;
	}

	/**
	 * 处理订单的当前状态
	 * 
	 * @param order
	 * @return
	 */
	private String buildCurrentStatus(OrdOrder order) {
		StringBuilder builder = new StringBuilder();
		//组装订单状态
		if(OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())){
			builder.append("取消");
		}else if(OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())){
			builder.append("正常");
		}else if(OrderEnum.ORDER_STATUS.COMPLETE.name().equals(order.getOrderStatus())){
			builder.append("完成");
		}else{
			builder.append(order.getOrderStatus());
		}
		
		builder.append("<br>");
		
		//组装审核状态
		if(OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(order.getInfoStatus())
				&& OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())){
			builder.append("未审核");
		}else if(OrderEnum.INFO_STATUS.INFOFAIL.name().equals(order.getInfoStatus())
				||OrderEnum.RESOURCE_STATUS.LOCK.name().equals(order.getResourceStatus())){
			builder.append("审核不通过");
		}else if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(order.getInfoStatus())
				&&OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())){
			builder.append("审核通过");
		}else{
			builder.append("审核中");
		}
		
		builder.append(" | ");
		
		//组装凭证确认状态
		if(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("未确认");
		}else if(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("已确认");
		}else{
			builder.append("未确认");
		}
		
		builder.append("<br>");
		
		//组装支付状态
		builder.append(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		
		return builder.toString();
	}
	
	/**
	 * 进入选择订单负责人页面
	 * @param model
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/ordManualDistOrder/showSelectEmployee.do")
	public String showSelectEmployee(Model model,String orderIds,String objectType,HttpServletRequest request) throws BusinessException{
		model.addAttribute("orderIds", orderIds);
		model.addAttribute("objectType", objectType);
		return "/order/ordManualDistOrder/inc/select_employee_dialog";
	}
	
	/**
	 * 员工查询
	 * @param model
	 * @param operatorName
	 * @param page
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/ordManualDistOrder/queryEmployeeList.do")
	public String queryEmployeeList(Model model,String operatorName,Integer page,HttpServletRequest request) throws BusinessException{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("userName", operatorName);
		//不分页查找到用户总数
		Long totalCount=permUserServiceAdapater.queryPermUserByParamCount(parameters);
		
		int pagenum = page == null ? 1 : page;
		Page resultPage = Page.page(totalCount, 10, pagenum);
		resultPage.buildJSONUrl(request);
		//设置分页参数
		parameters.put("skipResults", resultPage.getStartRows());
		parameters.put("maxResults", resultPage.getEndRows());
		//先分页查询员工列表
		List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(parameters);
		
		//根据接单状态填充并清洗集合
//		List<WorkStatusVO> workStatusVOList = this.fillWorkStatus(permUserList);
		
		resultPage.setItems(permUserList);
		//保存查询结果
		model.addAttribute("resultPage", resultPage);
		
		return "/order/ordManualDistOrder/inc/employee_query_result";
	}
	
	/**
	 * 
	 * @param model
	 * @param operatorName
	 * @param orderIds
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/ord/ordManualDistOrder/batchDistOrder.do")
	@ResponseBody
	public Object batchDistOrder(Model model,String operatorName,String objectType,String orderIds,HttpServletRequest req) {
		 
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			  if(StringUtils.isEmpty(operatorName)){
				  throw new IllegalArgumentException("请选择订单处理人");
			  }
			  if(StringUtils.isEmpty(orderIds)){
				  throw new IllegalArgumentException("请选择要处理的订单");
			  }
			  //List<Long> objectIds,String objectType,PermUser assignTargetUser,String operatorName
			  String[] orderIdArray=orderIds.split(",");
			  List<Long> objectIds=new ArrayList<Long>(orderIdArray.length);
			  for (String orderId : orderIdArray) {
				  objectIds.add(Long.valueOf(orderId));
			  }
			  
			  PermUser assignTargetUser=permUserServiceAdapater.getPermUserByUserName(operatorName);
			  
			 int result=orderResponsibleService.updateManualAssign(objectIds, objectType, assignTargetUser, getLoginUserName());
			  if(objectIds.size()==result){
				  msg.setAttributes(attributes);
				  msg.setCode("success");
				  msg.setMessage("分单成功");
			  }else{
				  msg.setAttributes(attributes);
				  msg.setCode("success");
				  msg.setMessage("分单成功:"+result+"条"+"，分单失败:"+(objectIds.size()-result)+"条");
			  }
			
		}catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("分单失败:"+e.getMessage());
			log.error(e);
		}
		 return msg;
	}
	
	
	/**
	 * 
	 * @param model
	 * @param operatorName
	 * @param orderIds
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/ord/order/getOrderListByCategoryId.do")
	@ResponseBody
	public Object getOrderListByCategoryId(Model model,String categoryId,HttpServletRequest req) {
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	//保证每次请求都是一个新的对象
				ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
				//组装订单标志类条件
				condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
				condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
				condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
				condition.getOrderFlagParam().setOrderPageFlag(false);//需要分页

				//组装订单排序类条件
				condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
				condition.getOrderIndentityParam().setOrderCategoryId(11L);
				// 根据条件获取订单集合
				List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
				msg.addObject("orderList", orderList);
		}catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			msg.setMessage("查询失败:"+e.getMessage());
		}
		 return msg;
	}
	
	/**
	 * 获取登录用户名
	 * @return
	 */
	private String getLoginUserName(){
		//添加操作日志
		 try {
			 return this.getLoginUser().getUserName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		 return "未知用户";
	}
}
