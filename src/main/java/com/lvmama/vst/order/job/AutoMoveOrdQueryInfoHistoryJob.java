package com.lvmama.vst.order.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderQueryInfoService;

@Service
public class AutoMoveOrdQueryInfoHistoryJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AutoMoveOrdQueryInfoHistoryJob.class);
	
	@Autowired
	private IOrdOrderQueryInfoService ordOrderQueryInfoService;
	
	@Override
	public void run() {
		logger.info("AutoMoveOrdQueryInfoHistoryJob start...");
		if(Constant.getInstance().isJobRunnable()){
			logger.info("start move order query info history data:");
//			for(int i = 0; i < 10; i++) {
//				List<Long> queryInfoIds = ordOrderQueryInfoService.findOrdQueryInfoHistoryData();
//				if(CollectionUtils.isNotEmpty(queryInfoIds)) {
//					int fromIndex = 0;
//					int toIndex = 0;
//	
//					// 分批执行，每次100条
//					while (fromIndex < queryInfoIds.size()) {
//						if (fromIndex + 100 >= queryInfoIds.size()) {
//							toIndex = queryInfoIds.size();
//						} else {
//							toIndex = fromIndex + 100;
//						}
//						ordOrderQueryInfoService.saveOrdQueryInfoHistoryData(queryInfoIds.subList(fromIndex, toIndex));
//						
//						fromIndex = toIndex;
//					}
//				}
//			}
			logger.info("end move order query info history data:");
		}
		logger.info("AutoMoveOrdQueryInfoHistoryJob end...");
	}
}
