package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdFuncRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdFuncRelationDao extends MyBatisDao {

	public OrdFuncRelationDao() {
		super("ORD_FUNCRELATION");
	}

	public int deleteByPrimaryKey(Long ordFunctionRelationId) {
		return super.delete("deleteByPrimaryKey", ordFunctionRelationId);
	}

	public int insert(OrdFuncRelation record) {
		return super.insert("insert", record);
	}

	public int insertSelective(OrdFuncRelation record) {
		return super.insert("insertSelective", record);

	}

	public OrdFuncRelation selectByPrimaryKey(Long ordFunctionRelationId) {
		return super.get("selectByPrimaryKey", ordFunctionRelationId);
	}

	public int updateByPrimaryKeySelective(OrdFuncRelation record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdFuncRelation record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<OrdFuncRelation> findOrdFuncRelationList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

}