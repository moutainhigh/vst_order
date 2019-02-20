package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdInvoiceRelation;
import com.lvmama.vst.order.dao.OrdInvoiceRelationDao;
import com.lvmama.vst.order.service.IOrderInvoiceRelationService;

@Service
public class OrderInvoiceRelationService implements
		IOrderInvoiceRelationService {

	@Autowired
	OrdInvoiceRelationDao ordInvoiceRelationDao;
	
	@Override
	public int deleteByPrimaryKey(Long invoiceRelationId) {
		return ordInvoiceRelationDao.deleteByPrimaryKey(invoiceRelationId);
	}

	@Override
	public int insert(OrdInvoiceRelation ordInvoiceRelation) {
		return ordInvoiceRelationDao.insert(ordInvoiceRelation);
	}

	@Override
	public int insertSelective(OrdInvoiceRelation ordInvoiceRelation) {
		return ordInvoiceRelationDao.insertSelective(ordInvoiceRelation);
	}

	@Override
	public OrdInvoiceRelation selectByPrimaryKey(Long invoiceRelationId) {
		return ordInvoiceRelationDao.selectByPrimaryKey(invoiceRelationId);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdInvoiceRelation ordInvoiceRelation) {
		return ordInvoiceRelationDao.updateByPrimaryKeySelective(ordInvoiceRelation);
	}

	@Override
	public int updateByPrimaryKey(OrdInvoiceRelation ordInvoiceRelation) {
		return ordInvoiceRelationDao.updateByPrimaryKey(ordInvoiceRelation);
	}

	@Override
	public List<OrdInvoiceRelation> getListByParam(Map<String, Object> map) {
		return ordInvoiceRelationDao.getListByParam(map);
	}

}
