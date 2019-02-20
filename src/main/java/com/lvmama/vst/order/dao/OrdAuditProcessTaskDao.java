package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.po.OrdAuditProcessTask;

@Repository
public class OrdAuditProcessTaskDao extends MyBatisDao{

	public OrdAuditProcessTaskDao(){
		super("ORD_AUDIT_PROCESS_TASK");
	}
	
	public int insert(OrdAuditProcessTask record){
		return super.insert("insert", record);
	} 

	public OrdAuditProcessTask selectByPrimaryKey(Long orderId){
		return super.get("selectByPrimaryKey", orderId);
	}
	
	public List<Long> selectValidOrderIdList(){
		Map<String, Object> param = new HashMap<String, Object>();
		return super.queryForList("selectValidOrderIdList", param);
	}
	
	public int addTimes(Long orderId){
		return super.update("addTimes", orderId);
	}
	
	public int makeSuccess(Long orderId){
		return super.update("makeSuccess", orderId);
	}
	
	public int makeValid(Long orderId){
		return super.update("makeValid", orderId);
	}
	
}
