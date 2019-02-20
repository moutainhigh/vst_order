package com.lvmama.vst.order.dao.goods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsShareTotalStock;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsShareTotalStockOraDao extends MyBatisOraDao {

	public SuppGoodsShareTotalStockOraDao() {
		super("SUPP_GOODS_SHARE_TOTAL_STOCK");
	}

    /**
     * 根据主键查询
     * @param stockId 共享总库存ID
     * @return 共享总库存
     */
    public SuppGoodsShareTotalStock selectByPrimaryKey(Long stockId) {
        return super.get("selectByPrimaryKey", stockId);
    }

    public Integer getTotalStockCount(Map<String, Object> params) {
        return super.get("getTotalStockCount", params);
    }

    public List<SuppGoodsShareTotalStock> selectByParams(Map<String, Object> params) {
        return super.queryForList("selectByParams", params);
    }

	/**
	 * 根据主键删除
	 * @param stockId 共享总库存ID
	 * @return 删除
	 */
	public Integer deleteByPrimaryKey(Long stockId) {
		return super.delete("deleteByPrimaryKey", stockId);
	}

    public Integer deleteByGroupId(Long groupId) {
        return super.delete("deleteByGroupId", groupId);
    }

	/**
	 * 全字段插入
	 * @param suppGoodsShareTotalStock 共享总库存
	 * @return 插入
	 */
	public Integer insert(SuppGoodsShareTotalStock suppGoodsShareTotalStock) {
		return super.insert("insert", suppGoodsShareTotalStock);
	}

    public Integer updateByStockIdAndGroupId(SuppGoodsShareTotalStock suppGoodsShareTotalStock) {
        return super.update("updateByStockIdAndGroupId", suppGoodsShareTotalStock);
    }
    
    public int updateStockForOrder(Long shareTotalStockId, Long stock) {
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareTotalStockId", shareTotalStockId);
        params.put("stock", stock);
    	return super.update("updateStockForOrder", params);
    }
}