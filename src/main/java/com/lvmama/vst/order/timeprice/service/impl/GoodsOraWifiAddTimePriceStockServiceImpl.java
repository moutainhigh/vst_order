package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.comm.utils.JsonBeanUtils;
import com.lvmama.price.api.comm.vo.PriceResultHandleT;
import com.lvmama.price.api.strategy.service.SuppGoodsAddTimePriceApiService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareDayLimitGroupOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareDayLimitOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareTotalStockGroupOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareTotalStockOraDao;

/**
 * wifi/电话卡时间价格实现类
 * （借用门票时间价格表）
 *
 */
@Component("goodsOraWifiAddTimePriceStockService")
public class GoodsOraWifiAddTimePriceStockServiceImpl implements IGoodsTimePriceStockService{
    private static final Logger logger = LoggerFactory.getLogger(GoodsOraWifiAddTimePriceStockServiceImpl.class);

	/*@Autowired
	private SuppGoodsAddTimePriceOraDao suppGoodsAddTimePriceDao;*/

	@Autowired(required=false)
	private ComPushClientService comPushServiceRemote;
	
	@Autowired
    private SuppGoodsGroupStockOraDao suppGoodsGroupStockDao;
	
	@Autowired
	private SuppGoodsShareTotalStockOraDao suppGoodsShareTotalStockDao;
	
	@Autowired
	private SuppGoodsShareDayLimitOraDao suppGoodsShareDayLimitDao;

    @Autowired
    private SuppGoodsShareTotalStockGroupOraDao suppGoodsShareTotalStockGroupDao;
    
    @Autowired
    private SuppGoodsShareDayLimitGroupOraDao suppGoodsShareDayLimitGroupDao;

	@Autowired(required=false)
	private SuppGoodsAddTimePriceApiService addTimePriceApiService;

	@Override
	public boolean updateStock(Long timePriceId, Long stock) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("timePriceId", timePriceId);
		params.put("stock", stock);
		PriceResultHandleT<Integer> priceResultHandleT = addTimePriceApiService.updateStockForOrder(params);
		logger.info("com.lvmama.vst.order.timeprice.service.impl.GoodsOraWifiAddTimePriceStockServiceImpl.updateStock#timePriceId=" + timePriceId +" &stock="+stock);
		int num = priceResultHandleT.getReturnContent();
		logger.info("com.lvmama.vst.order.timeprice.service.impl.GoodsOraWifiAddTimePriceStockServiceImpl.updateStock#num = " + priceResultHandleT.getReturnContent());
		if(num == 1) {
			SuppGoodsAddTimePrice timePrice = JsonBeanUtils.copyProperties(addTimePriceApiService.selectByTimePriceId(timePriceId).getReturnContent(),SuppGoodsAddTimePrice.class);
			comPushServiceRemote.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
		}
		return num>0;
	}

	@Override
	public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate,
			boolean checkAhead) {
		SuppGoodsBaseTimePrice timePrice;
		if(checkAhead){
			// 生成订单时门票时间价格表需要“预订天数限制”
			timePrice = JsonBeanUtils.copyProperties(addTimePriceApiService.getTimePriceAtCreateOrder(goodsId, specDate).getReturnContent(),SuppGoodsAddTimePrice.class);
			if(timePrice==null){
				return null;
			}
			if(timePrice.getAheadBookTime()==null){
				logger.warn("supp goods id:{},timePriceId:{}, ahead is null",new Object[]{timePrice.getSuppGoodsId(),timePrice.getTimePriceId()});
				throw new NullPointerException("时间价格表异常");
			}
			Date date = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getAheadBookTime().intValue());
			if(date.before(new Date())){
				return null;
			}
		}else{
			timePrice = JsonBeanUtils.copyProperties(addTimePriceApiService.getTimePrice(goodsId, specDate , checkAhead).getReturnContent(),SuppGoodsAddTimePrice.class);
		}
		return timePrice;
	}
	
	

	@Override
	public boolean updateGroupStock(Long timePriceId, Long stock,
			Long orderItemId, List<OrdOrderGroupStock> ordOrderGroupStockList) {
        return false;
	}

	public Long getShareStock(Long groupId, Date specDate){
		return 0L;
    }

	@Override
	public boolean updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) throws Exception {
		//stock大于0，返库存
		if(stock > 0) {
			return revertStock(timePriceId, stock, dataMap);
		}
		//扣库存
		return deductStock(timePriceId, stock, dataMap);
	}
	
	/**
	 * 库存返还
	 * @param timePriceId
	 * @param stock
	 * @param dataMap
	 * @return
	 */
	private boolean revertStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) {
			Long orderStockId = null;
			if(dataMap.get("orderStockId") != null) {
			orderStockId = (Long)dataMap.get("orderStockId");
			logger.info("com.lvmama.vst.order.timeprice.service.impl.GoodsOraWifiAddTimePriceStockServiceImpl.revertStock# orderStockId = "+orderStockId);
			}
			final String key="VST_ORDER_STOCK_REVERT_"+orderStockId;
			logger.info(key);
			if (orderStockId != null && SynchronizedLock.isOnDoingMemCached(key)) {
				logger.error("订单库存重复返还，orderStockId：" + orderStockId);
				return false;
			}
			return this.updateStock(timePriceId, stock);

	}
	
	/**
	 * 库存扣减
	 * @param timePriceId
	 * @param stock
	 * @param dataMap
	 * @return
	 */
	private boolean deductStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) throws Exception{
		
		return this.updateStock(timePriceId, stock);
	}

}
