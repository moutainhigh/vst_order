/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lvmama.price.api.strategy.model.vo.SuppGoodsNotimeTimePriceVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsNotimeTimePriceOraDao;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author lancey
 *
 */
@Component("goodsOraTicketNotimeTimePriceStockService")
public class GoodsOraTicketNotimeTimePriceStockServiceImpl implements IGoodsTimePriceStockService{

	@Autowired
	private SuppGoodsNotimeTimePriceOraDao suppGoodsNotimeTimePriceDao;

	@Autowired(required=false)
	private ComPushClientService comPushServiceRemote;
	
	@Autowired
    private SuppGoodsGroupStockOraDao suppGoodsGroupStockDao;
	
	@Autowired
	private IGoodsBaseTimePriceStockService goodsBaseTimePriceStockServiceImpl;

	@Autowired
	private com.lvmama.price.api.strategy.service.SuppGoodsNotimeTimePriceApiService suppGoodsNotimeTimePriceApiServiceRemote;

	@Autowired
	private com.lvmama.vst.back.client.goods.service.SuppGoodsClientService suppGoodsClientService;

	@Override
	public boolean updateStock(Long timePriceId, Long stock) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("timePriceId", timePriceId);
		params.put("stock", stock);
		int rs = suppGoodsNotimeTimePriceDao.updateStockForOrder(params);
		if(rs == 1) {
			SuppGoodsNotimeTimePrice timePrice = suppGoodsNotimeTimePriceDao.selectByPrimaryKey(timePriceId);
//			comPushServiceRemote.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
			goodsBaseTimePriceStockServiceImpl.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
		}
		return rs>0;
	}

	@Override
	public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate,
			boolean checkAhead) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", goodsId);
		params.put("specDate", specDate);
		SuppGoodsNotimeTimePrice timePrice = null;
		if(null != goodsId){
			SuppGoods goods = suppGoodsClientService.findSuppGoodsById(goodsId).getReturnContent();
			if(null!=goods && (goods.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() ||
					goods.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() ||
			goods.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId())){
				SuppGoodsNotimeTimePriceVo vo = suppGoodsNotimeTimePriceApiServiceRemote.getTimePrice(params).getReturnContent();
				if(null != vo){
					String jsonStr = com.alibaba.fastjson.JSON.toJSONString(vo, SerializerFeature.DisableCircularReferenceDetect);
					timePrice = com.alibaba.fastjson.JSON.parseObject(jsonStr,new TypeReference<SuppGoodsNotimeTimePrice>(){});
				}
			}else {
				timePrice = suppGoodsNotimeTimePriceDao.getTimePrice(params);
			}
		}

		if(timePrice==null){
			return null;
		}

		if(checkAhead && timePrice.getAheadBookTime()!=null){
			Date date = DateUtils.addMinutes(timePrice.getEndDate(), -timePrice.getAheadBookTime().intValue());
			if(date.before(new Date())){
				return null;
			}
		}
		return timePrice;
	}

	@Override
	public boolean updateGroupStock(Long timePriceId, Long stock,
			Long orderItemId, List<OrdOrderGroupStock> ordOrderGroupStockList) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Long getShareStock(Long groupId, Date specDate){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupId", groupId);
        params.put("specDate", specDate);
        return suppGoodsGroupStockDao.findShareStock(params);
    }

	@Override
	public boolean updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
