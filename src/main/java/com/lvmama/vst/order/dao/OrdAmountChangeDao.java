package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdAmountChangeDao extends MyBatisDao {
	public OrdAmountChangeDao() {
		super("ORD_AMOUNT_CHANGE");
	}

	public int insert(OrdAmountChange ordAmountChange) {
		return super.insert("insert", ordAmountChange);
	}

	public OrdAmountChange selectByPrimaryKey(Long ordAmountChangeId) {
		return super.get("selectByPrimaryKey", ordAmountChangeId);
	}
	
	public Integer isOrderApproving(Long orderId) {
		return super.get("isApproving", orderId);
	}

	public int updateByPrimaryKeySelective(OrdAmountChange ordAmountChange) {
		return super.update("updateByPrimaryKeySelective", ordAmountChange);
	}

	public int updateByPrimaryKey(OrdAmountChange ordAmountChange) {
		return super.update("updateByPrimaryKey", ordAmountChange);
	}
	
	public List<OrdAmountChange> findOrdAmountChangeList(HashMap<String,Object> params){
		return super.getList("selectByParams", params);
	}
	
	public Integer findOrdAmountChangeCounts(HashMap<String,Object> params){
		return super.get("selectByParamsCounts",params);
	}
	
	public Integer queryApprovingRecords(Long orderId) {
		return super.get("queryApprovingRecords", orderId);
	}
}