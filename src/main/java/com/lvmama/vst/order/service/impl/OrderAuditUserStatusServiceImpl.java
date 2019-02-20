package com.lvmama.vst.order.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrdUserCounter;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.vo.UserOrderCountVO;
import com.lvmama.vst.comm.mybatis.annotation.ForceRead;
import com.lvmama.vst.order.dao.OrdAuditUserStatusDAO;
import com.lvmama.vst.order.dao.OrdUserCounterDao;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
/**
 * 用户状态业务层实现类
 * 
 * @author wenzhengtao
 *
 */
@Service("orderAuditUserStatusService")
public class OrderAuditUserStatusServiceImpl implements IOrderAuditUserStatusService{

	@Autowired
	private OrdAuditUserStatusDAO auditUserStatusDAO;
	
	@Autowired
	private OrdUserCounterDao userCounterDao;
	
	@Override
	public int deleteByPrimaryKey(String operatorName) {
		return auditUserStatusDAO.deleteByPrimaryKey(operatorName);
	}

	@Override
	public int insert(OrdAuditUserStatus record) {
		int num = auditUserStatusDAO.insert(record);
		doAddCounter(record);
		return num;
	}

	private void doAddCounter(OrdAuditUserStatus record) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("userName", record.getOperatorName());
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
		if(userCounterDao.selectCount(params)==0){
			OrdUserCounter counter = new OrdUserCounter();
			counter.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			counter.setOrderCount(0L);
			counter.setUserName(record.getOperatorName());
			userCounterDao.insert(counter);
		}
		
		params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		if(userCounterDao.selectCount(params)==0){
			OrdUserCounter counter = new OrdUserCounter();
			counter.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			counter.setOrderCount(0L);
			counter.setUserName(record.getOperatorName());
			userCounterDao.insert(counter);
		}
	}
	
	

	@Override
	public int insertSelective(OrdAuditUserStatus record) {
		return auditUserStatusDAO.insertSelective(record);
	}

	@Override
	public OrdAuditUserStatus selectByPrimaryKey(String operatorName) {
		return auditUserStatusDAO.selectByPrimaryKey(operatorName);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdAuditUserStatus record) {
		return auditUserStatusDAO.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(OrdAuditUserStatus record) {
		return auditUserStatusDAO.updateByPrimaryKey(record);
	}
	
	
	public List<OrdAuditUserStatus> findOrdAuditUserStatusList(Map<String, Object> params){
		
		return auditUserStatusDAO.findOrdStatusGroupList(params);
	}
    
	public int findOrdAuditUserStatusCount(Map<String, Object> params){
		
		return auditUserStatusDAO.getTotalCount(params);
	}

	@Override
	@ReadOnlyDataSource
	@ForceRead
	public OrdAuditUserStatus getRandomUserByUsers(String objectType,List<String> userIds) {
//		return auditUserStatusDAO.getRandomUserByUsers(objectType, userIds);
		
		List<UserOrderCountVO> userOrderCountVOList = auditUserStatusDAO.getUserOrderCount(objectType, userIds);
		OrdAuditUserStatus user = getRandomUserByUsers(userOrderCountVOList);
		return user;
	}

	private OrdAuditUserStatus getRandomUserByUsers(
			List<UserOrderCountVO> userOrderCountVOList) {
		OrdAuditUserStatus user = null;
		if(userOrderCountVOList != null && userOrderCountVOList.size() > 0){
			Collections.sort(userOrderCountVOList);
			Long tempCount = userOrderCountVOList.get(0).getCount();
			int index = 0;
			for(int i = 1 ; i < userOrderCountVOList.size(); i++){
				UserOrderCountVO userOrderCountVO = userOrderCountVOList.get(i);
				Long count = userOrderCountVO.getCount();
				
				if(count > tempCount){
					Random ra =new Random();
					index = ra.nextInt(i);
					break;
				}
				
				if(i == userOrderCountVOList.size() - 1){
					Random ra =new Random();
					index = ra.nextInt(i + 1);
				}
			}
			
			UserOrderCountVO userOrderCountVO = userOrderCountVOList.get(index);
			user = new OrdAuditUserStatus();
			user.setOperatorName(userOrderCountVO.getOperatorName());
			user.setUserStatus(userOrderCountVO.getUserStatus());
			
		}
		return user;
	}

}
