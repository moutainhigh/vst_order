package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * 
 * @author sunjian
 *
 */
@Repository
public class OrdTravelContractDAO extends MyBatisDao {
    public OrdTravelContractDAO() {
		super("ORD_TRAVEL_CONTRACT");
	}

    public int deleteByPrimaryKey(Long ordContractId) {
    	return super.delete("deleteByPrimaryKey", ordContractId);
    }

    public int insert(OrdTravelContract record) {
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdTravelContract record) {
    	return super.insert("insertSelective", record);
    }

    public OrdTravelContract selectByPrimaryKey(Long ordContractId) {
    	return super.get("selectByPrimaryKey", ordContractId);
    }

    public int updateByPrimaryKeySelective(OrdTravelContract record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdTravelContract record) {
    	return super.update("updateByPrimaryKey", record);
    }
    
    public List<OrdTravelContract> selectByParam(Map<String, Object> params) {
    	return super.queryForList("selectByParam", params);
    }
    
    public int updateContractStatusByOrderId(Map<String, Object> params) {
    	return super.update("updateContractStatusByOrderId", params);
    }
    public int updateSendEmailFlag(Set<Long> ids) {
    	Map<String,Object> params=new HashMap<String, Object>();
    	params.put("ids", ids);
    	return super.update("updateSendEmailFlag",params);
    }

    public List<Map<String, Object>> selectPushDataByParam(Map<String, Object> params) {
    	return super.queryForList("selectPushDataByParam", params);
    }
    
    public int updatePushDataByContractId(Map<String, Object> params) {
    	return super.update("updatePushDataByContractId", params);
    }
}