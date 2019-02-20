package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.order.dao.OrdItemContractRelationDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;

@Service
public class OrdItemContractRelationImpl implements IOrdItemContractRelationService{
	
	private static final Log LOG = LogFactory.getLog(OrdItemContractRelationImpl.class);
	
	@Autowired
	private OrdItemContractRelationDao ordItemContractRelationDao;

	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	
	
	@Override
	public int insert(OrdItemContractRelation ordItemContractRelation) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.insert(ordItemContractRelation);
	}

	@Override
	public OrdItemContractRelation selectByPrimaryKey(
			Long id) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(
			OrdItemContractRelation ordItemContractRelation) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.updateByPrimaryKeySelective(ordItemContractRelation);
	}

	@Override
	public int updateByPrimaryKey(
			OrdItemContractRelation ordItemContractRelation) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.updateByPrimaryKey(ordItemContractRelation);
	}

	@Override
	public List<OrdItemContractRelation> findOrdItemContractRelationList(
			HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.findOrdItemContractRelationList(params);
	}

	@Override
	public Integer findOrdItemContractRelationCounts(
			HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return ordItemContractRelationDao.findOrdItemContractRelationCounts(params);
	}
	
	
	
}
