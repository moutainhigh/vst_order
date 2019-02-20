package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.goods.service.SuppGoodsCircusClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsCircusDetailClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsCircus;
import com.lvmama.vst.back.goods.po.SuppGoodsCircusDetail;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.supp.client.service.SupplierStockCheckService;

@Service
public class AutoSyncCircusDataJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AutoSyncCircusDataJob.class);
	
	@Autowired
	private SuppGoodsCircusClientService suppGoodsCircusClientService;
	
	@Autowired
	private SuppGoodsCircusDetailClientService suppGoodsCircusDetailClientService;
	
	@Autowired
	private OrderService orderService;
	
	@Resource(name="supplierStockCheckService")
	private SupplierStockCheckService supplierStockCheckService;
	
	private final int SYNC_DAYS = 31;
	
	@Override
	public void run() {
//		if(Constant.getInstance().isJobRunnable()){
			logger.info("AutoSyncCircusDataJob start...");
			try {
				ResultHandleT<List<SuppGoodsCircus>> suppGoodsCircusResult = suppGoodsCircusClientService.findSuppGoodsCircus();
				if(suppGoodsCircusResult == null) {
					logger.error("suppGoodsCircusResult is null.");
					return;
				}
				
				if(suppGoodsCircusResult.isFail())  {
					logger.error("suppGoodsCircusResult fail, error message:" + suppGoodsCircusResult.getMsg());
					return;
				}
				
				List<SuppGoodsCircus> suppGoodsCircusList = suppGoodsCircusResult.getReturnContent();
				if(CollectionUtils.isNotEmpty(suppGoodsCircusList)) {
					for(SuppGoodsCircus suppGoodsCircus : suppGoodsCircusList) {
						if (suppGoodsCircus.getLastSyncTime() != null
								&& suppGoodsCircus.getLastSyncTime().after(
										DateUtil.getTodayYMDDate())) {
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("suppGoodsId", suppGoodsCircus.getSuppGoodsId());
							List<SuppGoodsCircusDetail> stockLimitedCircusList = suppGoodsCircusDetailClientService.queryStockLimitedCircus(params);
							if(CollectionUtils.isNotEmpty(stockLimitedCircusList)) {
								for(SuppGoodsCircusDetail stockLimitedCircus : stockLimitedCircusList) {
									stockLimitedCircus.setStock(supplierStockCheckService.getActCount(
													stockLimitedCircus.getSuppGoodsId(),
													stockLimitedCircus.getVisitTime(),
													String.valueOf(stockLimitedCircus.getActId())));
									
									suppGoodsCircusDetailClientService.updateCircusDetail(stockLimitedCircus);
								}
							}
							continue;
						}
						
						for (int day = 0; day <= SYNC_DAYS; day++) {
							Date date = DateUtil.toYMDDate(DateUtil
									.getDateAfterDays(new Date(), day));
							List<SuppGoodsCircusDetail> suppGoodsCircusDetails = syncCircusTimesAndStock(
									suppGoodsCircus.getSuppGoodsId(), date);
							//保存
							suppGoodsCircusDetailClientService
									.saveCircusDetail(
											suppGoodsCircus.getSuppGoodsId(),
											date, suppGoodsCircusDetails);
						}
						
						//修改最后同步时间
						suppGoodsCircusClientService.updateLastSyncTime(suppGoodsCircus.getCircusId());
					}
					
					//刷新缓存
					for(SuppGoodsCircus suppGoodsCircus : suppGoodsCircusList) {
						for (int day = 0; day <= SYNC_DAYS; day++) {
							Date date = DateUtil.toYMDDate(DateUtil
									.getDateAfterDays(new Date(), day));
							String key = "CIRCUS_TIMES_DETAIL_" + suppGoodsCircus.getSuppGoodsId() + "_" + DateUtil.formatSimpleDate(date);
							if(MemcachedUtil.getInstance().keyExists(key)) {
								MemcachedUtil.getInstance().remove(key);
							}
							if(StringUtils.isBlank(suppGoodsCircus.getCacheFlag()) || "Y".equals(suppGoodsCircus.getCacheFlag())) {
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("suppGoodsId", suppGoodsCircus.getSuppGoodsId());
								params.put("visitTime", date);
								MemcachedUtil.getInstance()
										.set(key, 24 * 60 * 60,
												suppGoodsCircusDetailClientService.queryCircusByCondition(params));
							}
						}
					}
				}
				
			} catch (Exception e) {
				logger.error("{}", e);
			}
			
			logger.info("AutoSyncCircusDataJob end...");
//		}
	}
	
	/**
	 * 根据商品ID和游玩日期获取场次库存信息
	 * @param suppGoodsId
	 * @param visitTime
	 * @return
	 * @throws Exception
	 */
	private List<SuppGoodsCircusDetail> syncCircusTimesAndStock(Long suppGoodsId, Date visitTime) throws Exception {
		List<String> actTimes = supplierStockCheckService.getActTimes(suppGoodsId, visitTime);
		List<SuppGoodsCircusDetail> suppGoodsCircusDetails = new ArrayList<SuppGoodsCircusDetail>();
		if(CollectionUtils.isEmpty(actTimes)) {
			return suppGoodsCircusDetails;
		}
		for(String act : actTimes) {
			String[] actArray = act.split(",");
			if(actArray == null || actArray.length <= 2) {
				logger.error("数据异常, act:" + act);
				continue;
			}
			SuppGoodsCircusDetail suppGoodsCircusDetail = new SuppGoodsCircusDetail();
			suppGoodsCircusDetails.add(suppGoodsCircusDetail);
			suppGoodsCircusDetail.setSuppGoodsId(suppGoodsId);
			suppGoodsCircusDetail.setVisitTime(visitTime);
			suppGoodsCircusDetail.setActId(Long.valueOf(actArray[0]));
			suppGoodsCircusDetail.setStartTime(DateUtil.toDate(actArray[1], DateUtil.HHMM_DATE_FORMAT));
			suppGoodsCircusDetail.setEndTime(DateUtil.toDate(actArray[2], DateUtil.HHMM_DATE_FORMAT));
			//获取库存
			suppGoodsCircusDetail.setStock(supplierStockCheckService.getActCount(suppGoodsId, visitTime, actArray[0]));
		}
		
		return suppGoodsCircusDetails;
	}

}
