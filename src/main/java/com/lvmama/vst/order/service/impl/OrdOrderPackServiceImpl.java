package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.service.IOrdOrderPackService;

@Service
public class OrdOrderPackServiceImpl implements IOrdOrderPackService {

	private static final Log LOG = LogFactory
			.getLog(OrdOrderPackServiceImpl.class);
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	@Override
	public int addOrdOrderPack(OrdOrderPack ordOrderPack) {
		// TODO Auto-generated method stub
		return ordOrderPackDao.insert(ordOrderPack);
	}
	@Override
	public OrdOrderPack findOrdOrderPackById(Long id) {
		// TODO Auto-generated method stub
		return ordOrderPackDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdOrderPack> findOrdOrderPackList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordOrderPackDao.selectByParams(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdOrderPack ordOrderPack) {
		// TODO Auto-generated method stub
		return ordOrderPackDao.updateByPrimaryKeySelective(ordOrderPack);
	}
	@Override
	public List<OrdOrderPack> findOrdOrderByOrderIds(Collection<Long> orderIds) {
		// TODO Auto-generated method stub
		if(CollectionUtils.isEmpty(orderIds)){
			return Collections.emptyList();
		}
		List<Long> ids=new ArrayList<Long>();
		ids.addAll(orderIds);
		return ordOrderPackDao.selectOrdOrderByOrderIds(ids);
	}
	

	

}
