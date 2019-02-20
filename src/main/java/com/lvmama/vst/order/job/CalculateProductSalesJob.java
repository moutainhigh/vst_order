package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.VstSearchSaleService;
import com.lvmama.vst.order.service.impl.VstSearchSaleServiceImpl;

/**
 * 产品近10天的销售额的统计 
 * @author yanliping
 *
 */
public class CalculateProductSalesJob implements Runnable{
	
	private static Logger LOG = LoggerFactory.getLogger(VstSearchSaleServiceImpl.class);
	
	@Autowired
	private VstSearchSaleService vstSearchSaleService;

	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable()+"");
		if(Constant.getInstance().isJobRunnable()){
			LOG.info("CalculateProductSalesJob Start");
			vstSearchSaleService.createVstSearchSale();
			LOG.info("CalculateProductSalesJob end");
		}
	}

}
