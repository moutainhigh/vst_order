package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComAuditActiviNum;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 订单审核数据库访问层
 * 
 * @author wenzhengtao
 *
 */
@Repository
public class ComAuditDao extends MyBatisDao {

	public ComAuditDao() {
		super("COM_AUDIT");
	}
	 
	public int deleteByPrimaryKey(Long auditId){
		return super.delete("deleteByPrimaryKey", auditId);
	}
	
	/**
	 * 此方法有后置通知切面，异步冗余活动查询条件数据
	 * @param record
	 * @return
	 */
	public int insert(ComAudit record){
		return super.insert("insert", record);
	}
	
	public int insertSelective(ComAudit record){
		return super.insert("insertSelective", record);
	}
	
	public ComAudit selectByPrimaryKey(Long auditId){
		return super.get("selectByPrimaryKey", auditId);
	}
	
	public Integer updateByPrimaryKeySelective(ComAudit record){
		return super.update("updateByPrimaryKeySelective", record);
	}
	
	public int updateByPrimaryKey(ComAudit record){
		return super.update("updateByPrimaryKey", record);
	}

	public int updateComAuditSeqByJob(ComAudit record){
		return super.update("updateComAuditSeqByJob", record);
	}

	public int updateByPrimaryKeyNew(ComAudit record){
		return super.update("updateByPrimaryKeyNew", record);
	}
	
	/**
	 * 动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByCondition(Map<String, Object> param){
		return super.queryForList("queryAuditListByCondition", param);
	}
	
	/**
	 * 动态查询
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByCriteria(Map<String, Object>param){
		return super.queryForList("queryAuditListForMyWork", param);
	}
	
	/**
	 * 更新remindTime
	 * @param param
	 * @return
	 * @throws Exception
	 * @author ltwangwei
	 * @date 2016-3-16 下午6:04:25
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	public int updateRemindTimeByAuditId(Map<String, Object> param) throws Exception {
		return super.update("updateRemindTimeByAuditId", param);
	}
	
	/**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryAuditListByParam(Map<String, Object> param){
		return super.queryForList("selectByParams", param);
	}
	
	
	
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param) {
		return super.get("getTotalCount", param);
	}
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer countAuditByCondition(Map<String, Object> param) {
		return super.get("countAuditByCondition", param);
	}
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer countAuditByRaid(Map<String, Object> param) {
		return super.get("countAuditByRaid", param);
	}
	
	/**
	 * 动态统计
	 * @param param
	 * @return
	 */
	public Integer countAuditByMyWork(Map<String, Object> param){
		return super.get("countAuditForMyWork", param);
	}
	/**
	 * 动态统计
	 * @param param
	 * @return
	 */
	public Integer countAuditByDestWork(Map<String, Object> param){
		return super.get("countAuditForDestWork", param);
	}
	
	public List<ComAudit> queryDestAuditListByCriteria(Map<String, Object>param){
		return super.queryForList("queryAuditListForDestWork", param);
	}
	
	/**
	 * 查找待分单的任务集合
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryComAuditListByPool(Map<String, Object> param){
		return super.queryForList("queryComAuditListByPool", param);
	}
	
	/**
	 * 查找自动过未分配到人的任务集合
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAudit> queryComAuditListByProcessed(Map<String, Object> param){
		return super.queryForList("queryComAuditListByProcessed", param);
	}
	
	/**
	 * 统计符合条件的订单/子订单总数
	 * 
	 * @param param
	 * @return
	 */
	public Integer countMyOrderByCondition(Map<String, Object> param) {
		return super.get("countMyOrderByCondition", param);
	}
	
	/**
	 * 查询符合条件的订单/子订单
	 * 
	 * @param param
	 * @return
	 */
	public List<Map<String, Object>> queryMyOrderListByCondition(Map<String, Object> param){
		return super.queryForList("queryMyOrderListByCondition", param);
	}
	
	/**
	 * 统计符合条件的订单/子订单总数
	 * 
	 * @param param
	 * @return
	 */
	public Integer countMyOrderByRaid(Map<String, Object> param) {
		return super.get("countMyOrderByRaid", param);
	}
	
	/**
	 * 查询符合条件的订单/子订单
	 * 
	 * @param param
	 * @return
	 */
	public List<Map<String, Object>> queryMyOrderListByRaid(Map<String, Object> param){
		return super.queryForList("queryMyOrderListByRaid", param);
	}
	
	/**
	 * 只更新记录为POOL状态的记录
	 * 
	 * @param param
	 * @return
	 */
	public int updateComAuditByPool(Map<String, Object> param){
		return super.update("updateComAuditByPool", param);
	}
	
	/**
	 * 只更新记录为UNPROCESSED状态的记录
	 * 
	 * @param param
	 * @return
	 */
	public int updateComAuditByUnProcessed(Map<String, Object> param){
		return super.update("updateComAuditByUnProcessed", param);
	}
	
	/**
	 * 只更新记录为PROCESSED状态的记录
	 * 
	 * @param param
	 * @return
	 */
	public int updateComAuditByProcessed(Map<String, Object> param){
		return super.update("updateComAuditByProcessed", param);
	}	
	
	/**
	 * 根据条件统计活动数量
	 * 
	 * @param param
	 * @return
	 */
	public Integer countActivityNum(Map<String, Object> param){
		return super.get("countActivityNum", param);
	}
	/**
	 * 根据条件统计订单数量
	 * @param param
	 * @return
	 */
	public Integer countOrderNum(Map<String, Object> param){
		return super.get("countOrderNum", param);
	}
	
	/**
	 * 根据条件统计活动数量
	 * 
	 * @param param
	 * @return
	 */
	public List<Map<String, Object>> countGroupActivityNum(Map<String, Object> param){
		return super.queryForList("countGroupActivityNum", param);
	}
	/**
	 * 根据条件统计订单数量
	 * @param param
	 * @return
	 */
	public List<Map<String, Object>> countGroupOrderNum(Map<String, Object> param){
		return super.queryForList("countGroupOrderNum", param);
	}
	
	/**
	 * 将订单的审核状态更新为已处理
	 * 
	 * @param param
	 */
	public int updateComAuditToProcessed(Long objectId,String auditType,String operatorName){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("auditType", auditType);
		param.put("operatorName", operatorName);
		return super.update("updateComAuditToProcessed", param);
	}
	
	/**
	 * 将子订单的审核状态更新为已处理
	 * 
	 * @param param
	 */
	public int updateChildOrderAuditToProcessed(Long objectId,String auditType,String operatorName){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("auditType", auditType);
		param.put("operatorName", operatorName);
		return super.update("updateChildOrderAuditToProcessed", param);
	}
	
	
	public void markValid(final Long auditId){
		super.update("markValid", auditId);
	}
	
	/**
	 * 更新状态为不可再分单
	 * @param auditId
	 */
	public void markCanNotReaudit(final Long auditId){
		super.update("markCanNotReaudit", auditId);
	}
	
	public int updateComAuditValid(final Long auditId){
		return super.update("updateComAuditValid", auditId);
	}
	
	/**
	 * 查询根据订单号查询com_audit 数据
	 * */
	public List<ComAudit> queryComAuditByObjectId(Long objectId){
		Map<String,Object> params = new HashMap<String, Object>();
		if(objectId == null){
			objectId = 0L;
		}
		params.put("objectId", objectId);
		return super.getList("queryComAuditByObjectId", params);
	}
	
	/**
	 * 根据auditId来更改audit_status为PROCESSED
	 * */
	public int updateComAuditStatusByAuditId(long auditId){
		return super.update("updateComAuditStatusByAuditId", auditId);
	}
	/**
	 * 修改活动状态
	 * @param param 
	 * @return
	 */
	public int updateComAuditStatus(Map<String, Object> param) {
		return super.update("updateComAuditStatus", param);
	}

	/**
	 * 修改下次分单时间
	 * @param
	 * @return
	 * */
	public int updateNextAssignTime(Map<String, Object> param){return super.update("updateNextAssignTime",param);}
	
	//查询是否有出票失败的预定通知
	public List<Long> getOrderItemIdsByFlightTicketFail(List<Long> orderItemIds) {
		return super.queryForList("getOrderItemIdsByFlightTicketFail", orderItemIds);
	}
	public int queryOrderListCount(Map<String, Object> param){
		return super.get("queryOrderListCount", param);
	}
	public List<ComAudit> queryOrderAuditList(Map<String, Object> param){
		return super.queryForList("queryOrderAuditList", param);
	}
	
	public List<ComAuditActiviNum> countActivityUnprocessedNum(Map<String, Object> param){
		return super.queryForList("countActivityUnprocessedNum", param);
	}
	
	public List<ComAudit> queryAuditListByConditionByNewConsole(Map<String, Object> param){
		return super.queryForList("queryAuditListByNewConsole", param);
	}
	public Integer queryAuditListByConditionByNewConsoleCount(Map<String, Object> param){
		return super.get("queryAuditListByNewConsoleCount", param);
	}
	public int updateComAuditByAuditlist(List<Long> auditIdlist){
		return super.update("updateComAuditByAuditlist", auditIdlist);
	}
}