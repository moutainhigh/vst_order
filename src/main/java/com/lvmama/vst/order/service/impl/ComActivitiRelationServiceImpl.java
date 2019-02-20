/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComActivitiRelation.OBJECT_TYPE;
import com.lvmama.vst.order.dao.ComActivitiRelationDao;
import com.lvmama.vst.order.service.ComActivitiRelationService;

/**
 * @author lancey
 *
 */
@Service
public class ComActivitiRelationServiceImpl implements
		ComActivitiRelationService {
	@Autowired
	private ComActivitiRelationDao comActivitiRelationDao;

	/* (non-Javadoc)
	 * @see com.lvmama.vst.back.pub.service.ComActivitiRelationService#saveRelation(java.lang.String, java.lang.Long, java.lang.String)
	 */
	@Override
	public void saveRelation(String processKey,String processId, Long objectId, ComActivitiRelation.OBJECT_TYPE objectType) {
		ComActivitiRelation r = new ComActivitiRelation();
		r.setCreateTime(new Date());
		r.setObjectId(objectId);
		r.setObjectType(objectType.name());
		r.setProcessId(processId);
		r.setProcessKey(processKey);
		comActivitiRelationDao.insert(r);
	}

	@Override
	public ComActivitiRelation queryTaskRelation(Long auditId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComActivitiRelation queryRelation(String processKey, Long objectId,
			OBJECT_TYPE objectType) {
		ComActivitiRelation record = new ComActivitiRelation();
		record.setObjectId(objectId);
		record.setObjectType(objectType.name());
		record.setProcessKey(processKey);
		List<ComActivitiRelation> list = comActivitiRelationDao.queryList(record);
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<Long> queryClearProcessByCondition(Map<String, Object> param) {
		return comActivitiRelationDao.queryClearProcessByCondition(param);
	}

	@Override
	public Integer updateProcessStatus(String processId, String processStatus) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("processId", processId);
		param.put("processStatus", processStatus);
		
		return comActivitiRelationDao.updateProcessStatus(param);
	}

}
