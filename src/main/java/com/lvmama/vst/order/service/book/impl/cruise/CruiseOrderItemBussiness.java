package com.lvmama.vst.order.service.book.impl.cruise;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

@Component("cruiseOrderItemBussiness")
public class CruiseOrderItemBussiness implements OrderInitBussiness{

	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		// TODO Auto-generated method stub
		return false;
	}

}
