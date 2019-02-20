/**
 * 
 */
package com.lvmama.vst.order.web;

import java.util.List;

import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.order.BaseOrderAciton;
import com.lvmama.vst.order.service.IOrderAuditService;

/**
 * @author lancey
 *
 */
@Controller
public class AjaxOrderStatusAction extends BaseOrderAciton{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(AjaxOrderStatusAction.class);
	
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@RequestMapping("/ord/order/queryComAudit")
	@ResponseBody
	public Object queryComAudit(Long auditId){
		ResultMessage result = ResultMessage.createResultMessage();
		ComAudit audit = orderAuditService.queryAuditById(auditId);
		result.addObject("audit", audit);
		return result;
	}
	
	@RequestMapping("/ord/order/queryComAuditByObjectId")
	@ResponseBody
	public Object queryComAuditByObjectId(Long objectId){
		ResultMessage result = ResultMessage.createResultMessage();
		if(objectId == null){
			result.addObject("audit", "objectId不能为空");
		}else{
			List<ComAudit> auditLists = orderAuditService.queryComAuditByObjectId(objectId);
			result.addObject("audit", auditLists);
		}
		return result;
	}
	

	@RequestMapping("/ord/order/updateComAuditStatusByAuditId")
	@ResponseBody
	public Object updateComAuditStatusByAuditId(Long auditId){
		ResultMessage result = ResultMessage.createResultMessage();
		if(auditId == null || auditId.longValue() <= 0){
			result.addObject("audit", "auditId不能为空");
		}else{
			int resultValue = orderAuditService.updateComAuditStatusByAuditId(auditId);
			if(resultValue ==1){
				result.addObject("audit", "执行成功");
			}else{
				result.addObject("audit", "执行失败:原因可能有  <1>:auditId没有对应的comAudit记录  <2>:状态已经是processed");
			}
		}
		return result;
	}

	@RequestMapping("/ord/order/queryVipComAuditStatus")
	@ResponseBody
	public Object queryVipComAuditStatus(String key){
		ResultMessage result = ResultMessage.createResultMessage();
		if(StringUtil.isEmptyString(key)){
			result.addObject("key", "key isEmpty");
		}else{
			String queryStatus = MemcachedUtil.getInstance().get(key);
			result.addObject("queryStatus", queryStatus);
		}
		return result;
	}

	@RequestMapping("/ord/order/updateComAudit")
	@ResponseBody
	public Object updateComAudit(Long auditId){
		ResultMessage result = ResultMessage.createResultMessage();
		int count = orderAuditService.updateValid(auditId);
		result.addObject("count", count);

		if (logger.isInfoEnabled()) {
			logger.info("updateComAudit(Long) - 更改活动为有效 userId:"+getLoginUserId()); //$NON-NLS-1$
		}

		return result;
	}
}
