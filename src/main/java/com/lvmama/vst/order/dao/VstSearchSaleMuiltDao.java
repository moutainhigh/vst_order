package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.VstSearchSale;
import com.lvmama.vst.back.order.po.VstSearchSaleMuilt;
import com.lvmama.vst.comm.mybatis.MyBatisDao;


@Repository
public class VstSearchSaleMuiltDao extends MyBatisDao{
	 public VstSearchSaleMuiltDao() {
			super("VST_SEARCH_SALE_MUILT");
		}
   
   public  List<VstSearchSaleMuilt>  selectProductWeekSaleByDistribute(Map<String, Object> params){
	   return  super.queryForList("selectProductWeekSaleByDistribute", params);
   }
   
   public  List<VstSearchSaleMuilt>  selectPackProductWeekSaleByDistribute(Map<String, Object> params){
	   return  super.queryForList("selectPackProductWeekSaleByDistribute", params);
   }
   
   public  List<VstSearchSaleMuilt>  selectHotelWeekSaleByDistribute(Map<String, Object> params){
	   return  super.queryForList("selectHotelWeekSaleByDistribute", params);
   }
   
   public int deleteAll(){
	  return super.delete("deleteAll",new Object());
   }
   
   
   public int insertSingleSelective(VstSearchSaleMuilt sale){
	   return super.insert("insertSelective", sale);
   }
   
   public Integer countProductWeekSaleByDistribute(Map<String, Object> params){
	   return super.get("countProductWeekSaleByDistribute",params);
   }
   
   public Integer countHotelWeekSaleByDistribute(Map<String, Object> params){
	   return super.get("countHotelWeekSaleByDistribute",params);
   }
   
   public Integer countPackProductWeekSaleByDistribute(Map<String, Object> params){
	   return super.get("countPackProductWeekSaleByDistribute",params);
   }
   
   public String selectFlagByPrdId(Long productId){
	   return super.get("selectFlagByPrdId",productId);
   }
}