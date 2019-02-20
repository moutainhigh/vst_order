package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrdItemAddition;
import com.lvmama.vst.order.dao.OrdItemAdditionDAO;
import com.lvmama.vst.order.service.IOrdItemAdditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrdItemAdditionServiceImpl implements IOrdItemAdditionService {
	@Autowired
	private OrdItemAdditionDAO ordItemAdditionDAO;

	@Override
	public int addOrdItemAddition(OrdItemAddition ordItemAddition) {
		return ordItemAdditionDAO.insert(ordItemAddition);
	}
	@Override
	public OrdItemAddition findOrdItemAdditionById(Long id) {
		return ordItemAdditionDAO.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdItemAddition> findOrdItemAdditionList(Map<String, Object> params) {
		return ordItemAdditionDAO.selectByParams(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdItemAddition ordItemAddition) {
		return ordItemAdditionDAO.updateByPrimaryKeySelective(ordItemAddition);
	}
}
