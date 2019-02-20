package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdWechatTemplate;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdWechatTemplateDao extends MyBatisDao {
	public OrdWechatTemplateDao(){
		super("ORD_WECHAT_TEMPLATE");
	}
	public int insert(OrdWechatTemplate ordWechatTemplate){
		return super.insert("insert", ordWechatTemplate);
	}
	public int insertSelective(OrdWechatTemplate ordWechatTemplate){
		return super.insert("insertSelective", ordWechatTemplate);
	}
	public int deleteByPrimaryKey(Long id){
		return super.delete("deleteByPrimaryKey", id);
	}
	public int updateByPrimaryKey(OrdWechatTemplate ordWechatTemplate){
		return super.update("updateByPrimaryKey", ordWechatTemplate);
	}
	public int updateByPrimaryKeySelective(OrdWechatTemplate ordWechatTemplate){
		return super.update("updateByPrimaryKeySelective", ordWechatTemplate);
	}
	public OrdWechatTemplate selectByPrimaryKey(Long id){
		return super.get("selectByPrimaryKey", id);
	}
	public List<OrdWechatTemplate> findOrdWechatTemplateList(Map<String, Object> params){
		return super.queryForList("selectByParams", params);
	}
	public Integer getTotalCount(Map<String, Object> params){
		return super.get("getTotalCount", params);
	}
}
