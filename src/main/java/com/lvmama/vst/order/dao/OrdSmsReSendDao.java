package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdSmsReSend;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdSmsSend;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdSmsReSendDao  extends MyBatisDao {
    public OrdSmsReSendDao(){
        super("ORD_SMS_RESEND");
    }
    public int deleteByPrimaryKey(Long smsId){
        return super.delete("deleteByPrimaryKey", smsId);
    }

    public int insert(OrdSmsReSend record){
        return super.insert("insert", record);
    }

    public int insertSelective(OrdSmsReSend record){
        return super.insert("insertSelective", record);
    }

    public OrdSmsReSend selectByPrimaryKey(Long smsId){
        return super.get("selectByPrimaryKey", smsId);
    }

    public int updateByPrimaryKeySelective(OrdSmsReSend record){
        return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdSmsReSend record){
        return super.update("updateByPrimaryKey", record);
    }
    public Integer getTotalCount(Map<String, Object> params) {
        return super.get("getTotalCount", params);
    }

    public List<OrdSmsReSend> findOrdSmsSendList(Map<String, Object> params) {
        return super.queryForList("selectByParams", params);
    }
    public int updateByPrimaryKeyEmail(List<Long> ids){
        return super.update("updateByPrimaryKeyEmail", ids);
    }
}