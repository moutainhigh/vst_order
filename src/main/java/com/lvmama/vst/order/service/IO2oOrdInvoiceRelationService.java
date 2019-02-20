package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.O2oOrdInvoiceRelation;
import com.lvmama.vst.back.order.po.OrdInvoice;

/**
 * 
 * @ClassName: IO2oOrdInvoiceRelationService 
 * @Description: 门店订单发票service
 * @author: LuoGuang
 * @date: 2016-11-16 下午3:37:00
 */
public interface IO2oOrdInvoiceRelationService {

	/**
	 * 获取发票总条数
	 * @param paramsMap
	 * @return
	 */
	public Integer getInvoiceCount(Map<String, Object> paramsMap);
	/**
	 * 根据条件查询发票列表
	 * @param paramsMap
	 * @return
	 */
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String, Object> paramsMap);
	
	/****
	 * 插入门店与发票关系
	 * @param o2oOrdInvoiceRelation
	 * @return
	 */
	public int insertO2oOrdInvoiceRelation(O2oOrdInvoiceRelation o2oOrdInvoiceRelation);
	
	/**
	 * 
	 * @Title: selectByInvoiceId 
	 * @Description: 根据发票id查询门店发票关系
	 * @param invoiceId
	 * @return
	 * @return: O2oOrdInvoiceRelation
	 */
	public O2oOrdInvoiceRelation selectByInvoiceId(Long invoiceId);
}
