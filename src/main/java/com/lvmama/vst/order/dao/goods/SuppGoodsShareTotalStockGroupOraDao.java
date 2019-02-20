package com.lvmama.vst.order.dao.goods;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsShareTotalStockGroup;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsShareTotalStockGroupOraDao extends MyBatisOraDao {

	public SuppGoodsShareTotalStockGroupOraDao() {
		super("SG_SHARE_TOTAL_STOCK_GROUP");
	}

    public List<SuppGoodsShareTotalStockGroup> selectByGroupId(Long groupId) {
        return super.queryForList("selectByGroupId", groupId);
    }

    public Integer getTotalStockGroupCount(Map<String, Object> params) {
        return super.get("getTotalStockGroupCount", params);
    }

    public List<SuppGoodsShareTotalStockGroup> selectByGoodsIds(List<Long> suppGoodsIds) {
        return super.queryForList("selectByGoodsIds", suppGoodsIds);
    }

    public List<SuppGoodsShareTotalStockGroup> selectByParams(Map<String, Object> params) {
        return super.queryForList("selectByParams", params);
    }
    
    public List<SuppGoodsShareTotalStockGroup> selectByCondition(Map<String, Object> params) {
        return super.queryForList("selectByCondition", params);
    }
    
    public Integer countByGroupId(Long groupId) {
        return super.get("countByGroupId", groupId);
    }

    public void deleteByParams(Map<String, Object> params) {
        super.delete("deleteByParams", params);
    }

    public Integer insertList(List<SuppGoodsShareTotalStockGroup> list) {
        return this.insert("insertList", list);
    }

    public Integer insertGoodsIds(List<Long> goodsIds) {
        return this.insert("insertGoodsIds", goodsIds);
    }
}