/**
 * 
 */
package com.lvmama.vst.precontrol.hotel.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.lvmama.precontrol.hotel.HotelResPrecontrolBindGoodsService;
import com.lvmama.precontrol.vo.VstOrderItemVo;
import com.lvmama.vst.precontrol.impl.CommResPrecontrolBindsServiceImpl;
import com.lvmama.vst.precontrol.impl.RemoteResPrecontrolBindGoodsServiceImpl;

/**
 * @author chenlizhao
 *
 */
@Service("hotelResPrecontrolBindGoodsService")
public class HotelResPrecontrolBindGoodsServiceImpl extends CommResPrecontrolBindsServiceImpl implements HotelResPrecontrolBindGoodsService {
	
	private static final Log LOG = LogFactory.getLog(HotelResPrecontrolBindGoodsServiceImpl.class);
	
	@Override
	public Long getHotelOrderItemNum(Map<String, Object> param) {
		LOG.info("call getHotelOrderItemNum with " + JSON.toJSONString(param));
		return remoteResPrecontrolBindGoodsDao.getHotelOrderItemNum(param);
	}
	
	@Override
	public List<VstOrderItemVo> getVstNotBuyoutOrderHotel(Map<String, Object> params) {
		LOG.info("call getVstNotBuyoutOrderHotel with " + JSON.toJSONString(params));
		return remoteResPrecontrolBindGoodsDao.getVstNotBuyoutOrderHotel(params);
	}
	
	@Override
	public int updateVstBudgetFlagBylistHotel(Map<String, Object> params) {
		LOG.info("call updateVstBudgetFlagBylistHotel with " + JSON.toJSONString(params));
		return remoteResPrecontrolBindGoodsDao.updateVstBudgetFlagBylistHotel(params);
	}

	@Override
	public int updateOrderBatchHotel(List<VstOrderItemVo> list) {		
		LOG.info("call updateOrderBatchHotel with " + JSON.toJSONString(list));
		return remoteResPrecontrolBindGoodsDao.updateOrderBatchHotel(list);
	}		
}
