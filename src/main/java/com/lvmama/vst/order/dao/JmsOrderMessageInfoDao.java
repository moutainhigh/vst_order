package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.JmsOrderMessageInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JmsOrderMessageInfoDao extends MyBatisDao {

	public JmsOrderMessageInfoDao() {
		super("JMS_ORDER_MESSAGE_INFO");
	}

	public int deleteByPrimaryKey(Long messageInfoId) {
		return super.delete("deleteByPrimaryKey", messageInfoId);
	}

	public int deleteByOrderId(Long orderId) {
		return super.delete("deleteByOrderId", orderId);
	}

	public int deleteByOrderItemId(Long orderItemId) {
		return super.delete("deleteByOrderItemId", orderItemId);
	}

	public int insert(JmsOrderMessageInfo jmsOrderMessageInfo) {
		return super.insert("insert", jmsOrderMessageInfo);
	}

	public int insertSelective(JmsOrderMessageInfo jmsOrderMessageInfo) {
		return super.insert("insertSelective", jmsOrderMessageInfo);
	}


	public JmsOrderMessageInfo selectByPrimaryKey(Long messageInfoId) {
		return super.get("selectByPrimaryKey", messageInfoId);
	}
	
	public List<JmsOrderMessageInfo> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public int updateByPrimaryKeySelective(JmsOrderMessageInfo jmsOrderMessageInfo) {
		return super.update("updateByPrimaryKeySelective", jmsOrderMessageInfo);
	}

	public int updateByPrimaryKey(JmsOrderMessageInfo jmsOrderMessageInfo) {
		return super.update("updateByPrimaryKey", jmsOrderMessageInfo);
	}
	

	public List<JmsOrderMessageInfo> selectByOrderId(Long orderId) {
		return super.queryForList("selectByOrderId", orderId);
	}

	public List<JmsOrderMessageInfo> selectByOrderItemId(Long orderItemId) {
		return super.queryForList("selectByOrderItemId", orderItemId);
	}

	public int clearTwoMonthAgoMessageInfo(){
		return super.delete("clearTwoMonthAgoMessageInfo",new HashMap());
	}

}