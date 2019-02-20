package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.web.BusinessException;

public interface IOrderSmsTemplateService {

	public List<OrdSmsTemplate> findOrdSmsTemplateList(Map<String, Object> params) throws BusinessException;

	public OrdSmsTemplate findOrdSmsTemplateById(Long templateId) throws BusinessException;

	public int addOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate) throws BusinessException;

	public int findOrdSmsTemplateCount(Map<String, Object> params) throws BusinessException;

	public int updateOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate) throws BusinessException;
	
	public int deleteOrdSmsTemplate(Long templateId) throws BusinessException;
	
	public int editFlag(Long templateId , String valid) throws BusinessException;

}
