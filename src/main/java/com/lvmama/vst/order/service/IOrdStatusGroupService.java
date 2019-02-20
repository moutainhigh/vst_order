package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdStatusGroup;

public interface IOrdStatusGroupService {

	public int insertOrdStatusGroup(OrdStatusGroup ordStatusGroup);

	public List<OrdStatusGroup> findOrdStatusGroupList(Map<String, Object> params);

	public OrdStatusGroup findOrdStatusGroupById(Long id);

	public int updateOrdStatusGroup(OrdStatusGroup ordStatusGroup);

	public int findOrdStatusGroupCount(Map<String, Object> params);
}
