package com.lvmama.vst.order.service;

import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;

public interface IOrdOrderWifiPickingPointService {
	/**
	 * 增加一条WIFI订单的信息
	 * @param ordOrderWifiPickingPoint
	 * @return
	 */
	int insertOrdOrderWifiPickingPoint(OrdOrderWifiPickingPoint ordOrderWifiPickingPoint);
	
	/**
	 * 修改一条WIFI订单的信息
	 * @param ordOrderWifiPickingPoint
	 */
	int updateOrdOrderWifiPickingPoint(OrdOrderWifiPickingPoint ordOrderWifiPickingPoint);

	/**
	 * 获取一条订单的信息
	 * @param ordOrderWifiPickingPoint
	 * @return
	 */
	OrdOrderWifiPickingPoint getOrderWifiPickingPointById(Long ordOrderId);
	
	/**
	 * 删除一条wifi订单的信息
	 * @param ordOrderId
	 */
	int deleteOrdOrderWifiPickingPointById(Long ordOrderId);
}
