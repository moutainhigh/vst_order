package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsGroupStock;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

/**
 * 共享库存DAO
 * 
 * @author mayonghua
 * @date 2013-12-09
 */
@Repository
public class SuppGoodsGroupStockOraDao extends MyBatisOraDao {

	public SuppGoodsGroupStockOraDao() {
		super("SUPP_GOODS_GROUP_STOCK");
	}

	public int deleteByPrimaryKey(Long stockId) {
		return super.delete("deleteByPrimaryKey", stockId);
	}

	public int deleteByGroup(Long groupId) {
		return super.delete("deleteByGroupId", groupId);
	}

	public int insert(SuppGoodsGroupStock suppGoodsGroupStock) {
		return super.insert("insert", suppGoodsGroupStock);
	}

	public boolean isGroupStockExist(HashMap<String, Object> params) {
		return (Integer) super.get("isGroupStockExist", params) > 0;
	}

/*	public int insertSelective(SuppGoodsGroupStock suppGoodsGroupStock) {
		return super.insert("insertSelective", suppGoodsGroupStock);
	}*/

	public SuppGoodsGroupStock selectByPrimaryKey(Long stockId) {
		return super.get("selectByPrimaryKey", stockId);
	}

	public SuppGoodsGroupStock selectByDateAndGroup(Map<String, Object> params) {
		// 防止重复数据导致报错，采用以下方式
		List<SuppGoodsGroupStock> suppGoodsGroupStockList = super.queryForList("selectBySpecDateAndGroup", params);
		if (suppGoodsGroupStockList != null && suppGoodsGroupStockList.size() > 0) {
			return suppGoodsGroupStockList.get(0);
		}
		return null;
	}

	/**
	 * 根据日期范围和共享组ID查询符合条件的库存记录 add by zhoudengyun
	 * 
	 * @param params
	 *            (beginDate、endDate、groupId)
	 * @return
	 */
	public List<SuppGoodsGroupStock> selectBySpecDateRangeAndGroupId(Map<String, Object> params) {
		return super.queryForList("selectBySpecDateRangeAndGroupId", params);
	}

	/**
	 * 根据共享库存ID更新共享库存
	 * 被EBK client 相关代码占用，无其他影响 add by zhoudengyun
	 * @param params
	 * @return
	 */
	public Integer updateGroupStockByPrimaryKey(HashMap<String, Object> params) {
		return super.update("updateGroupStockByPrimaryKey", params);
	}

	/**
	 * 根据商品ID更新共享库存
	 * 
	 * @param params
	 * @return
	 */
	public Integer updateGroupStockByGoodsId(Map<String, Object> params) {
		return super.update("updateGroupStockByGoodsId", params);
	}

/*	public int updateByPrimaryKeySelective(SuppGoodsGroupStock suppGoodsGroupStock) {
		return super.update("updateByPrimaryKeySelective", suppGoodsGroupStock);
	}*/

	public int updateByPrimaryKey(SuppGoodsGroupStock suppGoodsGroupStock) {
		return super.update("updateByPrimaryKey", suppGoodsGroupStock);
	}

	public Long findShareStock(Map<String, Object> params) {
		return super.get("findShareStock", params);
	}
	
	/****
	 * 根据共享组id查找对应商品集合
	 * @param groupId
	 * @return
	 */
	public List<Long> findSuppGoodsByGroupId(Long groupId) {
		return super.queryForList("selectGoodsByGroupId", groupId);
	}
	
	/****
	 * 根据参数查找对应日期集合
	 * @param params
	 * @return
	 */
	public List<Date> findDateByParams(Map<String, Object> params) {
		return super.queryForList("selectDatesByParams", params);
	}
	
}