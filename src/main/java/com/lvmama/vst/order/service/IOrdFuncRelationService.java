package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdFuncRelation;
import com.lvmama.vst.comm.web.BusinessException;

public interface IOrdFuncRelationService {

	public List<OrdFuncRelation> findOrdFuncRelationList(Map<String, Object> params) throws BusinessException;

	public OrdFuncRelation findOrdFuncRelationById(Long id) throws BusinessException;

	public int insertOrdFuncRelation(OrdFuncRelation ordFuncRelation);

	public int updateOrdFuncRelation(OrdFuncRelation ordFuncRelation);
}
