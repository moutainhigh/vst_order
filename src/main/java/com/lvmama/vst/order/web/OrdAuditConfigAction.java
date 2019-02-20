package com.lvmama.vst.order.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.lvmama.vst.back.biz.po.BusinessRule;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.order.po.OrdAuditAllocation;
import com.lvmama.vst.back.order.po.OrdAuditAllocationRelation;
import com.lvmama.vst.back.order.po.OrdAuditConfig;
import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.OrdAuditConfigInfo;
import com.lvmama.vst.comm.vo.order.OrdFunctionInfo;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IBusinessRuleService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAuditAllocationService;
import com.lvmama.vst.order.service.IOrdAuditConfigService;
import com.lvmama.vst.order.service.IOrdFunctionService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.vo.OrdAuditConfigVo;
import com.lvmama.vst.pet.adapter.PermOrganizationServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 任务分配人员配置action
 * 
 * @author jszhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/ordAuditConfig")
public class OrdAuditConfigAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrdAuditConfigAction.class);

	@Autowired
	private IOrdAuditConfigService ordAuditConfigService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private CategoryClientService  categoryClientService;
	
	@Autowired
	private  IBusinessRuleService businessRuleService;
	
	@Autowired
	private IOrdFunctionService ordFunctionService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapater;
	
	@Autowired
	private IOrdAuditAllocationService ordAuditAllocationService;
	
	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private PermOrganizationServiceAdapter permOrganizationServiceAdapter;
	
	@RequestMapping(value = "/showOrdAuditConfigList")
	public String showOrdAuditConfigList(Model model, HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrdAuditConfigList>");
		}
		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		/*
		String secondDeptId = "";
		for (Object obj : secondDepMap.entrySet()) {
			 Entry entry = (Entry) obj;
			 secondDeptId= (String) entry.getKey();
			 break;
		 }

		Map<String,String> threeDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(secondDeptId));
		*/
		Map<String,String> initMap=new HashMap<String, String>();
		initMap.put("", "请选择");
		
		firstDepMap.remove("");
		//产品类型字典
		List<BizCategory> bizCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		model.addAttribute("bizCategoryList", bizCategoryList);
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", initMap);
		
		//select默认选中值
		OrdAuditConfigInfo ordAuditConfigInfo=new OrdAuditConfigInfo();
		ordAuditConfigInfo.setFirstDepartment(Constants.ORG_ID_CTI+"");//默认呼叫中心 55 orgid
		
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		
		return "/order/ordAuditConfig/findOrdAuditConfigList";
	}
	
	@RequestMapping(value = "/findOrdAuditConfigList")
	public String findOrdAuditConfigList(Model model, Integer page,Integer pageSize,OrdAuditConfigInfo ordAuditConfigInfo, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrdAuditConfigList>");
		}
		Map<String, Object> userParams = new HashMap<String, Object>();
		String operatorName =ordAuditConfigInfo.getOperatorName();
		String threeDepartment=ordAuditConfigInfo.getThreeDepartment();
		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
		if (!StringUtils.isEmpty(operatorName)) {
			userParams.put("userNameEQ", operatorName);
		}
		if (!StringUtils.isEmpty(threeDepartment)) {
			userParams.put("treeDepartmentId", threeDepartment);
		}else if (!StringUtils.isEmpty(secondDepartment)) {
			userParams.put("treeDepartmentId", secondDepartment);
		}else if (!StringUtils.isEmpty(firstDepartment)) {
			userParams.put("treeDepartmentId", firstDepartment);
		}
		
		userParams.put("valid", "Y");
		
		Long countLong=permUserServiceAdapater.queryPermUserByParamCount(userParams);
		
		int count=countLong.intValue();
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(request);
		
		userParams.put("maxResults", pageParam.getEndRows());
		userParams.put("skipResults", pageParam.getStartRows());
		// parameters.put("_orderby","supp.SUPPLIER_ID desc");

		List<PermUser> permUserList = permUserServiceAdapater.queryPermUserByParam(userParams);
		pageParam.setItems(permUserList);
		
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("permUserList", permUserList);
		
		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();
		
		if (!StringUtils.isEmpty(firstDepartment)) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(firstDepartment));
		}
		if (!StringUtils.isEmpty(secondDepartment)) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(secondDepartment));
		}
		
		firstDepMap.remove("");
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		
		return "/order/ordAuditConfig/findOrdAuditConfigList";
	}
	
	/**
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param ordAuditConfigInfo
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/findOrdAuditConfigListNew")
	public String findOrdAuditConfigListNew(Model model, Integer page,Integer pageSize,OrdAuditConfigInfo ordAuditConfigInfo, HttpServletRequest request){
		
		String threeDepartment=ordAuditConfigInfo.getThreeDepartment();
		String secondDepartment=ordAuditConfigInfo.getSecondDepartment();
		String firstDepartment=ordAuditConfigInfo.getFirstDepartment();
		String distributionChannel=ordAuditConfigInfo.getDistributionChannel();
		Long categoryId=ordAuditConfigInfo.getCategoryId();
		Page pageParam =null;
		List<BizCategory> bizCategoryList=null;
		List<BusinessRule> businessRuleList = null;
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			if (null!=categoryId) {
				params.put("categoryId", categoryId);
			}
			if(distributionChannel != null){
				params.put("distributionChannel", distributionChannel);
			}
			/*查询品类Start*/
			 bizCategoryList=categoryClientService.findCategoryByAllValid().getReturnContent();
			/*查询品类 End*/
			 
			//查询业务规则
			ResultHandleT<List<BusinessRule>> resultHandleT=businessRuleService.findBusinessRuleByAllValid();
			businessRuleList=resultHandleT.getReturnContent();
				
			/*查询订单当中操作的方法Start*/
			Map<String, Object> para = new HashMap<String, Object>();
			List<OrdFunction> ordFunctionList=ordFunctionService.findOrdFunctionList(para);
			/*查询订单当中操作的方法 End*/
			
			
			
			List<Long> orgIds = new ArrayList<Long>();
			if (!StringUtils.isEmpty(threeDepartment)) { //选择指定组的情况
				
				params.put("orgId", threeDepartment);
				
			}else if(!StringUtils.isEmpty(secondDepartment)){ //选择指定二级部门的情况
				long second=NumberUtils.toLong(secondDepartment);
				if(second>0){
					orgIds.add(second);
					List<PermOrganization> treeDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(second);
					if(null!=treeDepartmentList&&treeDepartmentList.size()>0){
						for (int i = 0; i < treeDepartmentList.size(); i++) {
							orgIds.add(treeDepartmentList.get(i).getOrgId());
						}
					}
				}
				
			}else if (!StringUtils.isEmpty(firstDepartment)) {  //选择指定一级部门的情况
				long first=NumberUtils.toLong(firstDepartment);
				if(first>0){
					orgIds.add(first);
					
					List<PermOrganization> secondDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(new Long(firstDepartment));
					for (PermOrganization second : secondDepartmentList) {
						orgIds.add(second.getOrgId());
						
						List<PermOrganization> treeDepartmentList=permOrganizationServiceAdapter.getChildPermOrgList(second.getOrgId());
						if(null!=treeDepartmentList&&treeDepartmentList.size()>0){
							for (int i = 0; i < treeDepartmentList.size(); i++) {
								orgIds.add(treeDepartmentList.get(i).getOrgId());
							}
						}
					}
				}
			}
			if(!orgIds.isEmpty()){
				params.put("orgIdArray",orgIds);
			}
			
			//不分页查得到总数
			int totalCount=ordAuditAllocationService.getTotalCount(params);
			int pagenum = page == null ? 1 : page;
			pageParam = Page.page(totalCount, 10, pagenum);
			pageParam.buildUrl(request);
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());
			//分页查询列表
			List<OrdAuditAllocation> list= ordAuditAllocationService.queryOrdAuditAllocationListByParam(params);
			List<OrdAuditConfigVo> resultList=new ArrayList<OrdAuditConfigVo>();
			for (OrdAuditAllocation item : list) {
				OrdAuditConfigVo vo=new OrdAuditConfigVo();
				vo.setOrdAllocationId(item.getOrdAllocationId());
				vo.setThreeDeptId(item.getOrgId());
				vo.setDistributionChannel(item.getDistributionChannel());
				//得到组的部门层级
				List<Map<String, Object>> orgList=permOrganizationServiceAdapter.getOrganizationByChildId(item.getOrgId());
				if(CollectionUtils.isNotEmpty(orgList)){
					for(Map<String, Object> map : orgList){
						Long level = Long.valueOf(String.valueOf(map.get("permLevel")));
						String name = String.valueOf(map.get("departmentName"));
						if(null != level){
							if(level.intValue() == 1){
								vo.setFirstDeptName(name);
							}
							if(level.intValue() == 2){
								vo.setSecondDeptName(name);
							}
							if(level.intValue() == 3){
								vo.setThreeDeptName(name);
							}
						}
					}
				}
				
				//设置品类
				for (BizCategory bizCategory : bizCategoryList) {
					if(bizCategory.getCategoryId().longValue()==item.getCategoryId().longValue()){
						vo.setBizCategory(bizCategory);
						break;
					}
				}
				
				//销售渠道
				
				//业务规则
				for(BusinessRule businessRule : businessRuleList){
					if(item.getBusinessRuleId() != null && (businessRule.getBusinessRuleId().longValue() == item.getBusinessRuleId().longValue())){
						vo.setBusinessRule(businessRule);
						break;
					}
				}
				
				
				//设置操作的方法
				Map<String, Object> functionParams = new HashMap<String, Object>();
				functionParams.put("ordAllocationId", item.getOrdAllocationId());
				List<OrdAuditAllocationRelation> relationList=ordAuditAllocationService.queryOrdAuditAllocationRelationListByParam(functionParams);
				
				List<OrdFunctionInfo> ordFunctionInfoList=new ArrayList<OrdFunctionInfo>();
				for (OrdFunction ordFunction : ordFunctionList) {
					OrdFunctionInfo ordFunctionInfo=new OrdFunctionInfo();
					try {
						BeanUtils.copyProperties(ordFunctionInfo,ordFunction);
					} catch (Exception e) {
						LOG.error(ExceptionFormatUtil.getTrace(e));
					}
					ordFunctionInfo.setChecked("false");
					for (OrdAuditAllocationRelation relation : relationList) {
						if (relation.getOrdFunctionId().longValue()==ordFunctionInfo.getOrdFunctionId().longValue()) {
							ordFunctionInfo.setChecked("true");
							break;
						}
					}
					ordFunctionInfoList.add(ordFunctionInfo);
				}
				
				vo.setOrdFunctionInfoList(ordFunctionInfoList);
				 
				resultList.add(vo);
			}
			
			pageParam.setItems(resultList);
			
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e);
		}
		//保存查询结果
		model.addAttribute("pageParam", pageParam);
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=Collections.emptyMap();
		Map<String,String> threeDepMap=Collections.emptyMap();		
		
		if (!StringUtils.isEmpty(firstDepartment)) {
			secondDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(firstDepartment));
		}
		if (!StringUtils.isEmpty(secondDepartment)) {
			threeDepMap=permOrganizationServiceAdapter.getChildOrgList(new Long(secondDepartment));
		}
		//产品类型字典
		model.addAttribute("bizCategoryList", bizCategoryList);
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", threeDepMap);
		model.addAttribute("ordAuditConfigInfo",ordAuditConfigInfo);
		
		return "/order/ordAuditConfig/findOrdAuditConfigList";
	}
	

	@RequestMapping(value = "/showAddOrdAuditConfig")
	public String showAddOrdAuditConfig(Model model, HttpServletRequest req) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddOrdAuditConfig>");
		}

		ResultHandleT<List<BizCategory>> handleT=categoryClientService.findCategoryByAllValid();
		List<BizCategory> bizCategoryList=handleT.getReturnContent();
		
		ResultHandleT<List<BusinessRule>> resultHandleT=businessRuleService.findBusinessRuleByAllValid();
		List<BusinessRule> businessRuleList=resultHandleT.getReturnContent();
		
		
		/*查询订单当中操作的方法Start*/
		Map<String, Object> para = new HashMap<String, Object>();
		List<OrdFunction> ordFunctionList=ordFunctionService.findOrdFunctionList(para);
		/*查询订单当中操作的方法 End*/
		
		Map<String,String> firstDepMap=permOrganizationServiceAdapter.getOrganizationByLevel(Constants.FIRST_ORG_LEVEL);
		Map<String,String> secondDepMap=permOrganizationServiceAdapter.getChildOrgList(Constants.ORG_ID_CTI);
		
		Map<String,String> initMap=new HashMap<String, String>();
		initMap.put("", "请选择");
		
		//select默认选中值
		OrdAuditConfigVo ordAuditConfigVo=new OrdAuditConfigVo();
		ordAuditConfigVo.setFirstDeptId(Constants.ORG_ID_CTI);//默认呼叫中心 55 orgid
		
		model.addAttribute("ordAuditConfigVo",ordAuditConfigVo);
		
		model.addAttribute("firstDepMap", firstDepMap);
		model.addAttribute("secondDepMap",secondDepMap);
		model.addAttribute("threeDepMap", initMap);
		
		model.addAttribute("ordFunctionList",ordFunctionList);
		model.addAttribute("bizCategoryList", bizCategoryList);
		model.addAttribute("businessRuleList", businessRuleList);
		
		return "/order/ordAuditConfig/inc/addOrdAuditConfig";
	}
	
	@RequestMapping(value = "/saveOrdAuditConfig")
	@ResponseBody
	public Object saveOrdAuditConfig(Model model, OrdAuditAllocation ordAuditAllocation,Long threeDeptId,String ordFunctionIds,HttpServletRequest req) {
		 
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			  if(null==ordAuditAllocation){
				  throw new IllegalArgumentException("请选择订单品类和组");
			  }else if(null==ordAuditAllocation.getCategoryId()){
				  throw new IllegalArgumentException("请选择订单品类");
			  }
			  if(null==threeDeptId){
				  throw new IllegalArgumentException("请选择组");
			  }
			  
			  PermOrganization po= permOrganizationServiceAdapter.getPermOrganization(threeDeptId);
			  if(!(po.getPermLevel()==2||po.getPermLevel()==3)){
				  throw new IllegalArgumentException("目前只支持2-3级的组织");
			  }
			  if(StringUtils.isEmpty(ordFunctionIds)){
				  throw new IllegalArgumentException("请选择可接收的活动");
			  }
			  String[] ordfunctionIdArray= ordFunctionIds.split(",");
			  Long[] functionIdArray=new Long[ordfunctionIdArray.length];
			  for (int i = 0; i < functionIdArray.length; i++) {
				  functionIdArray[i]=Long.valueOf(ordfunctionIdArray[i]);
			  }
			  ordAuditAllocation.setOrgId(threeDeptId);
			  ordAuditAllocationService.saveOrUpdateOrdAuditConfig(ordAuditAllocation, functionIdArray);
			  
			msg.setAttributes(attributes);
			msg.setCode("success");
			msg.setMessage("保存成功");
		}catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("保存失败:"+e.getMessage());
			log.error(e);
		}
		return msg;
	}

	@RequestMapping(value = "/showUpdateOrdAuditConfig")
	public String showUpdateOrdAuditConfig(Model model,Long ordAllocationId,HttpServletRequest req) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateOrdAuditConfig>");
		}
		if(null==ordAllocationId){
			throw new IllegalArgumentException("要修改记录为空");
		}
		
		OrdAuditAllocation ordAuditAllocation=ordAuditAllocationService.findOrdAuditAllocationById(ordAllocationId);
		
		ResultHandleT<List<BizCategory>> handleT=categoryClientService.findCategoryByAllValid();
		List<BizCategory> bizCategoryList=handleT.getReturnContent();
		
		
		/*查询订单当中操作的方法Start*/
		Map<String, Object> para = new HashMap<String, Object>();
		List<OrdFunction> ordFunctionList=ordFunctionService.findOrdFunctionList(para);
		/*查询订单当中操作的方法 End*/
		
		//设置操作的方法
		Map<String, Object> functionParams = new HashMap<String, Object>();
		functionParams.put("ordAllocationId", ordAuditAllocation.getOrdAllocationId());
		List<OrdAuditAllocationRelation> relationList=ordAuditAllocationService.queryOrdAuditAllocationRelationListByParam(functionParams);
		
		List<OrdFunctionInfo> ordFunctionInfoList=new ArrayList<OrdFunctionInfo>();
		for (OrdFunction ordFunction : ordFunctionList) {
			OrdFunctionInfo ordFunctionInfo=new OrdFunctionInfo();
			try {
				BeanUtils.copyProperties(ordFunctionInfo,ordFunction);
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			ordFunctionInfo.setChecked("false");
			for (OrdAuditAllocationRelation relation : relationList) {
				if (relation.getOrdFunctionId().longValue()==ordFunctionInfo.getOrdFunctionId().longValue()) {
					ordFunctionInfo.setChecked("true");
					break;
				}
			}
			ordFunctionInfoList.add(ordFunctionInfo);
		}

		//select默认选中值
		OrdAuditConfigVo ordAuditConfigVo=new OrdAuditConfigVo();
		 for (BizCategory bizCategory : bizCategoryList) {
			if(bizCategory.getCategoryId().longValue()==ordAuditAllocation.getCategoryId().longValue()){
				ordAuditConfigVo.setBizCategory(bizCategory);
			}
		}
		ordAuditConfigVo.setThreeDeptId(ordAuditAllocation.getOrgId());
		ordAuditConfigVo.setOrdFunctionInfoList(ordFunctionInfoList);
		
		//得到组的部门层级
		List<Map<String, Object>> orgList=permOrganizationServiceAdapter.getOrganizationByChildId(ordAuditAllocation.getOrgId());
		if(CollectionUtils.isNotEmpty(orgList)){
			for(Map<String, Object> map : orgList){
				Long level = Long.valueOf(String.valueOf(map.get("permLevel")));
				String name = String.valueOf(map.get("departmentName"));
				Long orgId = Long.valueOf(String.valueOf(map.get("orgId")));
				if(null != level){
					if(level.intValue() == 1){
						ordAuditConfigVo.setFirstDeptName(name);
						ordAuditConfigVo.setFirstDeptId(orgId);
					}
					if(level.intValue() == 2){
						ordAuditConfigVo.setSecondDeptName(name);
						ordAuditConfigVo.setSecondDeptId(orgId);
					}
					if(level.intValue() == 3){
						ordAuditConfigVo.setThreeDeptName(name);
					}
				}
			}
		}
		model.addAttribute("ordAuditConfigVo",ordAuditConfigVo);
		model.addAttribute("bizCategoryList", bizCategoryList);
		model.addAttribute("ordAllocationId",ordAllocationId);
		return "/order/ordAuditConfig/inc/updateOrdAuditConfig";
	}
	
	
	@RequestMapping(value = "/delOrdAuditConfig")
	@ResponseBody
	public Object delOrdAuditConfig(Model model, Long ordAllocationId,HttpServletRequest req) {
		 
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		 try {
			 	if(null==ordAllocationId){
					throw new IllegalArgumentException("要删除记录为空");
				}
			  ordAuditAllocationService.delOrdAuditAllocationById(ordAllocationId);
			  
			msg.setAttributes(attributes);
			msg.setCode("success");
			msg.setMessage("删除成功");
		}catch (Exception e) {
			// TODO: handle exception
			msg.setCode("error");
			msg.setMessage("删除失败:"+e.getMessage());
			log.error(e);
		}
		 return msg;
	}
	
	
	@RequestMapping(value = "/viewOrdAuditConfig")
	public String viewOrdAuditConfig(Model model, HttpServletRequest req) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<viewOrdAuditConfig>");
		}
		String operatorName=req.getParameter("userName");
		
		ResultHandleT<List<BizCategory>> handleT=categoryClientService.findCategoryByAllValid();
		List<BizCategory> bizCategoryList=handleT.getReturnContent();
		
		List<OrdAuditConfigInfo> ordAuditConfigInfoList =new ArrayList<OrdAuditConfigInfo>();
		Map<String, Object> para = new HashMap<String, Object>();
		List<OrdFunction> ordFunctionList=ordFunctionService.findOrdFunctionList(para);
		
		for (BizCategory bizCategory : bizCategoryList) 
		{
			OrdAuditConfigInfo ordAuditConfigInfoObj=new OrdAuditConfigInfo();
			Long  categoryId=bizCategory.getCategoryId();
			String categoryName=bizCategory.getCategoryName();
			
			/*Map<String, Object> param = new HashMap<String, Object>();
			param.put("categoryId",categoryId);
			param.put("operatorName",operatorName);
			List<OrdAuditConfig> ordAuditConfigObjList = ordAuditConfigService.findOrdAuditConfigList(param);
			*/
			List<OrdFunctionInfo>  ordFunctionInfoList=new ArrayList<OrdFunctionInfo>();
			
			for (OrdFunction ordFunction : ordFunctionList) 
			{
				OrdFunctionInfo ordFunctionInfo=new OrdFunctionInfo();
				try {
					BeanUtils.copyProperties(ordFunctionInfo,ordFunction);
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
				} 
				
				Map<String, Object> auditConfigParameters = new HashMap<String, Object>();
				auditConfigParameters.put("operatorName",operatorName);
				auditConfigParameters.put("categoryId",categoryId);
				auditConfigParameters.put("ordFunctionId",ordFunction.getOrdFunctionId());
				
				List<OrdAuditConfig> list = ordAuditConfigService.findOrdAuditConfigList(auditConfigParameters);
				if (list.size()>0) {
					OrdAuditConfig ordAuditConfigObj=list.get(0);
					ordFunctionInfo.setChecked("true");
					ordFunctionInfo.setTaskLimit(ordAuditConfigObj.getTaskLimit());
				}else{
					ordFunctionInfo.setChecked("false");
				}
				ordFunctionInfoList.add(ordFunctionInfo);
			}
			ordAuditConfigInfoObj.setCategoryId(categoryId);
			ordAuditConfigInfoObj.setCategoryName(categoryName);
			ordAuditConfigInfoObj.setOperatorName(operatorName);
			ordAuditConfigInfoObj.setOrdFunctionInfoList(ordFunctionInfoList);
			
			ordAuditConfigInfoList.add(ordAuditConfigInfoObj);
			
		}
		
		model.addAttribute("ordAuditConfigInfoList",ordAuditConfigInfoList);
		
		return "/order/ordAuditConfig/viewOrdAuditConfig";
	}
}
