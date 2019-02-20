package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.dao.TimePriceUpdateStock;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.supp.vo.SuppGoodsLineTimePriceVo;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsLineTimePriceOraDao extends MyBatisOraDao implements
		TimePriceUpdateStock {

	public SuppGoodsLineTimePriceOraDao() {
		super("SUPP_GOODS_LINE_TIME_PRICE");
	}

	public int insert(SuppGoodsLineTimePrice record) {
		return super.insert("insert", record);
	}

	/**
	 * 根据GoodsId,SupplierId,SpecDate 查询时间价格
	 * 
	 * @param params
	 * @return
	 */
	public SuppGoodsLineTimePrice selectByGoodsSpecDate(
			Map<String, Object> params) {
		// 因为有可能会出现重复的时间价格记录，因此采用取列表第一个的方法解决该问题
		List<SuppGoodsLineTimePrice> suppGoodsTimePriceLineList = super
				.queryForList("selectByParams", params);
		if (suppGoodsTimePriceLineList != null
				&& suppGoodsTimePriceLineList.size() > 0)
			return suppGoodsTimePriceLineList.get(0);
		return null;
	}

	public List<SuppGoodsLineTimePrice> selectByParams(
			Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}

	public SuppGoodsLineTimePrice selectByPrimaryKey(Long timePriceId) {
		return super.get("selectByPrimaryKey", timePriceId);
	}

	public int updateByPrimaryKey(SuppGoodsLineTimePrice record) {
		return super.update("updateByPrimaryKey", record);
	}
	public int updateByPrimaryKeyAndParamsNull(SuppGoodsLineTimePrice record) {
		return super.update("updateByPrimaryKeyAndParamsNull", record);
	}
	@Override
	public int updateStockForOrder(Map<String, Object> params) {
		return super.update("updateStockForOrder", params);
	}

	public List<SuppGoodsLineTimePriceVo> selectLineTimePriceCalByParams(
			Map<String, Object> params) {
		return super.queryForList("selectLineTimePriceCalByParams", params);
	}
	
	/**
	 * 获取线路酒店套餐共享库存数据
	 * @param params
	 * @return
	 */
	public List<SuppGoodsLineTimePriceVo> selectLineTimePriceCalGroupStockByParams(
			Map<String, Object> params) {
		return super.queryForList("selectLineTimePriceCalGroupStockByParams", params);
	}

	public SuppGoodsLineTimePrice getTimePrice(final Long suppGoodsId,
			Date specDate, boolean checkAhead) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);
		params.put("checkAhead", checkAhead);
		return super.get("getTimePrice", params);
	}

	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @return
	 */
	public SuppGoodsLineTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
			Date date) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", date);
		params.put("checkAhead", true);
		return super.get("getOneSaleAbleTimePrice", params);
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
	 * 判断是不是共享商品
	 * @param timePriceId
	 * @return
	 */
	public Long getGroupId(Long timePriceId){
		return super.get("isShareStock", timePriceId);
	}

	/**
	 * 获得共享组中库存最多的时间价格
	 * @param params
	 * @return
	 */
	public List<SuppGoodsLineTimePrice> selectMaxGoodsTimepriceList(Map<String, Object> params){
		return super.queryForList("selectMaxGoodsTimeprice", params);
	}
	
	public Integer getNextDaysOnSale(Map<String, Object> params) {
		return super.get("getNextDaysOnSale", params);
	}
	
	/**
	 * 删除线路商品对应的时间价格表
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
	
	public int updateByPrimaryKeySelective(SuppGoodsLineTimePrice record) {
		return super.update("updateByPrimaryKeySelective", record);
	}
	
	
	public List<SuppGoodsLineTimePrice> selectByParamsForStockAlert(Map<String, Object> params) {
		return super.queryForList("selectByParamsForStockAlert", params);
	}
	
	/**
	 * 获取线路酒店套餐所有时间价格库存数据
	 * @param params
	 * @return
	 */
	public List<SuppGoodsLineTimePriceVo> selectALLLineTimePriceCalByParams(
			Map<String, Object> params) {
		return super.queryForList("selectALLLineTimePriceCalByParams", params);
	}
	
	public Long findGroupIdBySuppGoodsId(Long suppGoodsId){
	    Map<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", suppGoodsId);
	    return super.get("findGroupIdBySuppGoodsId", params);
	}

}