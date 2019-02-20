package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTicketRefundSerial;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdTicketRefundSerialDao extends MyBatisDao {

	public OrdTicketRefundSerialDao() {
		super("ORD_TICKET_REFUND_SERIAL");
	}

	public int insert(OrdTicketRefundSerial record) {
		return super.insert("insert", record);
	}
	

	public OrdTicketRefundSerial selectByPrimaryKey(Long ordTicketRefundSerialId) {
		return super.get("selectByPrimaryKey", ordTicketRefundSerialId);
	}
	
	
	public List<OrdTicketRefundSerial> findOrdTicketRefundSerialList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}


}