package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdFunction;
import com.lvmama.vst.order.dao.OrdFunctionDao;
import com.lvmama.vst.order.service.IOrdFunctionService;

@Service
public class OrdFunctionServiceImpl implements IOrdFunctionService {

	private static final Log LOG = LogFactory.getLog(OrdFunctionServiceImpl.class);
	@Autowired
	private OrdFunctionDao ordFuncDao;

	@Override
	public int findOrdFunctionCount(Map<String, Object> params) {
		// TODO Auto-generated method stub

		return ordFuncDao.getTotalCount(params);
	}

	@Override
	public List<OrdFunction> findOrdFunctionList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		List<OrdFunction> ordFunctionList = null;
		ordFunctionList = ordFuncDao.findOrdFunctionList(params);

		return ordFunctionList;
	}

	@Override
	public int insertOrdFunction(OrdFunction ordFunction) {
		// TODO Auto-generated method stub
		return ordFuncDao.insert(ordFunction);
	}

	@Override
	public OrdFunction findOrdFunctionById(Long id) {
		// TODO Auto-generated method stub
		return ordFuncDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateOrdFunction(OrdFunction ordFunction) {
		// TODO Auto-generated method stub
		return ordFuncDao.updateByPrimaryKeySelective(ordFunction);
	}
	
	
	
	
	
	
	
	

}
