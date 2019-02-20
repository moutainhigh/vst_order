package com.lvmama.vst.order.dao;


import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
@Repository
public class OrdOrderWifiPickingPointDao extends MyBatisDao{
	public OrdOrderWifiPickingPointDao() {
		super("ORD_ORDER_WIFI_PICKING_POINT");
	}
	
	public OrdOrderWifiPickingPoint selectByPrimaryKey(Long OrdOrderWifiPickingPointId) throws DataAccessException{
		return super.get("selectByPrimaryKey", OrdOrderWifiPickingPointId);
	}
	
	public List<OrdOrderWifiPickingPoint> findOrdOrderWifiPickingPoint(Map<String, Object> params) throws DataAccessException{
		List<OrdOrderWifiPickingPoint> list = super.queryForList("selectByParams", params);
		return list;
	}

	public int deleteByPrimaryKey(Long OrdOrderWifiPickingPointId) throws DataAccessException{
		return super.delete("deleteByPrimaryKey", OrdOrderWifiPickingPointId);
	}

	public int insert(OrdOrderWifiPickingPoint orderWifiPickingPoint) throws DataAccessException {
		super.insert("insert", orderWifiPickingPoint);
		return Integer.parseInt(orderWifiPickingPoint.getOrdPickingPointId()+"");
	}

	public int insertSelective(OrdOrderWifiPickingPoint orderWifiPickingPoint) throws DataAccessException {
		super.insert("insertSelective", orderWifiPickingPoint);
		return Integer.parseInt(orderWifiPickingPoint.getOrdPickingPointId()+"");
	}
	
	public int updateByPrimaryKeySelective(OrdOrderWifiPickingPoint orderWifiPickingPoint) throws DataAccessException {
		return super.update("updateByPrimaryKeySelective", orderWifiPickingPoint);
	}

	public int updateByPrimaryKey(OrdOrderWifiPickingPoint orderWifiPickingPoint) throws DataAccessException{
		return super.update("updateByPrimaryKey", orderWifiPickingPoint);
	}
}
