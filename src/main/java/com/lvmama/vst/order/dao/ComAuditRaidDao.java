package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import com.lvmama.vst.back.pub.po.ComAuditRaid;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
@Repository
public class ComAuditRaidDao extends MyBatisDao{

	public ComAuditRaidDao(){
		super("COM_AUDIT_RAID");
	}
	
	public int insert(ComAuditRaid record){
		return super.insert("insert", record);
	} 

	public int insertSelective(ComAuditRaid record){
		return super.insert("insertSelective", record);
	}
	
	public ComAuditRaid selectByPrimaryKey(Long id){
		return super.get("selectByPrimaryKey", id);
	}
	
	public int updateComAuditRaidByAuditlist(List<Long> auditIdlist){
		return super.update("updateComAuditRaidByAuditlist", auditIdlist);
	}
	
	/**
	 * 动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<ComAuditRaid> queryAuditListByCondition(Map<String, Object> param){
		return super.queryForList("queryAuditListByCondition", param);
	}
}
