package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.supp.po.SuppGoodsIDCardLimit;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdIDCardAgeLimitDao extends MyBatisDao {

	public OrdIDCardAgeLimitDao() {
		super("ORD_IDCARD_LIMIT_AGE");
	}

	public int deleteByPrimaryKey(Long idCardAgelimitId) {
		return super.delete("deleteByPrimaryKey", idCardAgelimitId);
	}

	public int insert(SuppGoodsIDCardLimit record) {
		return super.insert("insert", record);
	}
	
	public List<SuppGoodsIDCardLimit> selectByGoodsIdKey(Long suppGoodsId) {
		return super.getList("selectByGoodsIdKey", suppGoodsId);
	}

	public int updateByPrimaryKey(SuppGoodsIDCardLimit record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<SuppGoodsIDCardLimit> findIDCARDAgeLimitList(Map<String, Object> params){
		return super.queryForList("selectByParams", params);
	}

	public Long queryCount(Long suppGoodsId) {
		return super.get("queryCount", suppGoodsId);
	}

	public SuppGoodsIDCardLimit findSuppGoodsIdcardLimitById(Long idCardAgelimitId){
		return super.get("selectByIdKey",idCardAgelimitId);
	}
}