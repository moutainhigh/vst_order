package com.lvmama.vst.order.confirm.web;

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

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.Confirm_Booking_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.BACK_USER_WORK_STATUS;
import com.lvmama.vst.back.pub.po.ComAuditActiviNum;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.order.NewWorkMonitorStatusVO;
import com.lvmama.vst.comm.vo.order.OrdAuditMonitor;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 工作台任务监控
 * @author renjiangyi
 *
 */
@Controller
@RequestMapping("/ord/order/monitor/")
public class NewOrderConsoleMonitorAction extends BaseActionSupport{
	/**
	 * 日志记录器
	 */
	private static final Log LOGGER = LogFactory.getLog(NewOrderConsoleMonitorAction.class); 
	/**
	 * 默认分页大小
	 */
	private static final Integer DEFAULT_PAGE_SIZE = 10; 
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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@RequestMapping(value = "queryOrderConsoleMonitor")
	public String queryOrderConsoleMonitor(Model model,Integer page,OrdAuditMonitor ordAuditMonitor,HttpServletRequest request,HttpServletResponse response){
		//一级部门默认选中呼叫中心
		ordAuditMonitor.setFirstDepartment(Constants.ORG_ID_CTI+"");
		initQueryForm(model, ordAuditMonitor);
		return "order/orderConsole/NewOrderConsoleMonitor";
	}
	
	/**
	 * 初始化查询表单
	 * @param model
	 * @param ordAuditConfigInfo
	 */
	private void initQueryForm(Model model,OrdAuditMonitor ordAuditMonitor) {
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
		ordAuditMonitor.setStartDateTime(DateUtil.formatDate(DateUtil.getDateAfterMonths(new Date(), -1), DateUtil.HHMMSS_DATE_FORMAT));
		model.addAttribute("ordAuditConfigInfo",ordAuditMonitor);
	}
	
	@RequestMapping(value = "queryOrderConsole")
	public String queryOrderConsole(Model model,Integer page,Integer pageSize,OrdAuditMonitor ordAuditMonitor,HttpServletRequest request,HttpServletResponse response) {
		Map<String, String> workStatusMap = showQueryForm(model, ordAuditMonitor);
		//设置页面关于员工查询的条件
		Map<String, Object> userParams = new HashMap<String, Object>();
		String threeDepartment=ordAuditMonitor.getThreeDepartment();
		String secondDepartment=ordAuditMonitor.getSecondDepartment();
		String firstDepartment=ordAuditMonitor.getFirstDepartment();
		String operatorName =ordAuditMonitor.getOperatorName();
		
		String workStatus = ordAuditMonitor.getWorkStatus();
		
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
		Page<NewWorkMonitorStatusVO> resultPage = Page.page(totalCount.intValue(), currentPageSize, currentPage);
		resultPage.buildUrl(request);
		//查找所有有效的用户
		userParams.put("skipResults", 1);
		userParams.put("maxResults", totalCount.intValue());
		
		LOGGER.info("queryPermUserByParam start...");
		
		//先分页查询员工列表
		List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
		
		LOGGER.info("queryPermUserByParam end...");
		
		//根据接单状态填充并清洗集合
		List<String> operatorNameList=new ArrayList<>();
		List<NewWorkMonitorStatusVO> workStatusVOList = this.fillWorkStatus(workStatus, permUserList,operatorNameList);
		
		
		if(null != workStatusVOList && !workStatusVOList.isEmpty()){
			List<NewWorkMonitorStatusVO> workStatusVOListPage = this.buildResultPage(workStatusVOList, currentPage, currentPageSize);
			//在内存中构建每页的结果,引用重新指向
			//获取用户不同活动数
			fillOperatorActivitCount(operatorNameList, workStatusVOListPage,workStatusMap);
			resultPage.setItems(workStatusVOListPage);
			resultPage.setTotalResultSize(workStatusVOList.size());
			
		}
		
		//保存查询结果
		model.addAttribute("resultPage", resultPage);
		return "order/orderConsole/NewOrderConsoleMonitor";
	}
	
	private void fillOperatorActivitCount(List<String> operatorNameList,
			List<NewWorkMonitorStatusVO> workStatusVOList,Map<String, String> workStatusMap) {
		Map<String, Object> param=new HashMap<>();
		param.put("operatorNameList", operatorNameList);
		List<ComAuditActiviNum> list=orderAuditService.countActivityUnprocessedNum(param);
		for (NewWorkMonitorStatusVO newWorkMonitorStatusVO : workStatusVOList) {
			for (ComAuditActiviNum comAuditActiviNum : list) {
				if(newWorkMonitorStatusVO.getUserName().equals(comAuditActiviNum.getOperatorName())) {
					if(Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {	
						newWorkMonitorStatusVO.setInconfirmAudit((newWorkMonitorStatusVO.getInconfirmAudit()==null?0:newWorkMonitorStatusVO.getInconfirmAudit())+comAuditActiviNum.getActiviNum());
						if(Constants.N_FLAG.equals(comAuditActiviNum.getStockFlag())) {
							newWorkMonitorStatusVO.setInconfirmStockFlagIsN(comAuditActiviNum.getActiviNum());
						}
					}else if(Confirm_Enum.CONFIRM_AUDIT_TYPE.FULL_AUDIT.name().equals(comAuditActiviNum.getAuditType())
							||Confirm_Enum.CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT.name().equals(comAuditActiviNum.getAuditType())
							||Confirm_Enum.CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {
						newWorkMonitorStatusVO.setFullAudit((newWorkMonitorStatusVO.getFullAudit()==null?0:newWorkMonitorStatusVO.getFullAudit())+comAuditActiviNum.getActiviNum());
					}else if(Confirm_Enum.CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {
						newWorkMonitorStatusVO.setCancelConfirmAudit((newWorkMonitorStatusVO.getCancelConfirmAudit()==null?0:newWorkMonitorStatusVO.getCancelConfirmAudit())+comAuditActiviNum.getActiviNum());
					}else if(Confirm_Enum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {
						newWorkMonitorStatusVO.setNewOrderAudit((newWorkMonitorStatusVO.getNewOrderAudit()==null?0:newWorkMonitorStatusVO.getNewOrderAudit())+comAuditActiviNum.getActiviNum());
					}else if(Confirm_Enum.CONFIRM_AUDIT_TYPE.INQUIRY_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {
						newWorkMonitorStatusVO.setInquiryAudit((newWorkMonitorStatusVO.getInquiryAudit()==null?0:newWorkMonitorStatusVO.getInquiryAudit())+comAuditActiviNum.getActiviNum());
					}else if(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_TYPE.CONFIRM_BOOKING_AUDIT.name().equals(comAuditActiviNum.getAuditType())) {
						newWorkMonitorStatusVO.setConfirmBookingAudit((newWorkMonitorStatusVO.getConfirmBookingAudit()==null?0:newWorkMonitorStatusVO.getConfirmBookingAudit())+comAuditActiviNum.getActiviNum());
					}
					if("UNPROCESSED".equals(comAuditActiviNum.getAuditStatus())) {
						newWorkMonitorStatusVO.setSumNum((newWorkMonitorStatusVO.getSumNum()==null?0:newWorkMonitorStatusVO.getSumNum())+comAuditActiviNum.getActiviNum());
					}
				}
			}
			newWorkMonitorStatusVO.setWorkStatus(this.buildWorkStatusCN(newWorkMonitorStatusVO.getWorkStatus(), workStatusMap));
		}
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
	
   /** 在内存中构建一页结果
	 * 由于是跨库查询，页面有本地的过滤条件，应该将远程库的所有结果拿到本地在内存中先过滤再分页
	 * @param workStatusVOList
	 * @param currentPage
	 * @param currentPageSize
	 * @return
	 */
	private List<NewWorkMonitorStatusVO> buildResultPage(List<NewWorkMonitorStatusVO> workStatusVOList,Integer currentPage,Integer currentPageSize){
		List<NewWorkMonitorStatusVO> workStatusVOListForOnePage = Collections.emptyList();
		if(null != workStatusVOList && !workStatusVOList.isEmpty()){
			if(workStatusVOList.size()>currentPage*currentPageSize){
				workStatusVOListForOnePage = new ArrayList<NewWorkMonitorStatusVO>(workStatusVOList.subList((currentPage-1)*currentPageSize, currentPage*currentPageSize));
			}else{
				workStatusVOListForOnePage = new ArrayList<NewWorkMonitorStatusVO>(workStatusVOList.subList((currentPage-1)*currentPageSize, workStatusVOList.size()));
			}
		}
		return workStatusVOListForOnePage;
	}
	
	/**
	 * 填充接单状态
	 * 
	 * @param workStatus
	 * @param workStatusVOList
	 * @return
	 */
	private List<NewWorkMonitorStatusVO> fillWorkStatus(String pageWorkStatus,List<PermUser> permUserList,List<String> operatorNameList){
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
		
		List<NewWorkMonitorStatusVO> workStatusVOList = new ArrayList<NewWorkMonitorStatusVO>();
		for(PermUser permUser:permUserList){
			String userName = permUser.getUserName();
			String currentWorkStatus = workStatusMap.containsKey(userName) ? workStatusMap
					.get(userName)
					: OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name();
			NewWorkMonitorStatusVO workStatusVO = new NewWorkMonitorStatusVO();
			workStatusVO.setUserId(permUser.getUserId());
			workStatusVO.setUserName(permUser.getUserName());
			workStatusVO.setRealName(permUser.getRealName());
			workStatusVO.setDepartmentId(permUser.getDepartmentId());
			workStatusVO.setDepartmentName(permUser.getDepartmentName());
			workStatusVO.setWorkStatus(currentWorkStatus);
			if(currentWorkStatus.equals(pageWorkStatus)|| StringUtil.isEmptyString(pageWorkStatus)){
				workStatusVOList.add(workStatusVO);
				operatorNameList.add(userName);
			}
		}
		
		return workStatusVOList;
	}
	
	/**
	 * 初始化查询过后的界面
	 * 
	 * @param model
	 * @param ordAuditConfigInfo
	 */
	private Map<String, String> showQueryForm(Model model,OrdAuditMonitor ordAuditMonitor) {
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();
		//根据页面条件查询级联字典
		if (!StringUtils.isEmpty(ordAuditMonitor.getFirstDepartment())) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(ordAuditMonitor.getFirstDepartment()));
		}
		if (!StringUtils.isEmpty(ordAuditMonitor.getSecondDepartment())) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(Long.valueOf(ordAuditMonitor.getSecondDepartment()));
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
		model.addAttribute("ordAuditConfigInfo", ordAuditMonitor);
		
		return workStatusMap;
	}
}
