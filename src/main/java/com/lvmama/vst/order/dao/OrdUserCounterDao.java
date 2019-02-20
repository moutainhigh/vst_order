package com.lvmama.vst.order.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdUserCounter;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdUserCounterDao extends MyBatisDao{
	
    public OrdUserCounterDao() {
		super("ORD_USER_COUNTER");
		// TODO Auto-generated constructor stub
	}
    
    public void deleteAll(){
    	super.delete("deleteAll", 1L);
    }
    
    public int increase(String user,String objectType){
    	OrdUserCounter record = new OrdUserCounter();
    	record.setUserName(user);
    	record.setObjectType(objectType);
    	int count=super.update("increase", record);
    	if(count==0){
    		record.setOrderCount(1L);
    		count = insert(record);
    	}
    	return count;
    }

    public int insert(OrdUserCounter record){
    	return super.insert("insert", record);
    }
    
	/*int deleteByPrimaryKey(Long userCounterId);

    

    int insertSelective(OrdUserCounter record);

    OrdUserCounter selectByPrimaryKey(Long userCounterId);

    int updateByPrimaryKeySelective(OrdUserCounter record);

    int updateByPrimaryKey(OrdUserCounter record);*/
    
    public int selectCount(Map<String,Object> params){
    	Integer count = super.get("selectCount",params);
    	if(count==null){
    		count=0;
    	}
    	return count;
    }
}