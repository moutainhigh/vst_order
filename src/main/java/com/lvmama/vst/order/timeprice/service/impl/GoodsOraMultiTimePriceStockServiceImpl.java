package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsMultiTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsMultiTimePriceOraDao;

/**
 * 
 * @author sunjian
 *
 */
@Service("goodsOraMultiTimePriceStockService")
public class GoodsOraMultiTimePriceStockServiceImpl implements IGoodsTimePriceStockService {
	
	@Autowired
	private SuppGoodsMultiTimePriceOraDao suppGoodsMultiTimePriceDao;

	@Autowired(required=false)
	private ComPushClientService comPushServiceRemote;
	
	@Autowired
    private SuppGoodsGroupStockOraDao suppGoodsGroupStockDao;
	
	@Autowired
	private IGoodsBaseTimePriceStockService goodsBaseTimePriceStockServiceImpl;
	
	@Override
	public boolean updateStock(Long timePriceId, Long stock) {
		boolean isSuccess = false;
		
		if(timePriceId != null) {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("timePriceId", timePriceId);
			params.put("stock", stock);
			int ret = suppGoodsMultiTimePriceDao.updateStockForOrder(params);
			
			if (ret > 0) {
				SuppGoodsMultiTimePrice timePrice = suppGoodsMultiTimePriceDao.selectByPrimaryKey(timePriceId);
//				comPushServiceRemote.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
				goodsBaseTimePriceStockServiceImpl.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
				isSuccess = true;
			}
		}
		
		return isSuccess;
	}

	@Override
	public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
		return suppGoodsMultiTimePriceDao.getTimePrice(goodsId, specDate, checkAhead);
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
