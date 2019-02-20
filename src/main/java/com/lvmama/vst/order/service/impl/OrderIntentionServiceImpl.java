package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.intentionOrder.po.IntentionOrder;
import com.lvmama.vst.back.intentionOrder.service.IntentionOrderClientService;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrderIntentionService;
/**
 * 意向单管理接口层
 * @author chenpingfan
 *
 */
@Service("orderIntentionService")
public class OrderIntentionServiceImpl implements IOrderIntentionService {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderIntentionServiceImpl.class);
	@Autowired
	private IntentionOrderClientService intentionOrderClientServiceRemote;
	
	@Resource
	private ComLogClientService comLogClientServiceRemote;
	
	@Override
	public List<IntentionOrder> queryIntentionsByCriteria(Map<String, Object> param) {	
		ResultHandleT<List<IntentionOrder>> result = intentionOrderClientServiceRemote.findIntentionOrderList(param);
		if(result == null)
			return null;
		return result.getReturnContent();
	}

	@Override
	public Integer getTotalCount(Map<String, Object> param) {
		ResultHandleT<Integer> result = intentionOrderClientServiceRemote.findIntentionOrderCount(param);
		if(result == null)
			return null;
		return result.getReturnContent();
	}

	@Override
	public int updateIntention(IntentionOrder intentionOrder) {
		ResultHandleT<Integer> result = intentionOrderClientServiceRemote.editIntentionOrder(intentionOrder);
		if(result == null)
			return 0;
		int updateCount = result.getReturnContent();
		StringBuffer errMsg = new StringBuffer();
		//添加更新日志
		if(updateCount>0){
			try {
				ComLog log = new ComLog();
				log.setObjectType(ComLog.COM_LOG_OBJECT_TYPE.INTENTION_ORDER_UPDATE.name());
				log.setParentType(ComLog.COM_LOG_PARENT_TYPE.INTENTION_ORDER.name());
				log.setParentId(intentionOrder.getIntentionOrderId());
				log.setLogType(ComLog.COM_LOG_LOG_TYPE.INTENTION_ORDER.name());
				log.setObjectId(intentionOrder.getIntentionOrderId());
				log.setOperatorName(intentionOrder.getLoginName());
				log.setCreateTime(new Date());
				log.setContent("更改意向单的状态为"+intentionOrder.getStateView());
				log.setLogName("意向单订单状态修改");
				comLogClientServiceRemote.addComLog(log);
			} catch (Exception e) {
				LOGGER.error(ExceptionFormatUtil.getTrace(e));
			}			
		}
		return updateCount;
	}	
	
	
}
