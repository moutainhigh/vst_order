package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.po.OrdPayProcessJob;
@Repository
public class OrdPayProcessJobDao extends MyBatisDao{

	public OrdPayProcessJobDao(){
		super("ORD_PAY_PROCESS_JOB");
	}
	
	public int insert(OrdPayProcessJob record){
		return super.insert("insert", record);
	} 

	public OrdPayProcessJob selectByPrimaryKey(Long orderId){
		return super.get("selectByPrimaryKey", orderId);
	}
	
	public List<Long> selectValidOrderIdList(){
		Map<String, Object> param = new HashMap<String, Object>();
		return super.queryForList("selectValidOrderIdList", param);
	}
	
	public int addTimes(Long orderId){
		return super.update("addTimes", orderId);
	}
	
	public int makeValid(Long orderId){
		return super.update("makeValid", orderId);
	}
	
}
