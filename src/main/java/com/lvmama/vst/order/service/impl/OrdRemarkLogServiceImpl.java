package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdRemarkLog;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdRemarkLogDao;
import com.lvmama.vst.order.service.IOrdRemarkLogService;

@Service
public class OrdRemarkLogServiceImpl implements IOrdRemarkLogService {
	
	private static final Log LOG = LogFactory.getLog(OrdRemarkLogServiceImpl.class);
	
	@Autowired
	OrdRemarkLogDao ordRemarkLogDao ;
	
	@Override
	public int findOrdRemarkLogCount(Map<String, Object> params) throws BusinessException {
		int count = 0;
		try{
			count = ordRemarkLogDao.getTotalCount(params);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public List<OrdRemarkLog> findOrdRemarkLogList(Map<String, Object> params) throws BusinessException {
		List<OrdRemarkLog> ordRemarkLogs = null;
		try{
			ordRemarkLogs = ordRemarkLogDao.findOrdRemarkLogList(params);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordRemarkLogs;	
	}

	@Override
	public OrdRemarkLog findOrdRemarkLogById(Long logId) throws BusinessException {
		OrdRemarkLog ordRemarkLog = null;
		try{
			ordRemarkLog = ordRemarkLogDao.selectByPrimaryKey(logId);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return ordRemarkLog;
	}

	@Override
	public int addOrdRemarkLog(OrdRemarkLog ordRemarkLog) throws BusinessException {
		int count = 0;
		try{
			count = ordRemarkLogDao.insert(ordRemarkLog);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public int updateOrdRemarkLog(OrdRemarkLog ordRemarkLog) throws BusinessException {
		int count = 0;
		try{
			count = ordRemarkLogDao.updateByPrimaryKey(ordRemarkLog);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public int updateOrdRemarkLogStatus(OrdRemarkLog ordRemarkLog) throws BusinessException {
		int count = 0;
		try{
			count = ordRemarkLogDao.updateByPrimaryKeySelective(ordRemarkLog);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

	@Override
	public int deleteOrdRemarkLog(Long logId) throws BusinessException {
		int count = 0;
		try{
			count = ordRemarkLogDao.deleteByPrimaryKey(logId);
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return count;
	}

}
