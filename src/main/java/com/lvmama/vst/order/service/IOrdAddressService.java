package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdPerson;


/**
 * @author 张伟
 *
 */
public interface IOrdAddressService {

	
	public int addOrdAddress(OrdAddress ordAddress);
	
	public OrdAddress findOrdAddressById(Long id);
	
	public List<OrdAddress> findOrdAddressList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdAddress ordAddress);
	

	public void updateOrdAddressAndPerson(OrdPerson ordPerson,
			OrdAddress ordAddress, Long orderId,String loginUserId);

	
}
