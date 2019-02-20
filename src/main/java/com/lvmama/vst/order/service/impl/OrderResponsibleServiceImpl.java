/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_OBJECT_TYPE;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.order.dao.ComAuditDao;
import com.lvmama.vst.order.dao.OrdAuditAllocationDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdResponsibleDao;
import com.lvmama.vst.order.dao.OrdUserCounterDao;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * @author lancey
 *
 */
@Service
public class OrderResponsibleServiceImpl implements IOrderResponsibleService{
	private static final Log LOG = LogFactory.getLog(OrderResponsibleServiceImpl.class);

	@Autowired
	private OrdUserCounterDao userCounterDao;
	
	@Autowired
	private OrdResponsibleDao responsibleDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdAuditAllocationDao auditAllocationDao;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private ComAuditDao comAuditDao;
	@Autowired
	private OrderDistributionBusiness orderDistributionBusiness;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	
	
	@Override
	public void deleteCounterAll() {
		userCounterDao.deleteAll();
	}

	@Override
	public List<OrdResponsible> selectWaitObjectList(Map<String,Object> params) {
		return responsibleDao.selectWaitObjectList(params);
	}
	
	@Override
	public List<Map> queryResponsibleListByCondition(Map<String, Object> params) {
		return responsibleDao.selectResponsibleList(params);
	}

	@Override
	public Integer selectResponsibleCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return responsibleDao.selectResponsibleCount(param);
	}

	@Override
	public List<OrdResponsible> findOrdResponsibleList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return responsibleDao.selectByParams(params);
	}

	public PermUser getOrderPrincipal( String objectType,
			Long objectId) {
		
		PermUser permUser=null;
		
		Map<String, Object> paramsOrdRespom=new HashMap<String, Object>();
		paramsOrdRespom.put("objectId",objectId );
		paramsOrdRespom.put("objectType",objectType);
		
		List<OrdResponsible>  ordResonsibleList=this.findOrdResponsibleList(paramsOrdRespom);
		
		if (!ordResonsibleList.isEmpty()) {
			OrdResponsible ordResponsible=ordResonsibleList.get(0);
			
//			Map<String, Object> params=new HashMap<String, Object>();
//			params.put("objectId",objectId );
//			params.put("objectType",objectType);
			
//			List<PermUser>  userList=permUserService.selectUsersByParams(params);
			Map<String, Object> params=new HashMap<String, Object>();
			params.put("userNameEQ", ordResponsible.getOperatorName());
			params.put("maxResults", 100);
            params.put("skipResults", 0);
			List<PermUser> permUserList = permUserServiceAdapter.queryPermUserByParam(params);
			if(!CollectionUtils.isEmpty(permUserList) && permUserList.size() > 0) {
				permUser = permUserList.get(0);
			}
			//接口getPermUserByUserName如果用户如果被锁定，则查询结果为null，而接口queryPermUserByParam不会出现该问题
//			permUser=permUserServiceAdapter.getPermUserByUserName(ordResponsible.getOperatorName());
		}
		if (permUser==null) {
			 permUser=new PermUser();
		}
		return permUser;
	}
	
	@Override
	public Integer getTotalCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return responsibleDao.getTotalCount(param);
	}

	@Override
	public int updateManualAssign(List<Long> objectIds, String objectType,
			PermUser user, String operatorName) {
		Date createTime = new Date();
		int num=0;
		for(Long objectId:objectIds){
			try{
				OrdResponsible res = responsibleDao.getResponsibleByObject(objectId,objectType);
				StringBuffer sb = new StringBuffer();
				if(res==null){
					res = new OrdResponsible();
					res.setObjectId(objectId);
					res.setObjectType(objectType);
					res.setOperatorName(user.getUserName());
					res.setOrgId(user.getDepartmentId());
					res.setCreateTime(createTime);
					responsibleDao.insert(res);
					sb.append("订单负责人分配给");
					sb.append(user.getUserName());
				}else{
					String oldUser=res.getOperatorName();
					res.setOperatorName(user.getUserName());
					res.setOrgId(user.getDepartmentId());
					responsibleDao.updateByPrimaryKey(res);
					sb.append("订单负责人由");
					sb.append(oldUser);
					sb.append("转换成");
					sb.append(user.getUserName());
					changeTaskOperator(objectId,objectType,oldUser,user);
				}
				
				userCounterDao.increase(res.getOperatorName(),res.getObjectType());
				insertLog(objectId, objectType, operatorName, sb.toString());
				num++;
			}catch(Exception ex){
				LOG.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
		return num;
	}
	
	private void changeTaskOperator(final Long objectId,final String objectType,String sourceOperator,PermUser user){
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("objectType", objectType);
		param.put("operatorName", sourceOperator);
		param.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
		
		List<ComAudit> list = comAuditDao.queryAuditListByCondition(param);
		if(CollectionUtils.isNotEmpty(list)){
			for(ComAudit ca:list){
				if(!OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name().equals(ca.getAuditType())){
					orderDistributionBusiness.changeTaskOperator(ca,user);
				}
			}
		}
	}
	
	private void insertLog(Long objectId,String objectType,String operatorName,String content){
		COM_LOG_OBJECT_TYPE t=COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType)){
			t=COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}
		
		lvmmLogClientService.sendLog(t, objectId, objectId, operatorName, content, ComLog.COM_LOG_LOG_TYPE
				.ORD_ORDER_DISTRIBUTION.name(), "人工修改订单负责人", "");
	}

	@Override
	public void saveOrdResponsible(OrdResponsible ordResponsible) {
		OrdResponsible res = responsibleDao.getResponsibleByObject(ordResponsible.getObjectId(),ordResponsible.getObjectType());
		if(res == null){
			responsibleDao.insert(ordResponsible);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PermUser getResourceApprover(Long objectId, String objectType) {
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("objectType", objectType);
		param.put("auditType", AUDIT_TYPE.RESOURCE_AUDIT.getCode());
		List<ComAudit> comAudits = comAuditDao.queryAuditListByParam(param);
		
		PermUser permUser = null;
		if (comAudits != null && !comAudits.isEmpty()) {
			String operator = comAudits.get(0).getOperatorName();
			if (operator != null && !"".equals(operator) && !"SYSTEM".equals(operator)) {
				permUser = permUserServiceAdapter.getPermUserByUserName(operator);
			}
		}
		if (permUser == null) {
			permUser = new PermUser();
		}
		return permUser;
	}
}
