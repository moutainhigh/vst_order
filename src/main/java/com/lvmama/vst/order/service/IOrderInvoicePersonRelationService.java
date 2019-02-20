package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdInvoicePersonRelation;

public interface IOrderInvoicePersonRelationService {

    int deleteByPrimaryKey(Long invoicePersonRelationId);

    int insert(OrdInvoicePersonRelation record);

    int insertSelective(OrdInvoicePersonRelation record);

    OrdInvoicePersonRelation selectByPrimaryKey(Long invoicePersonRelationId);

    int updateByPrimaryKeySelective(OrdInvoicePersonRelation record);

    int updateByPrimaryKey(OrdInvoicePersonRelation record);
}
