package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundRst;
import com.lvmama.vst.order.dao.OrdExpiredRefundDao;
import com.lvmama.vst.order.service.OrdExpiredRefundService;

/**
 * 过期退意向单Service
 */
@Service
public class OrdExpiredRefundServiceImpl implements OrdExpiredRefundService {
	
	private static final Log LOG = LogFactory.getLog(OrdExpiredRefundServiceImpl.class);
	
	@Autowired
	private OrdExpiredRefundDao ordExpiredRefundDao;

	/**
	 * 插入过期退意向单数据
	 * 
	 * @param ordExpiredRefund OrdExpiredRefund
	 * @return int
	 */
	@Override
	public int insert(OrdExpiredRefund ordExpiredRefund) {
		if (ordExpiredRefund == null) {
			return 0;
		}
		
		try {
			return ordExpiredRefundDao.insert(ordExpiredRefund);
		} catch (Exception e) {
			LOG.error("OrdExpiredRefundService insert is error!", e);
			return -1;
		}
	}

	/**
	 * 批量插入过期退意向单数据
	 * 
	 * @param list List<OrdExpiredRefund>
	 * @return int
	 */
	@Override
	public int batchInsert(List<OrdExpiredRefund> list) {
		if (CollectionUtils.isEmpty(list)) {
			return 0;
		}
		
		try {
			return ordExpiredRefundDao.batchInsert(list);
		} catch (Exception e) {
			LOG.error("OrdExpiredRefundService batchInsert is error!", e);
			return -1;
		}
	}

	/**
	 * 分页查询过期退数据列表
	 * 
	 * @param params Map<String, Object>
	 * @return List<OrdExpiredRefundRst>
	 */
	@Override
	@ReadOnlyDataSource
	public List<OrdExpiredRefundRst> queryListForPage(Map<String, Object> params) {
		return ordExpiredRefundDao.queryListForPage(params);
	}

	/**
	 * 分页查询过期退列表COUNT
	 * 
	 * @param params Map<String, Object>
	 * @return Long
	 */
	@Override
	@ReadOnlyDataSource
	public Long queryTotalCountForPage(Map<String, Object> params) {
		return ordExpiredRefundDao.queryTotalCountForPage(params);
	}

}