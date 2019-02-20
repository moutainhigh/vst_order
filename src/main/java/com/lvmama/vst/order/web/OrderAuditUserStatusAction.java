package com.lvmama.vst.order.web;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.order.base.jedis.JedisClusterAdapter;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
/**
 * 用户状态业务处理
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderAuditUserStatusAction extends BaseActionSupport{
	//日志记录器
	private static final Log LOGGER = LogFactory.getLog(OrderAuditUserStatusAction.class);
	//用户状态业务
	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	
	/**
	 * @Description: redis缓存
	 */
	@Autowired
	private JedisClusterAdapter jedisCluster;
	/**
	 * 更新当前登录用户的接单状态
	 * 
	 * @param model
	 * @param userName
	 * @param userStatus
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/updateUserStatus.do")
	@ResponseBody
	public void updateUserStatus(Model model,String userStatus,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		Map<String, String> map = new HashMap<String, String>();
		
		if(!UtilityTool.isValid(userStatus)){
			if(LOGGER.isDebugEnabled()){
				LOGGER.error("parameter is error!");
				map.put("result", "failure");
				String jsonStr = JSONObject.fromObject(map).toString();
				JSONOutput.writeJSONP(response,jsonStr, request.getParameter("callback"));
				return;
			}
		}
		
		//从登录的session中获取当前登录用户
		String userName = this.getLoginUserId();
		
		//用户登录成功后将工作状态默认修改为可接单
		//pet_back里来电弹屏页面自己将状态修改为可接单
		//vst里的我的工作台上自己将状态修改为可接单
		if(userStatus.equals(OrderEnum.BACK_USER_WORK_STATUS.ONLINE.name())){
			OrdAuditUserStatus auditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(userName);
			if(null != auditUserStatus){
				auditUserStatus.setUserStatus(userStatus);
				orderAuditUserStatusService.updateByPrimaryKeySelective(auditUserStatus);
			}else{
				OrdAuditUserStatus auditUserStatusNew = new OrdAuditUserStatus();
				auditUserStatusNew.setOperatorName(userName);
				auditUserStatusNew.setUserStatus(userStatus);
				auditUserStatusNew.setCreateTime(Calendar.getInstance().getTime());
				orderAuditUserStatusService.insert(auditUserStatusNew);
			}
		//pet_back里来电弹屏页面自己将状态修改为忙碌
		//vst里的我的工作台上自己将状态修改为忙碌
		}
		/*else if(userStatus.equals(OrderEnum.BACK_USER_WORK_STATUS.BUSY.name())){
			OrdAuditUserStatus auditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(userName);
			if(null != auditUserStatus){
				auditUserStatus.setUserStatus(userStatus);
				orderAuditUserStatusService.updateByPrimaryKeySelective(auditUserStatus);
			}else{
				OrdAuditUserStatus auditUserStatusNew = new OrdAuditUserStatus();
				auditUserStatusNew.setOperatorName(userName);
				auditUserStatusNew.setUserStatus(userStatus);
				auditUserStatusNew.setCreateTime(Calendar.getInstance().getTime());
				orderAuditUserStatusService.insert(auditUserStatusNew);
			}
		//pet_back里来电弹屏页面自己将状态修改为不可接单
		//vst里我的工作台上自己将状态修改为不可接单
		}*/
		else{
			OrdAuditUserStatus auditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(userName);
			if(null != auditUserStatus){
				orderAuditUserStatusService.deleteByPrimaryKey(userName);
			}
		}
		
		this.updateOperatorCacheStatus(userName,userStatus);
		
		map.put("result", "success");
		String jsonStr = JSONObject.fromObject(map).toString();
		JSONOutput.writeJSONP(response, jsonStr, request.getParameter("callback"));
	}
	
	/**
	 * 检测当前登录用户的接单状态
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/checkUserStatus.do")
	@ResponseBody
	public void checkUserStatus(Model model,HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		Map<String, String> map = new HashMap<String, String>();
		//从登录的session中获取当前登录用户
		String userName = this.getLoginUserId();
		OrdAuditUserStatus auditUserStatus = orderAuditUserStatusService.selectByPrimaryKey(userName);
		if(null != auditUserStatus){
			//可接单或者忙碌
			map.put("result", auditUserStatus.getUserStatus());
		}else{
			//找不到即为不可接单
			map.put("result", OrderEnum.BACK_USER_WORK_STATUS.OFFLINE.name());
		}
		String jsonStr = JSONObject.fromObject(map).toString();
		JSONOutput.writeJSONP(response,jsonStr, request.getParameter("callback"));
	}
	
	/** 
	 * @Title: updateOperatorCacheStatus 
	 * @Description: 更新客服接单状态的缓存
	 * @param operatorName 客服
	 * @param onlineFlag 在线状态
	 */
	private void updateOperatorCacheStatus(String operatorName, String status) {
		if(jedisCluster.exists("ALLOCATION_USER_ONLINE_STATUS")) 
			jedisCluster.hset("ALLOCATION_USER_ONLINE_STATUS", operatorName, status); 
	}
}
