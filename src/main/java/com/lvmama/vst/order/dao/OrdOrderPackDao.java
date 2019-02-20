package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderPackDao extends MyBatisDao {
	public OrdOrderPackDao() {
		super("ORD_ORDER_PACK");
	}

	public List<OrdOrderPack> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	
	public int deleteByPrimaryKey(Long orderPackId) {
		return super.delete("deleteByPrimaryKey", orderPackId);
	}

	public int insert(OrdOrderPack ordOrderPack) {
		return super.insert("insert", ordOrderPack);
	}

	public int insertSelective(OrdOrderPack ordOrderPack) {
		return super.insert("insertSelective", ordOrderPack);
	}

	public OrdOrderPack selectByPrimaryKey(Long orderPackId) {
		return super.get("selectByPrimaryKey", orderPackId);
	}

	public int updateByPrimaryKeySelective(OrdOrderPack ordOrderPack) {
		return super.update("updateByPrimaryKeySelective", ordOrderPack);
	}

	public int updateByPrimaryKey(OrdOrderPack ordOrderPack) {
		return super.update("updateByPrimaryKey", ordOrderPack);
	}

	public List<OrdOrderPack> selectOrdOrderByOrderIds(List<Long> ids) {
		return super.getList("selectOrdOrderByOrderIds", ids);
	}
}