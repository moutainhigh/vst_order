package com.lvmama.vst.order.confirm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.OrderItemConfirmStatusClientService;
@Service("OrderItemConfirmStatusClientServiceRemote")
public class OrderItemConfirmStatusClientServiceImpl implements OrderItemConfirmStatusClientService {

	@Autowired
    private IOrderAuditService orderAuditService;
	@Override
	public ResultHandle updateOrderItemStatusByOrderItemIdList(List<Long> orderItemIdList,Confirm_Enum.CONFIRM_AUDIT_TYPE type) {
		ResultHandle result=new ResultHandle();
		if(orderItemIdList==null ||orderItemIdList.size()<=0){
			result.setErrorCode("ORDER_ITEMID_LIST_NULL");
			result.setInfoMsg("orderItemList is null");
		}
		if(orderItemIdList!=null&&!orderItemIdList.isEmpty()){
			//查询未处理的活动
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("auditType", type.name());
			params.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			params.put("objectIds", orderItemIdList);
			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
			if(auditList != null && auditList.size() > 0){
				//记录已回传
				List<Long> auditIdlist=new ArrayList<>();
				for (ComAudit comAudit : auditList) {
					auditIdlist.add(comAudit.getAuditId());
				}
				orderAuditService.updateComAuditByAuditlist(auditIdlist);
			}
		}
		return result;
	}
}
