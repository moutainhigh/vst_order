package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAuditAllocationRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdAuditAllocationRelationDao extends MyBatisDao{
    
	public OrdAuditAllocationRelationDao() {
		super("ORD_AUDIT_ALLOCATION_RELATION");
		// TODO Auto-generated constructor stub
	}

	/*int deleteByPrimaryKey(Long relationId);

    int insert(OrdAuditAllocationRelation record);

    int insertSelective(OrdAuditAllocationRelation record);

    OrdAuditAllocationRelation selectByPrimaryKey(Long relationId);

    int updateByPrimaryKeySelective(OrdAuditAllocationRelation record);

    int updateByPrimaryKey(OrdAuditAllocationRelation record);*/
	
	
	public int deleteByPrimaryKey(Long relationId){
    	return super.delete("deleteByPrimaryKey", relationId);
    }
    
    public int insert(OrdAuditAllocationRelation record){
    	return super.insert("insert", record);
    }
    
    public int updateByPrimaryKeySelective(OrdAuditAllocationRelation record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdAuditAllocationRelation record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    public int deleteByOrdAllocationId(Long ordAllocationId){
    	return super.delete("deleteByOrdAllocationId", ordAllocationId);
    }
    
	    
	/**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<OrdAuditAllocationRelation> queryOrdAuditAllocationRelationListByParam(Map<String, Object> param){
		return super.queryForList("selectByParams", param);
	}
	
	/**
	 * 动态统计
	 * 
	 * @param param
	 * @return
	 */
	public Integer getTotalCount(Map<String, Object> param) {
		return super.get("getTotalCount", param);
	}
}