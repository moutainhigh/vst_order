package com.lvmama.vst.order.service;

import java.util.HashMap;
import java.util.List;

import com.lvmama.vst.back.order.po.OrdItemContractRelation;


/**
 * 
 * 订单结算价修改业务
 * 
 * @author YungHua.Ma
 *
 */
public interface IOrdItemContractRelationService {
	
	public int insert(OrdItemContractRelation ordItemContractRelation);
	
	public OrdItemContractRelation selectByPrimaryKey(Long id);
	
	public int updateByPrimaryKeySelective(OrdItemContractRelation ordItemContractRelation);
	
	public int updateByPrimaryKey(OrdItemContractRelation ordItemContractRelation);
	
	public List<OrdItemContractRelation> findOrdItemContractRelationList(HashMap<String,Object> params);
	
	public Integer findOrdItemContractRelationCounts(HashMap<String,Object> params);
	
	
}
