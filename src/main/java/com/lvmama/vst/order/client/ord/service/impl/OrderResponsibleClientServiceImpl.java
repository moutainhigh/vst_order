package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.client.ord.service.OrderResponsibleClientService;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.order.service.IOrderResponsibleService;

@Component("orderResponsibleServiceRemote")
public class OrderResponsibleClientServiceImpl implements OrderResponsibleClientService {
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	@Override
	public List<OrdResponsible> selectWaitObjectList(Map<String, Object> params) {
		return orderResponsibleService.selectWaitObjectList(params);
	}

	@Override
	public List<Map> queryResponsibleListByCondition(Map<String, Object> params) {
		return orderResponsibleService.queryResponsibleListByCondition(params);
	}

	@Override
	public Integer selectResponsibleCount(Map<String, Object> param) {
		return orderResponsibleService.selectResponsibleCount(param);
	}

	@Override
	public List<OrdResponsible> findOrdResponsibleList(Map<String, Object> params) {
		return orderResponsibleService.findOrdResponsibleList(params);
	}
	@Override
	public PermUser getOrderPrincipal(String objectType, Long objectId) {
		return orderResponsibleService.getOrderPrincipal(objectType, objectId);
	}
	@Override
	public Integer getTotalCount(Map<String, Object> param) {
		return orderResponsibleService.getTotalCount(param);
	}

	@Override
	public PermUser getResourceApprover(Long objectId, String objectType) {
		return orderResponsibleService.getResourceApprover(objectId, objectType);
	}
}
