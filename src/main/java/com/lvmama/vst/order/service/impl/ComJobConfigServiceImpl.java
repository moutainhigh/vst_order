package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.po.ComJobConfig.JOB_TYPE;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.order.dao.ComJobConfigDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author sunjian
 *
 */
@Service
public class ComJobConfigServiceImpl implements ComJobConfigService {
	private static final Log LOG = LogFactory.getLog(ComJobConfigServiceImpl.class);
	
	@Autowired
	private ComJobConfigDAO comJobConfigDAO;

	@Override
	public List<ComJobConfig> selectList(JOB_TYPE type, Date date) {
		LOG.info("ComJobConfigServiceImpl.selectList: type=" + type + ",date=" + date);
		String jobType = null;
		if (type != null) {
			jobType = type.name();
		}
		return comJobConfigDAO.selectList(jobType, date);
	}

	@Override
	public void deleteComJobConfig(Long comJobConfigId) {
		LOG.info("ComJobConfigServiceImpl.deleteComJobConfig: comJobConfigId=" + comJobConfigId);
		comJobConfigDAO.deleteByPrimaryKey(comJobConfigId);
	}

	@Override
	public void saveComJobConfig(ComJobConfig record) {
		LOG.info("ComJobConfigServiceImpl.saveComJobConfig: ObjectId=" + record.getObjectId());
		comJobConfigDAO.insertSelective(record);
	}

	@Override
	public List<ComJobConfig> selectByParams(JOB_TYPE type, Date beginDate,
			Date endDate) {
		LOG.info("ComJobConfigServiceImpl.selectByParams: type=" + type + ",beginDate=" + beginDate + ", endDate" + endDate);
		String jobType = null;
		if (type != null) {
			jobType = type.name();
		}
		return comJobConfigDAO.selectByParams(jobType, beginDate, endDate);
	}

	@Override
	public void updateComJobConfig(ComJobConfig record) {
		LOG.info("ComJobConfigServiceImpl.updateComJobConfig: ComJobConfigId=" + record.getComJobConfigId() + ",ObjectId=" + record.getObjectId());
		comJobConfigDAO.updateByPrimaryKeySelective(record);
	}

	@Override
	public void deleteComJobConfigByCondition(ComJobConfig comJobConfig) {
		Assert.notNull(comJobConfig);
		LOG.info("ComJobConfigServiceImpl.deleteComJobConfigByCondition: objectId=" + comJobConfig.getObjectId() + ",objectType=" + comJobConfig.getObjectType() + ",jobType=" + comJobConfig.getJobType());
		comJobConfigDAO.deleteComJobConfigByCondition(comJobConfig);

	}
}
