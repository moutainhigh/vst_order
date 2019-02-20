package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdSmsTemplateDao  extends MyBatisDao{
	public OrdSmsTemplateDao(){
		super("ORD_SMS_TEMPLATE");
	}
	public int deleteByPrimaryKey(Long templateId){
    	return super.delete("deleteByPrimaryKey", templateId);
    }

	public int insert(OrdSmsTemplate record){
    	return super.insert("insert", record);
    }

	public int insertSelective(OrdSmsTemplate record){
    	return super.insert("insertSelective", record);
    }

	public OrdSmsTemplate selectByPrimaryKey(Long templateId){
    	return super.get("selectByPrimaryKey", templateId);
    }

	public int updateByPrimaryKeySelective(OrdSmsTemplate record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

	public int updateByPrimaryKey(OrdSmsTemplate record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    public List<OrdSmsTemplate> findOrdSmsTemplateList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
    
    public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}
    public boolean isNameExists(Map<String, Object> params) {
		return (Integer) super.get("isNameExists", params) > 0;
	}
}