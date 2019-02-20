package com.lvmama.vst.order.client.ord.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.allocation.jms.AllocationLogMessageProducer;
import com.lvmama.vst.back.client.ord.service.OrderAllocationClientService;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.pet.adapter.ConnRecordServiceAdapter;
/**
 * @author zhudongquan
 * 订单提供给vst_allocation的接口
 */
@Component("orderAllocationServiceRemote")
public class OrderAllocationClientServiceImpl implements OrderAllocationClientService {
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	@Autowired
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	@Autowired
	private ConnRecordServiceAdapter connRecordService;
	@Autowired
	private IOrdPersonService orderPersonService;
	
	@Autowired
	private IOrderAuditService iOrderAuditService;
	
//	private static final Log LOG = LogFactory.getLog(OrderAllocationClientServiceImpl.class);
	
	@Resource(name="allocationLogMessageProducer")
	private AllocationLogMessageProducer allocationLogMessageProducer;
	
	/**
	 * 查找订单是否有订单负责人
	 */
	@Override
	public OrdResponsible findResponsibleByObject(ComAudit comAudit) {
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objectId",    comAudit.getObjectId());
		paramMap.put("objectType",  comAudit.getObjectType());
		List<OrdResponsible> ordResponsibleList =  orderResponsibleService.findOrdResponsibleList(paramMap);
		if(CollectionUtils.isNotEmpty(ordResponsibleList)){
			return (OrdResponsible) ordResponsibleList.get(0);//一个订单/子订单只有一条负责人记录。
		}
		return null;
	}

	/**
	 * 查找订单是否有订单负责人
	 */
	@Override
	public OrdResponsible findOrderResponsible(Long objectId,String objectType) {
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objectId", objectId);
		paramMap.put("objectType", objectType);
		List<OrdResponsible> ordResponsibleList =  orderResponsibleService.findOrdResponsibleList(paramMap);
		if(CollectionUtils.isNotEmpty(ordResponsibleList)){
			return (OrdResponsible) ordResponsibleList.get(0);//一个订单/子订单只有一条负责人记录。
		}
		return null;
	}
	
	@Override
	public String findOperateUserByAuditTypeAndOrder(OrdOrder order, ComAudit comAudit, List<OrdOrderItem> ordOrderItemList) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("auditType", comAudit.getAuditType());
		params.put("auditSubtype", comAudit.getAuditSubtype());
		Long[] objectids = new Long[ordOrderItemList.size()+1];
		objectids[0] = order.getOrderId();
		int i= 1;
		for(OrdOrderItem item : ordOrderItemList){
			objectids[i++] = item.getOrderItemId();
		}
		params.put("objectIds", objectids); //查询订单~含主子订单
		params.put("excludeOperator", "SYSTEM");
		params.put("descSort","desc");
		// LOG.info("findOperateUserByAuditTypeAndOrder:"+order.getOrderId()+":"+objectids.length);
		List<ComAudit> comAuditList = iOrderAuditService.queryAuditListByCondition(params);
		if(CollectionUtils.isNotEmpty(comAuditList)){
			return comAuditList.get(0).getOperatorName();
		}
		return null;
	}
	
	@Override
	public String findOperateUserByRoleUser(OrdOrder order,List<PermUser> permUserList) {
		if(!CollectionUtils.isEmpty(permUserList)){
			Map<String,Object> params = new HashMap<String, Object>();
			Long[] objectids = new Long[order.getOrderItemList().size()+1];
			objectids[0] = order.getOrderId();
			int i= 1;
			for(OrdOrderItem item : order.getOrderItemList()){
				objectids[i++] = item.getOrderItemId();
			}
			params.put("objectIds", objectids); //查询订单~含主子订单
			
			String[]  operatorNameArray = new String[permUserList.size()];
			for(int j=0;j<permUserList.size();j++){
				operatorNameArray[j] = permUserList.get(j).getUserName();
			}
			params.put("operatorNameArray", operatorNameArray);
			// LOG.info("findOperateUserByRoleUser:"+order.getOrderId()+":"+operatorNameArray.length+":"+objectids.length);
			List<ComAudit> comAuditList = iOrderAuditService.queryAuditListByCondition(params);
			if(CollectionUtils.isNotEmpty(comAuditList)){
				return comAuditList.get(0).getOperatorName();
			}
		}	
		return null;
	}

	/**
	 * 保存订单负责人
	 */
	@Override
	public void saveOrdResponsible(OrdResponsible ordResponsible) {
		orderResponsibleService.saveOrdResponsible(ordResponsible);
	}
	
	/**
	 *判断是否在线
	 */
	@Override
	public boolean isOnline(String operatorName) {
		OrdAuditUserStatus user = orderAuditUserStatusService.selectByPrimaryKey(operatorName);
		return user != null;
	}


	@Override
	public int getOnlineCount(String[] operatorNameArray) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userStatus", "ONLINE");
		params.put("operatorNameArray", operatorNameArray);
		int count =  orderAuditUserStatusService.findOrdAuditUserStatusCount(params);
		return count;
	}
	
	/**
	 *判断是否在线
	 */
	@Override
	public boolean isOnline(String operatorName,ComAudit comAudit) {
		OrdAuditUserStatus user = orderAuditUserStatusService.selectByPrimaryKey(operatorName);
		if(user != null){
			allocationLogMessageProducer.newAllocationLogMessage(comAudit, "分单人："+operatorName+"在线");
		}else{
			allocationLogMessageProducer.newAllocationLogMessage(comAudit, "分单人："+operatorName+"不在线");
		}
		return user != null;
	}


	@Override
	public int getOnlineCount(String[] operatorNameArray,ComAudit comAudit) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userStatus", "ONLINE");
		params.put("operatorNameArray", operatorNameArray);
		int count =  orderAuditUserStatusService.findOrdAuditUserStatusCount(params);
		StringBuffer sb = new StringBuffer();
		if(operatorNameArray!=null&&operatorNameArray.length>0){
			for (String operatorName : operatorNameArray) {
				sb.append(operatorName+",");
			}
			sb.deleteCharAt(sb.length()-1);
		}
		if(count > 0){
			allocationLogMessageProducer.newAllocationLogMessage(comAudit, "分单人："+sb.toString()+"在线");
		}else{
			allocationLogMessageProducer.newAllocationLogMessage(comAudit, "分单人："+sb.toString()+"不在线");
		}
		return count;
	}
	
	/**
	 * 获得lvcc呼出记录
	 */
	@Override
	public String findLvccRecord(OrdOrder order) {
		String mobile = null;
		OrdPerson ordPerson=order.getContactPerson(); //联系人
		if(ordPerson == null){
			Map<String, Object> params = new HashMap<String, Object>();//重新初始化ordPerson
			params.put("objectType", "ORDER");
			params.put("objectId", order.getOrderId());
			order.setOrdPersonList(orderPersonService.findOrdPersonList(params));
			ordPerson = order.getContactPerson();
		}
		if(ordPerson != null){
			mobile = ordPerson.getMobile();
		}
		if(StringUtil.isNotEmptyString(mobile)){
			return connRecordService.queryConnRecordWithPage(mobile, 1L, 1);
		}
		return null;
	}

	/**
	 * 根据用户平均分配
	 */
	@Override
	public String findOprUserByRandom(ComAudit comAudit,String buType,List<PermUser> permUserList) {
		List<String> userList = new ArrayList<String>();
		if(CollectionUtils.isNotEmpty(permUserList)){
			for(PermUser permUser : permUserList){
				userList.add(permUser.getUserName());
			}
		}
		if(CollectionUtils.isNotEmpty(userList)&&!StringUtils.isEmpty(buType)){
			String objectType = null;
			if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equals(buType)){
				objectType = OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name();
			}else{
				objectType = comAudit.getObjectType();
			}
			OrdAuditUserStatus user = orderAuditUserStatusService.getRandomUserByUsers(objectType, userList);
			if(user != null){
				allocationLogMessageProducer.newAllocationLogMessage(comAudit, "活动处理人平均分配为："+user.getOperatorName());
				//LOG.info("findOprUserByRandom：com_audit_id:"+comAudit.getAuditId()+" 活动处理人平均分配为："+user.getOperatorName());
				return user.getOperatorName();
			}
		}
		allocationLogMessageProducer.newAllocationLogMessage(comAudit, "平均分配失败");
		return null;
	}

	@Override
	public String findOperateUserByAuditTypeAndRoleUser(OrdOrder order,
			ComAudit comAudit, List<OrdOrderItem> ordOrderItemList,
			List<PermUser> permUserList) {
		if(!CollectionUtils.isEmpty(permUserList)){
			Map<String,Object> params = new HashMap<String, Object>();
			// 同一活动类型
			params.put("auditType", comAudit.getAuditType());
			params.put("auditSubtype", comAudit.getAuditSubtype());
			// 同一订单
			Long[] objectids = new Long[ordOrderItemList.size()+1];
			objectids[0] = order.getOrderId();
			int i= 1;
			for(OrdOrderItem item : ordOrderItemList){
				objectids[i++] = item.getOrderItemId();
			}
			params.put("objectIds", objectids); //查询订单~含主子订单
			// 有权限
			String[]  operatorNameArray = new String[permUserList.size()];
			for(int j=0;j<permUserList.size();j++){
				operatorNameArray[j] = permUserList.get(j).getUserName();
			}
			params.put("operatorNameArray", operatorNameArray);
			// SYSTEM 除外
			params.put("excludeOperator", "SYSTEM");
			params.put("descSort","desc");
			List<ComAudit> comAuditList = iOrderAuditService.queryAuditListByCondition(params);
			if(CollectionUtils.isNotEmpty(comAuditList)){
				return comAuditList.get(0).getOperatorName();
			}
		}
		return null;
	}

	@Override
	public String findOperateUserByAuditType(ComAudit comAudit) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("auditType", OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name());
		Long[] objectids = new Long[1];
		objectids[0] = comAudit.getObjectId();
		params.put("objectIds", objectids);
		List<ComAudit> comAuditList = iOrderAuditService.queryAuditListByCondition(params);
		if(CollectionUtils.isNotEmpty(comAuditList)){
			return comAuditList.get(0).getOperatorName();
		}
		return null;
	}

}