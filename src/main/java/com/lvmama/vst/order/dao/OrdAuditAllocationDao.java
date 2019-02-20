package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAuditAllocation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdAuditAllocationDao extends MyBatisDao{
    public OrdAuditAllocationDao() {
		super("ORD_AUDIT_ALLOCATION");
		// TODO Auto-generated constructor stub
	}
    
    public List<OrdAuditAllocation> selectList(Map<String,Object> params){
    	return super.queryForList("selectList",params);
    }
    
    public Integer selectCount(Map<String,Object> params){
    	return super.get("selectListCount",params);
    }

	/*int deleteByPrimaryKey(Long ordAllocationId);

    int insert(OrdAuditAllocation record);

    int insertSelective(OrdAuditAllocation record);

    OrdAuditAllocation selectByPrimaryKey(Long ordAllocationId);

    int updateByPrimaryKeySelective(OrdAuditAllocation record);

    int updateByPrimaryKey(OrdAuditAllocation record);*/
    
    public int deleteByPrimaryKey(Long ordAllocationId){
    	return super.delete("deleteByPrimaryKey", ordAllocationId);
    }
    
    public int insert(OrdAuditAllocation record){
    	return super.insert("insert", record);
    }
    
    public OrdAuditAllocation selectByPrimaryKey(Long ordAllocationId){
    	return super.get("selectByPrimaryKey", ordAllocationId);
    }
    
    public int updateByPrimaryKeySelective(OrdAuditAllocation record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdAuditAllocation record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    /**
	 * map动态查询
	 * 
	 * @param param
	 * @return
	 */
	public List<OrdAuditAllocation> queryOrdAuditAllocationListByParam(Map<String, Object> param){
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