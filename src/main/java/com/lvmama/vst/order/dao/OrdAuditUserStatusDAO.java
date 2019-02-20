package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.vo.UserOrderCountVO;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 * 员工工作状态数据库访问层
 * 
 * @author wenzhengtao
 *
 */
@Repository("ordAuditUserStatusDAO")
public class OrdAuditUserStatusDAO extends MyBatisDao{
    
	public OrdAuditUserStatusDAO() {
		super("ORD_AUDIT_USER_STATUS");
	}

	public int deleteByPrimaryKey(String operatorName){
		return super.delete("deleteByPrimaryKey", operatorName);
	}

    public int insert(OrdAuditUserStatus record){
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdAuditUserStatus record){
    	return super.insert("insertSelective", record);
    }

    public OrdAuditUserStatus selectByPrimaryKey(String operatorName){
    	return super.get("selectByPrimaryKey", operatorName);
    }

    public int updateByPrimaryKeySelective(OrdAuditUserStatus record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdAuditUserStatus record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    
    
    public List<OrdAuditUserStatus> findOrdStatusGroupList(Map<String, Object> params) {

		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}
	
	public OrdAuditUserStatus getRandomUserByUsers(String objectType,List<String> userIds){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("objectType", objectType);
		params.put("userIds", userIds);
		return super.get("getRandomUserByUsers",params);
	}
	
	public OrdAuditUserStatus getRandomUserByOrgIds(String objectType,List<Long> orgIds){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("objectType", objectType);
		params.put("orgIds", orgIds);
		return super.get("getRandomUserByOrgIds",params);
	}
	
	public OrdAuditUserStatus getMinTaskCountRandomUserByOrgIds(String auditType,List<Long> orgIds){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("auditType", auditType);
		params.put("orgIds", orgIds);
		return super.get("getMinTaskCountRandomUserByOrgIds",params);
	}
	
	public List<UserOrderCountVO> getUserOrderCount(String objectType,List<String> userIds){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("objectType", objectType);
		params.put("userIds", userIds);
		return super.queryForList("getUserOrderCount",params);
	}
}