package com.lvmama.vst.order.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderQueryInfoDao extends MyBatisDao {
//	@Autowired
//	private OrdOrderQueryInfoHistoryDao queryInfoHistoryDao;
	
	public OrdOrderQueryInfoDao() {
		super("ORD_ORDER_QUERY_INFO");
	}

	public int insert(OrdOrderQueryInfo orderQueryInfo) {
		return super.insert("insert", orderQueryInfo);
	}

	public int updateByPrimaryKey(OrdOrderQueryInfo orderQueryInfo) {
//		queryInfoHistoryDao.updateByPrimaryKey(orderQueryInfo);
		return super.update("updateByPrimaryKey", orderQueryInfo);
	}

//	public int updateByOrderId(OrdOrderQueryInfo orderQueryInfo) {
//		queryInfoHistoryDao.updateByOrderId(orderQueryInfo);
//		return super.update("updateByOrderId", orderQueryInfo);
//	}
//	
//	public int updateByOrderItemId(OrdOrderQueryInfo orderQueryInfo) {
//		queryInfoHistoryDao.updateByOrderItemId(orderQueryInfo);
//		return super.update("updateByOrderItemId", orderQueryInfo);
//	}
//	
//	public List<Long> findOrdQueryInfoHistoryData() {
//		return super.queryForList("findOrdQueryInfoHistoryData", 0);
//	}
//	
//	public int moveOrdQueryInfoHistoryData(List<Long> queryInfoIds) {
//		return super.insert("moveOrdQueryInfoHistoryData", queryInfoIds);
//	}
//	
//	public int deleteOrdQueryInfoData(List<Long> queryInfoIds) {
//		return super.delete("deleteOrdQueryInfoData", queryInfoIds);
//	}
//	
//	public List<Long> findOrderIdsByCondition(OrderMonitorCnd orderMonitorCnd) {
//		return super.queryForList("queryOrderIdsByCondition", orderMonitorCnd);
//	}
//	
//	public Long findOrderCountByCondition(OrderMonitorCnd orderMonitorCnd) {
//		return super.get("queryOrderCountByCondition", orderMonitorCnd);
//	}
}