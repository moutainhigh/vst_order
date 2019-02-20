package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;

/**
 * @author 张伟
 *
 */
public interface IOrdItemPersonRelationService {

	
	public int addOrdItemPersonRelation(OrdItemPersonRelation ordItemPersonRelation);
	
	public OrdItemPersonRelation findOrdItemPersonRelationById(Long id);
	
	public List<OrdItemPersonRelation> findOrdItemPersonRelationList(Map<String, Object> params);

	public int updateByPrimaryKeySelective(OrdItemPersonRelation ordItemPersonRelation);
	
	
	public int updateSelective(Map<String, Object> params);
	
	public Long getPersonCountByProductId(Long productId, Date groupDate);
	
	/**
	 * 根据ORDERID查询该ORDER中存在的商品、人员关联信息
	 * @param orderId
	 * @return goodid:personsId数组
	 */
    public HashMap<String,ArrayList<String>> findPersonGoodRelationByOrderId(String orderId);
    
    public int insertBatch(List<OrdItemPersonRelation> list);

}
