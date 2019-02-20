package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.pub.po.ComActivitiRelation;

public interface ComActivitiRelationService {

	void saveRelation(String processKey,String processId,Long objectId,ComActivitiRelation.OBJECT_TYPE objectType);
	
	ComActivitiRelation queryTaskRelation(Long auditId);
	
	ComActivitiRelation queryRelation(final String processKey,final Long objectId,ComActivitiRelation.OBJECT_TYPE objectType);
	
	List<Long> queryClearProcessByCondition(Map<String, Object> param);
	
	Integer updateProcessStatus(String processId, String processStatus);
}
