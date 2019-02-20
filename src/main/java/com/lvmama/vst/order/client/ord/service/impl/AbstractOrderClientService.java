/**
 * 
 */
package com.lvmama.vst.order.client.ord.service.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * @author lancey
 *
 */
public class AbstractOrderClientService {
	private static final Logger logger = LoggerFactory.getLogger(AbstractOrderClientService.class);
	
	//公共操作日志业务
	@Autowired
	protected ComLogClientService comLogClientService;
	
	@Autowired
	private DictDefClientService dictDefClientService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;

	/**
	 * 
	 * 保存日志
	 * 
	 */
	protected void insertOrderLog( OrdOrder order ,String type,String assignor,String memo,String cancelCode,String reason){
		if (order != null) {
			String zhOrderStatus = OrderEnum.ORDER_STATUS.getCnName(type);
			Long orderId=order.getOrderId();
			
		    if (cancelCode != null && StringUtils.isNumeric(cancelCode)) {
		    	BizDictDef bizDictDef=dictDefClientService.findDictDefById(new Long(cancelCode)).getReturnContent();
		    	if (bizDictDef != null) {
		    		cancelCode = bizDictDef.getDictDefName();
		    	}
		    }
		    
		    if (cancelCode == null) {
		    	cancelCode = "默认";
		    }
		    
		    //拼接日志内容
			String cancelStr="   取消类型："+ OrderEnum.ORDER_CANCEL_CODE.getCnName(cancelCode) +",取消原因："+reason;
			String content="将编号为["+orderId+"]的订单活动变更为["+ zhOrderStatus +"]"+cancelStr;
			if (order.isSupplierOrder()) {
				content+="。此订单为供应商订单，自动发送消息给供应商，等待供应商确认后才可会真正取消订单";
			}
			
			ResultHandleT<Integer> rht = comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
					orderId, 
					orderId, 
					assignor, 
					content, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ zhOrderStatus +"]",
					memo);
			if(rht != null){
				logger.info("AbstractOrderClientService.insertOrderLog,orderId:" + orderId + ",isSuccess:" + rht.isSuccess());
			}
		}
	}
}
