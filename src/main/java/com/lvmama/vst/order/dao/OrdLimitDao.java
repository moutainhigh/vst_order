package com.lvmama.vst.order.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.supp.po.SuppGoodsLimit;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdLimitDao extends MyBatisDao {

	public OrdLimitDao() {
		super("ORD_LIMIT");
	}

	public int deleteByPrimaryKey(Long limitId) {
		return super.delete("deleteByPrimaryKey", limitId);
	}

	public int insert(SuppGoodsLimit record) {
		return super.insert("insert", record);
	}

	public int insertSelective(SuppGoodsLimit record) {
		return super.insert("insertSelective", record);
	}

	public SuppGoodsLimit selectByPrimaryKey(Long limitId) {
		return super.get("selectByPrimaryKey", limitId);
	}
	
	public SuppGoodsLimit selectByGoodKey(Long goodId) {
		List<SuppGoodsLimit> limits = super.getList("selectByGoodKey", goodId);
		if(CollectionUtils.isEmpty(limits))
			return null;
		return limits.get(0);
	}

	public int updateByPrimaryKeySelective(SuppGoodsLimit record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(SuppGoodsLimit record) {
		return super.update("updateByPrimaryKey", record);
	}
	
}