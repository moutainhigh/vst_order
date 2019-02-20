package com.lvmama.vst.order.client.ord.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdOrderItemExtendService;
import com.lvmama.vst.back.order.po.OrdOrderItemExtend;
import com.lvmama.vst.order.dao.OrdOrderItemExtendDao;

@Component("ordOrderItemExtendServiceRemote")
public class OrdOrderItemExtendServiceImpl implements OrdOrderItemExtendService {

	@Autowired
	private OrdOrderItemExtendDao ordOrderItemExtendDao;
	
	@Override
	public OrdOrderItemExtend selectByOrderItemId(Long orderItemId) {
		return ordOrderItemExtendDao.selectByPrimaryKey(orderItemId);
	}

	@Override
	public int updateForeignTotalSettlementPrice(OrdOrderItemExtend ordOrderItemExtend) {
		return ordOrderItemExtendDao.updateByPrimaryKeySelective(ordOrderItemExtend);
	}

}
