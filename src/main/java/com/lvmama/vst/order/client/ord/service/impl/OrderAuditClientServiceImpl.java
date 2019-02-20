package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderAuditClientService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.order.service.IOrderAuditService;
@Component("orderAuditServiceRemote")
public class OrderAuditClientServiceImpl implements OrderAuditClientService {

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Override
	public List<ComAudit> queryAuditListByParam(Map<String, Object> param) {
		return orderAuditService.queryAuditListByParam(param);
	}

	@Override
	public Long saveAudit(ComAudit audit) {
		orderAuditService.saveAudit(audit);
		return audit.getAuditId();
	}

	@Override
	public int updateByPrimaryKey(ComAudit comAudit) {
		return orderAuditService.updateByPrimaryKey(comAudit);
	}
	
	@Override
	public ResultMessage cancelOrderItemConfirmAudit(Long ordOrderItemId) {
		ResultMessage resultMessage=ResultMessage.createResultMessage();
		if(ordOrderItemId==null){
			resultMessage.setMessage("ordOrderItemId is null");
			resultMessage.setCode("ORD_ORDER_ITEM_ID_NULL");
			return resultMessage;
		}
		Map<String, Object> param=new HashMap<>();
		param.put("objectId", ordOrderItemId);
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		param.put("auditType", Confirm_Enum.CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name());
		List<ComAudit> comauditList=this.queryAuditListByParam(param);
		if(comauditList==null||comauditList.size()<=0){
			resultMessage.setMessage("CANCEL_CONFIRM_AUDIT is null");
			resultMessage.setCode("CANCEL_CONFIRM_AUDIT_NULL");
			return resultMessage;
		}
		for (ComAudit comAudit : comauditList) {
			comAudit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
			orderAuditService.updateByPrimaryKeyNew(comAudit);
		}
		return resultMessage;
	}

}
