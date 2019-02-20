package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdFuncRelation;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdFuncRelationDao;
import com.lvmama.vst.order.service.IOrdFuncRelationService;

@Service
public class OrdFuncRelationServiceImpl implements IOrdFuncRelationService {

	private static final Log LOG = LogFactory.getLog(OrdFunctionServiceImpl.class);
	@Autowired
	private OrdFuncRelationDao ordFuncRelationDao;

	@Override
	public List<OrdFuncRelation> findOrdFuncRelationList(Map<String, Object> params) throws BusinessException {
		// TODO Auto-generated method stub

		return ordFuncRelationDao.findOrdFuncRelationList(params);
	}

	@Override
	public OrdFuncRelation findOrdFuncRelationById(Long id) throws BusinessException {
		// TODO Auto-generated method stub
		return ordFuncRelationDao.selectByPrimaryKey(id);
	}

	@Override
	public int insertOrdFuncRelation(OrdFuncRelation ordFuncRelation) {
		// TODO Auto-generated method stub
		return ordFuncRelationDao.insert(ordFuncRelation);
	}

	@Override
	public int updateOrdFuncRelation(OrdFuncRelation ordFuncRelation) {
		// TODO Auto-generated method stub
		return ordFuncRelationDao.updateByPrimaryKey(ordFuncRelation);
	}

}
