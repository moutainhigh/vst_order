/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.PromPromotionService;


@Service("promPromotionService")
public class PromPromotionServiceImpl implements PromPromotionService {
	
	private static final Log LOG = LogFactory.getLog(PromPromotionServiceImpl.class);
	
//	@Autowired
//	private PromPromotionDao promPromotionDao;
	@Autowired
	private PromotionService promotionService;


	
    public int addPromAmount(Long amount,Long promPromotionId){
    	ResultHandleT<Long> resultHanle = promotionService.addPromAmount(amount, promPromotionId);
    	int result = 0;
    	if(resultHanle.isSuccess()){
    		result = resultHanle.getReturnContent().intValue();
    	}
    	return result;
    }
    
    public int subtractPromAmount(Long amount,Long promPromotionId){
    	ResultHandleT<Long> resultHanle = promotionService.subtractPromAmount(amount, promPromotionId);
    	int result = 0;
    	if(resultHanle.isSuccess()){
    		result = resultHanle.getReturnContent().intValue();
    	}
    	return result;
    }
	
}
