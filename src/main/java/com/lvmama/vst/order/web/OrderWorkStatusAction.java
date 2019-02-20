package com.lvmama.vst.order.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.order.base.jedis.JedisClusterAdapter;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.BACK_USER_WORK_STATUS;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.OrdAuditConfigInfo;
import com.lvmama.vst.comm.vo.order.WorkStatusVO;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 员工工作状态查询
 * 
 * @author jszhangwei
 * @author wenzhengtao
 * 
 */
@Controller
public class OrderWorkStatusAction extends BaseActionSupport {
	/**
	 * 日志记录器
	 */
	private static final Log LOGGER = LogFactory.getLog(OrderWorkStatusAction.class); 
	/**
	 * 默认分页大小
	 */
	private static final Integer DEFAULT_PAGE_SIZE = 10; 
	/**
	 * 员工工作状态查询页面
	 */
	private static final String WORK_STATUS_PAGE = "/order/workStatus/findWorkStatusList";
	/**
	 * 员工组织架构信息显示页面
	 */
	private static final String ORG_INFO_PAGE = "/order/workStatus/showOrganizationInfo";
	
	private static final String WORK_STATUS_LOG_PAGE = "/order/workStatus/findLogList";
	
	/**
	 * 部门业务
	 */
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;
	
	/**
	 * 员工业务
	 */
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;
	
	/**
	 * 员工接单状态业务
	 */
	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	
	/**
	 * 订单审核业务
	 */
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private ComLogClientService comLogClientService;
	
	/**
	 * @Description: redis缓存
	 */
	@Autowired
	private JedisClusterAdapter jedisCluster;
	
	/**
	 * 点击菜单操作
	 * @param model
	 * @param page
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoWorkStatus.do")
	public String intoWorkStatus(Model model,Integer page,OrdAuditConfigInfo ordAuditConfigInfo,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		//一级部门默认选中呼叫中心
		ordAuditConfigInfo.setFirstDepartment(Constants.ORG_ID_CTI+"");
		ordAuditConfigInfo.setUnProcessedOrder("Y");
		initQueryForm(model, ordAuditConfigInfo);
		return WORK_STATUS_PAGE;
	}
	
	/**
	 * 点击查询操作
	 * @param model
	 * @param page
	 * @param ordAuditConfigInfo
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryWorkStatus.do")
	public String queryWorkStatus(Model model,Integer page,Integer pageSize,OrdAuditConfigInfo ordAuditConfigInfo,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		
		Map<String, String> workStatusMap = showQueryForm(model, ordAuditConfigInfo);
		
		//设置页面关于员工查询的条件
		Map<String, Object> userParams = new HashMap<String, Object>();
		String threeDepartment=ordAuditConfigInfo.getThreeDepartment();
		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
		String operatorName =ordAuditConfigInfo.getOperatorName();
		
		String workStatus = ordAuditConfigInfo.getWorkStatus();
		
		if (!StringUtils.isEmpty(threeDepartment)) {
			userParams.put("treeDepartmentId", threeDepartment);
		}else if (!StringUtils.isEmpty(secondDepartment)) {
			userParams.put("treeDepartmentId", secondDepartment);
		}else if (!StringUtils.isEmpty(firstDepartment)) {
			userParams.put("treeDepartmentId", firstDepartment);
		}
		if (!StringUtils.isEmpty(operatorName)) {
			userParams.put("userName", operatorName);
		}
		userParams.put("valid", "Y");
		
		LOGGER.info("queryWorkStatus, firstDepartment:" + firstDepartment
				+ ", secondDepartment:" + secondDepartment
				+ ", threeDepartment:" + threeDepartment + ", operatorName:"
				+ operatorName + ", workStatus:" + workStatus);
		
		LOGGER.info("queryPermUserByParamCount start...");
		//不分页查找到用户总数
		Long totalCount=permUserServiceAdapater.queryPermUserByParamCount(userParams);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("user totalCount=="+totalCount);
		}
		LOGGER.info("queryPermUserByParamCount end, totalCount:" + totalCount);
		//构建分页对象
		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		Page<WorkStatusVO> resultPage = Page.page(totalCount.intValue(), currentPageSize, currentPage);
		resultPage.buildUrl(request);
		
		//设置分页参数
//		userParams.put("skipResults", resultPage.getStartRows());
//		userParams.put("maxResults", resultPage.getEndRows());
		
		//查找所有有效的用户
		userParams.put("skipResults", 1);
		userParams.put("maxResults", totalCount.intValue());
		
		LOGGER.info("queryPermUserByParam start...");
		
		//先分页查询员工列表
		List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
		
		LOGGER.info("queryPermUserByParam end...");
		
		LOGGER.info("fillWorkStatus start...");
		//根据接单状态填充并清洗集合
		List<WorkStatusVO> workStatusVOList = this.fillWorkStatus(workStatus, permUserList);
		
		LOGGER.info("fillWorkStatus end...");
		
		//根据清洗后的集合统计各种工作状态的条数
		Map<String, Integer> statMap = this.countByWorkStatus(workStatusVOList, workStatus);
		
		LOGGER.info("setProcessed start...");
		if(null != workStatusVOList && !workStatusVOList.isEmpty()){
			//在内存中构建每页的结果,引用重新指向
			List<WorkStatusVO> workStatusVOListPage = this.buildResultPage(workStatusVOList, currentPage, currentPageSize);
			/*
			List<String> userNameList = new ArrayList<String>();
			for(WorkStatusVO workStatusVO : workStatusVOListPage){
				userNameList.add(workStatusVO.getUserName());
			}
			
			Map<String, Integer> actCountMap = new HashMap<String, Integer>();
			Map<String, Integer> orderCountMap = new HashMap<String, Integer>();
			
			if(CollectionUtils.isNotEmpty(userNameList)) {
				List<Map<String, Object>> actNumMapList = this.buildGroupActivityNum(userNameList, ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime());
				List<Map<String, Object>> orderNumMapList = this.buildGroupOrderNum(userNameList, ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime());
				
				if(CollectionUtils.isNotEmpty(actNumMapList)) {
					for(Map<String, Object> actNumMap : actNumMapList) {
						actCountMap.put((String)actNumMap.get("OPERATORNAME") + "," + (String)actNumMap.get("AUDITSTATUS"), ((BigDecimal) actNumMap.get("ACTCOUNT")).intValue());
					}
				}
				
				if(CollectionUtils.isNotEmpty(orderNumMapList)) {
					for(Map<String, Object> orderNumMap : orderNumMapList) {
						orderCountMap.put((String)orderNumMap.get("OPERATORNAME") + "," + (String)orderNumMap.get("AUDITSTATUS"), ((BigDecimal) orderNumMap.get("ORDERCOUNT")).intValue());
					}
				}
			}*/
			
			//填充其他字段值
			for(WorkStatusVO workStatusVO : workStatusVOListPage){
				//构建接单状态的中文名称
				workStatusVO.setWorkStatus(this.buildWorkStatusCN(workStatusVO.getWorkStatus(), workStatusMap));
				//构建每页的处理数量
				if("Y".equals(ordAuditConfigInfo.getProcessedAudit())) {
					workStatusVO.setProcessedActivityNum(this.buildActivityNum(workStatusVO.getUserName(), OrderEnum.AUDIT_STATUS.PROCESSED.name(),ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime()));
				}
				if("Y".equals(ordAuditConfigInfo.getProcessedOrder())) {
					workStatusVO.setProcessedOrderNum(this.buildOrderNum(workStatusVO.getUserName(), OrderEnum.AUDIT_STATUS.PROCESSED.name(),ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime()));
				}
				if("Y".equals(ordAuditConfigInfo.getUnProcessedAudit())) {
					workStatusVO.setUnprocessedActivityNum(this.buildActivityNum(workStatusVO.getUserName(), OrderEnum.AUDIT_STATUS.UNPROCESSED.name(),ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime()));
				}
				if("Y".equals(ordAuditConfigInfo.getUnProcessedOrder())) {
					workStatusVO.setUnprocessedOrderNum(this.buildOrderNum(workStatusVO.getUserName(), OrderEnum.AUDIT_STATUS.UNPROCESSED.name(),ordAuditConfigInfo.getStartDateTime(),ordAuditConfigInfo.getEndDateTime()));
				}
//				workStatusVO.setProcessedActivityNum(actCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.PROCESSED.name()) == null ? 0 : actCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.PROCESSED.name()));
//				workStatusVO.setProcessedOrderNum(orderCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.PROCESSED.name()) == null ? 0 : orderCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.PROCESSED.name()));
//				workStatusVO.setUnprocessedActivityNum(actCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.UNPROCESSED.name()) == null ? 0 : actCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.UNPROCESSED.name()));
//				workStatusVO.setUnprocessedOrderNum(orderCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.UNPROCESSED.name()) == null ? 0 : orderCountMap.get(workStatusVO.getUserName() + "," + OrderEnum.AUDIT_STATUS.UNPROCESSED.name()));
			}
			
			LOGGER.info("setProcessed end...");
			
			resultPage.setItems(workStatusVOListPage);
			resultPage.setTotalResultSize(workStatusVOList.size());
			
		}
		
		//保存查询结果
		model.addAttribute("resultPage", resultPage);
		//保存统计结果
		model.addAttribute("onlineNum", statMap.get("onlineNum"));
		model.addAttribute("busyNum", statMap.get("busyNum"));
		model.addAttribute("offlineNum", statMap.get("offlineNum"));
		
		return WORK_STATUS_PAGE;
	}
	
	/**
	 * 异步加载员工部门
	 * 
	 * @param model
	 * @param departmentId
	 * @param request
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/queryDepartment.do")
	public String queryDepartment(Model model,Long departmentId,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		StringBuilder sb = new StringBuilder();
		List<Map<String, Object>> orgList = permOrganizationServiceAdapter.getOrganizationByChildId(departmentId);
		if(null != orgList && !orgList.isEmpty()){
			for(Map<String, Object> map : orgList){
				Long level = Long.valueOf(String.valueOf(map.get("permLevel")));
				String name = String.valueOf(map.get("departmentName"));
				if(null != level){
					if(level.intValue() == 1){
						sb.append("<b>一级部门:</b>"+name+"<br>");
					}
					if(level.intValue() == 2){
						sb.append("<b>二级部门:</b>"+name+"<br>");
					}
					if(level.intValue() == 3){
						sb.append("<b>三级组:</b>"+name);
					}
				}
			}
		}
		model.addAttribute("orgInfo", sb.toString());
		return ORG_INFO_PAGE;
	}
	
	/**
	 * 点击员工工号进入人工分单页面
	 * @param model
	 * @param operatorName 员工工号
	 * @param departmentId 当前部门ID
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoManuDistPage.do")
	public String intoManuDistPage(Model model,String operatorName,Long departmentId,HttpServletResponse response) throws BusinessException{
		Long firstOrgId = null;
		List<Map<String, Object>> orgList = permOrganizationServiceAdapter.getOrganizationByChildId(departmentId);
		if(CollectionUtils.isNotEmpty(orgList)){
			for(Map<String, Object> map : orgList){
				Long level = Long.valueOf(String.valueOf(map.get("permLevel")));
				Long orgId = Long.valueOf(String.valueOf(map.get("orgId")));
				if(null != level && level.intValue() == 1){
					firstOrgId = orgId;
				}
			}
		}
		//默认查员工未处理的活动
		String auditStatus = "UNPROCESSED";
		String url = "/vst_order/order/ordManualDistOrder/findManualDistOrderList.do?operatorName="+operatorName+"&firstDepartment="+firstOrgId+"&auditStatus="+auditStatus;
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			LOGGER.error(ExceptionFormatUtil.getTrace(e));
		}
		return null;
	}
	
	/**
	 * 初始化查询表单
	 * @param model
	 * @param ordAuditConfigInfo
	 */
	private void initQueryForm(Model model,OrdAuditConfigInfo ordAuditConfigInfo) {
		//一级部门
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		firstDepMap.remove("");
		
		//二级部门
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		
		//三级部门
		Map<String,String> threeDepMap=new HashMap<String, String>();
		threeDepMap.put("", "请选择");
		
		//工作状态
		Map<String, String> workStatusMap = new LinkedHashMap<String, String>();
		workStatusMap.put("", "全部");
		for (BACK_USER_WORK_STATUS item : BACK_USER_WORK_STATUS.values()) {
			workStatusMap.put(item.getCode(), item.getCnName());
		}
		
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		model.addAttribute("workStatusMap", workStatusMap);
		ordAuditConfigInfo.setStartDateTime(DateUtil.formatDate(DateUtil.getDateAfterMonths(new Date(), -1), DateUtil.HHMMSS_DATE_FORMAT));
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
	}
	
	/**
	 * 初始化查询过后的界面
	 * 
	 * @param model
	 * @param ordAuditConfigInfo
	 */
	private Map<String, String> showQueryForm(Model model, OrdAuditConfigInfo ordAuditConfigInfo) {
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();
		//根据页面条件查询级联字典
		if (!StringUtils.isEmpty(ordAuditConfigInfo.getFirstDepartment())) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(ordAuditConfigInfo.getFirstDepartment()));
		}
		if (!StringUtils.isEmpty(ordAuditConfigInfo.getSecondDepartment())) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(ordAuditConfigInfo.getSecondDepartment()));
		}
		
		//工作状态
		Map<String, String> workStatusMap = new LinkedHashMap<String, String>();
		workStatusMap.put("", "全部");
		for (BACK_USER_WORK_STATUS item : BACK_USER_WORK_STATUS.values()) {
			workStatusMap.put(item.getCode(), item.getCnName());
		}
		
		firstDepMap.remove("");
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		model.addAttribute("workStatusMap", workStatusMap);
		//回显查询条件
		model.addAttribute("ordAuditConfigInfo", ordAuditConfigInfo);
		
		return workStatusMap;
	}
	
	/**
	 * 构建工作状态显示字段
	 * 
	 * @return
	 */
	private String buildWorkStatusCN(String workStatus,Map<String, String> workStatusMap){
		String workStatusCN = "未知接单状态";
		if(null != workStatusMap && workStatusMap.size()>0){
			workStatusCN = workStatusMap.get(workStatus);
		}
		return workStatusCN;
	}
	
	/**
	 * 根据处理人和审核状态统计活动数量，系统自动过的活动不应该算在内。
	 * 
	 * @param userName
	 * @param auditType
	 * @return
	 */
	private int buildActivityNum(String userName,String auditStatus,String startDateTime,String endDateTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("operatorName", userName);
		param.put("auditStatus", auditStatus);
		param.put("auditFlag", "NO_SYSTEM");
		if(!StringUtils.isEmpty(startDateTime)){
			param.put("startDateTime", DateUtil.getDateByStr(startDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(endDateTime)){
			param.put("endDateTime", DateUtil.getDateByStr(endDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		int count = orderAuditService.countActivityNum(param);
		return count;
	}
	
	/**
	 * 根据处理人和审核状态统计订单数量
	 * 
	 * @param userName
	 * @param auditType
	 * @return
	 */
	private int buildOrderNum(String userName,String auditStatus,String startDateTime,String endDateTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("operatorName", userName);
		param.put("auditStatus", auditStatus);
		if(!StringUtils.isEmpty(startDateTime)){
			param.put("startDateTime", DateUtil.getDateByStr(startDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(endDateTime)){
			param.put("endDateTime", DateUtil.getDateByStr(endDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		param.put("auditFlag", "NO_SYSTEM");
		int count = orderAuditService.countOrderNum(param);
		return count;
	}
	
	/**
	 * 根据处理人和审核状态统计活动数量，系统自动过的活动不应该算在内。
	 * 
	 * @param userName
	 * @param auditType
	 * @return
	 */
	private List<Map<String, Object>> buildGroupActivityNum(List<String> userNameList,String startDateTime,String endDateTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("operatorNameArray", userNameList);
		param.put("auditFlag", "NO_SYSTEM");
		if(!StringUtils.isEmpty(startDateTime)){
			param.put("startDateTime", DateUtil.getDateByStr(startDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(endDateTime)){
			param.put("endDateTime", DateUtil.getDateByStr(endDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		return orderAuditService.countGroupActivityNum(param);
	}
	
	/**
	 * 根据处理人和审核状态统计订单数量
	 * 
	 * @param userName
	 * @param auditType
	 * @return
	 */
	private List<Map<String, Object>> buildGroupOrderNum(List<String> userNameList,String startDateTime,String endDateTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("operatorNameArray", userNameList);
		if(!StringUtils.isEmpty(startDateTime)){
			param.put("startDateTime", DateUtil.getDateByStr(startDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(endDateTime)){
			param.put("endDateTime", DateUtil.getDateByStr(endDateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		param.put("auditFlag", "NO_SYSTEM");
		return orderAuditService.countGroupOrderNum(param);
	}
	
	/**
	 * 填充接单状态
	 * 
	 * @param workStatus
	 * @param workStatusVOList
	 * @return
	 */
	private List<WorkStatusVO> fillWorkStatus(String pageWorkStatus,List<PermUser> permUserList){
		Map<String, String> workStatusMap = new HashMap<String, String>();
		if(CollectionUtils.isNotEmpty(permUserList)) {
			List<String> userNameList = new ArrayList<String>();
			List<OrdAuditUserStatus> userStatusist =new ArrayList<OrdAuditUserStatus>();
			int idx = 0;
			while(idx < permUserList.size()) {
				userNameList.add(permUserList.get(idx).getUserName());
				if((idx > 0 && idx % 100 == 0) || idx == permUserList.size() -1) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("operatorNameArray", userNameList);
					userStatusist.addAll(orderAuditUserStatusService.findOrdAuditUserStatusList(params));
					userNameList = new ArrayList<String>();
				}
				idx ++;
			}
			
			for(OrdAuditUserStatus userStatus : userStatusist) {
				workStatusMap.put(userStatus.getOperatorName(), userStatus.getUserStatus());
			}
		}
		
		
		List<WorkStatusVO> workStatusVOList = new ArrayList<WorkStatusVO>();
		if(UtilityTool.isValid(pageWorkStatus)){
			for(PermUser permUser:permUserList){
				String userName = permUser.getUserName();
				String currentWorkStatus = workStatusMap.containsKey(userName) ? workStatusMap
						.get(userName)
						: OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
				WorkStatusVO workStatusVO = new WorkStatusVO();
				workStatusVO.setUserId(permUser.getUserId());
				workStatusVO.setUserName(permUser.getUserName());
				workStatusVO.setRealName(permUser.getRealName());
				workStatusVO.setDepartmentId(permUser.getDepartmentId());
				workStatusVO.setDepartmentName(permUser.getDepartmentName());
				workStatusVO.setWorkStatus(currentWorkStatus);
				if(pageWorkStatus.equals(currentWorkStatus)){
					workStatusVOList.add(workStatusVO);
				}
			}
		}else{
			for(PermUser permUser:permUserList){
				String userName = permUser.getUserName();
				String currentWorkStatus = workStatusMap.containsKey(userName) ? workStatusMap
						.get(userName)
						: OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
				WorkStatusVO workStatusVO = new WorkStatusVO();
				workStatusVO.setUserId(permUser.getUserId());
				workStatusVO.setUserName(permUser.getUserName());
				workStatusVO.setRealName(permUser.getRealName());
				workStatusVO.setDepartmentId(permUser.getDepartmentId());
				workStatusVO.setDepartmentName(permUser.getDepartmentName());
				workStatusVO.setWorkStatus(currentWorkStatus);
				workStatusVOList.add(workStatusVO);
			}
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
			return ordAuditUserStatus.getUserStatus();
		}else{
			return OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
		}
	}
	
	/**
	 * 在内存中构建一页结果
	 * 由于是跨库查询，页面有本地的过滤条件，应该将远程库的所有结果拿到本地在内存中先过滤再分页
	 * @param workStatusVOList
	 * @param currentPage
	 * @param currentPageSize
	 * @return
	 */
	private List<WorkStatusVO> buildResultPage(List<WorkStatusVO> workStatusVOList,Integer currentPage,Integer currentPageSize){
		List<WorkStatusVO> workStatusVOListForOnePage = Collections.emptyList();
		if(null != workStatusVOList && !workStatusVOList.isEmpty()){
			if(workStatusVOList.size()>currentPage*currentPageSize){
				workStatusVOListForOnePage = new ArrayList<WorkStatusVO>(workStatusVOList.subList((currentPage-1)*currentPageSize, currentPage*currentPageSize));
			}else{
				workStatusVOListForOnePage = new ArrayList<WorkStatusVO>(workStatusVOList.subList((currentPage-1)*currentPageSize, workStatusVOList.size()));
			}
		}
		return workStatusVOListForOnePage;
	}
	
	/**
	 * 根据清洗后的集合统计各种状态的条数
	 * 
	 * @param workStatusVOList 已经是清洗后的集合
	 * @param workStatus 当前页面选择的工作状态
	 * @return
	 */
	private Map<String, Integer> countByWorkStatus(List<WorkStatusVO> workStatusVOList,String workStatus){
		Map<String, Integer> statMap = new HashMap<String, Integer>();
		if(null != workStatusVOList && !workStatusVOList.isEmpty()){
			if(UtilityTool.isValid(workStatus)){
				if(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name().equals(workStatus)){
					statMap.put("onlineNum",workStatusVOList.size());
					statMap.put("busyNum", 0);
					statMap.put("offlineNum", 0);
				}
				/*else if(OrderEnum.BACK_USER_WORK_STATUS.BUSY.name().equals(workStatus)){
					statMap.put("onlineNum",0);
					statMap.put("busyNum", workStatusVOList.size());
					statMap.put("offlineNum", 0);
				}*/
				else if(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name().equals(workStatus)){
					statMap.put("onlineNum",0);
					statMap.put("busyNum", 0);
					statMap.put("offlineNum", workStatusVOList.size());
				}
			}else{
				int onlineNum = 0;
				int busyNum = 0;
				int offlineNum = 0;
				for(WorkStatusVO workStatusVO:workStatusVOList){
					String status = workStatusVO.getWorkStatus();
					if(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name().equals(status)){
						onlineNum++;
					}
					/*else if(OrderEnum.BACK_USER_WORK_STATUS.BUSY.name().equals(status)){
						busyNum++;
					}*/
					else if(OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name().equals(status)){
						offlineNum++;
					}
				}
				statMap.put("onlineNum",onlineNum);
				statMap.put("busyNum", busyNum);
				statMap.put("offlineNum", offlineNum);
			}
		}else{
			statMap.put("onlineNum", 0);
			statMap.put("busyNum", 0);
			statMap.put("offlineNum", 0);
		}
		return statMap;
	}
	
	/**
	 * 设置员工接单状态
	 * @param sendId
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/updateAuditUserWorkStatus.do")
	@ResponseBody
	public Object updateAuditUserWorkStatus(Model model,String operatorName,Boolean onlineFlag) throws BusinessException {
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	//参数验证
			 if(StringUtil.isEmptyString(operatorName)){
				 throw new IllegalArgumentException("员工姓名为空");
			 }
			 OrdAuditUserStatus old=orderAuditUserStatusService.selectByPrimaryKey(operatorName);
			 PermUser user = permUserServiceAdapater.getPermUserByUserName(operatorName);
			 if(user==null){
				 throw new NullPointerException("员工不存在");
			 }
		 
			 if(onlineFlag&&null==old){
				 OrdAuditUserStatus auditUserStatusNew = new OrdAuditUserStatus();
				 auditUserStatusNew.setOperatorName(operatorName);
				 auditUserStatusNew.setUserStatus(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name());
				 auditUserStatusNew.setOrgId(user.getDepartmentId());
				 auditUserStatusNew.setCreateTime(new Date());
				 orderAuditUserStatusService.insert(auditUserStatusNew); 
				 addLog(user,true,getLoginUserId());
			 }else if(onlineFlag&&null!=old){
				 old.setUserStatus(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name());
				 old.setOrgId(user.getDepartmentId());
				 orderAuditUserStatusService.updateByPrimaryKey(old); 
				 addLog(user,true,getLoginUserId());
			 }else if(!onlineFlag&&null!=old){
				 orderAuditUserStatusService.deleteByPrimaryKey(operatorName);
				 addLog(user,false,getLoginUserId());
			 }
			 
			this.updateOperatorCacheStatus(operatorName,onlineFlag);
			
			attributes.put("operatorName",operatorName);
			 	
			msg.setAttributes(attributes);
			msg.setCode("success");
			msg.setMessage("设置成功");
		}catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("设置失败:"+e.getMessage());
			log.error(e);
		}
		 return msg;
	}
	
	
	/** 
	 * @Title: updateOperatorCacheStatus 
	 * @Description: 更新客服接单状态的缓存
	 * @param operatorName 客服
	 * @param onlineFlag 在线状态
	 */
	private void updateOperatorCacheStatus(String operatorName, Boolean onlineFlag) {
		if(jedisCluster.exists("ALLOCATION_USER_ONLINE_STATUS")) {
			String status = onlineFlag ? OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name() : OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
			jedisCluster.hset("ALLOCATION_USER_ONLINE_STATUS", operatorName, status); 
		}
	}

	private void addLog(PermUser pu,boolean online,String operatorName){
		String content="";
		if(online){
			content = "更改为可接单";
		}else{
			content = "更改为不可接单";
		}
		comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.PERM_USER, pu.getUserId(), pu.getUserId(),
				operatorName, content, ComLog.COM_LOG_LOG_TYPE.PERM_USER_ORDER_WORK_STATUS.getCode(), ComLog.COM_LOG_LOG_TYPE.PERM_USER_ORDER_WORK_STATUS.getCnName(), "");
	}
	
	@RequestMapping("/ord/order/workStatus/logList.do")
	public String showlogList(Model model,String userNo,String objectType,Integer page,HttpServletRequest req){
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("objectType", objectType);
		PermUser permUser = permUserServiceAdapater.getPermUserByUserName(userNo);
		Long objectId = -1L;
		if(permUser != null){
			objectId = permUser.getUserId();
		}
		parameters.put("objectId",objectId);
		int count = comLogClientService.getTotalCount(parameters).getReturnContent().intValue();
		
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildJSONUrl(req);
		//pageParam.buildJSONUrl(req,true);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		parameters.put("_orderby","Com_Log.create_time desc");
		List<ComLog> logList=comLogClientService.queryComLogListByCondition(parameters).getReturnContent();
		pageParam.setItems(logList);

		model.addAttribute("pageParam", pageParam);
		
		model.addAttribute("logList", logList);
		return WORK_STATUS_LOG_PAGE;
	}
}
