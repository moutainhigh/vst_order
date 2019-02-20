package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.order.dao.OrdCourierListingDao;
import com.lvmama.vst.order.service.IOrdCourierListingService;

@Service
public class OrdCourierListingServiceImpl implements IOrdCourierListingService {

	private static final Log LOG = LogFactory
			.getLog(OrdCourierListingServiceImpl.class);
	@Autowired
	private OrdCourierListingDao OrdCourierListingDao;
	@Override
	public int addOrdCourierListing(OrdCourierListing OrdCourierListing) {
		// TODO Auto-generated method stub
		return OrdCourierListingDao.insert(OrdCourierListing);
	}
	@Override
	public OrdCourierListing findOrdCourierListingById(Long id) {
		// TODO Auto-generated method stub
		return OrdCourierListingDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdCourierListing> findOrdCourierListingList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return OrdCourierListingDao.findOrdCourierListingList(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdCourierListing OrdCourierListing) {
		// TODO Auto-generated method stub
		return OrdCourierListingDao.updateByPrimaryKeySelective(OrdCourierListing);
	}

	
	
	
	
}
