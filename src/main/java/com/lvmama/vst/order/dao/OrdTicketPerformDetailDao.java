package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTicketPerformDetail;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdTicketPerformDetailDao extends MyBatisDao {

	public OrdTicketPerformDetailDao() {
		super("ORD_TICKET_PERFORM_DETAIL");
	}

	public int insert(OrdTicketPerformDetail record) {
		return super.insert("insert", record);
	}
	
	public Long selectCountByOrderItem(Long orderItemId){
		return super.get("selectCountByOrderItem",orderItemId);
	}

	public OrdTicketPerformDetail selectByPrimaryKey(Long ordTicketPerformDetailId) {
		return super.get("selectByPrimaryKey", ordTicketPerformDetailId);
	}
	
	public List<OrdTicketPerformDetail> selectByOrderItem(Long orderItemId){
		List<OrdTicketPerformDetail> list=super.queryForList("selectByOrderItem",orderItemId);
		return list;
	}

	public List<OrdTicketPerformDetail> findOrdTicketPerformDetailList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}