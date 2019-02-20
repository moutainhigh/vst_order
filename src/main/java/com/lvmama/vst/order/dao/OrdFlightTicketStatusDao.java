package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdFlightTicketStatusDao extends MyBatisDao {

	public OrdFlightTicketStatusDao() {
		super("ORD_FLIGHT_TICKET_STATUS");
	}

	public int insert(OrdFlightTicketStatus ordFlightTicketStatus) {
		return super.insert("insert", ordFlightTicketStatus);
	}

	public int update(Map<String, Object> params) {
		return super.update("update", params);
	}
	
	public int update(OrdFlightTicketStatus ordFlightTicketStatus) {
		return super.update("updateByPrimaryKey", ordFlightTicketStatus);
	}
	
	public OrdFlightTicketStatus selectByPrimaryKey(Long statusId) {
		return super.get("selectByPrimaryKey", statusId);
	}

	public List<OrdFlightTicketStatus> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	
	public Long getTotalCount(Map<String, Object> params){
		return super.get("getTotalCount", params);
	}

	//查询是否出票成功
	public List<Long> getOrderItemIdsByTicketSuccess(List<Long> orderItemIds) {
		return super.queryForList("getOrderItemIdsByTicketSuccess", orderItemIds);
	}
}