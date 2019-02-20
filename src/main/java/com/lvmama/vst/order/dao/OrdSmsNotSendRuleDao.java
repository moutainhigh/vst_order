package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdSmsNotSendRuleDao extends MyBatisDao {
    public OrdSmsNotSendRuleDao() {
		super("ORD_SMS_NOT_SEND_RULE");
	}

    public int deleteByPrimaryKey(Long ruleId){
		return super.delete("deleteByPrimaryKey", ruleId);
	}

    public int insert(OrdSmsNotSendRule record){
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdSmsNotSendRule record){
    	return super.insert("insertSelective",record);
    }

    public OrdSmsNotSendRule selectByPrimaryKey(Long ruleId){
    	return super.get("selectByPrimaryKey", ruleId);
    }

    public int updateByPrimaryKeySelective(OrdSmsNotSendRule record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdSmsNotSendRule record){
    	return super.update("updateByPrimaryKey", record);
    }
    public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

    public List<OrdSmsNotSendRule> findOrdSmsNotSendRuleList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
    
}