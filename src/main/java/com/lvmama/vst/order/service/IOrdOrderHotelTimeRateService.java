package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;

/**
 * @author 张伟
 *
 */
public interface IOrdOrderHotelTimeRateService {

	
	public int addOrdOrderHotelTimeRate(OrdOrderHotelTimeRate OrdOrderHotelTimeRate);
	
	public OrdOrderHotelTimeRate findOrdOrderHotelTimeRateById(Long id);
	
	public List<OrdOrderHotelTimeRate> findOrdOrderHotelTimeRateList(Map<String, Object> params);

	public int updateByPrimaryKeySelective(OrdOrderHotelTimeRate OrdOrderHotelTimeRate);
	
	public List<OrdOrderHotelTimeRate> findOrdOrderHotelTimeRateListByParams(Map<String, Object> params);
	
	/**
	 * 查询酒店最后离店时间
	 * @param ordorderItemId
	 * @return
	 */
	public List<Date> findOrdOrderItemHotelLastLeaveTimeByItemId(Long ordorderItemId);
}
