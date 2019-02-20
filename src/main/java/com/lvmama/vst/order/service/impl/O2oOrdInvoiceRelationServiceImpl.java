package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.O2oOrdInvoiceRelation;
import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.order.dao.O2oOrdInvoiceRelationDao;
import com.lvmama.vst.order.dao.OrdInvoiceDao;
import com.lvmama.vst.order.service.IO2oOrdInvoiceRelationService;

/**
 * 
 * @ClassName: O2oOrdInvoiceRelationServiceImpl 
 * @Description: 门店订单发票实现
 * @author: LuoGuang
 * @date: 2016-11-16 下午3:38:40
 */
@Service("o2oOrdInvoiceRelationService")
public class O2oOrdInvoiceRelationServiceImpl implements IO2oOrdInvoiceRelationService {

	@Autowired
	private O2oOrdInvoiceRelationDao o2oOrdInvoiceRelationDao;
	@Autowired
	private OrdInvoiceDao ordInvoiceDao;
	
	
	@Override
	public Integer getInvoiceCount(Map<String, Object> paramsMap) {
		return o2oOrdInvoiceRelationDao.getInvoiceCount(paramsMap);
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String, Object> paramsMap) {
		return ordInvoiceDao.selectOrdInvoiceListByParam(paramsMap);
	}

	@Override
	public int insertO2oOrdInvoiceRelation(O2oOrdInvoiceRelation o2oOrdInvoiceRelation) {
		if (o2oOrdInvoiceRelation==null) {
			return 0;
		}
		
		return o2oOrdInvoiceRelationDao.insertSelective(o2oOrdInvoiceRelation);
	}

	@Override
	public O2oOrdInvoiceRelation selectByInvoiceId(Long invoiceId){
		return o2oOrdInvoiceRelationDao.selectByInvoiceId(invoiceId);
    }

}
