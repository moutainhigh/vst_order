package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.play.connects.po.BizOrderConnectsProp;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.BizOrderConnectsPropDao;
import com.lvmama.vst.order.service.BizOrderConnectsPropService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BizOrderConnectsPropServiceImpl implements BizOrderConnectsPropService {

	private final Logger logger = LoggerFactory.getLogger(BizOrderConnectsPropServiceImpl.class);
	private final String CONNECTS_SERVICE_KEY ="connect_service_";
	
	@Autowired
	private BizOrderConnectsPropDao bizOrderConnectsPropDao;

	@Override
	public BizOrderConnectsProp selectByPrimaryKey(Long propId) {
		BizOrderConnectsProp bizOrderConnectsProp = null;
		try {
			bizOrderConnectsProp = this.bizOrderConnectsPropDao.selectByPrimaryKey(propId);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return bizOrderConnectsProp;
	}

	
	
	@Override
	public List<BizOrderConnectsProp> selectAllByParams(Map<String, Object> params) {
		List<BizOrderConnectsProp> bizOrderConnectsPropList = null;
		try {
			bizOrderConnectsPropList = this.bizOrderConnectsPropDao.selectAllByParams(params);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return bizOrderConnectsPropList;
	}

	@Override
	public Long insert(BizOrderConnectsProp bizOrderConnectsProp) {
		Long propId = null;
		try {
			propId = this.bizOrderConnectsPropDao.insert(bizOrderConnectsProp);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
		return propId;
	}

	@Override
	public Long insertSelective(BizOrderConnectsProp bizOrderConnectsProp) {
		return this.bizOrderConnectsPropDao.insertSelective(bizOrderConnectsProp);
	}

	@Override
	public void updateByPrimaryKeySelective(BizOrderConnectsProp bizOrderConnectsProp) {
		try {
			this.bizOrderConnectsPropDao.updateByPrimaryKeySelective(bizOrderConnectsProp);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
	}

	@Override
	public void updateByPrimaryKey(BizOrderConnectsProp bizOrderConnectsProp) {
		try {
			this.bizOrderConnectsPropDao.updateByPrimaryKey(bizOrderConnectsProp);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
	}

	@Override
	public void deleteByPrimaryKey(long propId) {
		try {
			this.bizOrderConnectsPropDao.deleteByPrimaryKey(propId);
		}catch (Exception e){
			logger.error(ExceptionFormatUtil.getTrace(e));
			throw new BusinessException(ErrorCodeMsg.ERR_SYS);
		}
	}



	@SuppressWarnings("unchecked")
	@Override
	public List<BizOrderConnectsProp> selectMemByBranchId(Long branchId) {
		Object obj = MemcachedUtil.getInstance().get(CONNECTS_SERVICE_KEY+branchId);
		if(obj != null) {
			return (List<BizOrderConnectsProp>)obj;
		}
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("branchId", branchId);
		params.put("orderby", "SEQ");
		List<BizOrderConnectsProp> bizOrderConnectsPropList = this.selectAllByParams(params);
		if(bizOrderConnectsPropList != null) {
			MemcachedUtil.getInstance().set(CONNECTS_SERVICE_KEY+branchId, MemcachedEnum.ProdProductSingle.getSec(), bizOrderConnectsPropList);
		}
		return bizOrderConnectsPropList;
	}
}