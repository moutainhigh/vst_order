package com.lvmama.vst.order.processer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.OrderItemConfirmStatusClientService;

public class UpdateOrderAuditStatusProcesser implements MessageProcesser {
	@Autowired
	OrderItemConfirmStatusClientService orderItemConfirmStatusClientService;
	@Override
	public void process(Message message) {
		if(MessageUtils.isOrderAuditPassByIsreturnBackMsg(message)){
			if(!message.getAddition().isEmpty()){
				String[] itemlistStr=message.getAddition().split(",");
				List<Long> orderItemList=new ArrayList<>();
				for (String str : itemlistStr) {
					orderItemList.add(Long.parseLong(str));
				}
				orderItemConfirmStatusClientService.updateOrderItemStatusByOrderItemIdList(orderItemList, Confirm_Enum.CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT);
			}
			
		}

	}

}
