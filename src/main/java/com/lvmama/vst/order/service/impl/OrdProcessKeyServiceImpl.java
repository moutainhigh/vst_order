package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.order.dao.OrdProcessKeyDao;
import com.lvmama.vst.order.service.IOrdProcessKeyService;

@Service("ordProcessKeyService")
public class OrdProcessKeyServiceImpl implements IOrdProcessKeyService {

	@Autowired
	private OrdProcessKeyDao ordProcessKeyDao;
	
	@Override
	public OrdProcessKey selectByPrimaryKey(Long ordProcessKeyId) {
		return ordProcessKeyDao.selectByPrimaryKey(ordProcessKeyId);
	}

	@Override
	public List<OrdProcessKey> query(OrdProcessKey ordProcessKey) {
		return ordProcessKeyDao.query(ordProcessKey);
	}

	@Override
	public List<OrdProcessKey> selectOrdProcessKeyList(Map<String, Object> params) {
		return ordProcessKeyDao.selectOrdProcessKeyList(params);
	}

	@Override
	public Integer insert(OrdProcessKey ordProcessKey) {
		return ordProcessKeyDao.insert(ordProcessKey);
	}

	@Override
	public Integer update(OrdProcessKey ordProcessKey) {
		return ordProcessKeyDao.update(ordProcessKey);
	}

	@Override
	public Integer updateStatus(Map<String, Object> params) {
		return ordProcessKeyDao.updateStatus(params);
	}

	@Override
	public int deleteOrdProcessKey(Map<String, Object> params) {
		return ordProcessKeyDao.deleteOrdProcessKey(params);
	}

	@Override
	public int deleteByPrimaryKey(Long ordProcessKeyId) {
		return ordProcessKeyDao.deleteByPrimaryKey(ordProcessKeyId);
	}

}
