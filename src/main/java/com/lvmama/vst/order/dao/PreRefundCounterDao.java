package com.lvmama.vst.order.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.PreRefundCounter;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class PreRefundCounterDao extends MyBatisDao{
	
    public PreRefundCounterDao() {
		super("PRE_REFUND_COUNTER");
		// TODO Auto-generated constructor stub
	}    
    
    public int increase(Long userId,String mobie){
    	PreRefundCounter record = new PreRefundCounter();
    	record.setUserId(userId);
    	record.setMobie(mobie);
    	int count=super.update("increase", record);
    	if(count==0){
    		record.setCount(1L);
    		count = insert(record);
    	}
    	return count;
    }

    public int insert(PreRefundCounter record){
    	return super.insert("insert", record);
    }
    
    
    public int selectCount(Map<String,Object> params){
    	Integer count = super.get("selectCount",params);
    	if(count==null){
    		count=0;
    	}
    	return count;
    }
}