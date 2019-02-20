package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.service.IOrdOrderAmountItemService;

@Service("ordOrderAmountItemService")
public class OrdOrderAmountItemServiceImpl implements IOrdOrderAmountItemService {

	@Autowired
	private OrdOrderAmountItemDao orderAmountItemDao;
	
	@Override
	public List<OrdOrderAmountItem> findOrderAmountItemList(
			Map<String, Object> params) {
		return orderAmountItemDao.findOrderAmountItemList(params);
	}

	@Override
	public int insertOrderAmountItem(OrdOrderAmountItem orderAmountItem) {
		return orderAmountItemDao.insert(orderAmountItem);
	}
}
