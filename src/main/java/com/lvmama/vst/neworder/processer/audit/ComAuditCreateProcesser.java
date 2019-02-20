package com.lvmama.vst.neworder.processer.audit;

import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.common.lang3.StringUtils;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.neworder.service.audit.IOrderAuditNewService;

/** 
* @ImplementProject vst_order
* @Description: 订单活动创建
* @author xiaoyulin
* @date 2017年8月29日 下午2:23:14 
*/
public class ComAuditCreateProcesser implements MessageProcesser {
	
	@Resource
	private IOrderAuditNewService orderAuditNewService;

	@Override
	public void process(Message message) {
		if(MessageUtils.isComAuditCreateMsg(message)){
			Map<String, Object> attributes = message.getAttributes();
			if(isValid(attributes)){
				orderAuditNewService.saveComAudit((Long) attributes.get("objectId"), 
						(String) attributes.get("objectType"), 
						(String) attributes.get("auditType"), 
						(String) attributes.get("auditSubType"));
			}
		}
	}
	
	/**
	 * 
	 * @param attributes
	 * (objectId,objectType,auditType,auditSubType)
	 * @return
	 */
	private boolean isValid(Map<String, Object> attributes) {
		if(attributes == null){
			return false;
		}
		Long objectId = (Long) attributes.get("objectId");
		String objectType = (String) attributes.get("objectType");
		String auditType = (String) attributes.get("auditType");
		if(objectId == null || StringUtils.isEmpty(objectType) || StringUtils.isEmpty(auditType)){
			return false;
		}
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(objectType.trim()) 
				|| OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType.trim())){
			return true;
		}else{
			return false;
		}
		
	}

}
