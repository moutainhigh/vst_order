package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdStatusGroup;
import com.lvmama.vst.order.dao.OrdStatusGroupDao;
import com.lvmama.vst.order.service.IOrdStatusGroupService;

@Service
public class OrdStatusGroupServiceImpl implements IOrdStatusGroupService {

	private static final Log LOG = LogFactory.getLog(OrdStatusGroupServiceImpl.class);
	@Autowired
	private OrdStatusGroupDao ordStatusGroupDao;

	@Override
	public int findOrdStatusGroupCount(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordStatusGroupDao.getTotalCount(params);
	}

	@Override
	public OrdStatusGroup findOrdStatusGroupById(Long id) {
		// TODO Auto-generated method stub

		return ordStatusGroupDao.selectByPrimaryKey(id);
	}

	@Override
	public List<OrdStatusGroup> findOrdStatusGroupList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordStatusGroupDao.findOrdStatusGroupList(params);
	}

	@Override
	public int updateOrdStatusGroup(OrdStatusGroup ordStatusGroup) {
		// TODO Auto-generated method stub
		return ordStatusGroupDao.updateByPrimaryKey(ordStatusGroup);
	}

	@Override
	public int insertOrdStatusGroup(OrdStatusGroup ordStatusGroup) {
		// TODO Auto-generated method stub
		return ordStatusGroupDao.insert(ordStatusGroup);
	}

}
