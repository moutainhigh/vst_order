package com.lvmama.vst.order.web;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.order.service.IOrderAuditService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.order.po.ComAuditSortRule;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.Confirm_Enum.ARRIVE_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderAuditSortRuleService;

/**
 * 项目名称：vst_order
 * 类名称：OrderAuditSortRuleAction
 * 类描述：订单排序规则action
 * 创建人：majunli
 * 创建时间：2016-10-15 下午3:31:05
 * 修改人：majunli
 * 修改时间：2016-10-15 下午3:31:05
 * 修改备注：
 */
@Controller
@RequestMapping("/ord/order/confirm")
public class OrderAuditSortRuleAction extends BaseActionSupport{
	
	private static final long serialVersionUID = 9127624491238626561L;

	//日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderAuditSortRuleAction.class);
	
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 20; 
	
	@Autowired
	private IOrderAuditSortRuleService orderAuditSortRuleService;
	
	@Autowired
    private BizBuEnumClientService bizBuEnumClientService;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	/**
	 * 查询订单排序规则
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param req
	 * @return
	 * @author majunli
	 * @date 2016-10-19 上午9:48:43
	 */
	@RequestMapping(value = "/queryAuditSortRuleList.do")
	public String queryAuditSortRuleList(Model model, Integer page, Integer pageSize,HttpServletRequest req){
		Map<String, Object> params = new HashMap<String, Object>();
		int count = orderAuditSortRuleService.getTotalCount(params);
		
 		int currentPage = page == null ? 1 : page;
		int currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		
		Page<ComAuditSortRule> pageData = Page.page(count, currentPageSize, currentPage);
		pageData.buildUrl(req);
		
		params.put("_start", pageData.getStartRows());
		params.put("_end", pageData.getEndRows());
		params.put("_orderby", "COM_AUDIT_SORT_RULE.SORT_RULE_ID asc"); 
		List<ComAuditSortRule> auditList = orderAuditSortRuleService.queryComAuditSortRuleListByParam(params);
		pageData.setItems(auditList);
		model.addAttribute("pageData", pageData);
		//所有bu
		Map<String, String> allBuMap = new LinkedHashMap<String, String>();
		ResultHandleT<List<BizBuEnum>> resultHandleT = bizBuEnumClientService.getAllBizBuEnumList();
		if(resultHandleT.isSuccess()){
			List<BizBuEnum> allBuList = resultHandleT.getReturnContent();
			for (BizBuEnum bizBuEnum : allBuList) {
				allBuMap.put(bizBuEnum.getCode(), bizBuEnum.getCnName());
			}
        }
		model.addAttribute("allBuMap", allBuMap);
		return "/order/confirm/orderAuditSortRuleList";
	}
	
	/**
	 * 显示编辑对话框
	 * @param model
	 * @param sortRuleId
	 * @param request
	 * @param response
	 * @return
	 * @author majunli
	 * @date 2016-10-19 上午9:48:53
	 */
	@RequestMapping(value = "/showEditAuditSortRuleDialog.do")
	public String showEditAuditSortRuleDialog(Model model, String sortRuleId,HttpServletRequest request, HttpServletResponse response){
		try {
			if(StringUtil.isNotEmptyString(sortRuleId)){				
				ComAuditSortRule auditSortRule = orderAuditSortRuleService
						.selectComAuditSortRuleByPrimaryKey(Long.parseLong(sortRuleId));
				if(auditSortRule!=null){
					model.addAttribute("auditSortRule", auditSortRule);
				}else{
					model.addAttribute("auditSortRule", new ComAuditSortRule());
				}
			}else{
				model.addAttribute("auditSortRule", new ComAuditSortRule());
			}
		} catch (Exception e) {
			LOGGER.error("OrderAuditSortRuleAction showEditAuditSortRuleDialog error!msg:" + e.getMessage());
			e.printStackTrace();
		}
		return "/order/confirm/edit_auditSortRule_dialog";
	}
	
	/**
	 * 编辑订单排序规则
	 * @param model
	 * @param auditSortRule
	 * @param request
	 * @param response
	 * @return
	 * @author majunli
	 * @date 2016-10-19 上午9:48:58
	 */
	@RequestMapping(value = "/editAuditSortRule.do")
	@ResponseBody
	public Object editAuditSortRule(Model model, ComAuditSortRule auditSortRule,HttpServletRequest request, HttpServletResponse response){
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setCode(ResultMessage.SUCCESS);
		try {
			if (auditSortRule != null
					&& StringUtil.isNotEmptyString(auditSortRule.getSortRuleName())) {
				String operateName = getLoginUserId();
				ComAuditSortRule comAuditSortRule = null;
				if(auditSortRule.getSortRuleId() != null){
					comAuditSortRule = orderAuditSortRuleService
							.selectComAuditSortRuleByPrimaryKey(auditSortRule.getSortRuleId());
				}
				
				auditSortRule.setObjectType(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name());
				auditSortRule.setOperateName(operateName);
				if(auditSortRule.getRemindTime() != null){
					//分钟转换为秒
					auditSortRule.setRemindTime(auditSortRule.getRemindTime() * 60);
				}
				if (comAuditSortRule == null) {
					Date date = new Date();
					auditSortRule.setCreateTime(date);
					auditSortRule.setUpdateTime(date);
					orderAuditSortRuleService
							.saveComAuditSortRule(auditSortRule);
			        //添加操作日志
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.AUDIT_SORT_RULE,
							auditSortRule.getSortRuleId(), 
							auditSortRule.getSortRuleId(), 
							operateName, 
							"新增分组ID为["+auditSortRule.getSortRuleId()+"]的排序规则", 
							ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.name(), 
							ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.getCnName()+"新增排序规则",
							"");
					LOGGER.info("OrderAuditSortRuleAction editAuditSortRule saveComAuditSortRule sortRuleId:"
							+ auditSortRule.getSortRuleId());
				} else {
					if (auditSortRule.getSortRuleId() != null && auditSortRule.getSortRuleId().equals(
						comAuditSortRule.getSortRuleId())) {
						auditSortRule.setUpdateTime(new Date());
						orderAuditSortRuleService
								.updateComAuditSortRuleByPrimaryKey(auditSortRule);
						String logContent = getChangeRecord(comAuditSortRule, auditSortRule);
						//添加操作日志
						lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.AUDIT_SORT_RULE,
								auditSortRule.getSortRuleId(), 
								auditSortRule.getSortRuleId(), 
								operateName, 
								"将分组ID为["+auditSortRule.getSortRuleId()+"]的排序规则，更新了："+ logContent, 
								ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.name(), 
								ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.getCnName()+"更新排序规则",
								"");
						LOGGER.info("OrderAuditSortRuleAction editAuditSortRule updateComAuditSortRule sortRuleId:"
								+ auditSortRule.getSortRuleId());
					}
				}
			}else{
				msg.setCode(ResultMessage.ERROR);
				msg.setMessage("参数不能为空");
			}
		} catch (Exception e) {
			LOGGER.error("OrderAuditSortRuleAction editAuditSortRule error!msg:" + e.getMessage());
			msg.setCode(ResultMessage.ERROR);
			msg.setMessage("运行出现异常");
			e.printStackTrace();
		}
		return msg;
	}
	
	/**
	 * 更新订单排序规则是否有效
	 * @param model
	 * @param sortRuleId
	 * @param valid
	 * @param request
	 * @param response
	 * @return
	 * @author majunli
	 * @date 2016-10-19 上午9:49:11
	 */
	@RequestMapping(value = "/updateAuditSortRuleValid.do")
	@ResponseBody
	public Object updateAuditSortRuleValid(Model model, Long sortRuleId, String valid, HttpServletRequest request, HttpServletResponse response){
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setCode(ResultMessage.SUCCESS);
		try {
			if (sortRuleId != null && StringUtil.isNotEmptyString(valid)
						&& (valid.equals("Y") || valid.equals("N"))) {
				ComAuditSortRule comAuditSortRule = orderAuditSortRuleService
						.selectComAuditSortRuleByPrimaryKey(sortRuleId);
				if(comAuditSortRule != null){
					String operateName = getLoginUserId();
					comAuditSortRule.setValid(valid);
					comAuditSortRule.setOperateName(operateName);
					comAuditSortRule.setUpdateTime(new Date());
					orderAuditSortRuleService.updateComAuditSortRuleByPrimaryKeySelective(comAuditSortRule);
					//添加操作日志
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.AUDIT_SORT_RULE,
							comAuditSortRule.getSortRuleId(), 
							comAuditSortRule.getSortRuleId(), 
							operateName, 
							"将分组ID为["+comAuditSortRule.getSortRuleId()+"]的排序规则删除", 
							ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.name(), 
							ComLog.COM_LOG_LOG_TYPE.AUDIT_SORT_RULE.getCnName()+"删除排序规则",
							"");
					LOGGER.info("OrderAuditSortRuleAction updateValid sortRuleId:" + sortRuleId);
				}else{
					msg.setCode(ResultMessage.ERROR);
					msg.setMessage("记录不存在");
				}
			}else{
				msg.setCode(ResultMessage.ERROR);
				msg.setMessage("参数不能为空");
			}
		} catch (Exception e) {
			msg.setCode(ResultMessage.ERROR);
			msg.setMessage("运行出现异常");
			LOGGER.error("OrderAuditSortRuleAction updateValid error,msg:" + e.getMessage());
			e.printStackTrace();
		}
		return msg;
	}
	
	@RequestMapping(value = "/testSortRule.do")
	@ResponseBody
	public Object testSortRule(Model model, Long orderItemId, HttpServletRequest request, HttpServletResponse response){
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setCode(ResultMessage.SUCCESS);
		try {
			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
			ComAuditSortRule comAuditSortRule = orderAuditSortRuleService.getComAuditSortRuleByOrderItem(ordOrderItem);
			if(comAuditSortRule!=null){
				log.info("id:"+comAuditSortRule.getSortRuleId());
				msg.setMessage("id:"+comAuditSortRule.getSortRuleId());
			}else{
				msg.setCode(ResultMessage.ERROR);
				msg.setMessage("not found");
			}
		} catch (Exception e) {
			msg.setCode(ResultMessage.ERROR);
			msg.setMessage("运行出现异常");
			LOGGER.error("OrderAuditSortRuleAction testSortRule orderItemId:"+ orderItemId +" error,msg:" + e.getMessage());
			e.printStackTrace();
		}
		return msg;
	}
	
	/**
	 * 获取具体更新内容新老值
	 * @param a1（老的对象）
	 * @param a2（新的对象）
	 * @return
	 * @author majunli
	 * @date 2016-11-12 下午5:47:05
	 */
	private String getChangeRecord(ComAuditSortRule r1, ComAuditSortRule r2){
		String change = "";
		if(r1 == null || r2 == null || !r1.getSortRuleId().equals(r2.getSortRuleId())){
			return change;
		}
		try {
			ComAuditSortRule a1 = new ComAuditSortRule();
			ComAuditSortRule a2 = new ComAuditSortRule();
			BeanUtils.copyProperties(r1, a1);
			BeanUtils.copyProperties(r2, a2);
			
			//所有bu
			Map<String, String> allBuMap = new LinkedHashMap<String, String>();
			ResultHandleT<List<BizBuEnum>> resultHandleT = bizBuEnumClientService.getAllBizBuEnumList();
			if(resultHandleT.isSuccess()){
				List<BizBuEnum> allBuList = resultHandleT.getReturnContent();
				for (BizBuEnum bizBuEnum : allBuList) {
					allBuMap.put(bizBuEnum.getCode(), bizBuEnum.getCnName());
				}
	        }
			//处理表示全部的空值
			a1 = replaceNullAttribute(a1);
			a2 = replaceNullAttribute(a2);
			
			StringBuffer sb = new StringBuffer();
			if(!a1.getSortRuleName().equals(a2.getSortRuleName())){
				sb.append(" 分组名称由：" + a1.getSortRuleName() + "改为：" + a2.getSortRuleName());
			}
			if(!a1.getArriveType().equals(a2.getArriveType())){
				sb.append(" 到店时间由：" + ARRIVE_TYPE.getCnName(a1.getArriveType()) + "改为：" + ARRIVE_TYPE.getCnName(a2.getArriveType()));
			}
			if(!a1.getBu().equals(a2.getBu())){
				String oldBu = allBuMap.get(a1.getBu()) != null ? allBuMap.get(a1.getBu()) : "全部";
				String newBu = allBuMap.get(a2.getBu()) != null ? allBuMap.get(a2.getBu()) : "全部";
				sb.append(" 所属BU由：" + oldBu + "改为：" + newBu);
			}
			if(!a1.getObjectId().equals(a2.getObjectId())){
				sb.append(" 酒店ID由：" + a1.getObjectId() + "改为：" + a2.getObjectId());
			}
			if(!a1.getSupplierId().equals(a2.getSupplierId())){
				sb.append(" 供应商ID由：" + a1.getSupplierId() + "改为：" + a2.getSupplierId());
			}
			if(!a1.getImmediatelyFlag().equals(a2.getImmediatelyFlag())){
				sb.append(" 是否即时由：" + a1.getImmediatelyFlag() + "改为：" + a2.getImmediatelyFlag());
			}
			if(!a1.getRemindTime().equals(a2.getRemindTime())){
				sb.append(" 调配时间由：" + a1.getRemindTime()/60 + "改为：" + a2.getRemindTime()/60);
			}
			if(!a1.getSeq().equals(a2.getSeq())){
				sb.append(" 优先级由：" + a1.getSeq() + "改为：" + a2.getSeq());
			}
			
			change = sb.toString();
		} catch (Exception e) {
			LOGGER.error("OrderAuditSortRuleAction getChangeRecord error,msg:" + e.getMessage());
			e.printStackTrace();
		}
		return change;
	}
	
	/**
	 * 处理表示全部的空值
	 * @param comAuditSortRule
	 * @return
	 * @author majunli
	 * @date 2016-11-12 下午5:44:48
	 */
	private ComAuditSortRule replaceNullAttribute(ComAuditSortRule comAuditSortRule){
		if(comAuditSortRule == null){
			return null;
		}
		if(StringUtil.isEmptyString(comAuditSortRule.getArriveType())){
			comAuditSortRule.setArriveType("全部");
		}
		if(StringUtil.isEmptyString(comAuditSortRule.getBu())){
			comAuditSortRule.setBu("全部");
		}
		if(StringUtil.isEmptyString(comAuditSortRule.getObjectId())){
			comAuditSortRule.setObjectId("全部");
		}
		if(StringUtil.isEmptyString(comAuditSortRule.getSupplierId())){
			comAuditSortRule.setSupplierId("全部");
		}
		if(StringUtil.isEmptyString(comAuditSortRule.getImmediatelyFlag())){
			comAuditSortRule.setImmediatelyFlag("全部");
		}
		return comAuditSortRule;
	}

	@RequestMapping(value = "/testUpdateSeq.do")
	@ResponseBody
	public Object testUpdateSeq(Model model, Long orderItemId, String nowDate, HttpServletRequest request, HttpServletResponse response){
		ResultMessage msg = ResultMessage.createResultMessage();
		msg.setCode(ResultMessage.SUCCESS);
		try {
			if (orderItemId == null) {
				msg.setMessage("orderItemId is null.");
				msg.setCode("error");
				return msg;
			}
			model.addAttribute("code", request.getParameter("code"));

			if(!checkUrlValid(request.getParameter("code"))){
				msg.setCode(ResultMessage.ERROR);
				msg.setMessage("没有权限");
			} else {
				orderAuditService.updateOrderAuditSeqByJob(orderItemId, nowDate);
			}
		} catch (Exception e) {
			msg.setCode(ResultMessage.ERROR);
			msg.setMessage("运行出现异常");
			LOGGER.error("OrderAuditSortRuleAction testSortRule orderItemId:"+ orderItemId +" error,msg:" + e.getMessage());
			e.printStackTrace();
		}
		return msg;
	}

	private boolean checkUrlValid(String code){
		if(code == null){
			return false;
		}

		try{
			code = DESCoder.decrypt(code);
			String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());

			if(today.equalsIgnoreCase(code)){
				return true;
			}
		}catch(Exception e){
			log.error(e);
		}
		return false;
	}
}	
