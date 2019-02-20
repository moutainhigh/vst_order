package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;
import com.lvmama.vst.comm.web.BusinessException;

public interface IOrderSmsNotSendRuleService {

	public List<OrdSmsNotSendRule> findOrdSmsNotSendRuleList(Map<String, Object> params) throws BusinessException;

	public OrdSmsNotSendRule findOrdSmsNotSendRuleById(Long templateId) throws BusinessException;

	public int addOrdSmsNotSendRule(OrdSmsNotSendRule ordSmsNotSendRule) throws BusinessException;

	public int findOrdSmsNotSendRuleCount(Map<String, Object> params) throws BusinessException;

	public int updateOrdSmsNotSendRule(OrdSmsNotSendRule ordSmsNotSendRule) throws BusinessException;
	
	public int deleteOrdSmsNotSendRule(Long templateId) throws BusinessException;
}
