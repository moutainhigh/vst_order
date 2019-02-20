package com.lvmama.vst.order.client.ord.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderDistributionBusinessClientService;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.IOrderDistributionBusiness;
@Component("orderDistributionBusinessServiceRemote")
public class OrderDistributionBusinessClientServiceImpl implements OrderDistributionBusinessClientService {

	@Autowired
	private IOrderDistributionBusiness distributionBusiness;
	
	@Override
	public ComAudit makeOrderAudit(ComAudit audit) {
		return distributionBusiness.makeOrderAudit(audit);
	}

}
