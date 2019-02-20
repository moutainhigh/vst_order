package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdWechatTemplate;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdWechatTemplateDao;
import com.lvmama.vst.order.service.IOrdWechatTemplateService;

@Service
public class OrdWechatTemplateServiceImpl implements IOrdWechatTemplateService {
	
	private static final Log LOG = LogFactory.getLog(OrdWechatTemplateServiceImpl.class);
	
	@Autowired
	OrdWechatTemplateDao ordWechatTemplateDao;
	
	@Override
	public int findOrdWechatTemplateCount(Map<String, Object> params)
			throws BusinessException {
		int count = 0;
		try{
			count = ordWechatTemplateDao.getTotalCount(params);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public List<OrdWechatTemplate> findOrdWechatTemplateList(
			Map<String, Object> params) throws BusinessException {
		List<OrdWechatTemplate> ordWechatTemplates = null;
		try{
			ordWechatTemplates = ordWechatTemplateDao.findOrdWechatTemplateList(params);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordWechatTemplates;		
	}

	@Override
	public OrdWechatTemplate findOrdWechatTemplateById(Long id)
			throws BusinessException {
		OrdWechatTemplate ordWechatTemplate = null;
		try{
			ordWechatTemplate = ordWechatTemplateDao.selectByPrimaryKey(id);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordWechatTemplate;	
	}

	@Override
	public int addOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate)
			throws BusinessException {
		int count = 0;
		try{
			count = ordWechatTemplateDao.insert(ordWechatTemplate);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;			
	}

	@Override
	public int updateOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate)
			throws BusinessException {
		int count = 0;
		try{
			count = ordWechatTemplateDao.updateByPrimaryKey(ordWechatTemplate);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public int updateOrdWechatTemplateStatus(OrdWechatTemplate ordWechatTemplate)
			throws BusinessException {
		int count = 0;
		try{
			count = ordWechatTemplateDao.updateByPrimaryKeySelective(ordWechatTemplate);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public int deleteOrdWechatTemplate(Long id)
			throws BusinessException {
		int count = 0;
		try{
			count = ordWechatTemplateDao.deleteByPrimaryKey(id);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

}
