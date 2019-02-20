package com.lvmama.vst.order.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.intentionOrder.po.IntentionOrder;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_INTENTION;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.OrderIntentionCnd;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderIntentionService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
/**
 * 意向单管理
 * @author chenpingfan
 *
 */
@Controller
public class OrderIntentionAction extends BaseActionSupport{

	private static final long serialVersionUID = -3986800113106925787L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderIntentionAction.class);
	
	//订单意向页面地址
	private static final String ORDER_INTENTION_PAGE = "/order/query/orderIntentionQueryList";
	
	//订单详细信息页面
	private static final String ORDER_INTENTION_DETAIL = "/order/orderStatusManage/allCategory/orderIntentionDetails";
	
	//意向单状态修改页面
	private static final String ORDER_INTENETION_UPDATE_STATE = "/order/orderStatusManage/allCategory/updateIntentionState";
	
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10; 
	
	private final String UPDATE_FORM_INDEX = "index";
	
	private final String UPDATE_FORM_DETAIL = "detail";
	
	@Autowired
	private IOrderIntentionService orderIntentionService;
	
	@Resource
	private ComLogClientService comLogClientServiceRemote;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	
	/**
	 * 订单意向查询页入口
	 * @param model
	 * @param orderMonitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoIntention.do")
	public String intoIntention(Model model,HttpServletRequest request) throws BusinessException{
		//将初始条件返回给查询页
		initQueryForm(model, request);
		OrderIntentionCnd intentionCnd = new OrderIntentionCnd();
		model.addAttribute("intentionCnd", intentionCnd);
		model.addAttribute("isDefaultStatus",true);
		return ORDER_INTENTION_PAGE;	
	}
	
	/**
	 * 查询意图订单列表
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param orderMonitorCnd
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/ord/order/queryIntentionOrderList.do")
	public String queryIntentionOrderList(Model model,Integer page,Integer pageSize,OrderIntentionCnd intentionCnd,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		initQueryForm(model, request);
		Integer currentPageSize = pageSize == null? DEFAULT_PAGE_SIZE : pageSize;
		Map<String, Object> param = getParamByCnd(intentionCnd);
		Integer count = orderIntentionService.getTotalCount(param);
		
		//构建分页对象
		Page pageParam = buildResultPage(page, currentPageSize, count, request);
		param.put("_start", pageParam.getStartRows());
		param.put("_end", pageParam.getEndRows());
		param.put("_orderby", "CREATE_TIME");
		
		//查询意向单
		List<IntentionOrder> IntentionOrderList = orderIntentionService.queryIntentionsByCriteria(param);
		for(IntentionOrder intentionOrder:IntentionOrderList){
			if(null !=StringUtils.trimToNull(intentionOrder.getLoginName())){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userName", intentionOrder.getLoginName());
				List <UserUser> userList =userUserProxyAdapter.getUsers(map);
				if(userList!=null&&userList.size()>0){
				intentionOrder.setLoginId(userList.get(0).getUserNo());
				}
			}
		}
		transformView(IntentionOrderList);
		
		//设置当前页面显示数据大小
		pageParam.setPageSize(currentPageSize);
		//存储分页结果
		pageParam.setItems(IntentionOrderList);
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("intentionCnd", intentionCnd);
		return ORDER_INTENTION_PAGE;		
	}
	
	/**
	 * 根据意向单ID查询详细信息
	 * @param model
	 * @param orderId
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/toIntentionDetail.do")
	public ModelAndView queryIntentionOrder(HttpServletRequest req,HttpServletResponse response)throws BusinessException{
		String intentionId = req.getParameter("intentionId");
		ModelAndView modelview = new ModelAndView(ORDER_INTENTION_DETAIL);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(IOrderIntentionService.ORD_NO, intentionId);
		List<IntentionOrder> intenetions =orderIntentionService.queryIntentionsByCriteria(param);
		transformView(intenetions);
		IntentionOrder intentionOrder = intenetions.get(0);
		if( StringUtils.isNotEmpty(intentionOrder.getLoginName())){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userName", intentionOrder.getLoginName());
			List <UserUser> userList =userUserProxyAdapter.getUsers(map);
			if(userList!=null&&userList.size()>0){
			intentionOrder.setLoginId(userList.get(0).getUserNo());
			}
		}
		modelview.addObject("intentionOrder", intentionOrder);
		return modelview;	
	}
	
	
	
	/**
	 * 初始化表单值
	 * @param model
	 * @param request
	 * @throws BusinessException
	 */
	private void initQueryForm(Model model,HttpServletRequest request) throws BusinessException{
		//意向单状态Map
		Map<String, String> IntentionStatusMap = new HashMap<String, String>();
		IntentionStatusMap.put("", "全部");
		for (ORDER_INTENTION item : ORDER_INTENTION.values()) {
			IntentionStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("IntentionStatusMap", IntentionStatusMap);
	}
	
	
	/**
	 * 组装分页对象
	 * 
	 * @author wenzhengtao
	 * @param model
	 * @param currentPage
	 * @param count
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page buildResultPage(Integer currentPage, Integer pageSize, Integer count, HttpServletRequest request) {
		// 如果当前页是空，默认为1
		Integer currentPageTmp = currentPage == null ? 1 : currentPage;
		// 从配置文件读取分页大小
		Integer defaultPageSize = DEFAULT_PAGE_SIZE;
		Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
		// 构造分页对象
		Page page = Page.page(count, pageSizeTmp, currentPageTmp);
		// 构造分页URL
		page.buildUrl(request);
		return page;
	}
	
	/**
	 * 根据页面参数条件拼装查询Map
	 * @param intentionCnd
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private Map<String, Object> getParamByCnd(OrderIntentionCnd intentionCnd){
		Map<String, Object> param = new HashMap<String, Object>();
		if(null != intentionCnd.getIntentionId()){
			param.put(IOrderIntentionService.ORD_NO, intentionCnd.getIntentionId());
		}
		if(null !=intentionCnd.getCreateTimeBegin()){
			param.put("createTimeBegin", intentionCnd.getCreateTimeBegin());
		}
		if(null != intentionCnd.getCreateTimeEnd()){
			Date endDate = intentionCnd.getCreateTimeEnd();
			endDate.setHours(23);
			endDate.setMinutes(59);
			endDate.setSeconds(59);
			param.put("createTimeEnd",endDate);
		}
		if(null != StringUtils.trimToNull(intentionCnd.getState())){
			//数据库被设计成用 “0”"1" "2" 来表示状态，只能在这里转化
			if(intentionCnd.getState().equals(ORDER_INTENTION.AWAIT.getCode())){
				param.put("state", "0");
			}	
			if(intentionCnd.getState().equals(ORDER_INTENTION.OVERBOOK.getCode())){
				param.put("state", "1");
			}	
			if(intentionCnd.getState().equals(ORDER_INTENTION.CANCEL.getCode())){
				param.put("state", "2");
			}	
		}
		if(null != intentionCnd.getProductId()){
			param.put("productId", intentionCnd.getProductId());
		}
		if(null != StringUtils.trimToNull(intentionCnd.getProductName())){
			param.put("productName", intentionCnd.getProductName());
		}
		if(null != StringUtils.trimToNull(intentionCnd.getProductManager())){
			param.put("productManager", intentionCnd.getProductManager());
		}
		if(null !=StringUtils.trimToNull(intentionCnd.getContactName())){
			param.put("contactName", intentionCnd.getContactName());
		}
		if(null != StringUtils.trimToNull(intentionCnd.getEmail())){
			param.put("email", intentionCnd.getEmail());
		}
		if(null != StringUtils.trimToNull(intentionCnd.getPhoneNumber())){
			param.put("phoneNumber", intentionCnd.getPhoneNumber());
		}
		return param;
		
	}
	
	/**
	 * 转化属性的格式为页面显示所用
	 * @param IntentionOrders
	 */
	private void transformView(List<IntentionOrder> intentionOrders){
		for (IntentionOrder intentionOrder : intentionOrders) {
			if(null != intentionOrder.getCreateTime()){
				intentionOrder.setCreateTimeStr(DateUtil.getFormatDate(intentionOrder.getCreateTime(),DateUtil.HHMM_DATE_FORMAT));
			}
			if(null !=intentionOrder.getTravelTime()){
				intentionOrder.setTravelTimeStr(DateUtil.getFormatDate(intentionOrder.getTravelTime(), DateUtil.HHMM_DATE_FORMAT));
			}
			if(null !=StringUtils.trimToNull(intentionOrder.getState())){
				if(intentionOrder.getState().equals("0")){
					intentionOrder.setStateView("待跟进");
				}
				if(intentionOrder.getState().equals("1")){
					intentionOrder.setStateView("已下单");
				}
				if(intentionOrder.getState().equals("2")){
					intentionOrder.setStateView("已取消");
				}
			}
		}
	} 
	
	
	/**
	 * 显示意向单状态修改页面
	 * @param intentionOrderId
	 * @return
	 */
	@RequestMapping("/ord/order/showUpdateIntentionState")
	public String showUpdateIntentionState(Model model,@RequestParam("intentionOrderId") String intentionOrderId){
		model.addAttribute("intentionOrderId", intentionOrderId);
		return ORDER_INTENETION_UPDATE_STATE;	
	} 
	
	/**
	 * 修改意向单状态
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/ord/order/updateIntentionState")
	public Map<String, String> updateIntentionState(HttpServletResponse response,HttpServletRequest req){
		//定义返回结果集
		Map<String, String> result = new HashMap<String, String>();
		String intentionOrderId = req.getParameter("intentionOrderId");
		String intentionState = req.getParameter("intentionState");
		String cancelReason = req.getParameter("cancelReason");
		String intentionRemark = req.getParameter("intentionRemark");
		//判断是首页还是详细页跳转
		String updateState = req.getParameter("updateState");		
		IntentionOrder intentionOrder = new IntentionOrder();
		//获取意向单ID失败，不更新
		if(StringUtils.isEmpty(intentionOrderId)){
			result.put("code", "falied");
			result.put("message", "更新失败！");
			return result;
		}		
		if(UPDATE_FORM_INDEX.equals(updateState) && StringUtils.isEmpty(intentionState)){
			result.put("code", "falied");
			result.put("message", "无需更新！");
			return result;
		}else if(UPDATE_FORM_DETAIL.equals(updateState)){
			if(StringUtils.isEmpty(intentionState) && StringUtils.isEmpty(intentionRemark)){
				result.put("code", "falied");
				result.put("message", "无需更新！");
				return result;
			}
		}
		intentionOrder.setIntentionOrderId(Long.valueOf(intentionOrderId));			
		intentionOrder.setState(intentionState);
		intentionOrder.setRemark(intentionRemark);
		 if(intentionState.equals("0")){
			result.put("stateView", ORDER_INTENTION.AWAIT.getCnName());
			intentionOrder.setStateView(ORDER_INTENTION.AWAIT.getCnName());
		}else if(intentionState.equals("1")){
			result.put("stateView", ORDER_INTENTION.OVERBOOK.getCnName());
			intentionOrder.setStateView(ORDER_INTENTION.OVERBOOK.getCnName());
		}else if(intentionState.equals("2")){
			//当状态为已取消的时候设置取消原因
			if(!StringUtils.isEmpty(cancelReason)){
				intentionOrder.setCancelReasonFlag(cancelReason);					
			}
			result.put("stateView", ORDER_INTENTION.CANCEL.getCnName());
			intentionOrder.setStateView(ORDER_INTENTION.CANCEL.getCnName());
		}			
	
		//获取操作用户
		PermUser user = (PermUser)ServletUtil.getSession(req, response,"SESSION_BACK_USER");				
		int updateCount = orderIntentionService.updateIntention(intentionOrder);
		if(updateCount>0){
			result.put("code", "success");
			result.put("message", "更新成功");
		}else{
			result.put("code", "falied");
			result.put("message", "更新失败");
		}
		return result;	
	}
	
	/**
	 * 查询意向单日志
	 * @param model
	 * @param page
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ord/order/findIntentionComLogList")
	public String findComLogList(Model model , Integer page , HttpServletRequest request){
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("start method<findComLogList>");
		}
		long count = 0;
		List<ComLog> comLogs = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String objectId =request.getParameter("objectId");	
		String objectType = ComLog.COM_LOG_OBJECT_TYPE.INTENTION_ORDER_UPDATE.name();
		
		paramMap.put("objectId",Long.valueOf(objectId));
		paramMap.put("objectType", objectType);
		ResultHandleT<Long> resultHandleT = comLogClientServiceRemote.getTotalCount(paramMap);
		if(resultHandleT.isSuccess()){
			count = resultHandleT.getReturnContent();
		}
		//构建分页查询
		long pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(request);
		paramMap.put("_start", pageParam.getStartRows());
		paramMap.put("_end", pageParam.getEndRows());
		paramMap.put("_orderby", "LOG_ID,CREATE_TIME"); 
		paramMap.put("_order", "DESC");
		
		ResultHandleT<List<ComLog>> resultHandleTList = comLogClientServiceRemote.queryComLogListByCondition(paramMap);
		if (resultHandleTList.isSuccess()) {
			comLogs = resultHandleTList.getReturnContent();
		}
		pageParam.setItems(comLogs);
		model.addAttribute("pageParam", pageParam);
		return "/order/orderStatusManage/allCategory/findComLogList";
	} 

}
