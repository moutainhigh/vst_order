package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdSettlementPriceRecordDao extends MyBatisDao {
	public OrdSettlementPriceRecordDao() {
		super("ORD_SETTLEMENT_PRICE_RECORD");
	}

	public int insert(OrdSettlementPriceRecord OrdSettlementPriceRecord) {
		return super.insert("insert", OrdSettlementPriceRecord);
	}

	public OrdSettlementPriceRecord selectByPrimaryKey(Long OrdSettlementPriceRecordId) {
		return super.get("selectByPrimaryKey", OrdSettlementPriceRecordId);
	}

	public int updateByPrimaryKeySelective(OrdSettlementPriceRecord OrdSettlementPriceRecord) {
		return super.update("updateByPrimaryKeySelective", OrdSettlementPriceRecord);
	}

	public int updateByPrimaryKey(OrdSettlementPriceRecord OrdSettlementPriceRecord) {
		return super.update("updateByPrimaryKey", OrdSettlementPriceRecord);
	}
	
	public List<OrdSettlementPriceRecord> findOrdSettlementPriceRecordList(HashMap<String,Object> params){
		return super.getList("selectByParams", params);
	}
	
	public Integer findOrdSettlementPriceRecordCounts(HashMap<String,Object> params){
		return super.get("selectByParamsCounts",params);
	}
}