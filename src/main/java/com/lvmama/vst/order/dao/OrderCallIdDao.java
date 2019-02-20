package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.po.OrderCallId;

@Repository
public class OrderCallIdDao extends MyBatisDao {
	
	public OrderCallIdDao(){
		super("ORDER_CALLID");
	}
  
	public List<OrderCallId> selectByParams(Map<String, Object> params) {
		return super.getList("selectByParams", params);
	}
	
	public long insert(OrderCallId orderCallId) {
		return super.insert("insert", orderCallId);
	}	
  
}
