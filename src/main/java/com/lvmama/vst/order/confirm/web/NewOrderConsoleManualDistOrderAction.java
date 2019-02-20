package com.lvmama.vst.order.confirm.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.back.order.po.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermOrganization;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.order.po.Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_AUDIT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.NOTICE_REGIMENT_STATUS_TYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ComAuditInfo;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrdAuditConfigInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 人工分单action
 * 
 * @author zhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/NewOrderConsole")
public class NewOrderConsoleManualDistOrderAction extends BaseActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(NewOrderConsoleManualDistOrderAction.class);

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private DistributorClientService distributorClientService;
	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderDistributionBusiness orderDistributionBusiness;
	
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;

	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;

	@Autowired
	private IOrderLocalService orderServiceRemote;
	
	@RequestMapping(value = "/showManualDistOrderList")
	public String showManualDistOrderList(Model model, HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showManualDistOrderList>");
		}
		//订单活动列表字典
		Map<String, String> auditTypeMap = new LinkedHashMap<String, String>();
		auditTypeMap.put("", "全部");
		
		for (CONFIRM_AUDIT_TYPE item : Confirm_Enum.CONFIRM_AUDIT_TYPE.values()) {
			auditTypeMap.put(item.name(), item.getCnName());
		}
		auditTypeMap.put(CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name(), "其他"+CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.getCnName());
		auditTypeMap.put("MAIN", "主预订通知");
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		
		Map<String,String> initMap=new HashMap<String, String>();
		initMap.put("", "请选择");
		
		firstDepMap.remove("");
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", initMap);
		
		//select默认选中值
		OrdAuditConfigInfo ordAuditConfigInfo=new OrdAuditConfigInfo();
		ordAuditConfigInfo.setFirstDepartment(Constants.ORG_ID_CTI+"");//默认呼叫中心 55 orgid
		
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		model.addAttribute("auditTypeMap", auditTypeMap);
		
		
		Map<String,String> auditStatusMap=new HashMap<String, String>();
		auditStatusMap.put("POOL", "待分配");
		auditStatusMap.put("UNPROCESSED", "未处理已分配");
		auditStatusMap.put("PROCESSED", "已处理已分配");
		
		model.addAttribute("auditStatusMap", auditStatusMap);
		
		//品类
		Map<String,String> categoryMap=new HashMap<String, String>();
		categoryMap.put("", "全部");
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCnName());
		model.addAttribute("categoryMap", categoryMap);
		
		//是否保留房
		Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
        stockFlagMap.put("", "全部");
        stockFlagMap.put("-1", "保留房");
        stockFlagMap.put("-2", "非保留房");
        model.addAttribute("stockFlagMap", stockFlagMap);

		// 出团通知书状态字典	
		Map<String, String> noticeRegimentStatusMap = new LinkedHashMap<String, String>();
		noticeRegimentStatusMap.put("", "全部");
		for (NOTICE_REGIMENT_STATUS_TYPE item : NOTICE_REGIMENT_STATUS_TYPE.values()) {
			noticeRegimentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("noticeRegimentStatusMap", noticeRegimentStatusMap);
		
		OrderMonitorCnd orderMonitorCnd = new OrderMonitorCnd();
		Date today = new Date();
		Date beginDate = DateUtils.addDays(today, -29);
        Date endDate = DateUtils.addDays(today, 1);
		orderMonitorCnd.setCreateTimeBegin(DateUtil.formatDate(beginDate, DateUtil.HHMMSS_DATE_FORMAT));
		orderMonitorCnd.setCreateTimeEnd(DateUtil.formatDate(endDate, DateUtil.HHMMSS_DATE_FORMAT));
		model.addAttribute("monitorCnd", orderMonitorCnd);
		
		ComAuditInfo comAuditInfo=new ComAuditInfo();
		comAuditInfo.setAuditStatus("UNPROCESSED");
		model.addAttribute("comAuditInfo",comAuditInfo);
		return "/order/ordNewConsoleManualDistOrder/findManualDistOrderList";
	}

	@RequestMapping(value = "/findManualDistOrderList")
	public String findManualDistOrderList(OrdAuditConfigInfo ordAuditConfigInfo,OrderMonitorCnd monitorCnd,ComAuditInfo comAuditInfo, Model model, Integer page,Integer pageSize, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findManualDistOrderList>");
		}
		List<ComAuditInfo> resultList=new ArrayList<ComAuditInfo>();
		
		
		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
		String threeDepartment=ordAuditConfigInfo.getThreeDepartment();
		String operatorName=comAuditInfo.getOperatorName();
		
		
		Date distributionTimeBegin=monitorCnd.getDistributionTimeBegin();
		Date distributionTimeEnd=monitorCnd.getDistributionTimeEnd();
		
		
		String auditStatus=comAuditInfo.getAuditStatus();//活动状态
		
		
		//定义查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		//param.put("objectId", orderId);
//		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		//param.put("auditType", auditType);
		//String[] auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.POOL.getCode(),OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()};
		param.put("auditStatus",auditStatus );
		
		if(StringUtil.isNotEmptyString(comAuditInfo.getCategoryId())
				&&(Long.parseLong(comAuditInfo.getCategoryId())==181||Long.parseLong(comAuditInfo.getCategoryId())==182)){
			param.put("subCategoryId",comAuditInfo.getCategoryId());
		}else if(StringUtil.isNotEmptyString(comAuditInfo.getCategoryId())){
			param.put("categoryId",comAuditInfo.getCategoryId());
		}
		if(StringUtil.isNotEmptyString(comAuditInfo.getStockFlag())){
			param.put("stockFlag",comAuditInfo.getStockFlag());
		}
		if ("POOL".equals(auditStatus)) {//未分配
			
			String auditType=comAuditInfo.getAuditType();
			if (!StringUtils.isEmpty(auditType)) {
				if(Confirm_Enum.CONFIRM_AUDIT_TYPE.RETURN_BACK_AUDIT.name().equals(auditType)){
					param.put("auditType",Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name());
					param.put("isReturnBack",true);
				}else if("MAIN".equals(auditType)){
					param.put("auditType",CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
					param.put("isReturnBack",false);
					param.put("objectType",OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
				}else{
					param.put("auditType",auditType );
					param.put("isReturnBack",false);
					param.put("objectType",OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
				}
			}
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				param.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
			}
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				param.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
			}
			Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
			if (visitTimeBegin!=null) {
	 			param.put("visitTimeBegin",visitTimeBegin);
			}
			Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
			if (visitTimeEnd!=null) {
				param.put("visitTimeEnd", DateUtil.addDays(visitTimeEnd, 1));
			}
			
			int count=orderAuditService.countAuditByNewConsole(param);
			
			int pagenum = page == null ? 1 : page;
			Page<ComAuditInfo> pageParam = Page.page(count, 10, pagenum);
			pageParam.buildUrl(request);
			param.put("_start", pageParam.getStartRows());
			param.put("_end", pageParam.getEndRows());
			
			//List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
			//modifed by wenzhengtao 20131219
			List<ComAudit> comAuditList=orderAuditService.queryAuditByNewConsole(param);
			getComAuditList(monitorCnd, resultList, auditStatus,
					comAuditList);
			pageParam.setItems(resultList);
			model.addAttribute("pageParam", pageParam);
			
		}else if("UNPROCESSED".equals(auditStatus) || "PROCESSED".equals(auditStatus)){//待处理
			
			if (operatorName!=null) {
				operatorName=operatorName.trim();
				monitorCnd.setOperatorName(operatorName);//联合查询的时候需要此参数
			}
			
			boolean userValid=true;
			if (!StringUtils.isEmpty(threeDepartment) && !StringUtils.isEmpty(operatorName)) {
				
				Map<String, Object> userParams = new HashMap<String, Object>();
				userParams.put("userNameEQ", operatorName);
				userParams.put("valid", "Y");
				userParams.put("maxResults", 100);
				userParams.put("skipResults",0);
				userParams.put("departmentId", threeDepartment);
				List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
				
				if (permUserList.isEmpty()) {
					userValid=false;
				}else{
					param.put("operatorName",operatorName );
				}
				/*PermUser permUser=permUserServiceAdapater.getPermUserByUserName(operatorName);
				if (permUser==null) {
					userValid=false;
				}
				*/
				
				
			}else if (!StringUtils.isEmpty(operatorName)) {
				param.put("operatorName",operatorName );
			}else if (!StringUtils.isEmpty(threeDepartment)) {
				
				Map<String, Object> userParams = new HashMap<String, Object>();
				userParams.put("valid", "Y");
				userParams.put("maxResults", 100);
				userParams.put("skipResults",0);
				userParams.put("departmentId", threeDepartment);
				List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
				
				if (permUserList.isEmpty()) {
					userValid=false;
				}else{
					
					String[] operatorNameArray=new String[permUserList.size()];
					for (int i = 0; i <permUserList.size(); i++) {
						operatorNameArray[i]=permUserList.get(i).getUserName() ;
						
					}
					param.put("operatorNameArray",operatorNameArray );
				}
			//start Modify by xuehualing 2014/05/30  
			}else if(StringUtils.isEmpty(threeDepartment) && StringUtils.isEmpty(operatorName)){
				Map<String, Object> userParams = new HashMap<String, Object>();
				userParams.put("valid", "Y");
				userParams.put("maxResults", 100);
				userParams.put("skipResults",0);
				userParams.put("departmentId", secondDepartment);
				List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
				if (permUserList.isEmpty()) {
					userValid=false;
				}else{
					
					String[] operatorNameArray=new String[permUserList.size()];
					for (int i = 0; i <permUserList.size(); i++) {
						operatorNameArray[i]=permUserList.get(i).getUserName() ;
						
					}
					param.put("operatorNameArray",operatorNameArray );
				}
			}
			//end Modify by  xuehualing 2014/05/30  
			
			if (userValid) {

				String auditType=comAuditInfo.getAuditType();
				if (!StringUtils.isEmpty(auditType)) {
					if(Confirm_Enum.CONFIRM_AUDIT_TYPE.RETURN_BACK_AUDIT.name().equals(auditType)){
						param.put("auditType",Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name() );
						param.put("isReturnBack",true);
					}else if("MAIN".equals(auditType)){
						param.put("auditType",CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name());
						param.put("isReturnBack",false);
						param.put("objectType",OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
					}else{
						param.put("auditType",auditType);
						param.put("isReturnBack",false);
						param.put("objectType",OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
					}
				}
				if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
					param.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
				}
				if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
					param.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
				}
				Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
				if (visitTimeBegin!=null) {
		 			param.put("visitTimeBegin",visitTimeBegin );
				}
				Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
				if (visitTimeEnd!=null) {
					param.put("visitTimeEnd",visitTimeEnd );
				}
				
				if (monitorCnd.getNoticeRegimentStatus() != null) {
					param.put("noticeRegimentStatus",monitorCnd.getNoticeRegimentStatus() );
				}
				
				if (distributionTimeBegin!=null) {
		 			param.put("distributionTimeBegin",distributionTimeBegin );
				}
				
				if (distributionTimeEnd!=null) {
					param.put("distributionTimeEnd",distributionTimeEnd );
				}
				
				//int count=orderAuditService.getTotalCount(param);
				//modifed by wenzhengtao 20131219
				int count=orderAuditService.countAuditByNewConsole(param);
				
				int pagenum = page == null ? 1 : page;
				Page<ComAuditInfo> pageParam = Page.page(count, 10, pagenum);
				pageParam.buildUrl(request);
				param.put("_start", pageParam.getStartRows());
				param.put("_end", pageParam.getEndRows());
				
				//List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
				//modifed by wenzhengtao 20131219
				List<ComAudit> comAuditList=orderAuditService.queryAuditByNewConsole(param);
				
				getComAuditList(monitorCnd, resultList, auditStatus,
						comAuditList);
				pageParam.setItems(resultList);
				model.addAttribute("pageParam", pageParam);
			}
			
		}
		
		
		
		model.addAttribute("auditStatus", auditStatus);
		
		model.addAttribute("resultList", resultList);
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		
		
		//订单活动列表字典
		Map<String, String> auditTypeMap = new LinkedHashMap<String, String>();
		auditTypeMap.put("", "全部");
		for (CONFIRM_AUDIT_TYPE item : Confirm_Enum.CONFIRM_AUDIT_TYPE.values()) {
			auditTypeMap.put(item.name(), item.getCnName());
		}
		auditTypeMap.put(CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name(), "其他"+CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.getCnName());
		auditTypeMap.put("MAIN", "主预订通知");
		
		model.addAttribute("auditTypeMap", auditTypeMap);
		model.addAttribute("initMap", Collections.emptyMap());
		
//		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
//		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
//		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();
		
		if (!StringUtils.isEmpty(firstDepartment)) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(firstDepartment));
		}
		if (!StringUtils.isEmpty(secondDepartment)) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(secondDepartment));
		}else{
			threeDepMap = new HashMap<String, String>();
			threeDepMap.put("", "请选择");
		}
		
		
		firstDepMap.remove("");
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		
		Map<String,String> auditStatusMap=new HashMap<String, String>();
		auditStatusMap.put("POOL", "待分配");
		auditStatusMap.put("UNPROCESSED", "未处理已分配");
		auditStatusMap.put("PROCESSED", "已处理已分配");		
		model.addAttribute("auditStatusMap", auditStatusMap);
		
		//品类
		Map<String,String> categoryMap=new HashMap<String, String>();
		categoryMap.put("", "全部");
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCnName());
		categoryMap.put(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId()+"", BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCnName());
		model.addAttribute("categoryMap", categoryMap);
		
		//是否保留房
		Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
        stockFlagMap.put("", "全部");
        stockFlagMap.put("-1", "保留房");
        stockFlagMap.put("-2", "非保留房");
        model.addAttribute("stockFlagMap", stockFlagMap);
		
		// 出团通知书状态字典	
/*		Map<String, String> noticeRegimentStatusMap = new LinkedHashMap<String, String>();
		noticeRegimentStatusMap.put("", "全部");
		for (NOTICE_REGIMENT_STATUS_TYPE item : NOTICE_REGIMENT_STATUS_TYPE.values()) {
			noticeRegimentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("noticeRegimentStatusMap", noticeRegimentStatusMap);*/
		
		return "/order/ordNewConsoleManualDistOrder/findManualDistOrderList";
	}



	private void getComAuditList(OrderMonitorCnd monitorCnd,
			List<ComAuditInfo> resultList, String auditStatus,
			List<ComAudit> comAuditList) {
		if (!comAuditList.isEmpty()) 
		{
			Set<Long> orderIds=new HashSet<Long>();
			Set<Long> orderItemIds = new HashSet<Long>();
			for(ComAudit ca:comAuditList)
			{
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(ca.getObjectType())){
					orderItemIds.add(ca.getObjectId());
				}else{
					orderIds.add(ca.getObjectId());
				}
			}
			List<OrdOrder> orderList=new ArrayList<OrdOrder>();
			if(!orderIds.isEmpty()){
				ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(auditStatus, monitorCnd,orderIds,OrderEnum.AUDIT_OBJECT_TYPE.ORDER);
				orderList = complexQueryService.queryOrderListByCondition(condition);
			}
			List<OrdOrder> orderList2=Collections.emptyList();
			if(!orderItemIds.isEmpty()){
				ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(auditStatus, monitorCnd,orderItemIds,OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM);
				orderList2 = complexQueryService.queryOrderListByCondition(condition);
			}
			doComAuditAfterHandle(resultList, comAuditList, orderList,
					orderList2);
		}
	}



	private void doComAuditAfterHandle(List<ComAuditInfo> resultList,
			List<ComAudit> comAuditList, List<OrdOrder> orderList,
			List<OrdOrder> orderList2) {
		if(!orderList2.isEmpty()){
			orderList.addAll(orderList2);
		}
		if (!orderList.isEmpty()){
			List<OrderMonitorRst> orderMonitorRstList=this.buildQueryResult(orderList);
			Map<Long,OrderMonitorRst> monitorRstMap = new HashMap<Long, OrderMonitorRst>();
			for(OrderMonitorRst monitorRst:orderMonitorRstList){
				monitorRstMap.put(monitorRst.getOrderId(), monitorRst);
			}
			for(ComAudit comAudit:comAuditList){
				//comAudit.setOrder(monitorRstMap.get(comAudit.getObjectId()));
				ComAuditInfo comAuditInfoObj=new ComAuditInfo();
				BeanUtils.copyProperties(comAudit, comAuditInfoObj);
				Long objectId=comAudit.getObjectId();
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(comAudit.getObjectType())){
					objectId = getOrderIdByOrderItemId(orderList2,comAudit.getObjectId());
				}
				comAuditInfoObj.setOrderMonitorRst(monitorRstMap.get(objectId));
				String auditTypeName = Confirm_Enum.CONFIRM_AUDIT_TYPE.isConfirmAuditType(comAudit.getAuditType()) ? Confirm_Enum.CONFIRM_AUDIT_TYPE.getCnName(comAudit.getAuditType()) : Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.getCnName(comAudit.getAuditType());
				comAuditInfoObj.setAuditTypeName(auditTypeName);
				comAuditInfoObj.setAuditStatusName(OrderEnum.AUDIT_STATUS.getCnName(comAudit.getAuditStatus()));
				resultList.add(comAuditInfoObj);
			}
		}
	}	
	
	private Long getOrderIdByOrderItemId(List<OrdOrder> orderLists,Long orderItemId){
		for(OrdOrder order:orderLists){
			for(OrdOrderItem item:order.getOrderItemList()){
				if(item.getOrderItemId().equals(orderItemId)){
					return item.getOrderId();
				}
			}
		}
		return null;
	}
	
	
	@RequestMapping(value = "/showManualDistOrder")
	public String showManualDistOrder(Model model, Integer page, OrdAuditConfig ordAuditConfig, HttpServletRequest request){

		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		
		firstDepMap.remove("");
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		
		Map<String,String> threeDepMap=new HashMap<String, String>();
		threeDepMap.put("", "请选择");
		model.addAttribute("threeDepMap", threeDepMap);
		
		Map<String,String> groupMemberMap=new HashMap<String, String>();
		groupMemberMap.put("", "请选择");
		model.addAttribute("groupMemberMap", groupMemberMap);
		
		//select默认选中值
		OrdAuditConfigInfo ordAuditConfigInfo=new OrdAuditConfigInfo();
		ordAuditConfigInfo.setFirstDepartment(Constants.ORG_ID_CTI+"");//默认呼叫中心 55 orgid
		
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		
		
		
		
		return "/order/ordNewConsoleManualDistOrder/manualDistOrder";
	}

	@RequestMapping(value = "/showMustDistOrder")
	public String showMustDistOrder(HttpServletRequest request){

		
		
		
		
		return "/order/ordNewConsoleManualDistOrder/mustDistOrder";
	}
	
	
	
	@RequestMapping(value = "/manualDistOrder")
	@ResponseBody
	public Object manualDistOrder(OrdAuditConfigInfo ordAuditConfigInfo, HttpServletRequest req,HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("start method<manualDistOrder>");
		}
		String oneData=req.getParameter("oneData");
		String manualDistOrder=req.getParameter("manualDistOrder");
		
		String loginUserId=getLoginUserId();
		
		
		List<String> auditIdStatusList=new ArrayList<String>();
		
		String auditIdStatus=req.getParameter("auditIdStatus");
		if ("true".equals(oneData)) {
			auditIdStatusList.add(auditIdStatus);
		}else{
			String auditIdStatusArray[]=auditIdStatus.split(",");
			auditIdStatusList=Arrays.asList(auditIdStatusArray);
		}
		
		String groupMember=ordAuditConfigInfo.getGroupMember();
		String threeDepartment= ordAuditConfigInfo.getThreeDepartment();
		String secondDepartment= ordAuditConfigInfo.getSecondDepartment();
		
		String message="";
		List<Long> orgIds =new ArrayList<Long>();
		
		if (!StringUtils.isEmpty(groupMember) ) {
			PermUser permUser=permUserServiceAdapater.getPermUserByUserName(groupMember);
			orgIds.add(permUser.getDepartmentId());
			if (!manualDistOrder.equals("true")) {
				OrdAuditUserStatus ordAuditUserStatus=orderAuditUserStatusService.selectByPrimaryKey(groupMember);
				if (ordAuditUserStatus==null ) {
					message="该员工离线状态";
					//message="该员工不适合（在线或者忙碌状态）";
				}
			}
		}else{
			if (!StringUtils.isEmpty(threeDepartment)) {
				orgIds.add(NumberUtils.toLong(threeDepartment));
			} else if (!StringUtils.isEmpty(secondDepartment)) {
				long second = NumberUtils.toLong(secondDepartment);
				if (second > 0) {
					orgIds.add(second);
					List<PermOrganization> list = permOrganizationServiceAdapter
							.getChildPermOrgList(second);
					for (PermOrganization po : list) {
						orgIds.add(po.getOrgId());
					}
				}
			}
		}
		
		if (message.length()>0) {
			return new ResultMessage(ResultMessage.ERROR,message);
		}
		
		Map<String, Object>  messageMap=new HashMap<String, Object>();
		messageMap=orderServiceRemote.makeOrderAuditForManualAudit(auditIdStatusList, loginUserId, orgIds,groupMember,manualDistOrder.equals("true"));
		message="选择"+messageMap.get("totalCount")+"条活动进行分单，"+messageMap.get("successCount")+"条成功，"+messageMap.get("failureCount")+"条失败";
		
		
		return new ResultMessage(ResultMessage.SUCCESS,message);
	}



	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrder> orderList) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>()).getReturnContent();
		Map<String, String> distributorMap = new HashMap<String, String>();
		for(Distributor distributor:distributorList){
			distributorMap.put(distributor.getDistributorId()+"", distributor.getDistributorName());
		}
		//Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
		for (OrdOrder order : orderList) {
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			//将订单来源转化为名称显示
			if(null != distributorMap){
				if(distributorMap.containsKey(order.getDistributorId()+"")){
					orderMonitorRst.setDistributorName(distributorMap.get(order.getDistributorId()+""));
				}else{
					orderMonitorRst.setDistributorName(order.getDistributorId()+"");
				}
			}else{
				orderMonitorRst.setDistributorName(order.getDistributorId()+"");
			}
			
			Long orderId=order.getOrderId();
			orderMonitorRst.setOrderId(orderId);
			orderMonitorRst.setProductName(this.buildProductName(order));
			orderMonitorRst.setBuyCount(this.buildBuyCount(order));
			orderMonitorRst.setCreateTime(this.buildCreateTime(order));
			orderMonitorRst.setVisitTime(this.buildVisitTime(order));
			orderMonitorRst.setContactName(this.buildContactName(order));
			orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order));
			
			Map<Long,String> orderItemNameMap=new HashMap<Long, String>();
			if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					orderItemNameMap.put(orderItem.getOrderItemId(), orderItem.getProductName()+"("+orderItem.getSuppGoodsName()+")");
				}
			}
			orderMonitorRst.setOrderItemNameMap(orderItemNameMap);
			
			resultList.add(orderMonitorRst);
		}
		return resultList;
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
	 * 构建订单购买商品数量
	 * 
	 * @param order
	 * @return
	 */
	private Integer buildBuyCount(OrdOrder order) {
		Integer buyCount = 0;
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
			buyCount = orderItem.getQuantity().intValue();
		}
		return buyCount;
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
	 * 新订单监控查询条件封装
	 * @param monitorCnd
	 * @param currentPage
	 * @param pageSize
	 * 
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryConditionForMonitor(String auditStatus,OrderMonitorCnd monitorCnd, Set<Long> ids ,OrderEnum.AUDIT_OBJECT_TYPE type) {
		//检查页面条件封装信息
		if(LOG.isDebugEnabled()){
			LOG.debug(" order monitor cnd "+monitorCnd);
		}
		//保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		if(type.equals(OrderEnum.AUDIT_OBJECT_TYPE.ORDER)){
			condition.getOrderIndentityParam().setOrderIds(ids);
		}else{
			condition.getOrderIndentityParam().setOrderItems(ids);
		}
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(false);//需要分页

		
		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);

		/*
		String auditType=monitorCnd.getActivityName();
		if ( auditType!=null && !"".equals(auditType)) {
			condition.getOrderActivityParam().setActivityName(auditType);//COM_AUDIT.AUDIT_TYPE 
		}
		
		String auditStatus="'"+OrderEnum.AUDIT_STATUS.POOL.getCode()+"','"+OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()+"'";
		condition.getOrderActivityParam().setActivityStatus(auditStatus);//COM_AUDIT.AUDIT_STATUS 
*/
		//组装订单时间类条件
//		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
//			condition.getOrderTimeRangeParam().setCreateTimeBegin(DateUtil.toDate(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
//		}
//		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
//			condition.getOrderTimeRangeParam().setCreateTimeEnd(DateUtil.toDate(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
//		}
		//condition.getOrderTimeRangeParam().setPaymentTimeBegin(monitorCnd.getPaymentTimeBegin());
		//condition.getOrderTimeRangeParam().setPaymentTimeEnd(monitorCnd.getPaymentTimeEnd());
//		condition.getOrderTimeRangeParam().setVisitTimeBegin(monitorCnd.getVisitTimeBegin());
//		condition.getOrderTimeRangeParam().setVisitTimeEnd(monitorCnd.getVisitTimeEnd());
		//condition.getOrderTimeRangeParam().setDistributionTimeBegin(monitorCnd.getDistributionTimeBegin());
		//condition.getOrderTimeRangeParam().setDistributionTimeEnd(monitorCnd.getDistributionTimeEnd());

		//组装订单分页类条件
		
		condition.getOrderPageIndexParam().setBeginIndex(0);
		condition.getOrderPageIndexParam().setEndIndex(ids.size());
		
		return condition;
	}




}
