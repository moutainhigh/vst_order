package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.ShowGoodsAddTimePrice;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;

/**
 * 加价模式商品时间价格表操作DAO实现
 * @author LIULIANG
 * @Date 2014-06-25
 */
@Repository
public class ShowGoodsAddTimePriceOraDao extends MyBatisOraDao {

	public ShowGoodsAddTimePriceOraDao() {
		super("SHOW_GOODS_ADD_TIME_PRICE");
	}

	public int deleteByPrimaryKey(Long timePriceId){
		return super.delete("deleteByPrimaryKey", timePriceId);
	}
	public int deleteByShowGoodsIdAndSupplierId(ShowGoodsAddTimePrice showGoodsAddTimePrice){
		if (showGoodsAddTimePrice.getSuppGoodsId()==null || showGoodsAddTimePrice.getSupplierId()==null) {
			return 0;
		}
		showGoodsAddTimePrice.setStartDate(DateUtil.getTodayDate());//设置删除的时间从今天开始
		return super.delete("deleteBySuppGoodsIdAndSupplierId", showGoodsAddTimePrice);
	}

    public int insert(ShowGoodsAddTimePrice showGoodsAddTimePrice){
    	super.insert("insert", showGoodsAddTimePrice);
    	Long timePriceId = showGoodsAddTimePrice.getTimePriceId();
    	return timePriceId.intValue();    	
    }

    public int insertSelective(ShowGoodsAddTimePrice showGoodsAddTimePrice){
    	super.insert("insertSelective", showGoodsAddTimePrice);
    	Long timePriceId = showGoodsAddTimePrice.getTimePriceId();
    	return timePriceId.intValue();
    }

    public List<ShowGoodsAddTimePrice> selectByExample(Map<String, Object> params){
    	return super.queryForList("selectByExample",params);
    }

    public List<ShowGoodsAddTimePrice> selectTimePriceNoShareStockList(Map<String, Object> params){
        return super.queryForList("selectBaseTimePriceNoShareStock",params);
    }

    public ShowGoodsAddTimePrice selectByPrimaryKey(Long timePriceId){
    	return super.get("selectByPrimaryKey", timePriceId);
    }

    public int updateByPrimaryKeySelective(ShowGoodsAddTimePrice showGoodsAddTimePrice){
    	return super.update("updateByPrimaryKeySelective", showGoodsAddTimePrice);
    }

    public int updateByPrimaryKey(ShowGoodsAddTimePrice showGoodsAddTimePrice){
    	return super.update("updateByPrimaryKey", showGoodsAddTimePrice);
    }
    
    
    /**
     * 
     * @param suppGoodsId
     * @param date
     * @return
     */
    public ShowGoodsAddTimePrice selectTimePriceForFront(Long suppGoodsId,Date date){
    	HashMap<String,Object> params = new HashMap<String,Object>();
    	params.put("suppGoodsId", suppGoodsId);
    	params.put("date", date);
    	return super.get("selectTimePriceForFront", params);
    }
    
    /**
     * 查询指定日后的条件排序后的第一个时间价格表（默认按价格排序）
     * @param suppGoodsId
     * @param date
     * @return
     */
    public ShowGoodsAddTimePrice getFirstTimePrice(HashMap<String,Object> params){
    	
        List<ShowGoodsAddTimePrice> result = super.getList("selectTimePrice", params);
        return null != result && result.size() > 0 ? result.get(0):null;
    }
    
    /**
     * 得到指定日期的时间价格表
     * @param suppGoodsId
     * @param date
     * @return
     */
    public ShowGoodsAddTimePrice getTimePrice(Long suppGoodsId,Date date){
    	return getTimePrice(suppGoodsId, date, false);
    }
    /**
     * 得到指定日期的时间价格表
     * @param suppGoodsId
     * @param date
     * @param checkAhead
     * @return
     */
    public ShowGoodsAddTimePrice getTimePrice(Long suppGoodsId,Date date, boolean checkAhead){
    	Map<String,Object> params = new HashMap<String, Object>();
    	params.put("suppGoodsId", suppGoodsId);
    	params.put("date", date);
		params.put("checkAhead", checkAhead);
    	return super.get("getTimePrice", params);
    }
    
    /**
     * 得到指定日期的时间价格表(用于生成订单时验证库存)
     * @param suppGoodsId
     * @param date
     * @return
     */
    public ShowGoodsAddTimePrice getTimePriceAtCreateOrder(Long suppGoodsId,Date date){
    	Map<String,Object> params = new HashMap<String, Object>();
    	params.put("suppGoodsId", suppGoodsId);
    	params.put("date", date);
    	return super.get("getTimePriceAtCreateOrder", params);
    }
    
    public int updateStockForOrder(Map<String,Object> params){
    	return super.update("updateStockForOrder", params);
    }
    
	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @param isLimitBookDay
	 * @return
	 */
	public ShowGoodsAddTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
			Date date,boolean isLimitBookDay) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", date);
		params.put("checkAhead", true);
		params.put("isLimitBookDay", isLimitBookDay);
		return super.get("getOneSaleAbleTimePrice", params);
	}

	 /**
	  * 日历控件
	  * @param params
	  * @return
	  */
    public List<ShowGoodsAddTimePrice> getTimePriceByGoodsIdAndDate(Map<String,Object> params){
    	return super.queryForList("getTimePriceByGoodsIdAndDate", params);
    }
    
    /**
	  * 门票自主打包日历日历控件
	  * @param params
	  * @return
	  */
   public List<ShowGoodsAddTimePrice> getCombTimePriceByProductIdAndDate(Map<String,Object> params){
   	return super.queryForList("getCombTimePriceByProductIdAndDate", params);
   }
   
   	/**
	  * 门票自主打包最早可售日期
	  * @param params
	  * @return
	  */
   public ShowGoodsAddTimePrice getFirstSaleCombTimePrice(Map<String, Object> params) {
		return super.get("getFirstSaleCombTimePrice", params);
	}
   
   /**
    * 设置商品的时间价格为禁售
    * @param goodsId
    * @return
    */
   public Integer updateTimePriceDisableByGoodsId(Long goodsId){
	   return super.update("updateTimePriceDisableByGoodsId", goodsId);
   }

    /**
     * 设置时间价格为可售
     * @param timePriceIdList
     * @return
     */
    public Integer updateTimePriceSaleFlagByTimePriceIdList(List<Long> timePriceIdList, String onsaleFlag){
        if (CollectionUtils.isEmpty(timePriceIdList) ||
                !(Constants.Y_FLAG.equals(onsaleFlag) || Constants.N_FLAG.equals(onsaleFlag))
                ) {
            return 0;
        }
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("timePriceIdList", timePriceIdList);
        param.put("onsaleFlag", onsaleFlag);
        return super.update("updateTimePriceSaleFlagByTimePriceIdList", param);
    }

   public Integer getNextDaysOnSale(Map<String,Object> params) {
		return super.get("getNextDaysOnSale", params);
    }

    public Integer updateShareTotalStockId(Long shareTotalStockId, List<Long> goodsIdList, boolean ignoreEmptyId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareTotalStockId", shareTotalStockId);
        params.put("goodsIdList", goodsIdList);
        params.put("ignoreEmptyId", ignoreEmptyId);
        return super.update("updateShareTotalStockIdByParams", params);
    }

    @SuppressWarnings("unchecked")
    public Integer updateShareTotalStockIdByParams(Map<String, Object> params) {
        if (params.get("shareTotalStockId") == null
                || CollectionUtils.isEmpty((List<Long>) params.get("goodsIdList"))
                || CollectionUtils.isEmpty((List<Date>) params.get("dateList"))
                ) {
            return 0;
        }
        return super.update("updateShareTotalStockIdByParams", params);
    }

    @SuppressWarnings("unchecked")
    public Integer updateShareDayLimitIdByParams(Map<String, Object> params) {
        if (params.get("limit") == null || CollectionUtils.isEmpty((List<Long>) params.get("goodsIdList"))) {
            return 0;
        }
        return super.update("updateShareDayLimitIdByParams", params);
    }
     
    
   /*查询基础时间价格，不关联共享库存数据*/
   /**
    * 
    * @param suppGoodsId
    * @param date
    * @return
    */
   public ShowGoodsAddTimePrice selectBaseTimePriceForFront(Long suppGoodsId,Date date){
   	HashMap<String,Object> params = new HashMap<String,Object>();
   	params.put("suppGoodsId", suppGoodsId);
   	params.put("date", date);
   	return super.get("selectBaseTimePriceForFront", params);
   }
   
   /**
    * 查询指定日后的条件排序后的第一个时间价格表（默认按价格排序）
    * @param suppGoodsId
    * @param date
    * @return
    */
   public ShowGoodsAddTimePrice getBaseFirstTimePrice(HashMap<String,Object> params){
   	
       List<ShowGoodsAddTimePrice> result = super.getList("selectBaseTimePrice", params);
       return null != result && result.size() > 0 ? result.get(0):null;
   }
   
   public ShowGoodsAddTimePrice getBaseTimePrice(Long suppGoodsId,Date date){
   	return getBaseTimePrice(suppGoodsId, date, false);
   }
   /**
    * 得到指定日期的时间价格表
    * @param suppGoodsId
    * @param date
    * @param checkAhead
    * @return
    */
   public ShowGoodsAddTimePrice getBaseTimePrice(Long suppGoodsId,Date date,boolean checkAhead){
   	Map<String,Object> params = new HashMap<String, Object>();
   	params.put("suppGoodsId", suppGoodsId);
   	params.put("date", date);
   	params.put("checkAhead", checkAhead);
   	return super.get("getBaseTimePrice", params);
   }
   
   /**
    * 得到指定日期的时间价格表(用于生成订单时验证库存)
    * @param suppGoodsId
    * @param date
    * @return
    */
   public ShowGoodsAddTimePrice getBaseTimePriceAtCreateOrder(Long suppGoodsId,Date date){
   	Map<String,Object> params = new HashMap<String, Object>();
   	params.put("suppGoodsId", suppGoodsId);
   	params.put("date", date);
   	return super.get("getBaseTimePriceAtCreateOrder", params);
   }
   
   /**
	  * 日历控件
	  * @param params
	  * @return
	  */
 public List<ShowGoodsAddTimePrice> getBaseTimePriceByGoodsIdAndDate(Map<String,Object> params){
 	return super.queryForList("getBaseTimePriceByGoodsIdAndDate", params);
 }
 /*查询基础时间价格，不关联共享库存数据*/
   
    
    public List<ShowGoodsAddTimePrice> selectTimePriceListByProdId(Long productId) {
        return super.queryForList("selectTimePriceListByProdId", productId);
    }

    /**
     * 查询时间价格表：根据商品Id和出发时间
     * @param params
     * @return
     */
    public List<ShowGoodsAddTimePrice> selectTimePriceList4ProdIdAndSpecDate(final Map<String,Object> params){
        return super.getList("selectTimePriceList4ProdIdAndSpecDate", params);
    }


    public List<ShowGoodsAddTimePrice> selectShowTicketTimePriceDateListByProdId(Long productId) {
        return super.queryForList("selectShowTicketTimePriceDateListByProdId", productId);
    }

    public List<ShowGoodsAddTimePrice> selectTimePriceListByParams(Map<String,Object> params){
        return super.queryForList("selectTimePriceListByParams", params);
    }

    /**
     * 获取一段时间内， 可卖的时间价格
     * @param params
     * @return
     */
    public List<ShowGoodsAddTimePrice> getSpecDateList(Map<String,Object> params){
        return super.queryForList("selectSpecDateList", params);
    }
    
    public int findTimePriceCountByProductId(Map<String,Object> params){
    	return super.get("findTimePriceCountByProductId", params);
    }

    public int deleteShowAddTimePriceByProductId(Long productId) {
        return super.delete("deleteShowAddTimePriceByProductId", productId);
    }

    public int backUpShowAddTimePriceByProductId(Long productId) {
        return super.insert("backUpShowAddTimePriceByProductId", productId);
    }
    public Long countShowGoodsAddTimePriceByGoodsId(Long suppGoodsId){
        return super.get("countShowGoodsAddTimePriceByGoodsId", suppGoodsId);
    }

}