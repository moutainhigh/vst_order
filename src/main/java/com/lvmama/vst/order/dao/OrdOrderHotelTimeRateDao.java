package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderHotelTimeRateDao extends MyBatisDao {

	public OrdOrderHotelTimeRateDao() {
		super("ORD_ORDER_HOTEL_TIME_RATE");
	}

	public int deleteByPrimaryKey(Long hotelTimeRateId) {
		return super.delete("deleteByPrimaryKey", hotelTimeRateId);
	}

	public int insert(OrdOrderHotelTimeRate ordOrderHotelTimeRate) {
		ordOrderHotelTimeRate.setUpdateTime(new Date());
		return super.insert("insert", ordOrderHotelTimeRate);
	}

	public int insertSelective(OrdOrderHotelTimeRate ordOrderHotelTimeRate) {
		ordOrderHotelTimeRate.setUpdateTime(new Date());
		return super.insert("insertSelective", ordOrderHotelTimeRate);
	}

	public OrdOrderHotelTimeRate selectByPrimaryKey(Long hotelTimeRateId) {
		return super.get("selectByPrimaryKey", hotelTimeRateId);
	}

	public int updateByPrimaryKeySelective(OrdOrderHotelTimeRate ordOrderHotelTimeRate) {
		ordOrderHotelTimeRate.setUpdateTime(new Date());
		return super.update("updateByPrimaryKeySelective", ordOrderHotelTimeRate);
	}

	public int updateByPrimaryKey(OrdOrderHotelTimeRate ordOrderHotelTimeRate) {
		ordOrderHotelTimeRate.setUpdateTime(new Date());
		return super.update("updateByPrimaryKey", ordOrderHotelTimeRate);
	}
	
	public List<OrdOrderHotelTimeRate> selectByParam(Map<String, Object> params) {
    	return super.queryForList("selectByParams", params);
    }
	public List<OrdOrderHotelTimeRate> selectListByParam(Map<String, Object> params) {
    	return super.queryForList("selectListByParams", params);
    }
	
	public List<Date> findOrdOrderItemHotelLastLeaveTimeByItemId(Long ordOrderItemId){
		return super.queryForList("findOrdOrderItemHotelLastLeaveTimeByItemId",ordOrderItemId);
	}
}