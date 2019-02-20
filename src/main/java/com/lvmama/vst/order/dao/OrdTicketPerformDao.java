package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdTicketPerformDao extends MyBatisDao {

	public OrdTicketPerformDao() {
		super("ORD_TICKET_PERFORM");
	}

	public int deleteByPrimaryKey(Long ordTicketPerformId) {
		return super.delete("deleteByPrimaryKey", ordTicketPerformId);
	}

	public int insert(OrdTicketPerform record) {
		return super.insert("insert", record);
	}
	
	public Long selectCountByOrderItem(Long orderItemId){
		return super.get("selectCountByOrderItem",orderItemId);
	}

	public int insertSelective(OrdTicketPerform record) {
		return super.insert("insertSelective", record);
	}

	public OrdTicketPerform selectByPrimaryKey(Long ordTicketPerformId) {
		return super.get("selectByPrimaryKey", ordTicketPerformId);
	}

	public int updateByPrimaryKeySelective(OrdTicketPerform record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(OrdTicketPerform record) {
		return super.update("updateByPrimaryKey", record);
	}
	
	public OrdTicketPerform selectByOrderItem(Long orderItemId){
		List<OrdTicketPerform> list=super.queryForList("selectByOrderItem",orderItemId);
		if (list != null && list.size() > 0) {
			for (OrdTicketPerform ticket : list) {
				if (ticket.getPerformTime() != null) {
					return ticket;
				}
			}
			return list.get(0);
		}
		return null;
	}
	
	public List<OrdTicketPerform> selectByOrderItems(List<Long> orderItemIds){
		return super.queryForList("selectByOrderItems",orderItemIds);
	}

	public List<OrdTicketPerform> findOrdTicketPerformList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public Integer getTotalCount(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}

}