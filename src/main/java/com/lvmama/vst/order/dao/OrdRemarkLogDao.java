package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdRemarkLog;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdRemarkLogDao extends MyBatisDao {
	public OrdRemarkLogDao(){
		super("ORD_ORDER_REMARK_LOG");
	}
	public int insert(OrdRemarkLog ordRemarkLog){
		return super.insert("insert", ordRemarkLog);
	}
	public int insertSelective(OrdRemarkLog ordRemarkLog){
		return super.insert("insertSelective", ordRemarkLog);
	}
	public int deleteByPrimaryKey(Long id){
		return super.delete("deleteByPrimaryKey", id);
	}
	public int updateByPrimaryKey(OrdRemarkLog ordRemarkLog){
		return super.update("updateByPrimaryKey", ordRemarkLog);
	}
	public int updateByPrimaryKeySelective(OrdRemarkLog ordRemarkLog){
		return super.update("updateByPrimaryKeySelective", ordRemarkLog);
	}
	public OrdRemarkLog selectByPrimaryKey(Long id){
		return super.get("selectByPrimaryKey", id);
	}
	public List<OrdRemarkLog> findOrdRemarkLogList(Map<String, Object> params){
		return super.queryForList("selectByParams", params);
	}
	public Integer getTotalCount(Map<String, Object> params){
		return super.get("getTotalCount", params);
	}
}
