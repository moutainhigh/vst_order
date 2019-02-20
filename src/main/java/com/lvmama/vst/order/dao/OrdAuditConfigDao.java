package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAuditConfig;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdAuditConfigDao extends MyBatisDao {

	public OrdAuditConfigDao() {
		super("ORD_AUDITCONFIG");
	}

	public int deleteByPrimaryKey(Long OrdAuditConfigId) {
		return super.delete("deleteByPrimaryKey", OrdAuditConfigId);
	}

	public int deleteOrdAuditConfigs (Map<String, Object> params){
		return super.delete("deleteOrdAuditConfigsByParams", params);
	}

	public int insert(OrdAuditConfig record) {
		return super.insert("insert", record);
	}

	public int insertSelective(OrdAuditConfig record) {
		return super.insert("insertSelective", record);
	}

	public OrdAuditConfig selectByPrimaryKey(Long OrdAuditConfigId) {
		return super.get("selectByPrimaryKey", OrdAuditConfigId);
	}

	public int updateByPrimaryKeySelective(OrdAuditConfig record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdAuditConfig record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<OrdAuditConfig> findOrdAuditConfigList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	public List<OrdAuditConfig> findOrdAuditConfigListGroupBy(Map<String, Object> params) {
		return super.queryForList("selectByParamsGroupBy", params);
	}
	

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}