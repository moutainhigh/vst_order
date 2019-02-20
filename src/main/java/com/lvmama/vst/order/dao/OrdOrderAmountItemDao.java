package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderAmountItemDao extends MyBatisDao {

	public OrdOrderAmountItemDao() {
		super("ORD_ORDER_AMOUNT_ITEM");
	}

	public int deleteByPrimaryKey(Long orderAmountItemId) {
		return super.delete("deleteByPrimaryKey", orderAmountItemId);
	}

	public int insert(OrdOrderAmountItem ordOrderAmountItem) {
		return super.insert("insert", ordOrderAmountItem);
	}

	public int insertSelective(OrdOrderAmountItem ordOrderAmountItem) {
		return super.insert("insertSelective", ordOrderAmountItem);
	}

	public OrdOrderAmountItem selectByPrimaryKey(Long orderAmountItemId) {
		return super.get("selectByPrimaryKey", orderAmountItemId);
	}

	public int updateByPrimaryKeySelective(OrdOrderAmountItem ordOrderAmountItem) {
		return super.update("updateByPrimaryKeySelective", ordOrderAmountItem);
	}

	public int updateByPrimaryKey(OrdOrderAmountItem ordOrderAmountItem) {
		return super.update("updateByPrimaryKey", ordOrderAmountItem);
	}
	
	public List<OrdOrderAmountItem> findOrderAmountItemList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
}