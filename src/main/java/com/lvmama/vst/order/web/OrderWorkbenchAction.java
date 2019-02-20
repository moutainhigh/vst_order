package com.lvmama.vst.order.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.lvmama.vst.comm.utils.StringUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.tnt.po.TntGoodsChannelVo;
import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_AUDIT_TYPE;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_SUB_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.NOTICE_REGIMENT_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ComAuditInfo;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.vo.order.WorkStatusVO;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.TntGoodsChannelCouponAdapter;

/**
 * 我的工作台
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderWorkbenchAction extends BaseActionSupport{
	// 日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderMonitorAction.class);
	//我的工作台页面地址
	private static final String ORDER_QUERY_PAGE = "/order/query/orderQueryList";
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10; 
	//订单综合查询
	@Autowired
	private IComplexQueryService complexQueryService;
	//订单活动业务
	@Autowired
	private IOrderAuditService orderAuditService;
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
    private BizBuEnumClientService bizBuEnumClientService;
	
	/**
	 * 员工接单状态业务
	 */
	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	
	/**
	 * 员工业务
	 */
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;
	
	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;
	
	@Autowired
	private IOrdPersonService orderPersonService;
	
	@Autowired
	private IComMessageService comMessageService;
	
	private static final String SELECT_TYPE_MYTASK = "MYTASK"; //我的任务
	
	private static final String SELECT_TYPE_MYORDER = "MYORDER"; //我的订单
	
	@Autowired
	private TntGoodsChannelCouponAdapter tntGoodsChannelCouponServiceRemote;
	
	/**
	 * 进入我的工作台页面
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoOrderQuery.do")
	public String intoOrderQuery(Model model,OrderMonitorCnd monitorCnd,HttpServletRequest request) throws BusinessException{
		return selectTabInWorkBench(model, monitorCnd, request, null, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param operatorName
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/validateTime.do")
	@ResponseBody
	public Object validateTime(Model model,OrderMonitorCnd monitorCnd,HttpServletRequest request) throws BusinessException{
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setCode(ResultMessage.SUCCESS);
		//下单时间
		if ((StringUtils.isNotEmpty(monitorCnd.getCreateTimeBegin()) && StringUtils
				.isNotEmpty(monitorCnd.getCreateTimeEnd()))
				|| (monitorCnd.getVisitTimeBegin()!=null && monitorCnd.getVisitTimeEnd()!=null)){

			if (StringUtils.isNotEmpty(monitorCnd.getCreateTimeBegin()) && StringUtils.isNotEmpty(monitorCnd.getCreateTimeEnd())){
				
				Date createTime=DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss");
				Date endTime=DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss");
				
				if (createTime.compareTo(endTime)>=0) {
					msg.setCode(ResultMessage.ERROR);
					msg.setMessage("下单时间区间开始时间必须小于结束时间");
					return msg;
				}
				
				Date endAddTime=DateUtils.addMonths(createTime,3);
				
				Date createAddTime=DateUtils.addMonths(endTime,-3);
				
				if (endTime.compareTo(endAddTime)>0) {
					msg.setCode(ResultMessage.ERROR);
					msg.setMessage("下单时间区间只支持3个月区间");
				}
			}
			
			if (monitorCnd.getVisitTimeBegin()!=null && monitorCnd.getVisitTimeEnd()!=null){
				if (monitorCnd.getVisitTimeBegin().compareTo(monitorCnd.getVisitTimeEnd())>=0) {
					msg.setCode(ResultMessage.ERROR);
					msg.setMessage("游玩时间区间开始时间必须小于结束时间");
					return msg;
				}
				
			}	
			
			return msg;
		}else{
			
			msg.setCode(ResultMessage.ERROR);
			msg.setMessage("下单时间或者游玩时间必填一项");
			return msg;
		}
			
	}
	
	/**
	 * 我的工作台订单查询
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/orderQueryListforPayed.do")
	public String orderQueryListForPayed(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		try { 
			// 初始化查询表单,给字典项赋值
			initQueryForm(model,request);
			
			String auditType = null;
			String auditSubtype = null;
			String allDistributorIdsStr = "";
			List<Long> allDistributorIds = new ArrayList<Long>();
			
			//组装订单审核列表条件
			Map<String, Object> auditParam = new HashMap<String, Object>();
			auditParam.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
			if(UtilityTool.isValid(monitorCnd.getOperatorName())){
				auditParam.put("operatorName", monitorCnd.getOperatorName());
			}
			if(UtilityTool.isValid(monitorCnd.getActivityName())){
				// 存储好，可能后面再次使用
				auditType = monitorCnd.getActivityName();
				auditParam.put("auditType", auditType);
			}
			if(UtilityTool.isValid(monitorCnd.getActivityDetail())){
				// 存储好，可能后面再次使用
				auditSubtype = monitorCnd.getActivityDetail();
				auditParam.put("auditSubtype", auditSubtype);
			}
			auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());//这里仅查未处理的活动
			//下单时间开始
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				auditParam.put("createTimeBegin", monitorCnd.getCreateTimeBegin());
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				auditParam.put("createTimeEnd", monitorCnd.getCreateTimeEnd());
			}
			Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
			if (visitTimeBegin!=null) {
				auditParam.put("visitTimeBegin",DateUtil.formatDate(visitTimeBegin, DateUtil.HHMMSS_DATE_FORMAT));
			}
			Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
			if (visitTimeEnd!=null) {
				auditParam.put("visitTimeEnd",DateUtil.formatDate(visitTimeEnd, DateUtil.HHMMSS_DATE_FORMAT));
			}
			
			if (null!=monitorCnd.getProductId()) {
				auditParam.put("productId",monitorCnd.getProductId());
			}
			if (null!=monitorCnd.getSupplierId()) {
				auditParam.put("supplierId",monitorCnd.getSupplierId());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getContactName())) {
				auditParam.put("contactName",monitorCnd.getContactName());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getContactMobile())) {
				auditParam.put("contactMobile",monitorCnd.getContactMobile());
			}
			if(StringUtils.isNotEmpty(monitorCnd.getDisneyOrder())){
				auditParam.put("disneyOrder", monitorCnd.getDisneyOrder());
			}
			if(StringUtils.isNotEmpty(monitorCnd.getBespokeOrder())){
				auditParam.put("bespokeOrder", monitorCnd.getBespokeOrder());
			}
			if(StringUtils.isNotBlank(monitorCnd.getDistributorIdsStr())){
				allDistributorIdsStr += monitorCnd.getDistributorIdsStr();
			}
			if(StringUtils.isNotBlank(allDistributorIdsStr)){
				String[] allDistributorIdsArr = allDistributorIdsStr.split(",");
				for (int i = 0; i < allDistributorIdsArr.length; i++) {
					allDistributorIds.add(Long.valueOf(allDistributorIdsArr[i]));
				}
				auditParam.put("allDistributorIds", allDistributorIds);
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderPostStatus())) {
				auditParam.put("orderPost",monitorCnd.getOrderPostStatus());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderLockStatus())) {
				auditParam.put("orderLock",monitorCnd.getOrderLockStatus());
			}
			auditParam.put("auditFlag","SYSTEM"); // 过滤系统自动过的
			
			// 新老查询开关
			String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
			if(StringUtils.isEmpty(newRule) || "false".equals(newRule)){
				auditParam.put("newRule", null);
			}
			else{
				auditParam.put("newRule", "newRule");
			}
			//订单是否支付
			if(StringUtils.isNotEmpty(monitorCnd.getIsorderPayed())){
				auditParam.put("isPayed", monitorCnd.getIsorderPayed());
			}
			//订单下单48小时内
			if(null!=monitorCnd.getCreateOrderTimeInterval()){
				Date nowdate=new Date();
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				auditParam.put("visitTimeBegin", sdf.format(nowdate));
				auditParam.put("visitTimeEnd", sdf.format(new Date(nowdate.getTime() + 2 * 24 * 60 * 60 * 1000)));
			}
			//统计订单审核列表条数
			int auditTotalCount = orderAuditService.countAuditByMyWork(auditParam);
			
			int currentPage = page == null ? 1 : page;
			int currentPageSize = pageSize == null?DEFAULT_PAGE_SIZE:pageSize;
			
			Page<ComAuditInfo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
			pageParam.buildUrl(request);
			
			auditParam.put("_start", pageParam.getStartRows());
			auditParam.put("_end", pageParam.getEndRows());
			
			//查询订单审核列表集合
//			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(auditParam);
			List<ComAudit> auditList = orderAuditService.queryAuditListByCriteria(auditParam);
					
			
			Set<Long> orderIds=new TreeSet<Long>();
			orderIds.add(0L);
			Set<Long> orderItemIds=new TreeSet<Long>();
			orderItemIds.add(0L);
			for (ComAudit comAudit : auditList) {
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())){
					orderIds.add(comAudit.getObjectId());
				}else{
					orderItemIds.add(comAudit.getObjectId());
				}
			}
			
			/** 查询订单信息 Start*/
			
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition orderCondition = buildQueryConditionForWork(0, 0, orderIds,null);
			// 根据条件获取订单集合
			List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(orderCondition);
			// 根据页面展示特色组装其想要的结果
			List<OrderMonitorRst> orderResultList = buildQueryResult(orderList,request);
			//将订单转化为map,方便数据整合
			Map<Long, OrderMonitorRst> orderResultMap = new HashMap<Long, OrderMonitorRst>(orderList.size());
			
			for(OrderMonitorRst orderMonitorRst:orderResultList){
				orderResultMap.put(orderMonitorRst.getOrderId(), orderMonitorRst);
			}
			
			
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition orderItemCondition = buildQueryConditionForWork(0, 0, null,orderItemIds);
			// 根据条件获取订单集合
			List<OrdOrder> orderItemList = complexQueryService.queryOrderListByCondition(orderItemCondition);
			// 根据页面展示特色组装其想要的结果
			List<OrderMonitorRst> orderItemResultList = buildQueryOrderItemResult(orderItemList,request);
			//将订单转化为map,方便数据整合
			Map<Long, OrderMonitorRst> orderItemResultMap = new HashMap<Long, OrderMonitorRst>(orderItemList.size()*2);
			for(OrderMonitorRst orderMonitorRst:orderItemResultList){
				orderItemResultMap.put(orderMonitorRst.getOrderItemId(), orderMonitorRst);
			}
			
			/** 查询订单信息 End*/
						
			
			//将订单对象整合到审核对象里
			List<ComAuditInfo> comAuditInfoList = new ArrayList<ComAuditInfo>();
			for(ComAudit comAudit:auditList){
				ComAuditInfo comAuditInfo=new ComAuditInfo();
				BeanUtils.copyProperties(comAudit, comAuditInfo);
				
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())){
					comAuditInfo.setOrderMonitorRst(orderResultMap.get(comAudit.getObjectId()));
				}else{
					comAuditInfo.setOrderMonitorRst(orderItemResultMap.get(comAudit.getObjectId()));
				}
				
				comAuditInfo.setAuditTypeName(this.buildCurrentActivityName(comAudit.getAuditType(), comAudit.getAuditSubtype(), request));
				comAuditInfo.setAuditStatusName(this.buildCurrentActivityStatus(comAudit.getAuditStatus(), request));
				//实现下单时间的过滤,不在下单时间范围内的审核任务不加入到集合
				/*if(null != orderResultMap.get(comAudit.getObjectId())){
					comAuditInfoList.add(comAuditInfo);
				}*/
				comAuditInfoList.add(comAuditInfo);
			}
			
			sortComAuditInfoList(comAuditInfoList);
			
			// 组装分页结果
			@SuppressWarnings("rawtypes")
			Page resultPage = buildResultPage(comAuditInfoList, currentPage, pageSize, NumberUtils.toLong(auditTotalCount+"", 0), request);

			// 存储分页结果
			model.addAttribute("resultPage", resultPage);
			
			// 统计订单各种活动状态的条数，先要移除子状态参数
			auditParam.remove("auditSubtype");
			
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				auditParam.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				auditParam.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
			}
			if (visitTimeBegin!=null) {
				auditParam.put("visitTimeBegin",visitTimeBegin);
			}
			if (visitTimeEnd!=null) {
				auditParam.put("visitTimeEnd",visitTimeEnd);
			}
			
		} catch (Exception e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error(e);
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		model.addAttribute("checkedTab", "MYTASKFORPAY");
		return ORDER_QUERY_PAGE;
	}
	
	/**
	 * 我的工作台订单查询
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/orderQueryList.do")
	public String orderQueryList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		try { 
			// 初始化查询表单,给字典项赋值
			initQueryForm(model,request);
			
			//从登录的session中获取当前登录用户
			//String loginUserId = this.getLoginUserId();
			//monitorCnd.setOperatorName(loginUserId);
			
			String auditType = null;
			String auditSubtype = null;
			String allDistributorIdsStr = "";
			List<Long> allDistributorIds = new ArrayList<Long>();
			//组装订单审核列表条件
			Map<String, Object> auditParam = new HashMap<String, Object>();
			if(UtilityTool.isValid(monitorCnd.getOperatorName())){
				auditParam.put("operatorName", monitorCnd.getOperatorName());
			}
			if(UtilityTool.isValid(monitorCnd.getActivityName())){
				// 存储好，可能后面再次使用
				auditType = monitorCnd.getActivityName();
				auditParam.put("auditType", auditType);
			}
			if(UtilityTool.isValid(monitorCnd.getActivityDetail())){
				// 存储好，可能后面再次使用
				auditSubtype = monitorCnd.getActivityDetail();
				auditParam.put("auditSubtype", auditSubtype);
			}
			/*
			
			if(UtilityTool.isValid(monitorCnd.getActivityStatus())){
				auditParam.put("auditStatus", monitorCnd.getActivityStatus());
			}*/
			
//			auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());//这里仅查订单
			auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());//这里仅查未处理的活动
			//下单时间开始
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				auditParam.put("createTimeBegin", monitorCnd.getCreateTimeBegin());
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				auditParam.put("createTimeEnd", monitorCnd.getCreateTimeEnd());
			}
			Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
			if (visitTimeBegin!=null) {
				auditParam.put("visitTimeBegin",DateUtil.formatDate(visitTimeBegin, DateUtil.HHMMSS_DATE_FORMAT));
			}
			Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
			if (visitTimeEnd!=null) {
				auditParam.put("visitTimeEnd",DateUtil.formatDate(visitTimeEnd, DateUtil.HHMMSS_DATE_FORMAT));
			}
			
			if (null!=monitorCnd.getProductId()) {
				auditParam.put("productId",monitorCnd.getProductId());
			}
			if (null!=monitorCnd.getSupplierId()) {
				auditParam.put("supplierId",monitorCnd.getSupplierId());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getContactName())) {
				auditParam.put("contactName",monitorCnd.getContactName());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getContactMobile())) {
				auditParam.put("contactMobile",monitorCnd.getContactMobile());
			}
			if(StringUtils.isNotEmpty(monitorCnd.getDisneyOrder())){
				auditParam.put("disneyOrder", monitorCnd.getDisneyOrder());
			}
			if(StringUtils.isNotEmpty(monitorCnd.getBespokeOrder())){
				auditParam.put("bespokeOrder", monitorCnd.getBespokeOrder());
			}
			if(StringUtils.isNotBlank(monitorCnd.getDistributorIdsStr())){
				allDistributorIdsStr += monitorCnd.getDistributorIdsStr();
			}
			if(StringUtils.isNotBlank(monitorCnd.getSuperChannelIdsStr())){
				allDistributorIdsStr += monitorCnd.getSuperChannelIdsStr();
				allDistributorIdsStr = allDistributorIdsStr.replace("4,", "");
			}else if(monitorCnd.getDistributorIdsStr().contains("4")){
				allDistributorIdsStr += monitorCnd.getAllSuperChannelIdsStr();
			}
			if(StringUtils.isNotBlank(allDistributorIdsStr)){
				String[] allDistributorIdsArr = allDistributorIdsStr.split(",");
				for (int i = 0; i < allDistributorIdsArr.length; i++) {
					allDistributorIds.add(Long.valueOf(allDistributorIdsArr[i]));
				}
				auditParam.put("allDistributorIds", allDistributorIds);
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderPostStatus())) {
				auditParam.put("orderPost",monitorCnd.getOrderPostStatus());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderLockStatus())) {
				auditParam.put("orderLock",monitorCnd.getOrderLockStatus());
			}
			auditParam.put("auditFlag","SYSTEM"); // 过滤系统自动过的
			
            // 房间类型
            String stockFlag = monitorCnd.getStockFlag();
            auditParam.put("stockFlag", stockFlag);
            
            // 所属BU
            String buCode = monitorCnd.getBelongBU();
            if (StringUtils.isNotBlank(buCode)) {
	            String[] arr = buCode.split("\\|");
				if(arr.length > 1) {
					auditParam.put("BuCodes", Arrays.asList(arr));
				} else {
					auditParam.put("BuCode", buCode);
				}
            }
			
			// 新老查询开关
			String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
			if(StringUtils.isEmpty(newRule) || "false".equals(newRule)){
				auditParam.put("newRule", null);
			}
			else{
				auditParam.put("newRule", "newRule");
			}
						
			//统计订单审核列表条数
//			int auditTotalCount = orderAuditService.countAuditByCondition(auditParam);
			int auditTotalCount = orderAuditService.countAuditByMyWork(auditParam);
			
			int currentPage = page == null ? 1 : page;
			int currentPageSize = pageSize == null?DEFAULT_PAGE_SIZE:pageSize;
			
			Page<ComAuditInfo> pageParam = Page.page(auditTotalCount, currentPageSize, currentPage);
			pageParam.buildUrl(request);
			
			auditParam.put("_start", pageParam.getStartRows());
			auditParam.put("_end", pageParam.getEndRows());
			
			//查询订单审核列表集合
//			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(auditParam);
			List<ComAudit> auditList = orderAuditService.queryAuditListByCriteria(auditParam);
					
			
			Set<Long> orderIds=new TreeSet<Long>();
			orderIds.add(0L);
			Set<Long> orderItemIds=new TreeSet<Long>();
			orderItemIds.add(0L);
			for (ComAudit comAudit : auditList) {
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())){
					orderIds.add(comAudit.getObjectId());
				}else{
					orderItemIds.add(comAudit.getObjectId());
				}
			}
			
			/** 查询订单信息 Start*/
			
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition orderCondition = buildQueryConditionForWork(0, 0, orderIds,null);
			// 根据条件获取订单集合
			List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(orderCondition);
			// 根据页面展示特色组装其想要的结果
			List<OrderMonitorRst> orderResultList = buildQueryResult(orderList,request);
			//将订单转化为map,方便数据整合
			Map<Long, OrderMonitorRst> orderResultMap = new HashMap<Long, OrderMonitorRst>(orderList.size());
			
			for(OrderMonitorRst orderMonitorRst:orderResultList){
				orderResultMap.put(orderMonitorRst.getOrderId(), orderMonitorRst);
			}
			
			
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition orderItemCondition = buildQueryConditionForWork(0, 0, null,orderItemIds);
			// 根据条件获取订单集合
			List<OrdOrder> orderItemList = complexQueryService.queryOrderListByCondition(orderItemCondition);
			// 根据页面展示特色组装其想要的结果
			List<OrderMonitorRst> orderItemResultList = buildQueryOrderItemResult(orderItemList,request);
			//将订单转化为map,方便数据整合
			Map<Long, OrderMonitorRst> orderItemResultMap = new HashMap<Long, OrderMonitorRst>(orderItemList.size()*2);
			for(OrderMonitorRst orderMonitorRst:orderItemResultList){
				orderItemResultMap.put(orderMonitorRst.getOrderItemId(), orderMonitorRst);
			}
			
			/** 查询订单信息 End*/
						
			
			//将订单对象整合到审核对象里
			List<ComAuditInfo> comAuditInfoList = new ArrayList<ComAuditInfo>();
			for(ComAudit comAudit:auditList){
				ComAuditInfo comAuditInfo=new ComAuditInfo();
				BeanUtils.copyProperties(comAudit, comAuditInfo);
				
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType())){
					comAuditInfo.setOrderMonitorRst(orderResultMap.get(comAudit.getObjectId()));
				}else{
					comAuditInfo.setOrderMonitorRst(orderItemResultMap.get(comAudit.getObjectId()));
				}
				
				comAuditInfo.setAuditTypeName(this.buildCurrentActivityName(comAudit.getAuditType(), comAudit.getAuditSubtype(), request));
				comAuditInfo.setAuditStatusName(this.buildCurrentActivityStatus(comAudit.getAuditStatus(), request));
				//实现下单时间的过滤,不在下单时间范围内的审核任务不加入到集合
				/*if(null != orderResultMap.get(comAudit.getObjectId())){
					comAuditInfoList.add(comAuditInfo);
				}*/
				comAuditInfoList.add(comAuditInfo);
			}
			
			sortComAuditInfoList(comAuditInfoList);
			
			// 组装分页结果
			@SuppressWarnings("rawtypes")
			Page resultPage = buildResultPage(comAuditInfoList, currentPage, pageSize, NumberUtils.toLong(auditTotalCount+"", 0), request);

			// 存储分页结果
			model.addAttribute("resultPage", resultPage);
			
			// 统计订单各种活动状态的条数，先要移除子状态参数
			auditParam.remove("auditSubtype");
			
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				auditParam.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				auditParam.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
			}
			if (visitTimeBegin!=null) {
				auditParam.put("visitTimeBegin",visitTimeBegin);
			}
			if (visitTimeEnd!=null) {
				auditParam.put("visitTimeEnd",visitTimeEnd);
			}
			
			Map<String, Integer> statMap=countByWorkStatus(auditParam);
			//保存统计结果
			model.addAttribute("pretrialAuditNum", statMap.get("pretrialAuditNum"));
			model.addAttribute("bookingAuditNum", statMap.get("bookingAuditNum"));
			model.addAttribute("infoAuditNum", statMap.get("infoAuditNum"));
			model.addAttribute("resourceAuditNum", statMap.get("resourceAuditNum"));
			model.addAttribute("certificateAuditNum", statMap.get("certificateAuditNum"));
			model.addAttribute("paymentAuditNum", statMap.get("paymentAuditNum"));
			model.addAttribute("timePaymentAuditNum", statMap.get("timePaymentAuditNum"));
			model.addAttribute("noticeAuditNum", statMap.get("noticeAuditNum"));
			model.addAttribute("saleAuditNum", statMap.get("saleAuditNum"));
			model.addAttribute("cancelAuditNum", statMap.get("cancelAuditNum"));
			model.addAttribute("visitAuditNum", statMap.get("visitAuditNum"));
			model.addAttribute("onlineRefundAuditNum", statMap.get("onlineRefundAuditNum"));
			
			// 统计订单各种子活动状态的条数
			Map<String, Integer> subTypeMap =countByBookingAudit(auditParam);
			
			model.addAttribute("subTypeList", OrderEnum.AUDIT_SUB_TYPE.values());
			model.addAttribute("subTypeMap", subTypeMap);
			
			
		} catch (Exception e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("服务器内部异常");
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		
		return ORDER_QUERY_PAGE;
	}
	
	/**
	 * 按审核状态审核中的排在前面
	 * @param comAuditInfoList
	 */
	private void sortComAuditInfoList(List<ComAuditInfo> comAuditInfoList) {
		//资源状态为待审核的活动 
		List<ComAuditInfo> comAuditInfoListOfWait = new ArrayList<ComAuditInfo>();
		//资源状态为已审核的活动
		List<ComAuditInfo> comAuditInfoListOfDone = new ArrayList<ComAuditInfo>();
		for(ComAuditInfo comAuditInfo : comAuditInfoList){
			OrderMonitorRst orderMonitorRst = comAuditInfo.getOrderMonitorRst();
			if(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(orderMonitorRst.getResourceStatus())
					|| OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(orderMonitorRst.getInfoStatus())){
				comAuditInfoListOfWait.add(comAuditInfo);
			}else{
				comAuditInfoListOfDone.add(comAuditInfo);
			}
		}
		comAuditInfoList.clear();
		comAuditInfoList.addAll(comAuditInfoListOfWait);
		comAuditInfoList.addAll(comAuditInfoListOfDone);
	}

	/**
	 * 切换tab
	 * @param model
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/selectTabInWorkBench.do")
	public String selectTabInWorkBench(Model model,OrderMonitorCnd monitorCnd,HttpServletRequest request, String checkedTab, String operatorName) throws BusinessException {
		
		try {
			// 初始化查询表单,给字典项赋值
			initQueryForm(model,request);
			//从登录的session中获取当前登录用户
			String loginUserId = this.getLoginUserId();
			if (StringUtil.isEmptyString(operatorName)) {
				operatorName = loginUserId;
			}
			monitorCnd.setOperatorName(operatorName);
			if(StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())) {
				Calendar nowCal = Calendar.getInstance();
				nowCal.add(Calendar.MONTH, -6);
				monitorCnd.setCreateTimeBegin(DateUtil.formatDate(nowCal.getTime(), DateUtil.HHMMSS_DATE_FORMAT));
			}
			//活动状态默认显示未处理
			monitorCnd.setActivityStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		} catch (Exception e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("服务器内部异常");
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}
		model.addAttribute("monitorCnd",monitorCnd);
		
		model.addAttribute("subTypeList", OrderEnum.AUDIT_SUB_TYPE.values());
		model.addAttribute("subTypeMap",new HashMap());
		
//		if(SELECT_TYPE_MYTASK.equals(checkedTab)) {
//			return "/order/query/myTaskList";
//		}
//	
//		if(SELECT_TYPE_MYORDER.equals(checkedTab)) {
//			return "/order/query/myOrderList";
//		}

		model.addAttribute("checkedTab", checkedTab);
		
		return ORDER_QUERY_PAGE;
	}
	
	
	/**
	 * 我的订单查询
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/queryMyOrderList.do")
	public String queryMyOrderList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		try {
			// 初始化查询表单,给字典项赋值
			initQueryForm(model,request);
			
			//组装订单审核列表条件
			Map<String, Object> orderParam = new HashMap<String, Object>();
			if(UtilityTool.isValid(monitorCnd.getOperatorName())){
				orderParam.put("operatorName", monitorCnd.getOperatorName());
			}
			
			if(monitorCnd.getActivityCodeList() != null && monitorCnd.getActivityCodeList().size() > 0){
				List<String> auditTypes = new ArrayList<String>();
				List<String> auditSubtypes = new ArrayList<String>();
				for(String code : monitorCnd.getActivityCodeList()){
					if(AUDIT_TYPE.hasAuditType(code)){
						//活动
						auditTypes.add(code);
					}
					if(AUDIT_SUB_TYPE.hasAuditSubType(code)){
						//子活动
						auditSubtypes.add(code);
					}
					if(CONFIRM_AUDIT_TYPE.isConfirmAuditType(code)){
						//新版酒店工作台订单活动类型
						auditTypes.add(code);
					}
				}
				if(auditTypes.size() > 0){
					orderParam.put("auditTypes", auditTypes.toArray());
				}
				if(auditSubtypes.size() > 0){
					orderParam.put("auditSubtypes", auditSubtypes.toArray());
				}
			}
			//处理方式
			if(UtilityTool.isValid(monitorCnd.getHandlingMode())){
				orderParam.put("handlingMode", monitorCnd.getHandlingMode());
			}
			
			//下单时间开始
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				orderParam.put("createTimeBegin", monitorCnd.getCreateTimeBegin());
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				orderParam.put("createTimeEnd", monitorCnd.getCreateTimeEnd());
			}
			Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
			if (visitTimeBegin!=null) {
				orderParam.put("visitTimeBegin",DateUtil.formatDate(visitTimeBegin, DateUtil.HHMMSS_DATE_FORMAT));
			}
			Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
			if (visitTimeEnd!=null) {
				orderParam.put("visitTimeEnd",DateUtil.formatDate(visitTimeEnd, DateUtil.HHMMSS_DATE_FORMAT));
			}
			
			if (StringUtils.isNotEmpty(monitorCnd.getNoticeRegimentStatus())) {
				orderParam.put("noticeRegimentStatus",monitorCnd.getNoticeRegimentStatus());
			}
			if (null!=monitorCnd.getSupplierId()) {
				orderParam.put("supplierId",monitorCnd.getSupplierId());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderStatus())) {
				orderParam.put("orderStatus",monitorCnd.getOrderStatus());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderPostStatus())) {
				orderParam.put("orderPost",monitorCnd.getOrderPostStatus());
			}
			if (StringUtils.isNotEmpty(monitorCnd.getOrderLockStatus())) {
				orderParam.put("orderLock",monitorCnd.getOrderLockStatus());
			}			
			//统计订单审核列表条数
			int ordTotalCount = countMyOrderByCondition(orderParam);
			
			int currentPage = page == null ? 1 : page;
			int currentPageSize = pageSize == null?DEFAULT_PAGE_SIZE:pageSize;
			
			Page<ComAuditInfo> pageParam = Page.page(ordTotalCount, currentPageSize, currentPage);
			pageParam.buildUrl(request);
			
			orderParam.put("_start", pageParam.getStartRows());
			orderParam.put("_end", pageParam.getEndRows());
			
			//查询订单审核列表集合
			List<Map<String, Object>> orderIdMapList = queryMyOrderListByCondition(orderParam);
			
			List<Long> orderIds = new ArrayList<Long>();
			List<Long> orderItemIds = new ArrayList<Long>();
			List<String> idList = new ArrayList<String>(); //用于前台页面按照时间顺序显示订单
			if(orderIdMapList != null) {
				for (Map<String, Object> map : orderIdMapList) {
					if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(map.get("objectType"))){
						orderIds.add(Long.parseLong(String.valueOf(map.get("objectId"))));
					}else{
						orderItemIds.add(Long.parseLong(String.valueOf(map.get("objectId"))));
					}
					idList.add(map.get("objectType") + String.valueOf(map.get("objectId")));
				}
			}
			
			/** 查询订单信息 Start*/
			List<OrdOrder> orderList = new ArrayList<OrdOrder>();
			List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
			if(orderIds.size() > 0) {
				orderList = this.orderUpdateService.queryOrdorderByOrderIdList(orderIds);
			}
			
			if(orderItemIds.size() > 0) {
				orderItemList = this.iOrdOrderItemService.selectOrderItemsByIds(orderItemIds);
			}
			
			Map<String, OrderMonitorRst> myOrderMap = buildMyOrder(monitorCnd.getOperatorName(), orderList, orderItemList);
			
			/** 查询订单信息 End*/
			 
			
			// 组装分页结果
			@SuppressWarnings("rawtypes")
			Page resultPage = buildResultPage(idList, currentPage, pageSize, NumberUtils.toLong(ordTotalCount+"", 0), request);

			// 存储分页结果
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("myOrderMap", myOrderMap);
		} catch (Exception e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("服务器内部异常");
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		model.addAttribute("checkedTab", "MYORDER");
		return ORDER_QUERY_PAGE;
	}
	
	private int countMyOrderByCondition(Map<String, Object> orderParam) {
		// 新老查询开关
		String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("countMyOrder is new query: " + newRule);
		if (StringUtils.isEmpty(newRule) || "false".equals(newRule)) {
			return orderAuditService.countMyOrderByCondition(orderParam);
		} else {
			return orderAuditService.countMyOrderByRaid(orderParam);
		}
	}
	
	private List<Map<String, Object>> queryMyOrderListByCondition(Map<String, Object> orderParam){
		String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("queryMyOrderList is new query: " + newRule);
		if (StringUtils.isEmpty(newRule) || "false".equals(newRule)) {
			return orderAuditService.queryMyOrderListByCondition(orderParam);
		} else {
			return orderAuditService.queryMyOrderListByRaid(orderParam);
		}
	}
	
	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model,HttpServletRequest request) throws BusinessException {
		//下单渠道
		List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>()).getReturnContent();
		Map<String, String> distributorMap = new HashMap<String, String>();
		for(Distributor distributor:distributorList){
			distributorMap.put(distributor.getDistributorId()+"", distributor.getDistributorName());
		}
		request.setAttribute("distributorMap", distributorMap);
		model.addAttribute("distributorList", distributorList);
		
		//加载分销渠道的分销商
		ResultHandleT<TntGoodsChannelVo> tntGoodsChannelVoRt = tntGoodsChannelCouponServiceRemote
				.getChannels(TntGoodsChannelCouponAdapter.CH_TYPE.NONE.name());
		TntGoodsChannelVo tntGoodsChannelVo = null;
		if(tntGoodsChannelVoRt != null && tntGoodsChannelVoRt.getReturnContent() != null){
			tntGoodsChannelVo = (TntGoodsChannelVo)tntGoodsChannelVoRt.getReturnContent();
		}
		model.addAttribute("tntGoodsChannelVo", tntGoodsChannelVo);
				
		//订单活动列表字典
		Map<String, String> auditTypeMap = new LinkedHashMap<String, String>();
		auditTypeMap.put("", "全部");
		for (AUDIT_TYPE item : AUDIT_TYPE.values()) {
			auditTypeMap.put(item.getCode(), item.getCnName());
		}
		request.setAttribute("auditTypeMap", auditTypeMap);
		
		//订单活动类型、子活动类型
		Map<String, String> allAuditTypeMap = new LinkedHashMap<String, String>();
		for (AUDIT_TYPE item : AUDIT_TYPE.values()) {
			allAuditTypeMap.put(item.getCode(), item.getCnName());
		}
		for (AUDIT_SUB_TYPE item : AUDIT_SUB_TYPE.values()) {
			allAuditTypeMap.put(item.getCode(), item.getCnName());
		}
		//新版酒店工作台订单活动类型
		for (CONFIRM_AUDIT_TYPE item : CONFIRM_AUDIT_TYPE.values()) {
			if(!CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name().equals(item.name())){
				allAuditTypeMap.put(item.name(), item.getCnName());
			}
		}
		request.setAttribute("allAuditTypeMap", allAuditTypeMap);		
		
		//活动处理类型
		Map<String, String> handlingModeMap = new LinkedHashMap<String, String>();
		handlingModeMap.put("", "全部");
		handlingModeMap.put("-1", "人工处理");
		handlingModeMap.put("-2", "系统辅助");
		request.setAttribute("handlingModeMap", handlingModeMap);			
		
		//活动细分字典，待定
		Map<String, String> auditDetailMap = new LinkedHashMap<String, String>();
		auditDetailMap.put("", "全部");
		request.setAttribute("auditDetailMap", auditDetailMap);
		
		//订单活动状态字典
		Map<String, String> auditStatusMap = new LinkedHashMap<String, String>();
		auditStatusMap.put("", "全部");
		for (AUDIT_STATUS item : AUDIT_STATUS.values()) {
			if(!"POOL".equals(item.name())){
				auditStatusMap.put(item.getCode(), item.getCnName());
			}
		}
		request.setAttribute("auditStatusMap", auditStatusMap);
		
		// 订单状态字典
		Map<String, String> orderStatusMap = new LinkedHashMap<String, String>();
		orderStatusMap.put("", "全部");
		for (ORDER_STATUS item : ORDER_STATUS.values()) {
			orderStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("orderStatusMap", orderStatusMap);

		// 订单是否锁定
		Map<String, String> orderLockMap = new LinkedHashMap<String, String>();
		orderLockMap.put("", "全部");
		orderLockMap.put("-1", "是");
		orderLockMap.put("-2", "否");
		model.addAttribute("orderLockMap", orderLockMap);

		//订单是否后置
		Map<String, String> orderPostMap = new LinkedHashMap<String, String>();
		orderPostMap.put("", "全部");
		orderPostMap.put("-1", "是");
		orderPostMap.put("-2", "否");
		model.addAttribute("orderPostMap", orderPostMap);
		// 出团通知书状态字典	
		Map<String, String> noticeRegimentStatusMap = new LinkedHashMap<String, String>();
		noticeRegimentStatusMap.put("", "全部");
		for (NOTICE_REGIMENT_STATUS_TYPE item : NOTICE_REGIMENT_STATUS_TYPE.values()) {
			noticeRegimentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("noticeRegimentStatusMap", noticeRegimentStatusMap);
		// 所属BU
        Map<String, String> belongBUMap = new LinkedHashMap<String, String>();
        belongBUMap.put("", "全部");
        //参考http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=12992
    	String key = "LOCAL_BU|DESTINATION_BU";
		String text = "国内度假事业部";
		belongBUMap.put(key, text);
		ResultHandleT<List<BizBuEnum>> resultHandleT = bizBuEnumClientService.getAllBizBuEnumList();
		if (resultHandleT.isSuccess() && null != resultHandleT.getReturnContent()
            && !resultHandleT.getReturnContent().isEmpty()) {
		    for (BizBuEnum bizBuEnum : resultHandleT.getReturnContent()) {
		    	if(!key.contains(bizBuEnum.getCode())) {
		    		belongBUMap.put(bizBuEnum.getCode(), bizBuEnum.getCnName());
		    	}
            }
        }
		model.addAttribute("belongBUMap", belongBUMap);
		// 是否保留房
		Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
        stockFlagMap.put("", "全部");
        stockFlagMap.put("Y", "保留房");
        stockFlagMap.put("N", "非保留房");
        model.addAttribute("stockFlagMap", stockFlagMap);
	}
	
	/**
	 * 我的工作台查询条件封装
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param monitorCnd
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryConditionForWork(Integer currentPage, Integer pageSize, Set<Long> orderIds,Set<Long> orderItemIds) {
		//保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		//联系人姓名  下单时间  游玩/入住时间      联系人姓名  联系人手机    供应商名称    供应商名称
		//组装订单内容类条件
	/*	condition.getOrderContentParam().setOperatorName(monitorCnd.getOperatorName());
		condition.getOrderContentParam().setContactName(monitorCnd.getContactName());
		condition.getOrderContentParam().setContactMobile(monitorCnd.getContactMobile());
		condition.getOrderContentParam().setProductId(monitorCnd.getProductId());
		condition.getOrderContentParam().setSupplierId(monitorCnd.getSupplierId());*/
		 
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(false);//需要分页
		condition.getOrderFlagParam().setOrderStockTableFlag(true);

		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
		
		//组装订单活动类条件
		/*condition.getOrderActivityParam().setActivityName(monitorCnd.getActivityName());
		condition.getOrderActivityParam().setActivityDetail(monitorCnd.getActivityDetail());
		condition.getOrderActivityParam().setActivityStatus(monitorCnd.getActivityStatus());*/

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
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrder> orderList,HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
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
			orderMonitorRst.setIsTestOrder(order.getIsTestOrder());
			orderMonitorRst.setOrderId(order.getOrderId());
			orderMonitorRst.setProductName(this.buildProductName(order));
			orderMonitorRst.setBuyCount(this.buildBuyCount(order));
			orderMonitorRst.setCreateTime(this.buildCreateTime(order));
			orderMonitorRst.setVisitTime(this.buildVisitTime(order));
			orderMonitorRst.setContactName(this.buildContactName(order));
			OrdPerson orderPerson = order.getContactPerson();
			if(null != orderPerson){
				orderMonitorRst.setContactMobile(orderPerson.getMobile());
			}
			orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order));
			//显示担保信息
			orderMonitorRst.setGuarantee(order.getGuarantee());
			//资源审核状态
			orderMonitorRst.setResourceStatus(order.getResourceStatus());
			//信息审核状态
			orderMonitorRst.setInfoStatus(order.getInfoStatus());
			//所属BU
			if("LOCAL_BU".equals(order.getBuCode()) || "DESTINATION_BU".equals(order.getBuCode()) ) {
				//http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=12992
				 orderMonitorRst.setBelongBU("国内度假事业部");
			} else {
	            ResultHandleT<BizBuEnum> resultHandleT = bizBuEnumClientService.getBizBuEnumByBuCode(order.getBuCode());
	            if(resultHandleT.isSuccess()){
	                orderMonitorRst.setBelongBU(resultHandleT.getReturnContent().getCnName());
	            }
			}
            //是否保留房
            if (OrdOrderUtils.hasHotelItem(order)) {
                orderMonitorRst.setStockFlag(OrdOrderUtils.hasStockFlag(order)?"Y":"N");
            }
			/*查询供应商 start*/
			OrdOrderItem orderItem=order.getMainOrderItem();
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
				if(null!=suppSupplier&&!StringUtils.isEmpty(suppSupplier.getSupplierName())){
					orderMonitorRst.setSupplierName(suppSupplier.getSupplierName());
				}
			} else {
				log.info("method:orderQueryList,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
			}
			/*查询供应商 end*/
			//游玩人后置标识
			orderMonitorRst.setTravellerDelayFlag(order.getTravellerDelayFlag());
			//游玩人锁定标识
			orderMonitorRst.setTravellerLockFlag(order.getTravellerLockFlag());
			
			resultList.add(orderMonitorRst);
		}
		return resultList;
	}

	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrderMonitorRst> buildQueryOrderItemResult(List<OrdOrder> orderList,HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
		for (OrdOrder order : orderList) {
			for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
				OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
				orderMonitorRst.setIsTestOrder(order.getIsTestOrder()); //是否是测试单
				//设置子订单号
				orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
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
				//分销商渠道ID
				Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
				if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
						&& (order.getCategoryId().longValue() == 15 
						|| BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(order.getCategoryId())
						|| (order.getCategoryId().longValue() == 18&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())))
						&& (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())
								||BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())
								||BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId()))){
					if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
							&&!ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())){
						orderMonitorRst.setIsHighLight(0);
					}else{
						orderMonitorRst.setIsHighLight(1);
					}
				}
				orderMonitorRst.setOrderId(order.getOrderId());
				orderMonitorRst.setProductName(this.buildProductName(order));
				orderMonitorRst.setBuyCount(this.buildBuyCount(order));
				orderMonitorRst.setCreateTime(this.buildCreateTime(order));
				orderMonitorRst.setVisitTime(this.buildVisitTime(order));
				orderMonitorRst.setContactName(this.buildContactName(order));
				OrdPerson orderPerson = order.getContactPerson();
				if(null != orderPerson){
					orderMonitorRst.setContactMobile(orderPerson.getMobile());
				}
				orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order));
				//显示担保信息
				orderMonitorRst.setGuarantee(order.getGuarantee());
				//资源审核状态
				orderMonitorRst.setResourceStatus(ordOrderItem.getResourceStatus());
				//信息审核状态
				orderMonitorRst.setInfoStatus(ordOrderItem.getInfoStatus());
				//所属BU
				if("LOCAL_BU".equals(ordOrderItem.getBuCode()) || "DESTINATION_BU".equals(ordOrderItem.getBuCode()) ) {
					//http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=12992
					 orderMonitorRst.setBelongBU("国内度假事业部");
				} else {
		            ResultHandleT<BizBuEnum> resultHandleT = bizBuEnumClientService.getBizBuEnumByBuCode(ordOrderItem.getBuCode());
		            if(resultHandleT.isSuccess()){
		                orderMonitorRst.setBelongBU(resultHandleT.getReturnContent().getCnName());
		            }
				}
	            //保留房标识
                if (OrdOrderUtils.isHotelItem(ordOrderItem)) {
                    orderMonitorRst.setStockFlag(ordOrderItem.isRoomReservations()?"Y":"N");
                }
				/*查询供应商 start*/
				ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(ordOrderItem.getSupplierId());
				if (resultHandleSuppSupplier.isSuccess()) {
					SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
					if(null!=suppSupplier&&!StringUtils.isEmpty(suppSupplier.getSupplierName())){
						orderMonitorRst.setSupplierName(suppSupplier.getSupplierName());
					}
				} else {
					log.info("method:orderQueryList,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
				}
				/*查询供应商 end*/
				//游玩人后置标识
				orderMonitorRst.setTravellerDelayFlag(order.getTravellerDelayFlag());
				//游玩人锁定标识
				orderMonitorRst.setTravellerLockFlag(order.getTravellerLockFlag());
				
				resultList.add(orderMonitorRst);
			}
		}
		return resultList;
	}
	/**
	 * 组装分页对象
	 * 
	 * @author wenzhengtao
	 * @param model
	 * @param currentPage
	 * @param totalCount
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page buildResultPage(List list, Integer currentPage, Integer pageSize, Long totalCount, HttpServletRequest request) {
		// 如果当前页是空，默认为1
		Integer currentPageTmp = currentPage == null ? 1 : currentPage;
		// 从配置文件读取分页大小
		Integer defaultPageSize = DEFAULT_PAGE_SIZE;
		Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
		// 构造分页对象
		Page page = Page.page(totalCount, pageSizeTmp, currentPageTmp);
		// 构造分页URL
		page.buildUrl(request);
		// 设置结果集
		page.setItems(list);
		return page;
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
			builder.append("凭证未确认");
		}else if(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("凭证已确认");
		}else{
			builder.append("凭证未确认");
		}
		
		builder.append("<br>");
		
		//组装支付状态
		builder.append(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		return builder.toString();
	}
	
	/**
	 * 构建当前活动名称
	 * 
	 * @param auditType
	 * @param auditSubtype
	 * @param request
	 * @return
	 */
	private String buildCurrentActivityName(String auditType,String auditSubtype,HttpServletRequest request){
		String currentActivityName = "未知活动";
		@SuppressWarnings("unchecked")
		Map<String, String> auditTypeMap = (Map<String, String>)request.getAttribute("auditTypeMap");
		@SuppressWarnings("unchecked")
		Map<String, String> auditDetailMap = (Map<String, String>)request.getAttribute("auditDetailMap");
		if(null != auditTypeMap.get(auditType)){
			currentActivityName = auditTypeMap.get(auditType);
		}
		if(null != auditDetailMap.get(auditSubtype)){
			currentActivityName += "-"+auditDetailMap.get(auditSubtype);
		}
		return currentActivityName;
	}
	
	/**
	 * 构建当前活动状态
	 * 
	 * @param auditStatus
	 * @param request
	 * @return
	 */
	private String buildCurrentActivityStatus(String auditStatus,HttpServletRequest request){
		String currentActivityStatus = "未知状态";
		@SuppressWarnings("unchecked")
		Map<String, String> auditStatusMap = (Map<String, String>)request.getAttribute("auditStatusMap");
		if(null != auditStatusMap.get(auditStatus)){
			currentActivityStatus = auditStatusMap.get(auditStatus);
		}
		return currentActivityStatus;
	}
	
	/**
	 * 进入选择订单负责人页面
	 * @param model
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/showSelectEmployee.do")
	public String showSelectEmployee(Model model,HttpServletRequest request) throws BusinessException{
		
		return "/order/query/inc/select_employee_dialog";
	}
	
	/**
	 * 进入选择订单负责人页面
	 * @param model
	 * @param operatorName
	 * @param page
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryEmployeeList.do")
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
		
		return "/order/query/inc/employee_query_result";
	}
	
	/**
	 * 查询员工接单状态
	 * @param model
	 * @param operatorName
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryEmployeeWorkStatus.do")
	@ResponseBody
	public Object queryEmployeeWorkStatus(Model model,String operatorName,HttpServletRequest request) throws BusinessException{
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
				String workstatus=getWorkStatus(operatorName);
			 	attributes.put("workstatus",workstatus);
				msg.setAttributes(attributes);
				msg.setCode("success");
				msg.setMessage("查询员工状态成功");
		} catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("查询员工状态失败:"+e.getMessage());
			log.error(e);
		}
		 return msg;
	}
	
	/**
	 * 预约中的订单-修改remindTime值
	 * @param model
	 * @param auditId
	 * @param remindTimeStr
	 * @param request
	 * @return
	 * @throws BusinessException
	 * @author ltwangwei
	 * @date 2016-3-16 下午6:00:25
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	@RequestMapping("/ord/order/updateRemindTime.do")
	@ResponseBody
	public Object updateRemindTime(Model model, String auditId, String remindTimeStr, HttpServletRequest request) throws BusinessException{
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			Calendar c = Calendar.getInstance();
			//当前加上预约的时间
			c.add(Calendar.MINUTE, Integer.parseInt(remindTimeStr));
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("auditId", auditId);
			param.put("remindTime", c.getTime());
			//存入数据库
			int upadteCount = orderAuditService.updateRemindTimeByAuditId(param);
			if(upadteCount == 1){
				msg.setCode("success");
				msg.setMessage("设置成功，如需查看，请查询‘预约中的订单’");
			}else{
				msg.setCode("error");
				msg.setMessage("设置失败，系统问题！");
				log.info("OrderWorkbenchAction.updateRemindTime() update失败，auditId不存在或者数据重复，param: auditId:" + auditId);
			}
		} catch (Exception e) {
			msg.setCode("error");
			msg.setMessage("设置失败，系统问题！");
			log.error(e);
		}
		return msg;
	}
	
	
	/**
	 * 填充接单状态
	 * 
	 * @param workStatus
	 * @param workStatusVOList
	 * @return
	 */
	private List<WorkStatusVO> fillWorkStatus(List<PermUser> permUserList){
		List<WorkStatusVO> workStatusVOList = new ArrayList<WorkStatusVO>();
		for(PermUser permUser:permUserList){
			String userName = permUser.getUserName();
			String currentWorkStatus = this.getWorkStatus(userName);
			WorkStatusVO workStatusVO = new WorkStatusVO();
			workStatusVO.setUserId(permUser.getUserId());
			workStatusVO.setUserName(permUser.getUserName());
			workStatusVO.setRealName(permUser.getRealName());
			workStatusVO.setDepartmentId(permUser.getDepartmentId());
			workStatusVO.setDepartmentName(permUser.getDepartmentName());
			workStatusVO.setWorkStatus(currentWorkStatus);
			workStatusVOList.add(workStatusVO);
		}
		return workStatusVOList;
	}
	
	/**
	 * 到本地库查询人员的接单状态
	 * 
	 * @param userName
	 * @return
	 */
	private String getWorkStatus(String userName){
		OrdAuditUserStatus ordAuditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(userName);
		if(null != ordAuditUserStatus){
			if(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name().equals(ordAuditUserStatus.getUserStatus())){
				return OrderEnum.BACK_USER_WORK_STATUS.ONLINE.getCnName();
			}else{
				return OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.getCnName();
			}
		}else{
			return OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.getCnName();
		}
	}
	
	/**
	 * 统计订单各种活动状态的条数
	 * @param comAuditInfoList
	 * @return
	 */
	private Map<String, Integer> countByWorkStatus(Map<String, Object> param){
		Map<String, Integer> statMap = new HashMap<String, Integer>();
			statMap.put("pretrialAuditNum",buildActivityNum(param,"PRETRIAL_AUDIT"));//订单预审
			statMap.put("bookingAuditNum", buildActivityNum(param,"BOOKING_AUDIT"));//预订通知
			statMap.put("infoAuditNum", buildActivityNum(param,"INFO_AUDIT"));//信息审核
			statMap.put("resourceAuditNum", buildActivityNum(param,"RESOURCE_AUDIT"));//资源审核
			statMap.put("certificateAuditNum", buildActivityNum(param,"CERTIFICATE_AUDIT"));//凭证确认
			statMap.put("paymentAuditNum", buildActivityNum(param,"PAYMENT_AUDIT"));//催支付
			statMap.put("timePaymentAuditNum", buildActivityNum(param,"TIME_PAYMENT_AUDIT"));//小驴分期催支付
			statMap.put("noticeAuditNum", buildActivityNum(param,"NOTICE_AUDIT"));//通知出团
			statMap.put("saleAuditNum", buildActivityNum(param,"SALE_AUDIT"));//售后
			statMap.put("cancelAuditNum", buildActivityNum(param,"CANCEL_AUDIT"));//订单取消确认
			statMap.put("onlineRefundAuditNum", buildActivityNum(param,"ONLINE_REFUND_AUDIT"));//订单取消确认
			statMap.put("visitAuditNum", buildActivityNum(param,"VISIT_AUDIT"));//入住确认
		return statMap;
	}
	
	/**
	 * 统计预定通知子类型的条数
	 * @param comAuditInfoList
	 * @return
	 */
	private Map<String, Integer> countByBookingAudit(Map<String, Object> param){
		Map<String, Integer> subTypeMap = new HashMap<String, Integer>();
		
		param.put("auditType", "BOOKING_AUDIT");
		
		for (OrderEnum.AUDIT_SUB_TYPE subType : OrderEnum.AUDIT_SUB_TYPE.values()) {
			param.put("auditSubtype", subType.getCode());
			Integer count = countAuditByCondition(param);
			
			subTypeMap.put(subType.getCode(),count );
		}
		param.remove("auditType");
		param.remove("auditSubtype");
		
		return subTypeMap;
	}
	
	private int countAuditByCondition(Map<String, Object> param) {
		// 新老查询开关
		String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("countAudit subType is new query: " + newRule);
		if (StringUtils.isEmpty(newRule) || "false".equals(newRule)) {
			return orderAuditService.countAuditByCondition(param);
		} else {
			return orderAuditService.countAuditByRaid(param);
		}
	}
	
	/**
	 * 根据处理人和审核状态统计活动数量
	 * 
	 * @param userName
	 * @param auditType
	 * @return
	 */
	private int buildActivityNum(Map<String, Object> param,String auditType){
		param.put("auditType", auditType);
		int count = countAuditByCondition(param);
		param.remove("auditType");
		return count;
	}
	
	private Map<String, OrderMonitorRst> buildMyOrder(String operatorName, List<OrdOrder> orderList, List<OrdOrderItem> orderItemList) {
		Map<String, OrderMonitorRst> myOrderMap = new HashMap<String, OrderMonitorRst>();
		OrderMonitorRst result = null;
		if(orderList != null) {
			for(OrdOrder order : orderList) {
				result = new OrderMonitorRst();
				myOrderMap.put("ORDER" + order.getOrderId(), result);
				result.setIsMainOrder("Y");
				result.setOrderId(order.getOrderId());
				result.setResponsibleName(operatorName);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectType", "ORDER");
				params.put("objectId", order.getOrderId());
				order.setOrdPersonList(orderPersonService.findOrdPersonList(params));
				OrdPerson orderPerson = order.getContactPerson();
				result.setContactName(orderPerson == null ? "" : orderPerson.getFullName());
				result.setContactMobile(orderPerson == null ? "" : orderPerson.getMobile());
				result.setBuyCount(order.getMainOrderItem() == null ? null : order.getMainOrderItem().getQuantity().intValue());
				
				List<OrdOrderItem> orderItems = order.getOrderItemList();
				Long totalPrice = 0L;
				Long totalSettlementPrice = 0L;
				if(orderItems != null) {
					for(OrdOrderItem item : orderItems) {
						totalPrice += item.getPrice() * item.getQuantity();
						totalSettlementPrice += item.getActualSettlementPrice() * item.getQuantity();
					}
				}
				result.setPrice(PriceUtil.trans2YuanStr(totalPrice));
				result.setActualTotalSettlementPrice(PriceUtil.trans2YuanStr(totalSettlementPrice));
				result.setProductId(order.getMainOrderItem() == null ? null : order.getMainOrderItem().getProductId());
				result.setProductName(order.getMainOrderItem() == null ? null : order.getMainOrderItem().getProductName());
				
				ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(order.getMainOrderItem() == null ? null : order.getMainOrderItem().getSupplierId());
				if (resultHandleSuppSupplier.isSuccess()) {
					SuppSupplier suppSupplier= resultHandleSuppSupplier.getReturnContent();
					result.setSupplierName(suppSupplier == null ? "" : suppSupplier.getSupplierName());
				}
				
				result.setVisitTime(DateUtil.formatDate(order.getVisitTime(), DateUtil.SIMPLE_DATE_FORMAT));
				result.setCreateTime(DateUtil.formatDate(order.getCreateTime(), DateUtil.HHMM_DATE_FORMAT));
				result.setOrderStatus(OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
				result.setPaymentStatus(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
				//游玩人后置标识
				result.setTravellerDelayFlag(order.getTravellerDelayFlag());
				//游玩人锁定标识
				result.setTravellerLockFlag(order.getTravellerLockFlag());

				params = new HashMap<String, Object>();
				params.put("orderId", order.getOrderId());
				order.setOrdAdditionStatusList(ordAdditionStatusService.findOrdAdditionStatusList(params));
				result.setNoticeRegimentStatusName(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus()));
			}
		}
		
		if(orderItemList != null) {
			for(OrdOrderItem orderItem : orderItemList) {
				result = new OrderMonitorRst();
				myOrderMap.put("ORDER_ITEM" + orderItem.getOrderItemId(), result);
				result.setIsMainOrder("N");
				result.setOrderId(orderItem.getOrderId());
				result.setOrderItemId(orderItem.getOrderItemId());
				result.setResponsibleName(operatorName);
				
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectType", "ORDER");
				params.put("objectId", order.getOrderId());
				order.setOrdPersonList(orderPersonService.findOrdPersonList(params));
				OrdPerson orderPerson = order.getContactPerson();
				result.setContactName(orderPerson == null ? "" : orderPerson.getFullName());
				result.setContactMobile(orderPerson == null ? "" : orderPerson.getMobile());
				result.setBuyCount(orderItem.getQuantity().intValue());
				
				result.setPrice(PriceUtil.trans2YuanStr(orderItem.getPrice() * orderItem.getQuantity()));
				result.setActualTotalSettlementPrice(PriceUtil.trans2YuanStr(orderItem.getActualSettlementPrice() * orderItem.getQuantity()));
				result.setProductId(orderItem.getProductId());
				result.setProductName(orderItem.getProductName());
				
				ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
				if (resultHandleSuppSupplier.isSuccess()) {
					SuppSupplier suppSupplier= resultHandleSuppSupplier.getReturnContent();
					result.setSupplierName(suppSupplier == null ? "" : suppSupplier.getSupplierName());
				}
				
				result.setVisitTime(DateUtil.formatDate(orderItem.getVisitTime(), DateUtil.SIMPLE_DATE_FORMAT));
				result.setCreateTime(DateUtil.formatDate(order.getCreateTime(), DateUtil.HHMM_DATE_FORMAT));
				result.setOrderStatus(OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
				result.setPaymentStatus(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
				
				//游玩人后置标识
				result.setTravellerDelayFlag(order.getTravellerDelayFlag());
				//游玩人锁定标识
				result.setTravellerLockFlag(order.getTravellerLockFlag());
				
				params = new HashMap<String, Object>();
				params.put("orderId", order.getOrderId());
				order.setOrdAdditionStatusList(ordAdditionStatusService.findOrdAdditionStatusList(params));
				result.setNoticeRegimentStatusName(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus()));
			}
		}
		return myOrderMap;
	}

	@RequestMapping(value = "/ord/order/showComAuditDetails")
	public String showOrderDetails(Model model, Integer page, Integer pageSize, Long objectId, String objectType, HttpServletRequest req){
		objectType=objectType.trim();
		req.setAttribute("objectType", objectType);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", objectId);
		if("ORDER".equals(objectType)){
			params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		}else if("ORDER_ITEM".equals(objectType)){
			params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
		}
		int count = orderAuditService.getTotalCount(params);
		
		int currentPage = page == null ? 1 : page;
		int currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		
		Page<ComAudit> pageData = Page.page(count, currentPageSize, currentPage);
		pageData.buildUrl(req);
		
		params.put("_start", pageData.getStartRows());
		params.put("_end", pageData.getEndRows());
		
		List<ComAudit> auditList = orderAuditService.queryAuditListByParam(params);
		pageData.setItems(auditList);
		model.addAttribute("pageData", pageData);
		
		Map<String, String> allAuditTypeMap = new LinkedHashMap<String, String>();
		for (AUDIT_TYPE item : AUDIT_TYPE.values()) {
			allAuditTypeMap.put(item.getCode(), item.getCnName());
		}
		for (AUDIT_SUB_TYPE item : AUDIT_SUB_TYPE.values()) {
			allAuditTypeMap.put(item.getCode(), item.getCnName());
		}
		//新版酒店工作台订单活动类型
		for (CONFIRM_AUDIT_TYPE item : CONFIRM_AUDIT_TYPE.values()) {
			if(!CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name().equals(item.name())){
				allAuditTypeMap.put(item.name(), item.getCnName());
			}
		}
		model.addAttribute("allAuditTypeMap", allAuditTypeMap);
		model.addAttribute("auditStatusList", AUDIT_STATUS.values());
		
		return "/order/query/orderComAuditList";
	}
	
	/**
	 * 进入批量处理预定通知页面
	 * @param model
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryBatchMessage.do")
	public String queryBatchMessage(Model model,OrderMonitorCnd monitorCnd,HttpServletRequest request) throws BusinessException{
		String loginUserId = this.getLoginUserId();
		monitorCnd.setOperatorName(loginUserId);
		model.addAttribute("monitorCnd",monitorCnd);
		model.addAttribute("subTypeList", OrderEnum.AUDIT_SUB_TYPE.values());
		model.addAttribute("subTypeMap",new HashMap());
		return "/order/query/batchUpdateMessage";
	}
	
	/**
	 * 查询预订通知统计结果
	 * @param model
	 * @param monitorCnd
	 * @param resetFlag 是否为置标志
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/order/queryResultMessage.do")
	public String queryResultMessage(Model model,OrderMonitorCnd monitorCnd,boolean resetFlag , HttpServletRequest request){
		if(resetFlag){
			String loginUserId = this.getLoginUserId();
			monitorCnd.setOperatorName(loginUserId);
			model.addAttribute("subTypeList", OrderEnum.AUDIT_SUB_TYPE.values());
			model.addAttribute("subTypeMap",new HashMap());
		}else{
			try {
				Map<String, Object> auditParam = combinationParameter(monitorCnd);
				//保存预订通知统计结果,查询预订通知数量的时候要去掉子类型的过滤条件
				if(UtilityTool.isValid(monitorCnd.getActivityDetail())){
					auditParam.remove("auditSubtype");
				}
				model.addAttribute("bookingAuditNum", buildActivityNum(auditParam,"BOOKING_AUDIT"));
				// 统计订单各种子活动状态的条数
				Map<String, Integer> subTypeMap =countByBookingAudit(auditParam);
				model.addAttribute("subTypeList", OrderEnum.AUDIT_SUB_TYPE.values());
				model.addAttribute("subTypeMap", subTypeMap);
			} catch (Exception e) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.error("服务器内部异常");
				}
				LOGGER.error(ExceptionFormatUtil.getTrace(e));
			}
		}
		return "/order/query/resultMessage";
	}
	
	/**
	 * 批量修改预订通知状态
	 * @param model
	 * @param request
	 * @param monitorCnd
	 * @return
	 */
	@RequestMapping(value = "/ord/order/batchUpdateMessage")
	public String batchUpdateMessage(Model model, HttpServletRequest request,OrderMonitorCnd monitorCnd){
		String message="服务器忙无心处理";
		String code = "failure";
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("start method<batchUpdateMessage>");
			}
			int batchTotalNum = 0 ;//批量修改通知数量
			int batchSucNum = 0 ;//批量修改通知成功数量
			int batchFailNum = 0 ;//批量修改通知失败数量
			
			//组装订单审核列表条件
			Map<String, Object> parameters = new HashMap<String, Object>();
			// 过滤系统自动过的
			parameters.put("auditFlag","SYSTEM"); 
			//未处理的活动
			parameters.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
			//订单负责人
			if(UtilityTool.isValid(monitorCnd.getOperatorName())){
				parameters.put("operatorName", monitorCnd.getOperatorName());
			}
			//审核类型
			if(UtilityTool.isValid(monitorCnd.getActivityName())){
				String auditType = monitorCnd.getActivityName();
				parameters.put("auditType", auditType);
			}
			//审核子类型
			if(UtilityTool.isValid(monitorCnd.getActivityDetail())){
				String auditSubtype = monitorCnd.getActivityDetail();
				parameters.put("auditSubtype", auditSubtype);
			}
			//下单时间开始
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
				parameters.put("createTimeBegin", monitorCnd.getCreateTimeBegin());
			}
			//下单时间结束
			if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
				parameters.put("createTimeEnd", monitorCnd.getCreateTimeEnd());
			}
			//游玩日期开始
			Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
			if (visitTimeBegin!=null) {
				parameters.put("visitTimeBegin", DateUtil.formatDate(visitTimeBegin, DateUtil.HHMMSS_DATE_FORMAT) );
			}
			//游玩日期结束
			Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
			if (visitTimeEnd!=null) {
				parameters.put("visitTimeEnd",DateUtil.formatDate(visitTimeEnd, DateUtil.HHMMSS_DATE_FORMAT));
			}
			//产品编号
			if (null!=monitorCnd.getProductId()) {
				parameters.put("productId",monitorCnd.getProductId());
			}
			
			// 新老查询开关
			String newRule = Constant.getInstance().getProperty("COM_AUDIT_NEW_QUERY");
			if(StringUtils.isEmpty(newRule) || "false".equals(newRule)){
				parameters.put("newRule", null);
			}
			else{
				parameters.put("newRule", "newRule");
			}
			
			
//			Map<String, Object> auditParam = combinationParameter(monitorCnd);//组合查询参数
			//查询订单审核列表集合
			List<ComAudit> comAuditList = orderAuditService.queryAuditListByCriteria(parameters);
			if(comAuditList != null && comAuditList.size() > 0){
				batchTotalNum = comAuditList.size();
			}
			String assignor=getLoginUserId();
			String orderRemark="预定通知已处理(批量处理)";
			batchSucNum = comMessageService.updateBatchReservationListProcessed(comAuditList, assignor, orderRemark);
			batchFailNum = batchTotalNum - batchSucNum;
			message = "完成批量处理成功:" + batchSucNum + ",失败:" + batchFailNum;
			code = "success";
		} catch (Exception e) {
			message="服务器内部异常";
			if(LOGGER.isDebugEnabled()){
				LOGGER.error(message);
			}
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}
		model.addAttribute("code", code);
		model.addAttribute("message", message);
		return queryResultMessage(model,monitorCnd,false,request);
	}
	
	/**
	 * 组合查询参数
	 * @param monitorCnd
	 * @return
	 */
	private Map<String, Object> combinationParameter(OrderMonitorCnd monitorCnd){
		//组装订单审核列表条件
		Map<String, Object> parameters = new HashMap<String, Object>();
		// 过滤系统自动过的
		parameters.put("auditFlag","SYSTEM"); 
		//未处理的活动
		parameters.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		//订单负责人
		if(UtilityTool.isValid(monitorCnd.getOperatorName())){
			parameters.put("operatorName", monitorCnd.getOperatorName());
		}
		//审核类型
		if(UtilityTool.isValid(monitorCnd.getActivityName())){
			String auditType = monitorCnd.getActivityName();
			parameters.put("auditType", auditType);
		}
		//审核子类型
		if(UtilityTool.isValid(monitorCnd.getActivityDetail())){
			String auditSubtype = monitorCnd.getActivityDetail();
			parameters.put("auditSubtype", auditSubtype);
		}
		//下单时间开始
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
			parameters.put("createTimeBegin", DateUtil.getDateByStr(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
		}
		//下单时间结束
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
			parameters.put("createTimeEnd", DateUtil.getDateByStr(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
		}
		//游玩日期开始
		Date visitTimeBegin=monitorCnd.getVisitTimeBegin();
		if (visitTimeBegin!=null) {
			parameters.put("visitTimeBegin",visitTimeBegin );
		}
		//游玩日期结束
		Date visitTimeEnd=monitorCnd.getVisitTimeEnd();
		if (visitTimeEnd!=null) {
			parameters.put("visitTimeEnd",visitTimeEnd );
		}
		//产品编号
		if (null!=monitorCnd.getProductId()) {
			parameters.put("productId",monitorCnd.getProductId());
		}
		return parameters;
	}
}