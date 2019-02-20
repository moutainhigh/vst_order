/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdAuditAllocation;
import com.lvmama.vst.back.order.po.OrdAuditAllocationRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.web.BusinessException;

/**
 * 
 * 订单活动分配业务类
 * @author pengyayun
 *
 */
public interface IOrdAuditAllocationService {
	
	 /**
	  * map动态查询
	  * 
	  * @param param
	  * @return
	  */
	public List<OrdAuditAllocation> queryOrdAuditAllocationListByParam(Map<String, Object> param);
	
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param);
	
	 /**
	  * map动态查询
	  * 
	  * @param param
	  * @return
	  */
	public List<OrdAuditAllocationRelation> queryOrdAuditAllocationRelationListByParam(Map<String, Object> param);
	
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getRelationTotalCount(Map<String, Object> param);
	
	/**
	 * 修改订单活动分配组织
	 * @param ordAuditAllocation
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdAuditAllocation(OrdAuditAllocation ordAuditAllocation) throws BusinessException;
	
	
	/**
	 * 新增订单活动分配组织
	 * @param ordAuditAllocation
	 * @return
	 * @throws BusinessException
	 */
	public Long addOrdAuditAllocation(OrdAuditAllocation ordAuditAllocation) throws BusinessException;
	
	/**
	 * 修改订单活动分配组织方法关系
	 * @param ordAuditAllocation
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdAuditAllocationRelation(OrdAuditAllocationRelation ordAuditAllocationRelation) throws BusinessException;
	
	
	/**
	 * 新增订单活动分配组织方法关系
	 * @param ordAuditAllocation
	 * @return
	 * @throws BusinessException
	 */
	public Long addOrdAuditAllocationRelation(OrdAuditAllocationRelation ordAuditAllocationRelation) throws BusinessException;
	
	/**
	 * 保存员工活动组
	 * @param ordAuditAllocation
	 * @param ordfunctionIds
	 * @throws BusinessException
	 */
	public void saveOrUpdateOrdAuditConfig(OrdAuditAllocation ordAuditAllocation,Long[] ordfunctionIds) throws BusinessException;
	
	
	public OrdAuditAllocation findOrdAuditAllocationById(Long ordAllocationId);
	
	public void delOrdAuditAllocationById(Long ordAllocationId);
}
