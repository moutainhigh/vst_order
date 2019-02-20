package com.lvmama.vst.order.snapshot.service;

import com.lvmama.vst.back.prod.po.ProdLineRoute;

public interface IOrderLinerouteSnapshotService {
	
	
	
	/**
	 * 根据索引键orderId查询行程信息
	 * @param orderId 订单号
	 * @return
	 */
	ProdLineRoute findOneLineRouteSnapShotByOrderId(Long orderId);
	
	/**
	 * 插入行程信息到mongo，orderId为索引键
	 * @param orderId 订单号
	 * @param lineRouteVo 行程信息
	 * @return
	 */
	Boolean insertOneLineRouteSnapshot (Long orderId, ProdLineRoute lineRoute);
}
