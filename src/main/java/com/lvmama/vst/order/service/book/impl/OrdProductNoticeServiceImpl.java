package com.lvmama.vst.order.service.book.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderNotice;
import com.lvmama.vst.order.dao.OrdNoticeDao;
import com.lvmama.vst.order.service.IOrdProductNotice;

@Service
public class OrdProductNoticeServiceImpl implements IOrdProductNotice{

	@Autowired
	private OrdNoticeDao ordNoticeDao;  
	
	@Override
	public int insert(OrdOrderNotice record) {
		return ordNoticeDao.insert(record);
	}

	@Override
	public List<OrdOrderNotice> findOrdNoticeList(Map<String, Object> params) {
		return ordNoticeDao.findOrdNoticeList(params);
	}
	
	@Override
	public List<OrdOrderNotice> findOrdNoticeList_notice(Map<String, Object> params) {
		return ordNoticeDao.findOrdNoticeList_notice(params);
	}

	
}
