package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdPaymentInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdPaymentInfoDao extends MyBatisDao {

	public OrdPaymentInfoDao() {
		super("ORD_PAYMENT_INFO");
	}

	public int insert(OrdPaymentInfo ordPaymentInfo) {
		return super.insert("insert", ordPaymentInfo);
	}

	public List<OrdPaymentInfo> findOrdPaymentInfoList(Map<String, Object> params) {
		return super.queryForList("queryByCondition", params);
	}	
}