package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdCourierListing;


/**
 * @author 张伟
 *
 */
public interface IOrdCourierListingService {

	
	public int addOrdCourierListing(OrdCourierListing OrdCourierListing);
	
	public OrdCourierListing findOrdCourierListingById(Long id);
	
	public List<OrdCourierListing> findOrdCourierListingList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdCourierListing OrdCourierListing);
	


	
}
