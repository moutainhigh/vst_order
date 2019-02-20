package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.order.dao.OrdItemPersonRelationDao;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;

@Service
public class OrdItemPersonRelationServiceImpl implements IOrdItemPersonRelationService {

	private static final Log LOG = LogFactory
			.getLog(OrdItemPersonRelationServiceImpl.class);
	@Autowired
	private OrdItemPersonRelationDao ordItemPersonRelationDao;
	@Override
	public int addOrdItemPersonRelation(
			OrdItemPersonRelation ordItemPersonRelation) {
		// TODO Auto-generated method stub
		return ordItemPersonRelationDao.insert(ordItemPersonRelation);
	}
	@Override
	public OrdItemPersonRelation findOrdItemPersonRelationById(Long id) {
		// TODO Auto-generated method stub
		return ordItemPersonRelationDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdItemPersonRelation> findOrdItemPersonRelationList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordItemPersonRelationDao.findOrdItemPersonRelationList(params);
	}

    @Override
    public HashMap<String, ArrayList<String>> findPersonGoodRelationByOrderId(String orderId) {
        List<Map<String, String>> list = ordItemPersonRelationDao.findPersonGoodRelationByOrderId(orderId);

        HashMap<String, ArrayList<String>> goodMap = new HashMap<String, ArrayList<String>>();
        for (Map<String, String> map : list) {
            String goodId = map.get("SUPP_GOODS_ID");
            String personId = map.get("ORD_PERSON_ID");
            ArrayList<String> personArr = goodMap.get(goodId);

            if (personArr == null) {
                personArr = new ArrayList<String>();
                goodMap.put(goodId, personArr);
            }
            personArr.add(personId);
        }
        return goodMap;
    }
	
	@Override
	public int updateByPrimaryKeySelective(
			OrdItemPersonRelation ordItemPersonRelation) {
		// TODO Auto-generated method stub
		return ordItemPersonRelationDao.updateByPrimaryKeySelective(ordItemPersonRelation);
	}
	
	public int updateSelective(Map<String, Object> params){
		
		return ordItemPersonRelationDao.updateSelective(params);
		
	}
	@Override
	public Long getPersonCountByProductId(Long productId, Date groupDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productId", productId);
		params.put("visitTime", groupDate);
		return ordItemPersonRelationDao.getPersonCountByProductId(params);
	}
	
	@Override
	  public int insertBatch(List<OrdItemPersonRelation> list) {
	    // TODO Auto-generated method stub
	    return ordItemPersonRelationDao.insertBatch(list);
	  }
}
