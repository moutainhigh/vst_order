/**
 * 
 */
package com.lvmama.vst.order.web;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.crm.enumerate.CsVipUserIdentityTypeEnum;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.vst.back.order.po.OrdPremUserRel;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrdPremUserRelService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单下单用户相关操作
 * @author lancey
 *
 */
@Controller
public class OrderUserAction extends BaseActionSupport{
	private static final Log log = LogFactory.getLog(OrderUserAction.class);
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;
	@Autowired
	IOrdPremUserRelService iOrdPremUserRelService;
	@Autowired(required=false)
	private CsVipDubboService csVipDubboService;
	
	private final String QUERY_USER_PAGE = "/order/orderProductQuery/member_list";

	/**
	 * 查询用户相关的数据
	 * @return
	 */
	@RequestMapping("/ord/book/queryUser.do")
	public String queryUser(String key,Model model){
		if(StringUtils.isNotEmpty(key)){
			Map<String,Object> param = new HashMap<String, Object>();
			param.put("search", key.trim());
			param.put("maxRows", 10);
			List<UserUser> list = userUserProxyAdapter.getUsers(param);
			model.addAttribute("userList", list);
			Map<String, Boolean> isVipMap = new HashMap<String, Boolean>();
			if(null != list && 0 != list.size()){
				for (UserUser user : list) {
					String id = String.valueOf(user.getId());
					Map<String, Object> map = csVipDubboService.getCsVipByCondition(id, 
							CsVipUserIdentityTypeEnum.USER_ID);
					boolean isCsVip = (Boolean) map.get("isCsVip");
					isVipMap.put(id, isCsVip);
				}
				model.addAttribute("isVipMap", isVipMap);
			}
		}
		return QUERY_USER_PAGE;
	}
	/**
	 * 根据userId查询当前用户是否被冻结
	 * @param param
	 * @param model
	 * @return
	 */
	@RequestMapping("/ord/book/validFrozenByUserId.do")
	@ResponseBody
	public Map<String,String> validFrozenByUserId(String param,Model model){
		String userId=param;
		Map<String,String> msg = new HashMap<String, String>();
		String isFrozen = "N";
		if(StringUtils.isNotEmpty(userId)){
			 UserUser  registUser = userUserProxyAdapter.getUserUserByUserNo(userId);
			if(  registUser  != null){
				log.info("OrderUserAction validFrozenByUserId userStatus="+registUser.getUserStatus());
				if(Constants.USER_STATUS_BLOCK.equalsIgnoreCase(registUser.getUserStatus())){
					isFrozen="Y";
				}
			}else{
				log.info("OrderUserAction validFrozenByUserId registUser is null ");
			}
		}else{
			log.info("OrderUserAction validFrozenByUserId userId is empty ");
		}
		msg.put("isFrozen", isFrozen);
		return msg;
	}
	
	/**
	 * 根据UserMobile查询当前用户是否被冻结
	 * @param param
	 * @param model
	 * @return
	 */
	@RequestMapping("/ord/book/validFrozenByUserMobile.do")
	@ResponseBody
	public Map<String,String> validFrozenByUserMobile(String param,Model model){
		Map<String,String> msg = new HashMap<String, String>();
		String isFrozen = "N";
		String userMobile=param;
		if(StringUtils.isNotEmpty(userMobile)){
			ResultHandleT<UserUser> queryHandler = userUserProxyAdapter.getUserByMobile(userMobile);
			if(queryHandler.isSuccess() && queryHandler.getReturnContent() != null){
				UserUser registUser = queryHandler.getReturnContent(); //表示有当前用户注册信息
				log.info("OrderUserAction validFrozenByUserId userStatus="+registUser.getUserStatus());
				if(Constants.USER_STATUS_BLOCK.equalsIgnoreCase(registUser.getUserStatus())){
					isFrozen="Y";
				}
			}else{
				log.info("OrderUserAction validFrozenByUserId queryHandler.getReturnContent() is empty ");
			}
		}else{
			log.info("OrderUserAction validFrozenByUserId userMobile is empty ");
		}
		msg.put("isFrozen", isFrozen);
		return msg;
	}
	
	@RequestMapping("/ord/book/regUser.do")
	@ResponseBody
	public Object regUser(String userMobile,Model model){
		ResultMessage msg = ResultMessage.createResultMessage();
		if(StringUtils.isEmpty(userMobile)||userMobile.length()<11||userMobile.length()>12||!userMobile.startsWith("1")){
			msg.raise("手机号基本信息不正确");
			return msg;
		}
		try{
			String userId = null;
			String channel_code = HttpServletLocalThread.getRequest().getParameter("channel_code");
			if(StringUtils.isNotBlank(channel_code) && !"no_o2o".equals(channel_code)){
				userId = userUserProxyAdapter.registUser(userMobile, channel_code, false);
			}else{
				userId=userUserProxyAdapter.registUser(userMobile, "");
			}
			//新增客服人员与注册用户关联关系 ,for update Zhang.Wei
			savePremUserRel(userMobile, userId);
			msg.addObject("userId",userId);
		}catch(Exception e){
			msg.raise(e.getMessage());
		}
		return msg;
	}

	/**
	 * 保存客服与注册用户的关系
	 * @param userMobile 注册用户手机号
	 * @param userNo 注册用户编号
	 * @author Zhang.Wei
	 */
	private void savePremUserRel(String userMobile, String userNo) {
		Assert.hasText(userNo,"userNo can't be null");
		UserUser userUser= userUserProxyAdapter.getUserUserByUserNo(userNo);//拿编号取用户信息
		PermUser permUser = getLoginUser();//获取客服人员登录信息
		if (permUser != null  && userUser != null  ) {//判断客服登录和注册用户是否有注册成功
            Long premUserId = permUser.getUserId();
            Long userUserId = userUser.getId();
            if( premUserId !=null&& userUserId !=null){//判断客服ID和用户ID是否有值
                OrdPremUserRel ordPremUserRel = new OrdPremUserRel(Long.valueOf(userUserId), premUserId, userMobile);
                iOrdPremUserRelService.saveOrdPremUserRel(ordPremUserRel);
            }else{
                log.info("premUserId can't be null premUserId is=" + premUserId + ". or userUserId  can't be null,userUserId is=" + userUserId);
            }
        } else {
            log.info("permUser object can't be null userUser="+userUser+". or permUser Object can't be null,premUser="+permUser);
        }
	}
}
