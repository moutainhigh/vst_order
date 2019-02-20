package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdItemAdditionStatus;


/**
 * @author 张伟
 *
 */
public interface IOrdItemAdditionStatusService {

	
	public int addOrdItemAdditionStatus(OrdItemAdditionStatus ordAdditionStatus);
	
	public OrdItemAdditionStatus findOrdItemAdditionStatusById(Long id);
	
	public List<OrdItemAdditionStatus> findOrdItemAdditionStatusList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdItemAdditionStatus ordAdditionStatus);
}
