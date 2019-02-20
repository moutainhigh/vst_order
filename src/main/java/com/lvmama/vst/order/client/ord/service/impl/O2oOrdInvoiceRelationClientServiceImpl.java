package com.lvmama.vst.order.client.ord.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.O2oOrdInvoiceRelationClientService;
import com.lvmama.vst.back.order.po.O2oOrdInvoiceRelation;
import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.order.service.IO2oOrdInvoiceRelationService;

/**
 * 
 * @ClassName: O2oOrdInvoiceRelationServiceImpl 
 * @Description: 门店订单发票实现
 * @author: LuoGuang
 * @date: 2016-11-16 下午3:38:40
 */
@Service("o2oOrdInvoiceRelationServiceRemote")
public class O2oOrdInvoiceRelationClientServiceImpl implements O2oOrdInvoiceRelationClientService {

	@Autowired
	private IO2oOrdInvoiceRelationService	o2oOrdInvoiceRelationService;
	
	@Override
	public Integer getInvoiceCount(Map<String, Object> paramsMap) {
		
		return o2oOrdInvoiceRelationService.getInvoiceCount(paramsMap);
	}
	
	@Override
	public int insertO2oOrdInvoiceRelation(O2oOrdInvoiceRelation o2oOrdInvoiceRelation) {
		
		return o2oOrdInvoiceRelationService.insertO2oOrdInvoiceRelation(o2oOrdInvoiceRelation);
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String, Object> paramsMap) {
		
		return o2oOrdInvoiceRelationService.getOrdInvoiceListByParam(paramsMap);
	}
	
	@Override
	public O2oOrdInvoiceRelation selectByInvoiceId(Long invoiceId){
		return o2oOrdInvoiceRelationService.selectByInvoiceId(invoiceId);
    }

}
