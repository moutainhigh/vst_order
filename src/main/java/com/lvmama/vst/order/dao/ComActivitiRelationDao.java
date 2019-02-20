package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;


@Repository
public class ComActivitiRelationDao extends MyBatisDao {

	public ComActivitiRelationDao() {
		super("COM_ACTIVITI_RELATION");
		// TODO Auto-generated constructor stub
	}
	// int deleteByPrimaryKey(Long activitiRelationId);
	//
	public int insert(ComActivitiRelation record){
		return super.insert("insert", record);
	}
	
	public List<ComActivitiRelation> queryList(ComActivitiRelation record){
		return super.queryForList("queryList",record);
	}
	//
	// int insertSelective(ComActivitiRelation record);
	//
	// ComActivitiRelation selectByPrimaryKey(Long activitiRelationId);
	//
	// int updateByPrimaryKeySelective(ComActivitiRelation record);
	//
	// int updateByPrimaryKey(ComActivitiRelation record);
	
	public List<Long> queryClearProcessByCondition(Map<String, Object> param) {
		return super.getList("queryClearProcessByCondition", param);
	}
	
	public Integer updateProcessStatus(Map<String, Object> params){
		return super.update("updateProcessStatus", params);
	}
}