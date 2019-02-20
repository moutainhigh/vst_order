package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdFormInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdFormInfoDao extends MyBatisDao {

	public OrdFormInfoDao() {
		super("ORD_FORM_INFO");
	}

	public int deleteByPrimaryKey(Long ordFormInfoId) {
		return super.delete("deleteByPrimaryKey", ordFormInfoId);
	}

	public int insert(OrdFormInfo record) {
		return super.insert("insert", record);
	}

	public int insertSelective(OrdFormInfo record) {
		return super.insert("insertSelective", record);
	}

	public OrdFormInfo selectByPrimaryKey(Long ordFormInfoId) {
		return super.get("selectByPrimaryKey", ordFormInfoId);
	}

	public int updateByPrimaryKeySelective(OrdFormInfo record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdFormInfo record) {
		return super.update("updateByPrimaryKey", record);
	}
	
	public int updateContentByPrimaryKey(Map<String, Object> params) {
		return super.update("updateContentByPrimaryKey", params);
	}

	public List<OrdFormInfo> findOrdFormInfoList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}