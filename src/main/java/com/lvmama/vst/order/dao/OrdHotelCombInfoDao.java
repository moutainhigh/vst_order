package com.lvmama.vst.order.dao;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelcombOption;

@Repository
public class OrdHotelCombInfoDao extends MyBatisDao {

	public OrdHotelCombInfoDao() {
		super("ORD_HOTELCOMB_INFO");
	}

	public int insert(HotelcombOption ordOrderItem) {
		return super.insert("insert", ordOrderItem);
	}
}
