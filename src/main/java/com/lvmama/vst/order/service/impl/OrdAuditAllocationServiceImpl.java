/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdAuditAllocation;
import com.lvmama.vst.back.order.po.OrdAuditAllocationRelation;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdAuditAllocationDao;
import com.lvmama.vst.order.dao.OrdAuditAllocationRelationDao;
import com.lvmama.vst.order.service.IOrdAuditAllocationService;

/**
 * @author pengyayun
 *
 */
@Service("ordAuditAllocationService")
public class OrdAuditAllocationServiceImpl implements IOrdAuditAllocationService{
	
	private static final Log LOG = LogFactory.getLog(OrdAuditAllocationServiceImpl.class);
	
	@Autowired
	private OrdAuditAllocationDao ordAuditAllocationDao;
	
	@Autowired
	private OrdAuditAllocationRelationDao ordAuditAllocationRelationDao;

	@Override
	public List<OrdAuditAllocation> queryOrdAuditAllocationListByParam(
			Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordAuditAllocationDao.queryOrdAuditAllocationListByParam(param);
	}

	@Override
	public Integer getTotalCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordAuditAllocationDao.getTotalCount(param);
	}

	@Override
	public List<OrdAuditAllocationRelation> queryOrdAuditAllocationRelationListByParam(
			Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordAuditAllocationRelationDao.queryOrdAuditAllocationRelationListByParam(param);
	}

	@Override
	public Integer getRelationTotalCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordAuditAllocationRelationDao.getTotalCount(param);
	}

	@Override
	public int updateOrdAuditAllocation(OrdAuditAllocation ordAuditAllocation)
			throws BusinessException {
		// TODO Auto-generated method stub
		LOG.info("OrdAuditAllocationServiceImpl.updateOrdAuditAllocation(OrdAuditAllocation) start");
		LOG.info("parameter: OrdAllocationId="+ordAuditAllocation.getOrdAllocationId());
		LOG.info("parameter: CategoryId="+ordAuditAllocation.getCategoryId());
		LOG.info("parameter: OrgId="+ordAuditAllocation.getOrgId());
		
		int result = 0;
		try {
			result=ordAuditAllocationDao.updateByPrimaryKeySelective(ordAuditAllocation);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public Long addOrdAuditAllocation(OrdAuditAllocation ordAuditAllocation)
			throws BusinessException {
		// TODO Auto-generated method stub
		LOG.info("OrdAuditAllocationServiceImpl.addOrdAuditAllocation(OrdAuditAllocation) start");
		LOG.info("parameter: CategoryId="+ordAuditAllocation.getCategoryId());
		LOG.info("parameter: OrgId="+ordAuditAllocation.getOrgId());
		
		long result = 0;
		try {
			ordAuditAllocationDao.insert(ordAuditAllocation);
			result = ordAuditAllocation.getOrdAllocationId();
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int updateOrdAuditAllocationRelation(
			OrdAuditAllocationRelation ordAuditAllocationRelation)
			throws BusinessException {
		// TODO Auto-generated method stub
		LOG.info("OrdAuditAllocationServiceImpl.updateOrdAuditAllocationRelation(OrdAuditAllocation) start");
		LOG.info("parameter: RelationId="+ordAuditAllocationRelation.getRelationId());
		LOG.info("parameter: OrdAllocationId="+ordAuditAllocationRelation.getOrdAllocationId());
		LOG.info("parameter: OrdFunctionId="+ordAuditAllocationRelation.getOrdFunctionId());
		
		int result = 0;
		try {
			result=ordAuditAllocationRelationDao.updateByPrimaryKeySelective(ordAuditAllocationRelation);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public Long addOrdAuditAllocationRelation(
			OrdAuditAllocationRelation ordAuditAllocationRelation)
			throws BusinessException {
		// TODO Auto-generated method stub
		LOG.info("OrdAuditAllocationServiceImpl.addOrdAuditAllocationRelation(OrdAuditAllocation) start");
		LOG.info("parameter: OrdAllocationId="+ordAuditAllocationRelation.getOrdAllocationId());
		LOG.info("parameter: OrdFunctionId="+ordAuditAllocationRelation.getOrdFunctionId());
		
		long result = 0;
		try {
			ordAuditAllocationRelationDao.insert(ordAuditAllocationRelation);
			result=ordAuditAllocationRelation.getRelationId();
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public void saveOrUpdateOrdAuditConfig(OrdAuditAllocation ordAuditAllocation,
			Long[] ordfunctionIds) throws BusinessException {
		// TODO Auto-generated method stub
		Long ordAllocationId= 0L;
		if(ordAuditAllocation.getOrdAllocationId()==null){
			//保存订单活动分配组织
			ordAllocationId= addOrdAuditAllocation(ordAuditAllocation);
		}else{
			updateOrdAuditAllocation(ordAuditAllocation);
			ordAllocationId=ordAuditAllocation.getOrdAllocationId();
		}
//		param.put("ordAllocationId", ordAllocationId);
//		List<OrdAuditAllocationRelation> relationList=queryOrdAuditAllocationRelationListByParam(param);
		
		//
		ordAuditAllocationRelationDao.deleteByOrdAllocationId(ordAllocationId);
		
		//保存订单活动分配组织关系
		for (Long ordFunctionId : ordfunctionIds) {
				OrdAuditAllocationRelation ordAuditAllocationRelation=new OrdAuditAllocationRelation();
				ordAuditAllocationRelation.setOrdAllocationId(ordAllocationId);
				ordAuditAllocationRelation.setOrdFunctionId(ordFunctionId);
				
				addOrdAuditAllocationRelation(ordAuditAllocationRelation);
		}
	}

	@Override
	public OrdAuditAllocation findOrdAuditAllocationById(Long ordAllocationId) {
		// TODO Auto-generated method stub
		return ordAuditAllocationDao.selectByPrimaryKey(ordAllocationId);
	}

	@Override
	public void delOrdAuditAllocationById(Long ordAllocationId) {
		// TODO Auto-generated method stub
		ordAuditAllocationDao.deleteByPrimaryKey(ordAllocationId);
		
		ordAuditAllocationRelationDao.deleteByOrdAllocationId(ordAllocationId);
	}
	
	
}
