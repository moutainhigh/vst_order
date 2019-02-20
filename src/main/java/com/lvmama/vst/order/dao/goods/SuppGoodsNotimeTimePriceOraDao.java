package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.prodcal.po.ProdPackageLineCal;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsNotimeTimePriceOraDao extends MyBatisOraDao {

	public SuppGoodsNotimeTimePriceOraDao() {
		super("SUPP_GOODS_NOTIME_TIME_PRICE");
	}
	public List<SuppGoodsNotimeTimePrice> findSuppGoodsNotimeTimePriceList(Map<String, Object> params) {
		return super.queryForList("selectListByParams", params);
	}

	public int deleteByPrimaryKey(Long suppGoodsNotimeTimePriceId) {
		return super.delete("deleteByPrimaryKey", suppGoodsNotimeTimePriceId);
	}

	public int insert(SuppGoodsNotimeTimePrice record) {
		return super.insert("insert", record);
	}

	public int insertSelective(SuppGoodsNotimeTimePrice record) {
		return super.insert("insertSelective", record);
	}

	public SuppGoodsNotimeTimePrice selectByPrimaryKey(Long suppGoodsNotimeTimePriceId) {
		return super.get("selectByPrimaryKey", suppGoodsNotimeTimePriceId);
	}

	public int updateByPrimaryKeySelective(SuppGoodsNotimeTimePrice record) {
		return super.update("updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(SuppGoodsNotimeTimePrice record) {
		return super.update("updateByPrimaryKey", record);
	}

	public List<SuppGoodsNotimeTimePrice> selectListByParams(Map<String, Object> params) {
		return super.queryForList("selectListByParams", params);
	}
	
	public SuppGoodsNotimeTimePrice getTimePrice(Map<String,Object> params){
		return super.get("getTimePrice",params);
	}
	
	public int updateStockForOrder(Map<String,Object> params){
		return super.update("updateStockForOrder", params);
	}
	
	/**
	 * 查询某一天之后可售的时间价格
	 * @param suppGoodsId
	 * @param date
	 * @return
	 */
	public SuppGoodsNotimeTimePrice getOneSaleAbleTimePrice(Long suppGoodsId,
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
	   /**
	    * 保险产品关联产品查询
	    * @param params
	    * @return
	    */
		public List<SuppGoods> findSuppGoodsNotimeTimePriceListForInsurance(Map<String, Object> params) {
			return super.queryForList("selectSuppGoodsListForInsurance", params);
		}
		
		public List<ProdPackageLineCal> selectTikcetScopeDate(Map<String, Object> params) {
			return super.queryForList("selectTikcetScopeDate", params);
		}
		
		public Integer getNextDaysOnSale(Map<String,Object> params) {
			return super.get("getNextDaysOnSale", params);
		}

    public List<SuppGoodsNotimeTimePrice> findNoTimePriceForAperiodCombTicket(Long productId) {
        return super.queryForList("findNoTimePriceForAperiodCombTicket",productId);
    }

	public int countGoodsTimePriceById(Long suppGoodsId) {
		return super.get("countGoodsTimePriceById", suppGoodsId);
	}

}