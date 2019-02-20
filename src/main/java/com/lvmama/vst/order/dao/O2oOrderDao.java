package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.O2oOrder;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * O2o 门店系统， O2o订单Dao
 * @date 2016 03 02
 * @author qixiaochen
 *
 */
@Repository
public class O2oOrderDao extends MyBatisDao {
	
	public O2oOrderDao() {
		super("O2O_ORDER");
	}
	
	public List<O2oOrder> queryO2oOrder(Map<String, Object> paramsMap){
		return super.queryForList("queryByProperty",paramsMap);
	}
	
	public List<O2oOrder> findO2oOrderList(Map paramsMap){
		return super.queryForList("findO2oOrderList",paramsMap);
	}
	
	public Long getCountByProperty(Map<String, Object> params) {
		return super.get("getCountByProperty", params);
	}
	
	public List<O2oOrder> queryForListForReport(Map<String, Object> paramsMap){
		return super.queryForListForReport("queryByProperty",paramsMap);
	}
	public int insertSelective(O2oOrder oOrder){
		if (oOrder==null) {
			return 0;
		}
		return super.insert("insertSelective", oOrder);
	}
	public int deleteByOrderId(Long orderId){
		if (orderId==null) {
			return 0;
		}
		return super.delete("deleteByOrderId", orderId);
	}
	
	public int updateO2oOrder(O2oOrder o2oOrder){
		return super.update("updateByPrimaryKeySelective", o2oOrder);
	}

}
