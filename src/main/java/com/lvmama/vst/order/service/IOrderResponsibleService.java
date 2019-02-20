/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.OrdResponsible;

/**
 * @author lancey
 *
 */
public interface IOrderResponsibleService {

	/**
	 * 删除所有的数据
	 */
	void deleteCounterAll();
	
	/**
	 * 查询待分配的订单列表
	 * @return
	 */
	List<OrdResponsible> selectWaitObjectList(Map<String,Object> params);
	
	
	/**
	 * 人工分单列表查询
	 * @return
	 */
	List<Map> queryResponsibleListByCondition(Map<String,Object> params);
	
	/**
	 * 根据条件统计总数量
	 * 
	 * @param param
	 * @return
	 */
	public Integer selectResponsibleCount(Map<String, Object> param);
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	List<OrdResponsible> findOrdResponsibleList(Map<String,Object> params);
	
	
	PermUser getOrderPrincipal( String objectType,Long objectId) ; 
	
	/**
	 * map动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param);
	
	
	/**
	 * 人工分配订单负责人
	 * @param objectId
	 * @param objectType
	 * @param assignTargetUser
	 * @param operatorName
	 * @return
	 */
	int updateManualAssign(List<Long> objectIds,String objectType,PermUser assignTargetUser,String operatorName);
	
	/**
	 * 插入OrdResponsible
	 * */
	void saveOrdResponsible(OrdResponsible ordResponsible);
	
	/**
	 * 获取资源审核人.
	 * 
	 * @param objectId 订单号或子订单号
	 * @param objectType order or orderItem
	 * @return PermUser
	 */
	PermUser getResourceApprover(Long objectId, String objectType);
}
