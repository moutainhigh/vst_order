package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdFunctionDao extends MyBatisDao {

	public OrdFunctionDao() {
		super("ORD_FUNCTION");
	}

	public int deleteByPrimaryKey(Long ordFunctionId) {
		return super.delete("deleteByPrimaryKey", ordFunctionId);
	}

	public int insert(OrdFunction record) {
		return super.insert("insert", record);
	}

	public int insertSelective(OrdFunction record) {
		return super.insert("insertSelective", record);
	}

	public OrdFunction selectByPrimaryKey(Long ordFunctionId) {
		return super.get("selectByPrimaryKey", ordFunctionId);
	}

	public int updateByPrimaryKeySelective(OrdFunction record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdFunction record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<OrdFunction> findOrdFunctionList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}