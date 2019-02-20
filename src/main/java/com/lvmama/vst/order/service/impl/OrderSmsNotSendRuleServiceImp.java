package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdSmsNotSendRule;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdSmsNotSendRuleDao;
import com.lvmama.vst.order.service.IOrderSmsNotSendRuleService;

@Service
public class OrderSmsNotSendRuleServiceImp implements IOrderSmsNotSendRuleService {

	private static final Logger LOG = LoggerFactory.getLogger(OrderSmsNotSendRuleServiceImp.class);
	@Autowired
	OrdSmsNotSendRuleDao ordSmsNotSendRuleDao;
	
	@Override
	public List<OrdSmsNotSendRule> findOrdSmsNotSendRuleList(
			Map<String, Object> params) throws BusinessException {
		List<OrdSmsNotSendRule> ordSmsNotSendRuleList = null;
		try {
			ordSmsNotSendRuleList = ordSmsNotSendRuleDao.findOrdSmsNotSendRuleList(params);
		} catch (Exception e) {
			LOG.error("method findOrdSmsNotSendRuleList error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordSmsNotSendRuleList;
	}

	@Override
	public OrdSmsNotSendRule findOrdSmsNotSendRuleById(Long templateId)
			throws BusinessException {
		OrdSmsNotSendRule ordSmsNotSendRule = null;
		try {
			ordSmsNotSendRule = ordSmsNotSendRuleDao.selectByPrimaryKey(templateId);
		} catch (Exception e) {
			LOG.error("method findOrdSmsNotSendRuleById error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordSmsNotSendRule;
	}

	@Override
	public int addOrdSmsNotSendRule(OrdSmsNotSendRule ordSmsNotSendRule)
			throws BusinessException {
		int result = 0;
		try {
			result = ordSmsNotSendRuleDao.insert(ordSmsNotSendRule);
		} catch (Exception e) {
			LOG.error("method addOrdSmsNotSendRule error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int findOrdSmsNotSendRuleCount(Map<String, Object> params)
			throws BusinessException {
		int result = 0 ;
		try {
			result = ordSmsNotSendRuleDao.getTotalCount(params);
		} catch (Exception e) {
			LOG.error("method findOrdSmsNotSendRuleCount error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int updateOrdSmsNotSendRule(OrdSmsNotSendRule ordSmsNotSendRule)
			throws BusinessException {
		int result = 0 ;
		try {
			result = ordSmsNotSendRuleDao.updateByPrimaryKeySelective(ordSmsNotSendRule);
		} catch (Exception e) {
			LOG.error("method updateOrdSmsNotSendRule error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int deleteOrdSmsNotSendRule(Long templateId) throws BusinessException {
		int result = 0 ;
		try {
			result = ordSmsNotSendRuleDao.deleteByPrimaryKey(templateId);
		} catch (Exception e) {
			LOG.error("method deleteOrdSmsNotSendRule error, ", e);
			LOG.error("{}", e);
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

}
