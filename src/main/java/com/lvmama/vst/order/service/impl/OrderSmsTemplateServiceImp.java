package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdSmsTemplateDao;
import com.lvmama.vst.order.service.IOrderSmsTemplateService;

@Service
public class OrderSmsTemplateServiceImp implements IOrderSmsTemplateService {

	private static final Logger LOG = LoggerFactory.getLogger(OrderSmsTemplateServiceImp.class);
	@Autowired
	OrdSmsTemplateDao ordSmsTemplateDao;
	
	@Override
	public List<OrdSmsTemplate> findOrdSmsTemplateList(
			Map<String, Object> params) throws BusinessException {
		List<OrdSmsTemplate> ordSmsTemplateList = null;
		try {
			ordSmsTemplateList = ordSmsTemplateDao.findOrdSmsTemplateList(params);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordSmsTemplateList;
	}

	@Override
	public OrdSmsTemplate findOrdSmsTemplateById(Long templateId)
			throws BusinessException {
		OrdSmsTemplate ordSmsTemplate = null;
		try {
			ordSmsTemplate = ordSmsTemplateDao.selectByPrimaryKey(templateId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordSmsTemplate;
	}

	@Override
	public int addOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate)
			throws BusinessException {
		int result = 0;
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (null != ordSmsTemplate.getTemplateName() && !"".equals(ordSmsTemplate.getTemplateName())) {
				params.put("templateName", ordSmsTemplate.getTemplateName());
			}
			if (null != ordSmsTemplate.getCategoryId()) {
				params.put("categoryId", ordSmsTemplate.getCategoryId());
			}
			if (null != ordSmsTemplate.getDistributorId()) {
				params.put("distributorId", ordSmsTemplate.getDistributorId());
			}
			
			if (null !=ordSmsTemplate.getSendNode() && !"".equals(ordSmsTemplate.getSendNode()) ) {
				params.put("sendNode", ordSmsTemplate.getSendNode());
			}
			if (null !=ordSmsTemplate.getOrderTime() && !"".equals(ordSmsTemplate.getOrderTime())) {
				params.put("orderTime", ordSmsTemplate.getOrderTime());
			}
			
			if (null != ordSmsTemplate.getSuplierId()) {
				params.put("suplierId", ordSmsTemplate.getSuplierId());
			}
			if (ordSmsTemplateDao.isNameExists(params)) {
				throw new BusinessException(ErrorCodeMsg.ERR_ORD_SMS_TEMPLATE_0001,ordSmsTemplate.getTemplateName());
			}
			result = ordSmsTemplateDao.insert(ordSmsTemplate);
		} 
		catch (BusinessException e) {
			throw e;
		}
		catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int findOrdSmsTemplateCount(Map<String, Object> params)
			throws BusinessException {
		int result = 0 ;
		try {
			result = ordSmsTemplateDao.getTotalCount(params);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int updateOrdSmsTemplate(OrdSmsTemplate ordSmsTemplate)
			throws BusinessException {
		int result = 0 ;
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (null != ordSmsTemplate.getTemplateId()) {
				params.put("templateId", ordSmsTemplate.getTemplateId());
			}
			if (null != ordSmsTemplate.getTemplateName() && !"".equals(ordSmsTemplate.getTemplateName())) {
				params.put("templateName", ordSmsTemplate.getTemplateName());
			}
			if (null != ordSmsTemplate.getCategoryId()) {
				params.put("categoryId", ordSmsTemplate.getCategoryId());
			}
			if (null != ordSmsTemplate.getDistributorId()) {
				params.put("distributorId", ordSmsTemplate.getDistributorId());
			}
			
			if (null !=ordSmsTemplate.getSendNode() && !"".equals(ordSmsTemplate.getSendNode()) ) {
				params.put("sendNode", ordSmsTemplate.getSendNode());
			}
			if (null !=ordSmsTemplate.getOrderTime() && !"".equals(ordSmsTemplate.getOrderTime())) {
				params.put("orderTime", ordSmsTemplate.getOrderTime());
			}
			
			if (null != ordSmsTemplate.getSuplierId()) {
				params.put("suplierId", ordSmsTemplate.getSuplierId());
			}

			result = ordSmsTemplateDao.updateByPrimaryKeySelective(ordSmsTemplate);
			
			}catch (BusinessException e) {
				throw e;
			}	
		 	catch (Exception e) {
		 		LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int deleteOrdSmsTemplate(Long templateId) throws BusinessException {
		int result = 0 ;
		try {
			result = ordSmsTemplateDao.deleteByPrimaryKey(templateId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

	@Override
	public int editFlag(Long templateId , String valid) throws BusinessException {
		
		int result = 0 ;
		try {
			OrdSmsTemplate ordSmsTemplate = new OrdSmsTemplate();
			ordSmsTemplate.setTemplateId(templateId);
			ordSmsTemplate.setValid(valid);
			result = ordSmsTemplateDao.updateByPrimaryKeySelective(ordSmsTemplate);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return result;
	}

}
