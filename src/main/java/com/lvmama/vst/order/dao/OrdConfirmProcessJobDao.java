package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.po.OrdConfirmProcessJob;
@Repository
public class OrdConfirmProcessJobDao extends MyBatisDao{

	public OrdConfirmProcessJobDao(){
		super("ORD_CONFIRM_PROCESS_JOB");
	}
	
	public int insert(OrdConfirmProcessJob record){
		return super.insert("insert", record);
	} 

	public OrdConfirmProcessJob selectByPrimaryKey(Long orderItemId){
		return super.get("selectByPrimaryKey", orderItemId);
	}
	
	public List<OrdConfirmProcessJob> selectValidOrdConfirmProcessJobList(){
		Map<String, Object> param = new HashMap<String, Object>();
		return super.queryForList("selectValidOrdConfirmProcessJobList", param);
	}
	
	public int addTimes(Long orderItemId){
		return super.update("addTimes", orderItemId);
	}
	
	public int makeValid(Long orderItemId){
		return super.update("makeValid", orderItemId);
	}
	
}
