package com.lvmama.vst.order.timeprice.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lvmama.comm.utils.JsonBeanUtils;
import com.lvmama.price.api.comm.vo.PriceResultHandleT;
import com.lvmama.price.api.strategy.model.vo.ShowGoodsAddTimePriceVo;
import com.lvmama.price.api.strategy.service.ShowGoodsAddTimePriceApiService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.goods.po.ShowGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsShareDayLimit;
import com.lvmama.vst.back.goods.po.SuppGoodsShareDayLimitGroup;
import com.lvmama.vst.back.goods.po.SuppGoodsShareStockLog;
import com.lvmama.vst.back.goods.po.SuppGoodsShareTotalStock;
import com.lvmama.vst.back.goods.po.SuppGoodsShareTotalStockGroup;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.goods.utils.BeanUtils;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.order.dao.goods.SuppGoodsGroupStockOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareDayLimitGroupOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareDayLimitOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareStockLogOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareTotalStockGroupOraDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsShareTotalStockOraDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演出票的时间价格表，提供给演出票使用
 * @author lancey
 *
 */
@Component("goodsOraTicketShowAddTimePriceStockService")
public class GoodsOraTicketShowAddTimePriceStockServiceImpl implements IGoodsTimePriceStockService{
    private static final int APPRISE_COUNT = 20;

//	@Autowired
//	private ShowGoodsAddTimePriceOraDao showGoodsAddTimePriceDao;

	@Autowired
	private ShowGoodsAddTimePriceApiService showGoodsAddTimePriceApiService;

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
    
	@Autowired
	private IGoodsBaseTimePriceStockService goodsBaseTimePriceStockServiceImpl;
	
	@Autowired
	private SuppGoodsShareStockLogOraDao suppGoodsShareStockLogDao;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Override
	public boolean updateStock(Long timePriceId, Long stock) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("timePriceId", timePriceId);
		params.put("stock", stock);
		int num = -1;
		PriceResultHandleT<Integer> priceResultHandleT = showGoodsAddTimePriceApiService.updateStockForOrder(params);
		if(priceResultHandleT.isFail()){
			logger.error(priceResultHandleT.getMsg());
		}else{
			num = priceResultHandleT.getReturnContent();
		}
		if(num == 1) {
			ShowGoodsAddTimePrice timePrice = transPriceVo(showGoodsAddTimePriceApiService.selectByTimePriceId(timePriceId));
			goodsBaseTimePriceStockServiceImpl.pushTimePrice(timePrice.getSuppGoodsId(), Collections.singletonList(timePrice.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
		}
		return num>0;
	}

	/**
	 * 将lvmama_price中 价格对象 转为 vst价格对象
	 * @param showGoodsAddTimePriceVos
	 * @return
	 */
	private ShowGoodsAddTimePrice transPriceVo(PriceResultHandleT<ShowGoodsAddTimePriceVo> priceResultHandleT){
		ShowGoodsAddTimePrice showGoodsAddTimePrice = new ShowGoodsAddTimePrice();
		try {
			ShowGoodsAddTimePriceVo showGoodsAddTimePriceVo = null;
			if (priceResultHandleT.isFail()) {
				logger.error(priceResultHandleT.getMsg());
			} else {
				showGoodsAddTimePriceVo = priceResultHandleT.getReturnContent();
			}
			if(null !=showGoodsAddTimePriceVo){
				showGoodsAddTimePrice = JsonBeanUtils.copyProperties(showGoodsAddTimePriceVo, ShowGoodsAddTimePrice.class);
			}
		} catch (Exception e) {
			logger.error("method<transPriceVo> execute fail..."+e.getMessage());
		}
		return showGoodsAddTimePrice;
	}

	@Override
	public SuppGoodsBaseTimePrice getTimePrice(Long goodsId, Date specDate,
			boolean checkAhead) {
		SuppGoodsBaseTimePrice timePrice;
		if(checkAhead){
			// 生成订单时门票时间价格表需要“预订天数限制”
			timePrice = getTimePriceAtCreateOrder(goodsId, specDate);
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
			timePrice = getTimePriceExt(goodsId, specDate,false);
		}
		return timePrice;
	}

	public ShowGoodsAddTimePrice getTimePriceAtCreateOrder(Long suppGoodsId,Date date){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", date);

		PriceResultHandleT<ArrayList<ShowGoodsAddTimePriceVo>> priceResultHandleT = showGoodsAddTimePriceApiService.selectByExample(params);
		if(priceResultHandleT.isFail()){
			logger.error(priceResultHandleT.getMsg());
		}else{
			List<ShowGoodsAddTimePriceVo> showGoodsAddTimePriceVos = priceResultHandleT.getReturnContent();
			if(CollectionUtils.isNotEmpty(showGoodsAddTimePriceVos)){
				ArrayList<ShowGoodsAddTimePrice> showGoodsAddTimePrices = BeanUtils.copyProperties(showGoodsAddTimePriceVos,new TypeReference<ArrayList<ShowGoodsAddTimePrice>>(){});

				SuppGoods suppGoods = null;
				if(params.get("suppGoodsId") != null){
					suppGoods = suppGoodsClientService.selectByPrimaryKey( Long.parseLong(params.get("suppGoodsId").toString()));
				}

				for(ShowGoodsAddTimePrice showGoodsAddTimePrice1:showGoodsAddTimePrices){
					//isLimitBookDay的筛选
					//done 解决循环调用 suppGoodsService.selectByPrimaryKey

					if(suppGoods == null) {
						suppGoods = suppGoodsClientService.selectByPrimaryKey(showGoodsAddTimePrice1.getSuppGoodsId());
					}else {
						if(!suppGoods.getSuppGoodsId().equals(showGoodsAddTimePrice1.getSuppGoodsId())){
							suppGoods = suppGoodsClientService.selectByPrimaryKey(showGoodsAddTimePrice1.getSuppGoodsId());
						}
					}

					//SuppGoods suppGoods = suppGoodsService.selectByPrimaryKey(showGoodsAddTimePrice1.getSuppGoodsId());
					Long limitBookDay = suppGoods.getLimitBookDay()==null?-1:suppGoods.getLimitBookDay();
					if(limitBookDay<=-1||(showGoodsAddTimePrice1.getSpecDate().getTime()< org.apache.commons.lang.time.DateUtils.addDays(new Date(),limitBookDay.intValue()).getTime())){//(nvl(sg.limit_book_day,-1) <= -1 or timeprice.SPEC_DATE < (sysdate + sg.limit_book_day))
						Long shareTotalStockId = showGoodsAddTimePrice1.getShareTotalStockId();
						Long shareDayLimitId = showGoodsAddTimePrice1.getShareDayLimitId();
						SuppGoodsShareTotalStock suppGoodsShareTotalStock = null;
						SuppGoodsShareDayLimit suppGoodsShareDayLimit = null;
						if(shareTotalStockId>0) {
							suppGoodsShareTotalStock = suppGoodsShareTotalStockDao.selectByPrimaryKey(showGoodsAddTimePrice1.getShareTotalStockId());
						}
						if(shareDayLimitId>0) {
							suppGoodsShareDayLimit = suppGoodsShareDayLimitDao.selectByPrimaryKey(showGoodsAddTimePrice1.getShareDayLimitId());
						}
						Long shareTotalStock = null;
						Long shareDayLimit = null;
						if(suppGoodsShareTotalStock!=null&&suppGoodsShareTotalStock.getCurrentCount()!=null){
							shareTotalStock = suppGoodsShareTotalStock.getCurrentCount();
						}
						if(suppGoodsShareDayLimit!=null&&suppGoodsShareDayLimit.getCurrentCount()!=null){
							shareDayLimit = suppGoodsShareDayLimit.getCurrentCount();
						}
						showGoodsAddTimePrice1.setStockFlag(shareTotalStockId==0?showGoodsAddTimePrice1.getStockFlag():"Y");
						Long stock = null;
						if(shareTotalStockId==0){
							stock = showGoodsAddTimePrice1.getStock();
						}else{
							if(shareDayLimitId==0){
								if(shareTotalStock!=null) {
									stock = shareTotalStock;
								}
							}else{
								if(shareTotalStock!=null&&shareDayLimit!=null) {
									stock = shareTotalStock > shareDayLimit ? shareDayLimit : shareTotalStock;
								}
							}
						}
						showGoodsAddTimePrice1.setStock(stock);
						showGoodsAddTimePrice1.setOversellFlag(shareTotalStockId==0?showGoodsAddTimePrice1.getOversellFlag():"N");
						return showGoodsAddTimePrice1;
					}
				}
			}else{
				return null;
			}
		}
		return null;
	}

	/**
	 * 得到指定日期的时间价格表
	 * @param suppGoodsId
	 * @param specDate
	 * @return
	 */
	public ShowGoodsAddTimePrice getTimePriceExt(Long suppGoodsId, Date specDate, boolean checkAhead) {
		Map<String, Object> params = new HashMap<>();
		params.put("suppGoodsId", suppGoodsId);
		params.put("specDate", specDate);
		params.put("checkAhead", checkAhead);
		PriceResultHandleT<ArrayList<ShowGoodsAddTimePriceVo>> priceResultHandleT = showGoodsAddTimePriceApiService.selectByExample(params);
		if(priceResultHandleT.isFail()){
			logger.error(priceResultHandleT.getMsg());
		}else{
			List<ShowGoodsAddTimePriceVo> showGoodsAddTimePriceVos = priceResultHandleT.getReturnContent();
			if(CollectionUtils.isNotEmpty(showGoodsAddTimePriceVos)){
				ArrayList<ShowGoodsAddTimePrice> showGoodsAddTimePrices = BeanUtils.copyProperties(showGoodsAddTimePriceVos,new TypeReference<ArrayList<ShowGoodsAddTimePrice>>(){});
				for(ShowGoodsAddTimePrice showGoodsAddTimePrice1:showGoodsAddTimePrices){
					Long shareTotalStockId = showGoodsAddTimePrice1.getShareTotalStockId();
					Long shareDayLimitId = showGoodsAddTimePrice1.getShareDayLimitId();
					SuppGoodsShareTotalStock suppGoodsShareTotalStock = null;
					SuppGoodsShareDayLimit suppGoodsShareDayLimit = null;
					if(shareTotalStockId>0) {
						suppGoodsShareTotalStock = suppGoodsShareTotalStockDao.selectByPrimaryKey(showGoodsAddTimePrice1.getShareTotalStockId());
					}
					if(shareDayLimitId>0) {
						suppGoodsShareDayLimit = suppGoodsShareDayLimitDao.selectByPrimaryKey(showGoodsAddTimePrice1.getShareDayLimitId());
					}
					Long shareTotalStock = null;
					Long shareDayLimit = null;
					if(suppGoodsShareTotalStock!=null&&suppGoodsShareTotalStock.getCurrentCount()!=null){
						shareTotalStock = suppGoodsShareTotalStock.getCurrentCount();
					}
					if(suppGoodsShareDayLimit!=null&&suppGoodsShareDayLimit.getCurrentCount()!=null){
						shareDayLimit = suppGoodsShareDayLimit.getCurrentCount();
					}
					showGoodsAddTimePrice1.setStockFlag(shareTotalStockId==0?showGoodsAddTimePrice1.getStockFlag():"Y");
					Long stock = null;
					if(shareTotalStockId==0){
						stock = showGoodsAddTimePrice1.getStock();
					}else{
						if(shareDayLimitId==0){
							if(shareTotalStock!=null) {
								stock = shareTotalStock;
							}
						}else{
							if(shareTotalStock!=null&&shareDayLimit!=null) {
								stock = shareTotalStock > shareDayLimit ? shareDayLimit : shareTotalStock;
							}
						}
					}
					showGoodsAddTimePrice1.setStock(stock);
					showGoodsAddTimePrice1.setOversellFlag(shareTotalStockId==0?showGoodsAddTimePrice1.getOversellFlag():"N");
					return showGoodsAddTimePrice1;
				}
			}else{
				return null;
			}
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(GoodsOraTicketShowAddTimePriceStockServiceImpl.class);

	@Override
	public boolean updateGroupStock(Long timePriceId, Long stock,
			Long orderItemId, List<OrdOrderGroupStock> ordOrderGroupStockList) {
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
		Long shareTotalStockId = 0l;
		Long shareDayLimitId = 0l;
		Long suppGoodsId = 0l;
		Date visitDate = null;
		Long orderStockId = null;
		
		if(dataMap.get("orderStockId") != null) {
			orderStockId = (Long)dataMap.get("orderStockId");
		}
		
		final String key="VST_ORDER_STOCK_REVERT_"+orderStockId;
		
		logger.info(key);
//		try{
			if (orderStockId != null && SynchronizedLock.isOnDoingMemCached(key)) {
				logger.error("订单库存重复返还，orderStockId：" + orderStockId);
				return false;
			}
		
			if(dataMap.get("suppGoodsId") != null) {
				suppGoodsId = (Long)dataMap.get("suppGoodsId");
			}
			
			if(dataMap.get("shareTotalStockId") != null) {
				shareTotalStockId = (Long)dataMap.get("shareTotalStockId");
			}
			
			if(dataMap.get("shareDayLimitId") != null) {
				shareDayLimitId = (Long)dataMap.get("shareDayLimitId");
			}
			
			if(dataMap.get("visitDate") != null) {
				visitDate = (Date)dataMap.get("visitDate");
			}
			logger.info("shareTotalStockId:" + shareTotalStockId + ",shareDayLimitId:" + shareDayLimitId);
			
			//走共享总库存
			if(shareTotalStockId > 0) {
				SuppGoodsShareTotalStock suppGoodsShareTotalStock = suppGoodsShareTotalStockDao.selectByPrimaryKey(shareTotalStockId);
				if(suppGoodsShareTotalStock == null) {
					logger.error("门票共享总库存返还失败, 找不到对应的SuppGoodsShareTotalStock, shareTotalStockId=" + shareTotalStockId);
					return false;
				}
				
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("groupId", suppGoodsShareTotalStock.getGroupId());
				params.put("suppGoodsId", suppGoodsId);
				
				List<SuppGoodsShareTotalStockGroup> shareTotalStockGroupList = suppGoodsShareTotalStockGroupDao.selectByCondition(params);
				
				if(CollectionUtils.isEmpty(shareTotalStockGroupList)) {
					logger.error("门票共享总库存返还失败, 商品" + suppGoodsId + "不在库存组"
							+ suppGoodsShareTotalStock.getGroupId()
							+ "中, shareTotalStockId=" + shareTotalStockId);
					return false;
				}
				
				if(visitDate == null || suppGoodsShareTotalStock.getBeginDate() == null || suppGoodsShareTotalStock.getEndDate() == null) {
					logger.error("门票共享总库存返还失败, 游玩日期/共享总库存起止日期为空, visitDate:" + visitDate
							+ ", beginDate:"
							+ suppGoodsShareTotalStock.getBeginDate() + ",endDate:"
							+ suppGoodsShareTotalStock.getEndDate()
							+ ", shareTotalStockId=" + shareTotalStockId);
					return false;
				}
				
				if(visitDate.before(suppGoodsShareTotalStock.getBeginDate()) || visitDate.after(suppGoodsShareTotalStock.getEndDate())) {
					logger.error("门票共享总库存返还失败, 游玩日期不在共享总库存起止日期范围内, visitDate:" + visitDate
							+ ", beginDate:"
							+ suppGoodsShareTotalStock.getBeginDate() + ",endDate:"
							+ suppGoodsShareTotalStock.getEndDate()
							+ ", shareTotalStockId=" + shareTotalStockId);
					return false;
				}
	
	            int updated = suppGoodsShareTotalStockDao.updateStockForOrder(shareTotalStockId, stock);
				if(updated <= 0) {
					logger.error("门票共享总库存返还失败, shareTotalStockId=" + shareTotalStockId);
					return false;
				}
				
				//共享总库存返还成功，如果存在共享日限制，则返还共享日限制
				if(shareDayLimitId > 0) {
					SuppGoodsShareDayLimit suppGoodsShareDayLimit = suppGoodsShareDayLimitDao.selectByPrimaryKey(shareDayLimitId);
					if(suppGoodsShareDayLimit == null) {
						logger.error("门票共享日限制返还失败, 找不到对应的SuppGoodsShareDayLimit, shareDayLimitId=" + shareDayLimitId);
						return false;
					}
					
					params = new HashMap<String, Object>();
					params.put("limitGroupId", suppGoodsShareDayLimit.getLimitGroupId());
					params.put("suppGoodsId", suppGoodsId);
					
					List<SuppGoodsShareDayLimitGroup> shareDayLimitGroupList = suppGoodsShareDayLimitGroupDao.selectByCondition(params);
					
					if(CollectionUtils.isEmpty(shareDayLimitGroupList)) {
						logger.error("门票共享日限制返还失败, 商品" + suppGoodsId + "不在日限制组"
								+ suppGoodsShareDayLimit.getLimitGroupId()
								+ "中, shareDayLimitId=" + shareDayLimitId);
						return false;
					}
					
					if(suppGoodsShareDayLimit.getSpecDate() == null) {
						logger.error("门票共享日限制返还失败, 游玩日期/共享日限制日期为空, visitDate:" + visitDate
								+ ", specDate:"
								+ suppGoodsShareDayLimit.getSpecDate()
								+ ", shareDayLimitId=" + shareDayLimitId);
						return false;
					}
					
					if(visitDate.compareTo(suppGoodsShareDayLimit.getSpecDate()) != 0) {
						logger.error("门票共享日限制返还失败, 游玩日期与共享日限制日期不相同, visitDate:" + visitDate
								+ ", specDate:"
								+ suppGoodsShareDayLimit.getSpecDate()
								+ ", shareDayLimitId=" + shareDayLimitId);
						return false;
					}
					
					updated = suppGoodsShareDayLimitDao.updateDayLimitForOrder(shareDayLimitId, stock);
					if(updated <= 0) {
						logger.error("门票共享日限制返还失败, shareDayLimitId=" + shareDayLimitId);
						return false;
					}
				}
				
				try{
					OrdOrderItem orderItem=new OrdOrderItem();
					if(dataMap.get("suppGoodsId") != null) {
						suppGoodsId = (Long)dataMap.get("suppGoodsId");
					}
					if(dataMap.get("orderItem") != null){
						orderItem=	 (OrdOrderItem)dataMap.get("orderItem");
					}
					logger.info("save revertStock SuppGoodsShareStockLog  begin====suppGoodsId:" + suppGoodsId +",dataMap:"+dataMap+ ",orderItem:" + orderItem.toString());
					SuppGoodsShareStockLog stockLog=new SuppGoodsShareStockLog();
					stockLog.setShareTotalStockId(shareTotalStockId);
					stockLog.setShareDayLimitId(shareDayLimitId);
					stockLog.setSuppGoodsId(suppGoodsId);
					stockLog.setOrderId(orderItem.getOrderId());
					stockLog.setOrderItemId(orderItem.getOrderItemId());
					stockLog.setStock(stock);
					stockLog.setCreateTime(new Date());
					stockLog.setVisitTime(orderItem.getVisitTime());
					suppGoodsShareStockLogDao.insert(stockLog);
				}catch(Exception e){
					logger.info("save SuppGoodsShareStockLog :"+ExceptionUtil.getExceptionDetails(e));
				}
				//分销推送逻辑
	            if (updated > 0) {
	                this.pushTimePrice(shareTotalStockId, shareDayLimitId);
	            }

	            return true;
			
			}
			
			//走日库存
			return this.updateStock(timePriceId, stock);
//		} finally {
//			if(orderStockId != null) {
//				logger.info("releaseMemCached, key:" + key);
//				SynchronizedLock.releaseMemCached(key);
//			}
//		}
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
		Long shareTotalStockId = 0l;
		Long shareDayLimitId = 0l;
		
		if(dataMap.get("shareTotalStockId") != null) {
			shareTotalStockId = (Long)dataMap.get("shareTotalStockId");
		}
		
		if(dataMap.get("shareDayLimitId") != null) {
			shareDayLimitId = (Long)dataMap.get("shareDayLimitId");
		}
	
		logger.info("shareTotalStockId:" + shareTotalStockId + ",shareDayLimitId:" + shareDayLimitId);
		
		//走共享总库存
		if(shareTotalStockId > 0) {
			SuppGoodsShareDayLimit suppGoodsShareDayLimit = suppGoodsShareDayLimitDao.selectByPrimaryKey(shareDayLimitId);
			if (shareDayLimitId > 0
					&& (suppGoodsShareDayLimit.getCurrentCount() == null || suppGoodsShareDayLimit.getCurrentCount() < stock)) {
				logger.error("门票共享日限制不足, shareDayLimitId=" + shareDayLimitId);
				throw new Exception("门票共享日限制不足, shareDayLimitId=" + shareDayLimitId);
			}
			
			int updated = suppGoodsShareTotalStockDao.updateStockForOrder(shareTotalStockId, stock);
			if(updated <= 0) {
				logger.error("门票共享总库存扣减失败, shareTotalStockId=" + shareTotalStockId);
				throw new Exception("门票共享总库存扣减失败, shareTotalStockId=" + shareTotalStockId);
			}
			
			if(shareDayLimitId > 0) {
				updated = suppGoodsShareDayLimitDao.updateDayLimitForOrder(shareDayLimitId, stock);
				if(updated <= 0) {
					logger.error("门票共享日限制扣减失败, shareDayLimitId=" + shareDayLimitId);
					throw new Exception("门票共享日限制扣减失败, shareDayLimitId=" + shareDayLimitId);
				}
			}
			try{
				Long suppGoodsId =0l;
				OrdOrderItem orderItem=new OrdOrderItem();
				if(dataMap.get("suppGoodsId") != null) {
					suppGoodsId = (Long)dataMap.get("suppGoodsId");
				}
				if(dataMap.get("orderItem") != null){
					orderItem=	 (OrdOrderItem)dataMap.get("orderItem");
				}
				logger.info("save deductStock SuppGoodsShareStockLog  begin====suppGoodsId:" + suppGoodsId +",dataMap:"+dataMap+ ",orderItem:" + orderItem.toString());
				SuppGoodsShareStockLog stockLog=new SuppGoodsShareStockLog();
				stockLog.setShareTotalStockId(shareTotalStockId);
				stockLog.setShareDayLimitId(shareDayLimitId);
				stockLog.setSuppGoodsId(suppGoodsId);
				stockLog.setOrderId(orderItem.getOrderId());
				stockLog.setOrderItemId(orderItem.getOrderItemId());
				stockLog.setStock(stock);
				stockLog.setCreateTime(new Date());
				stockLog.setVisitTime(orderItem.getVisitTime());
				suppGoodsShareStockLogDao.insert(stockLog);
			}catch(Exception e){
				logger.info("save SuppGoodsShareStockLog :"+ExceptionUtil.getExceptionDetails(e));
			}
			
			//分销推送逻辑
			if(updated > 0) {
                this.pushTimePrice(shareTotalStockId, shareDayLimitId);
			}
			
			return updated > 0;
		}
		
		//走日库存
		return this.updateStock(timePriceId, stock);
	}

    private void pushTimePrice(Long shareTotalStockId, Long shareDayLimitId) {
        SuppGoodsShareTotalStock shareTotalStock = suppGoodsShareTotalStockDao.selectByPrimaryKey(shareTotalStockId);
        if (shareTotalStock != null) {
            List<SuppGoodsShareTotalStockGroup> stockGroups = suppGoodsShareTotalStockGroupDao.selectByGroupId(shareTotalStock.getGroupId());
            List<Date> dateList = DateUtil.getDateList(shareTotalStock.getBeginDate(), shareTotalStock.getEndDate());
            List<Long> suppGoodsIdList = this.findSuppGoodsIdsByStock(stockGroups);
            Long leftCount = shareTotalStock.getCurrentCount();
            boolean appriseDistributorGetPrice = false;
            if (shareDayLimitId > 0) {
                SuppGoodsShareDayLimit shareDayLimit = suppGoodsShareDayLimitDao.selectByPrimaryKey(shareDayLimitId);
                if (shareDayLimit != null) {
                    Long shareDayLimitCurrentCount = shareDayLimit.getCurrentCount();
                    if (leftCount == null || (shareDayLimitCurrentCount != null && leftCount>shareDayLimitCurrentCount)) {
                        leftCount = shareDayLimitCurrentCount;
                    }
                    this.addToDateList(dateList, this.findDateByStockGroupsAndLimit(stockGroups, shareDayLimit));
                }
            }
            if (leftCount != null && leftCount <= APPRISE_COUNT) {
                appriseDistributorGetPrice = true;
            }
            if (CollectionUtils.isNotEmpty(suppGoodsIdList) && CollectionUtils.isNotEmpty(dateList)) {
//                comPushServiceRemote.pushTimePrice(suppGoodsIdList, dateList, ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS, appriseDistributorGetPrice);
            	goodsBaseTimePriceStockServiceImpl.pushTimePrice(suppGoodsIdList, dateList, ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS, appriseDistributorGetPrice);
            }
        }
    }

    private List<Long> findSuppGoodsIdsByStock(List<SuppGoodsShareTotalStockGroup> stockGroups) {
        List<Long> suppGoodsIdList = new ArrayList<Long>();
        if (CollectionUtils.isNotEmpty(stockGroups)) {
            for (SuppGoodsShareTotalStockGroup group : stockGroups) {
                suppGoodsIdList.add(group.getGoodsId());
            }
        }
        return suppGoodsIdList;
    }

    private void addToDateList(List<Date> dateList, Date specDate) {
        if (specDate == null || dateList == null) {
            return;
        }
        if (! dateList.contains(specDate)) {
            dateList.add(specDate);
        }
    }

    private Date findDateByStockGroupsAndLimit(List<SuppGoodsShareTotalStockGroup> stockGroups, SuppGoodsShareDayLimit limit) {
        if (limit == null) {
            return null;
        }
        List<Long> groupIdList = new ArrayList<Long>();
        for (SuppGoodsShareTotalStockGroup group : stockGroups) {
            groupIdList.add(group.getGoodsId());
        }
        if (CollectionUtils.isEmpty(groupIdList)) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupIds", groupIdList);
        List<SuppGoodsShareTotalStock> stocks = suppGoodsShareTotalStockDao.selectByParams(params);
        for (SuppGoodsShareTotalStock shareTotalStock : stocks) {
            if (this.isSpecDateInGap(limit.getSpecDate(), shareTotalStock.getBeginDate(), shareTotalStock.getEndDate())) {
                return limit.getSpecDate();
            }
        }
        return null;
    }

    private boolean isSpecDateInGap(Date specDate, Date beginDate, Date endDate) {
        return specDate.equals(beginDate) || specDate.equals(endDate) || (specDate.after(beginDate) && specDate.before(endDate));
    }
}
