package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdFlightTicketInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdFlightTicketInfoDao extends MyBatisDao {

	public OrdFlightTicketInfoDao() {
		super("ORD_FLIGHT_TICKET_INFO");
	}

	public int insert(OrdFlightTicketInfo ordFlightTicketInfo) {
		return super.insert("insert", ordFlightTicketInfo);
	}

	public int update(Map<String, Object> params) {
		return super.update("update", params);
	}
	
	public int update(OrdFlightTicketInfo ordFlightTicketInfo) {
		return super.update("updateByPrimaryKey", ordFlightTicketInfo);
	}
	
	public OrdFlightTicketInfo selectByPrimaryKey(Long infoId) {
		return super.get("selectByPrimaryKey", infoId);
	}

	public List<OrdFlightTicketInfo> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
}