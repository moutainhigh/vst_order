package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdInvoiceRelation;

public interface IOrderInvoiceRelationService {
	
	public int deleteByPrimaryKey(Long invoiceRelationId);

	public int insert(OrdInvoiceRelation ordInvoiceRelation);
	
	public int insertSelective(OrdInvoiceRelation ordInvoiceRelation);

	public OrdInvoiceRelation selectByPrimaryKey(Long invoiceRelationId);

	public int updateByPrimaryKeySelective(OrdInvoiceRelation ordInvoiceRelation);

	public int updateByPrimaryKey(OrdInvoiceRelation ordInvoiceRelation);
	
	public List<OrdInvoiceRelation> getListByParam(Map<String,Object> map);
}
