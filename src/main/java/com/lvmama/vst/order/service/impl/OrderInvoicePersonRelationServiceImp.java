package com.lvmama.vst.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdInvoicePersonRelation;
import com.lvmama.vst.order.dao.OrdInvoicePersonRelationDao;
import com.lvmama.vst.order.service.IOrderInvoicePersonRelationService;

@Service
public class OrderInvoicePersonRelationServiceImp implements
		IOrderInvoicePersonRelationService {

	@Autowired
	private OrdInvoicePersonRelationDao ordInvoicePersonRelationDao;
	
	@Override
	public int deleteByPrimaryKey(Long invoicePersonRelationId) {
		return ordInvoicePersonRelationDao.deleteByPrimaryKey(invoicePersonRelationId);
	}

	@Override
	public int insert(OrdInvoicePersonRelation record) {
		return ordInvoicePersonRelationDao.insert(record);
	}

	@Override
	public int insertSelective(OrdInvoicePersonRelation record) {
		return ordInvoicePersonRelationDao.insertSelective(record);
	}

	@Override
	public OrdInvoicePersonRelation selectByPrimaryKey(
			Long invoicePersonRelationId) {
		return ordInvoicePersonRelationDao.selectByPrimaryKey(invoicePersonRelationId);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdInvoicePersonRelation record) {
		return ordInvoicePersonRelationDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(OrdInvoicePersonRelation record) {
		return 0;
	}

}
