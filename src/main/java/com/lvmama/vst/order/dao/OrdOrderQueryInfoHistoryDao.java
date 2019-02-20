package com.lvmama.vst.order.dao;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderQueryInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderQueryInfoHistoryDao extends MyBatisDao {

	public OrdOrderQueryInfoHistoryDao() {
		super("ORD_ORDER_QUERY_INFO_HISTORY");
	}

	public int insert(OrdOrderQueryInfo orderQueryInfo) {
		return super.insert("insert", orderQueryInfo);
	}

	public int updateByPrimaryKey(OrdOrderQueryInfo orderQueryInfo) {
		return super.update("updateByPrimaryKey", orderQueryInfo);
	}

	public int updateByOrderId(OrdOrderQueryInfo orderQueryInfo) {
		return super.update("updateByOrderId", orderQueryInfo);
	}
	
	public int updateByOrderItemId(OrdOrderQueryInfo orderQueryInfo) {
		return super.update("updateByOrderItemId", orderQueryInfo);
	}
}