package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.ComAuditSortRule;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 订单排序规则数据库访问层
 * 
 * @author majunli
 *
 */
@Repository
public class ComAuditSortRuleDao extends MyBatisDao {

	public ComAuditSortRuleDao() {
		super("COM_AUDIT_SORT_RULE");
	}

	public int deleteByPrimaryKey(Long sortRuleId){
		return super.delete("deleteByPrimaryKey", sortRuleId);
	}
	
	public int insert(ComAuditSortRule record){
		return super.insert("insert", record);
	}
	
	public int insertSelective(ComAuditSortRule record){
		return super.insert("insertSelective", record);
	}
	
	public ComAuditSortRule selectByPrimaryKey(Long sortRuleId){
		return super.get("selectByPrimaryKey", sortRuleId);
	}
	
	public Integer updateByPrimaryKeySelective(ComAuditSortRule record){
		return super.update("updateByPrimaryKeySelective", record);
	}
	
	public int updateByPrimaryKey(ComAuditSortRule record){
		return super.update("updateByPrimaryKey", record);
	}
	
	/**
	 * map动态查询
	 * @param param
	 * @return
	 */
	public List<ComAuditSortRule> selectByParams(Map<String, Object> param){
		return super.queryForList("selectByParams", param);
	}
	
	/**
	 * 动态统计
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param) {
		return super.get("getTotalCount", param);
	}
}