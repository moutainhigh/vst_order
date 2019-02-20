package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.prodcal.po.ProdPackageLineCal;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

/**
 * 时间价格表DAO
 * 
 * @author mayonghua
 * @date 2013-10-24
 */
@Repository
public class SuppGoodsTimePriceOraDao extends MyBatisOraDao {

	public SuppGoodsTimePriceOraDao() {
		super("SUPP_GOODS_TIME_PRICE");
	}

	public List<SuppGoodsTimePrice> findTimePriceList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public int insert(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.insert("insert", suppGoodsTimePrice);
	}

	public Integer getTotalCounts(Map<String, Object> params) {
		return super.get("getTotalCount", params);
	}
	
	public Integer getSaleAbleTimePriceCounts(Map<String, Object> params) {
		return super.get("getSaleAbleTimePriceCounts", params);
	}
	
	//获得共享组中库存最多的时间价格
	public List<SuppGoodsTimePrice> selectMaxGoodsTimepriceList(Map<String, Object> params){
		return super.queryForList("selectMaxGoodsTimeprice", params);
	}

	/**
	 * 判断时间价格表是否存在
	 * 
	 * @param params
	 * @return
	 */
	public SuppGoodsTimePrice isTimePriceExists(Map<String, Object> params) {
		List<SuppGoodsTimePrice> list = super.queryForList("isTimePriceExists", params);
		if(list != null && list.size() >0){
			return list.get(0);
		}
		return null;
	}

	public SuppGoodsTimePrice selectByPrimaryKey(Long suppGoodsTimePriceId) {
		return super.get("selectByPrimaryKey", suppGoodsTimePriceId);
	}
	/**
	 * 根据时间区间和goodsIds查询
	 * @param params
	 * @return
	 */
	public List<SuppGoodsTimePrice> findBySpecDate(Map<String, Object> params){

		List<Long> tempGoodsIds = (List<Long>) params.get("goodsIds");
		if(tempGoodsIds!=null && tempGoodsIds.isEmpty()) params.put("goodsIds",null);
		return super.queryForList("selectBySpecDate", params);
	}
	/**
	 * 根据时间区间和goodsIds查询
	 * @param params
	 * @return
	 */
	public List<SuppGoodsTimePrice> selectTimePricesByGoodsId(Map<String, Object> params){
		return super.queryForList("selectTimePricesByGoodsId", params);
	}
	
	
	/**
	 * 判断是不是共享商品
	 * @param timePriceId
	 * @return
	 */
	public boolean isShareStock(Long timePriceId){
		return super.get("isShareStock", timePriceId) !=null;
	}
	
	/**
	 * 更新非共享商品的库存
	 * @param params
	 * @return
	 */
	public Integer updateTimePriceStock(Map<String,Object> params){
		return super.update("updateTimePriceStock", params);
	}
	
	/**
	 * 更新共享商品的库存
	 * @param params
	 * @return
	 */
	public Integer updateTimePriceStockGroup(HashMap<String,Object> params){
		return super.update("updateTimePriceStockGroup", params);
	}

	/**
	 * 将时间价格库存清零
	 * @param timePriceId
	 * @return
	 */
	public Integer cleanStock(Long timePriceId){
		return super.update("cleanStock", timePriceId);
	}
	
	/**
	 * 根据主键选择更新
	 * @param suppGoodsTimePrice
	 * @return
	 */
	public Integer updateByPrimaryKeySelective(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.update("updateByPrimaryKeySelective", suppGoodsTimePrice);
	}

	/**
	 * 根据主键全部更新
	 * @param suppGoodsTimePrice
	 * @return
	 */
	public Integer updateByPrimaryKey(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.update("updateByPrimaryKey", suppGoodsTimePrice);
	}
	
	/**
	 * 根据主键全部更新(Rest使用)
	 * @param suppGoodsTimePrice
	 * @return
	 */
	public Integer updateByPrimaryKeyForRest(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.update("updateByPrimaryKeyForRest", suppGoodsTimePrice);
	}

	/**
	 * 根据GoodsId,SpecDate 进行更新
	 * 
	 * @param suppGoodsTimePrice
	 * @return
	 */
	public Integer updateByGoodsSpecDate(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.update("updateByGoodsSpecDate", suppGoodsTimePrice);
	}

	/**
	 * 根据GoodsId,SpecDate进行选择更新
	 * @param params
	 * @return
	 */
	public int updateByGoodsSpecDateSelective(SuppGoodsTimePrice suppGoodsTimePrice) {
		return super.update("updateByGoodsSpecDateSelective", suppGoodsTimePrice);
	}
	
	
	/**
	 * 根据GoodsId,SupplierId,SpecDate 查询时间价格
	 * @param params
	 * @return
	 */
	public SuppGoodsTimePrice selectByGoodsSpecDate(Map<String, Object> params) {
		//因为有可能会出现重复的时间价格记录，因此采用取列表第一个的方法解决该问题
		List<SuppGoodsTimePrice> suppGoodsTimePriceList =  super.queryForList("selectByGoodsSpecDate",params);
		if(suppGoodsTimePriceList!=null&&suppGoodsTimePriceList.size()>0)
			return suppGoodsTimePriceList.get(0);
		return null;
	}
	
	
	public SuppGoodsTimePrice getTimePrice(final Long suppGoodsId, Date specDate,boolean checkAhead) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);
		params.put("checkAhead", checkAhead);
		return super.get("getTimePrice", params);
	}
	public List<SuppGoodsTimePrice> getTimePriceAfterSpecDate(final Long suppGoodsId, Date specDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);	
		return super.queryForList("getTimePriceAfterSpecDate", params);
	}
	public List<SuppGoodsTimePrice> onlyFindTimePriceList(Map<String, Object> params) {
		return super.queryForList("onlyFindTimePriceList", params);
	}
	
	public List<ProdPackageLineCal> selectLvmamaPackLineScopeDate(Map<String, Object> params) {
		return super.queryForList("selectLvmamaPackLineScopeDate", params);
	}
	
	public List<ProdPackageLineCal> selectSupplierPackLineScopeDate(Map<String, Object> params) {
		return super.queryForList("selectSupplierPackLineScopeDate", params);
	}
	
	public List<ProdPackageLineCal> selectTikcetScopeDate(Map<String, Object> params) {
		return super.queryForList("selectTikcetScopeDate", params);
	}
	
	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @return
	 */
	public SuppGoodsTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
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
	
	/**
	 * 删除商品对应的时间价格表
	 * @param goodsId
	 * @return
	 */
	public Integer deleteTimePriceBySuppGoodsId(Long goodsId){
		return super.delete("deleteBySuppGoodsId", goodsId);
	}
	
	public Long findGroupIdBySuppGoodsIdAndSpecDate(Long suppGoodsId, Date specDate){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", suppGoodsId);
        params.put("specDate", specDate);
        return super.get("findGroupIdBySuppGoodsIdAndSpecDate", params);
    }

	/**
	 * 用于库存预警时查询时间价格
	 * @param params
	 * @return
	 */
	public List<SuppGoodsTimePrice> findTimePriceListForStockAlert(
			Map<String, Object> params) {
		return super.queryForList("selectByParamsForStockAlert", params);
	}
	
	public List<SuppGoodsTimePrice> selectTimePriceListByProdId(Long productId) {
		return super.queryForList("selectTimePriceListByProdId", productId);
	}

	public void updateBatchSuppGoodsTimePriceList(List<SuppGoodsTimePrice> list) {
		super.update("updateBatchSuppGoodsTimePriceList", list);
	}

	/**
	 * 查询国内酒店时间价格表（数据迁移用）
	 * @param params
	 * @return
	 */
	public Integer getTimePriceTotalCount(Map<String, Object> parameters) {
		return super.get("getTimePriceTotalCount", parameters);
	}
	/**
	 * 查询国内酒店时间价格表（数据迁移用）
	 * @param params
	 * @return
	 */
	public List<SuppGoodsTimePrice> findSuppGoodsTimePriceList(Map<String, Object> params) {
		return super.queryForList("findSuppGoodsTimePriceList", params);
	}

	public List<SuppGoodsTimePrice> getTimePriceOnSaleAfterSpecDate(Map<String, Object> parameters) {
		return super.queryForList("getTimePriceOnSaleAfterSpecDate", parameters);
	}
}