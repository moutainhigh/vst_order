package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderStockDao extends MyBatisDao {

	public OrdOrderStockDao() {
		super("ORD_ORDER_STOCK");
	}

	public int deleteByPrimaryKey(Long orderStockId) {
		return super.delete("deleteByPrimaryKey", orderStockId);
	}

	public int insert(OrdOrderStock ordOrderStock) {
		return super.insert("insert", ordOrderStock);
	}

	public int insertSelective(OrdOrderStock ordOrderStock) {
		return super.insert("insertSelective", ordOrderStock);
	}

	public OrdOrderStock selectByPrimaryKey(Long orderStockId) {
		return super.get("selectByPrimaryKey", orderStockId);
	}

	public int updateByPrimaryKeySelective(OrdOrderStock ordOrderStock) {
		return super.update("updateByPrimaryKeySelective", ordOrderStock);
	}

	public int updateByPrimaryKey(OrdOrderStock ordOrderStock) {
		return super.update("updateByPrimaryKey", ordOrderStock);
	}
	
	public List<OrdOrderStock> selectByOrderItemId(Long orderItemId) {
		return super.queryForList("selectByOrderItemId", orderItemId);
	}
	
	public int updateStockStatusByOrderItemId(Map<String, Object> params) {
		return super.update("updateStockStatusByOrderItemId", params);
	}
    
    public int updateShareIdByOrderItemId(Map<String,Object> params){
        return super.update("updateShareIdByOrderItemId",params);
    }
}