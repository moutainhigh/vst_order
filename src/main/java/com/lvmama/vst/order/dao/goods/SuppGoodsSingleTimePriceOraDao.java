package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsSingleTimePrice;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

/**
 * SuppGoodsSingleTimePrice时间价格表DAO
 * 
 */
@Repository
public class SuppGoodsSingleTimePriceOraDao extends MyBatisOraDao {

	public SuppGoodsSingleTimePriceOraDao() {
		super("SUPP_GOODS_SINGLE_TIME_PRICE");
	}

	public List<SuppGoodsSingleTimePrice> findSuppGoodsSingleTimePriceList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public int insert(SuppGoodsSingleTimePrice suppGoodsSingleTimePrice) {
		return super.insert("insert", suppGoodsSingleTimePrice);
	}

	public Integer getTotalCounts(Map<String, Object> params) {
		return super.get("select_count_by_param", params);
	}
	
	
	public SuppGoodsSingleTimePrice selectByPrimaryKey(Long id) {
		return super.get("selectByPrimaryKey", id);
	}
	
	
	/**
	 * 根据主键选择更新
	 * @param suppGoodsSingleTimePrice
	 * @return
	 */
	public Integer updateByPrimaryKeySelective(SuppGoodsSingleTimePrice suppGoodsSingleTimePrice) {
		return super.update("updateByPrimaryKeySelective", suppGoodsSingleTimePrice);
	}

	/**
	 * 根据主键全部更新
	 * @param SuppGoodsSingleTimePrice
	 * @return
	 */
	public Integer updateByPrimaryKey(SuppGoodsSingleTimePrice suppGoodsSingleTimePrice) {
		return super.update("updateByPrimaryKey", suppGoodsSingleTimePrice);
	}

	/**
	 * 更新库存
	 * @param params
	 * @return
	 */
	public Integer updateStockForOrder(Map<String,Object> params){
		return super.update("updateStockForOrder", params);
	}
	
	public SuppGoodsSingleTimePrice getTimePrice(final Long suppGoodsId, Date specDate,boolean checkAhead) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);
		params.put("checkAhead", checkAhead);
		return super.get("getTimePrice", params);
	}
	
	public List<SuppGoodsSingleTimePrice> getTimePriceList(Map<String, Object> params) {
		return super.queryForList("getTimePriceList", params);
	}
	
	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @return
	 */
	public SuppGoodsSingleTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
			Date date) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", date);
		params.put("checkAhead", true);
		return super.get("getOneSaleAbleTimePrice", params);
	}
	
	 /**
    * 设置商品的时间价格为禁售
    * @param goodsId
    * @return
    */
   public Integer updateTimePriceDisableByGoodsId(Long goodsId){
	   return super.update("updateTimePriceDisableByGoodsId", goodsId);
   }
   
   public Integer getNextDaysOnSale(Map<String, Object> parmas) {
		return super.get("getNextDaysOnSale", parmas);
	}
}