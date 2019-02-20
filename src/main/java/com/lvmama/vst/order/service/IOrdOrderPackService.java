package com.lvmama.vst.order.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrderPack;


/**
 * @author 张伟
 *
 */
public interface IOrdOrderPackService {

	
	public int addOrdOrderPack(OrdOrderPack ordOrderPack);
	
	public OrdOrderPack findOrdOrderPackById(Long id);
	
	public List<OrdOrderPack> findOrdOrderPackList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdOrderPack ordOrderPack);

	public List<OrdOrderPack> findOrdOrderByOrderIds(Collection<Long> orderIds);


	
}
