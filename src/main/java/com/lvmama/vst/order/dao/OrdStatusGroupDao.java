package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdStatusGroup;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdStatusGroupDao extends MyBatisDao {

	public OrdStatusGroupDao() {
		super("ORD_STATUSGROUP");
	}

	public int deleteByPrimaryKey(Long statusGroupId) {
		return super.delete("deleteByPrimaryKey", statusGroupId);
	}

	public int insert(OrdStatusGroup record) {
		return super.insert("insert", record);
	}

	public int insertSelective(OrdStatusGroup record) {
		return super.insert("insertSelective", record);
	}

	public OrdStatusGroup selectByPrimaryKey(Long statusGroupId) {
		return super.get("selectByPrimaryKey", statusGroupId);
	}

	public int updateByPrimaryKeySelective(OrdStatusGroup record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdStatusGroup record) {
		return super.update("updateByPrimaryKey", record);
	}

	
	
	
	public List<OrdStatusGroup> findOrdStatusGroupList(Map<String, Object> params) {

		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}