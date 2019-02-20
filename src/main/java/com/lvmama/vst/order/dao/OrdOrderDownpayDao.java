package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderDownpayDao extends MyBatisDao {

	public OrdOrderDownpayDao() {
		super("ORD_ORDER_DOWNPAY");
	}

	public int insert(OrdOrderDownpay ordOrderDownpay) {
		return super.insert("insert", ordOrderDownpay);
	}
	
	//非null的值更新
	public int updateByPrimaryKeySelective(OrdOrderDownpay ordOrderDownpay) {
		return super.update("updateByPrimaryKeySelective", ordOrderDownpay);
	}
	
	//全部更新
	public int updateByPrimaryKey(OrdOrderDownpay ordOrderDownpay) {
		return super.update("updateByPrimaryKey", ordOrderDownpay);
	}
	
    /**
     * 根据orderId更新定金支付
     * @param ordOrderDownpay
     * @return
     */
	public int updateByPrimaryKeyOrderId(OrdOrderDownpay ordOrderDownpay) {
		return super.update("updateByPrimaryKeyOrderId", ordOrderDownpay);
	}
	
	public int updatePayStatusByOrderId(Map<String, Object> map) {
		return super.update("updatePayStatusByOrderId", map);
	}

	public OrdOrderDownpay selectByPrimaryKey(Long orderDownpayId) {
		return super.get("selectByPrimaryKey", orderDownpayId);
	}
	
	public List<OrdOrderDownpay> selectByOrderId(Long orderId) {
		 return super.queryForList("selectByOrderId", orderId);
	}
}