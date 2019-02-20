package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsMultiTimePrice;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsMultiTimePriceOraDao extends MyBatisOraDao {
	public SuppGoodsMultiTimePriceOraDao() {
		super("SUPP_GOODS_MULTI_TIME_PRICE");
	}
	
	/**
	 * 查询时间价格列表
	 * @param params
	 * @return
	 */
	public List<SuppGoodsMultiTimePrice> findSuppGoodsMultiTimePriceList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	/**
	 * 查询时间价格
	 * @param params
	 * @return
	 */
	public SuppGoodsMultiTimePrice findSuppGoodsMultiTimePrice(Map<String, Object> params) {
		List<SuppGoodsMultiTimePrice> list =  super.queryForList("selectByParams", params);
		if(list!=null && list.size() > 0){
			return list.get(0);
		}
		return null;
	}
	
	public Integer getTotalCount(Map<String, Object> params){
		return super.get("getTotalCount", params);
	}
	
	/**
	 * 保存
	 * @param record
	 * @return
	 */
    public int insert(SuppGoodsMultiTimePrice record) {
    	return super.insert("insert", record);
    }

    /**
     * 根据主键查询
     * @param timePriceId
     * @return
     */
    public SuppGoodsMultiTimePrice selectByPrimaryKey(Long timePriceId) {
    	return super.get("selectByPrimaryKey", timePriceId);
    }

    public int updateByPrimaryKeySelective(SuppGoodsMultiTimePrice record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }

    /**
     * 更新
     * @param record
     * @return
     */
    public int updateByPrimaryKey(SuppGoodsMultiTimePrice record) {
    	return super.update("updateByPrimaryKey", record);
    }
    
	/**
	 * 更新库存
	 * @param params
	 * @return
	 */
	public Integer updateStockForOrder(Map<String,Object> params){
		return super.update("updateStockForOrder", params);
	}
	
	/**
	 * 根据团期查询最低价格
	 * @return
	 */
	public Long findMinPriceByGroupDate(Map<String,Object> params){
		return super.get("selectMinPriceByGroupDate",params);
	}
	
	public SuppGoodsMultiTimePrice getTimePrice(final Long suppGoodsId, Date specDate,boolean checkAhead) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);
		params.put("checkAhead", checkAhead);
		return super.get("getTimePrice", params);
	}
	
	/**
	 * 查询指定日期后的时间价格列表
	 * @param params
	 * @return
	 */
	public List<SuppGoodsMultiTimePrice> getTimePriceList(Map<String, Object> params) {
		return super.queryForList("getTimePriceList", params);
	}
	
	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @return
	 */
	public SuppGoodsMultiTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
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
	   
	   public Integer getNextDaysOnSale(Map<String, Object> params) {
			return super.get("getNextDaysOnSale", params);
		}
}