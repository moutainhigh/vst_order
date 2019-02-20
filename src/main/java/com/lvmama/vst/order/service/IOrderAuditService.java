package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComAuditActiviNum;


/**
 * 
 * 订单审核业务
 * 
 * @author wenzhengtao
 *
 */
public interface IOrderAuditService {
	/**
	 * 保存订单审核信息
	 * 
	 * @param comAudit
	 */
	int saveAudit(ComAudit audit);
	
	
	/**
	 * 产生订单活动并且保存 待分配 状态
	 * 
	 * @param comAudit
	 */
	ComAudit saveCreateOrderAudit(Long objectId,String objectType,String auditType);
	
	
	/**
	 * 产生订单活动并且保存 待分配 状态
	 * 
	 * @param comAudit
	 */
	ComAudit saveCreateOrderAudit(Long orderId,String auditType);
	
	
	public ComAudit saveOrderAudit(Long orderId,String auditType,String  auditSubType);
	/**
	 * 产生订单活动并且保存 待分配 状态
	 * 
	 * @param comAudit
	 */
	ComAudit saveCreateChildOrderAudit(Long orderId,Long orderItmeId,String auditType);
	
	ComAudit saveChildOrderAudit(Long orderId,Long orderItmeId,String auditType,String  auditSubType);
	
	/**
	 * 按条件auditId查询订单审核信息
	 * 
	 * @param comAudit
	 * @return
	 */
	ComAudit queryAuditById(Long auditId);
	/**
	 * 按条件查询订单审核信息
	 * 
	 * @param comAudit
	 * @return
	 */
	List<ComAudit> queryAuditListByCondition(Map<String, Object> param);
	

	/**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByParam(Map<String, Object> param);
	
	/**
	 * 按条件统计订单审核信息
	 * 
	 * @param audit
	 * @return
	 */
	int countAuditByCondition(Map<String, Object> param);

	/**
	 * 按条件统计订单审核信息（new,从冗余表查）
	 * 
	 * @param audit
	 * @return
	 */
	int countAuditByRaid(Map<String, Object> param);
	
	Integer getTotalCount(Map<String, Object> param) ;
	/**
	 * 按条件更新订单审核信息(底层条件匹配多，使用请注意)
	 * 
	 * @param comAudit
	 * @return
	 */
	int updateComAuditByCondition(ComAudit comAudit);
	
	/**
	 * 按条件更新订单审核信息
	 * 
	 * @param comAudit
	 * @return
	 */
	int updateByPrimaryKey(ComAudit comAudit);
	
	/**
	 * 只更新未取消，非已处理的订单
	 * @param comAudit
	 * @return
	 */
	int updateByPrimaryKeyNew(ComAudit comAudit);
	
	
	/**
	 * 查找待分单的任务集合
	 * 
	 * @param param
	 * @return
	 */
	List<ComAudit> queryComAuditListByPool(Map<String, Object> param);
	/**
	 * 只更新记录为POOL状态的记录
	 * 
	 * @param param
	 * @return
	 */
	int updateComAuditByPool(Map<String, Object> param);
	/**
	 * 只更新记录为UNPROCESSED状态的记录
	 * 
	 * @param param
	 * @return
	 */
	int updateComAuditByUnProcessed(Map<String, Object> param);
	
	/**
	 * 只更新记录为PROCESSED状态的记录
	 * 
	 * @param param
	 * @return
	 */
	int updateComAuditByProcessed(Map<String, Object> param);
	
	/**
	 * 根据条件统计活动数量
	 * 
	 * @param param
	 * @return
	 */
	int countActivityNum(Map<String, Object> param);
	/**
	 * 根据条件统计订单数量
	 * @param param
	 * @return
	 */
	int countOrderNum(Map<String, Object> param);
	
	/**
	 * 根据条件统计活动数量
	 * 
	 * @param param
	 * @return
	 */
	List<Map<String, Object>> countGroupActivityNum(Map<String, Object> param);
	/**
	 * 根据条件统计订单数量
	 * @param param
	 * @return
	 */
	List<Map<String, Object>> countGroupOrderNum(Map<String, Object> param);
	
	/**
	 * 完成订单审核
	 * 
	 * @param orderId 订单ID
	 * @param auditType 活动类型
	 * @param operatorName 处理人
	 */
	int updateComAuditToProcessed(Long orderId,String auditType,String operatorName);
	/**
	 * 完成子订单审核
	 * 
	 * @param orderId 订单ID
	 * @param auditType 活动类型
	 * @param operatorName 处理人
	 */
	int updateChildOrderAuditToProcessed(Long orderId,String auditType,String operatorName) ;
	/**
	 * 设置为无效
	 */
	void markValid(final Long auditId);
	
	/**
	 * 分配活动并且操作负责人
	 * 如果
	 * @param audit
	 * @param item
	 * @param isNew 是否新分单规则
	 * @param hasCustomeUser
	 * @param user
	 * @param isOrderAndItemBuEqual 是否分单活动对象（主或子单）和主订单BU一致
	 * @return
	 */
	ComAudit updateAuditAssign(ComAudit audit, OrdOrderItem item,boolean isNew,boolean hasCustomeUser,OrdAuditUserStatus user, boolean isOrderAndItemBuEqual, boolean isCsVip, boolean isLocalVip, String csUserId);

	ComAudit updateAuditAssignForVip(ComAudit audit, OrdOrder order, String csUserId, String operatorName);

	/**
	 * 更改活动为有效
	 * @param audit
	 * @return
	 */
	int updateValid(Long audit);
	
	List<ComAudit> queryComAuditByObjectId(Long objectId);
	
	/**
	 * 更改活动状态为processed
	 * */
	int updateComAuditStatusByAuditId(Long auditId);
	
	/**
	 * 动态统计我的工作台复杂搜索查询数目
	 * @param param
	 * @return
	 */
	public int countAuditByMyWork(Map<String, Object> param);
	
	/**
	 * map动态查询（我的工作台）
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByCriteria(Map<String, Object> param);
	
	
	public int countAuditByDestWork(Map<String, Object> param);
	
	/**
	 * map动态查询（目的地员工库）
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryDestAuditListByCriteria(Map<String, Object> param);

	/**
	 * 每天更新最近出游的已审活动排序值
	 */
	public void updateOrderAuditSeqByJob(Long orderItemId, String nowDate);
	
	/**
	 * 预约中的订单
	 * @param param
	 * @return
	 * @throws Exception
	 * @author ltwangwei
	 * @date 2016-3-16 下午6:07:39
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	public int updateRemindTimeByAuditId(Map<String, Object> param) throws Exception;
	
	/**
	 * 查询符合条件的订单/子订单
	 * 
	 * @param param
	 * @return
	 */
	List<Map<String, Object>> queryMyOrderListByCondition(Map<String, Object> param);
	
	/**
	 * 统计符合条件的订单/子订单总数
	 * 
	 * @param param
	 * @return
	 */
	Integer countMyOrderByCondition(Map<String, Object> param);
	
	/**
	 * 统计符合条件的订单/子订单总数（new,从冗余表查）
	 * 
	 * @param param
	 * @return
	 */
	Integer countMyOrderByRaid(Map<String, Object> param);

	/**
	 * 查询符合条件的订单/子订单（new,从冗余表查）
	 * 
	 * @param param
	 * @return
	 */
	List<Map<String, Object>> queryMyOrderListByRaid(Map<String, Object> param);

	/**
	 * 查找自动过未分配到人的任务集合
	 * @param param
	 * @return
	 */
	List<ComAudit> queryComAuditListByProcessed(Map<String, Object> param);

	/**
	 * 更新状态为不可再分单
	 * @param auditId
	 */
	void markCanNotReaudit(Long auditId);
	/**
	 * 修改活动状态
	 * @param param(auditStatus,auditId)
	 * @return
	 */
	int updateComAuditStatus(Map<String, Object> param);
	/**
	 * 修改活动下次分单时间
	 * */
	int updateNextAssignTime(Map<String, Object> param);


	ComAudit updateAuditAssignForLocalVip(ComAudit audit, OrdOrder order,
			String csLocalUserId, String operatorName);
	
	/**
	 * 查询已审库活动
	 * @param param
	 * @return
	 */
	int queryOrderListCount(Map<String, Object> param);
	
	List<ComAudit> queryOrderAuditList(Map<String, Object> param);


	ComAudit updateAuditAssignForDriectSale(ComAudit audit, OrdOrder order,
			String operatorName);
	
	public List<ComAuditActiviNum> countActivityUnprocessedNum(Map<String, Object> param);
	
	int countAuditByNewConsole(Map<String, Object> param);
	
	public List<ComAudit> queryAuditByNewConsole(Map<String, Object> param);
	
	int updateComAuditByAuditlist(List<Long> auditIdlist);
	
	ComAudit updateAuditAssignForNewRule(ComAudit audit, OrdOrder order,
			String operatorName);
}
