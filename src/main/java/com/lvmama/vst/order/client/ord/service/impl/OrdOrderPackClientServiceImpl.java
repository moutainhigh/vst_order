package com.lvmama.vst.order.client.ord.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrdOrderPackClientService;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.order.service.IOrdOrderPackService;

@Component("ordOrderPackServiceRemote")
public class OrdOrderPackClientServiceImpl implements OrdOrderPackClientService {
	@Autowired
	private IOrdOrderPackService ordOrderPackService;

	@Override
	public int addOrdOrderPack(OrdOrderPack ordOrderPack) {
		return ordOrderPackService.addOrdOrderPack(ordOrderPack);
	}

	@Override
	public OrdOrderPack findOrdOrderPackById(Long id) {
		return ordOrderPackService.findOrdOrderPackById(id);
	}

	@Override
	public List<OrdOrderPack> findOrdOrderPackList(Map<String, Object> params) {
		return ordOrderPackService.findOrdOrderPackList(params);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdOrderPack ordOrderPack) {
		return ordOrderPackService.updateByPrimaryKeySelective(ordOrderPack);
	}

	@Override
	public List<OrdOrderPack> findOrdOrderByOrderIds(Collection<Long> orderIds) {
		return ordOrderPackService.findOrdOrderByOrderIds(orderIds);
	}

}
