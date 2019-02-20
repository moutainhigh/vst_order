package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdItemAdditionStatus;
import com.lvmama.vst.order.dao.OrdItemAdditionStatusDAO;
import com.lvmama.vst.order.service.IOrdItemAdditionStatusService;

@Service
public class OrdItemAdditionStatusServiceImpl implements IOrdItemAdditionStatusService {
	@Autowired
	private OrdItemAdditionStatusDAO ordItemAdditionStatusDAO;

	@Override
	public int addOrdItemAdditionStatus(OrdItemAdditionStatus ordItemAdditionStatus) {
		return ordItemAdditionStatusDAO.insert(ordItemAdditionStatus);
	}
	@Override
	public OrdItemAdditionStatus findOrdItemAdditionStatusById(Long id) {
		return ordItemAdditionStatusDAO.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdItemAdditionStatus> findOrdItemAdditionStatusList(Map<String, Object> params) {
		return ordItemAdditionStatusDAO.selectByParams(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdItemAdditionStatus ordItemAdditionStatus) {
		return ordItemAdditionStatusDAO.updateByPrimaryKeySelective(ordItemAdditionStatus);
	}
}
