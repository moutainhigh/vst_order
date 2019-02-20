package com.lvmama.vst.order.service;

import com.lvmama.vst.back.client.ord.po.OrdFormInfoQueryPO;
import com.lvmama.vst.back.order.po.OrdFormInfo;

import java.util.List;
import java.util.Map;
/**
 * 订单表单提交JSON记录 业务层  表=ORD_FORM_INFO
 * @author zhaomingzhu
 *
 */
public interface IOrdFormInfoService {
	/**
	 * 上车点、下车点查询
	 * @param params
	 * @return
	 */
	public Map<String,String> findFrontBusStop(Long orderId);
	
	/**
	 * 获取签证材料截止收取时间
	 * @param orderId：订单号
	 */
	public String findVisaDocLastTime(Long orderId);
	
	/**
	 * 修改订单中材料截止收取时间
	 * @param orderID 
	 * 			订单号
	 */
	public void updateVisaDocLastDate(Long orderId,String LastDate);

	/**
	 * 根据订单id查询ord_form_info表中的记录
	 */
	List<OrdFormInfo> findOrdFormInfoList(OrdFormInfoQueryPO ordFormInfoQueryPO);
}
