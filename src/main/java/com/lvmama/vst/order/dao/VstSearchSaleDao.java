package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.VstSearchSale;
import com.lvmama.vst.comm.mybatis.MyBatisDao;


@Repository
public class VstSearchSaleDao extends MyBatisDao{
	 public VstSearchSaleDao() {
			super("VST_SEARCH_SALE");
		}

   public  List<VstSearchSale>  selectProductWeekSale(Map<String, Object> params){
	   return  super.queryForList("selectProductWeekSale", params);
   }
   
   public  List<VstSearchSale>  selectPackProductWeekSale(Map<String, Object> params){
	   return  super.queryForList("selectPackProductWeekSale", params);
   }
   
   public  List<VstSearchSale>  selectHotelWeekSale(Map<String, Object> params){
	   return  super.queryForList("selectHotelWeekSale", params);
   }
   
   public int deleteAll(){
	  return super.delete("deleteAll",new Object());
   }
   
   public int insertSelective(VstSearchSale sale){
	   return super.insert("insertSelective", sale);
   }
   
   public Integer countProductWeekSale(Map<String, Object> params){
	   return super.get("countProductWeekSale",params);
   }
   
   public Integer countHotelWeekSale(Map<String, Object> params){
	   return super.get("countHotelWeekSale",params);
   }
   
   public Integer countPackProductWeekSale(Map<String, Object> params){
	   return super.get("countPackProductWeekSale",params);
   }
}