package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdSmsSendDao  extends MyBatisDao {
	public OrdSmsSendDao(){
		super("ORD_SMS_SEND");
	}
	public int deleteByPrimaryKey(Long smsId){
    	return super.delete("deleteByPrimaryKey", smsId);
    }

	public int insert(OrdSmsSend record){
    	return super.insert("insert", record);
    }

	public int insertSelective(OrdSmsSend record){
    	return super.insert("insertSelective", record);
    }

	public OrdSmsSend selectByPrimaryKey(Long smsId){
    	return super.get("selectByPrimaryKey", smsId);
    }

	public int updateByPrimaryKeySelective(OrdSmsSend record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

	public int updateByPrimaryKey(OrdSmsSend record){
    	return super.update("updateByPrimaryKey", record);
    }
    public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

    public List<OrdSmsSend> findOrdSmsSendList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
}