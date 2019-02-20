package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdItemPersonRelationDao extends MyBatisDao {

	public OrdItemPersonRelationDao() {
		super("ORD_ITEM_PERSON_RELATION");
	}

	public int deleteByPrimaryKey(Long id) {
		return super.delete("deleteByPrimaryKey", id);
	}

	public int insert(OrdItemPersonRelation ordItemPersonRelation) {
		return super.insert("insert", ordItemPersonRelation);
	}

	public int insertSelective(OrdItemPersonRelation ordItemPersonRelation) {
		return super.insert("insertSelective", ordItemPersonRelation);
	}

	public OrdItemPersonRelation selectByPrimaryKey(Long ID) {
		return super.get("selectByPrimaryKey", ID);
	}

	public int updateByPrimaryKeySelective(OrdItemPersonRelation ordItemPersonRelation) {
		return super.update("updateByPrimaryKeySelective", ordItemPersonRelation);
	}

	public int updateSelective(Map<String, Object> params) {
		return super.update("updateSelective", params);
	}
	
	public int updateByPrimaryKey(OrdItemPersonRelation ordItemPersonRelation) {
		return super.update("updateByPrimaryKey", ordItemPersonRelation);
	}

	public List<OrdItemPersonRelation> findOrdItemPersonRelationList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	
	/**
	 * 根据ORDERID查询该ORDER中存在的商品、人员关联信息
	 * @param orderId
	 * @return
	 */
    public List<Map<String,String>> findPersonGoodRelationByOrderId(String orderId) {
        return super.queryForList("selectPersonGoodRelationByOrderId", orderId);
    }
	
	public Long getPersonCountByProductId(Map<String, Object> params) {
		return super.get("getPersonCountByProductId", params);
	}
	
	public int insertBatch(List<OrdItemPersonRelation> list) {
       return super.insert("insertBatch", list);
    }
}