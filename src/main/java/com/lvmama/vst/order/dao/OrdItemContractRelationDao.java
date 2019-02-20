package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdItemContractRelationDao extends MyBatisDao {
	public OrdItemContractRelationDao() {
		super("ORD_ITEM_CONTRACT_RELATION");
	}

	public int insert(OrdItemContractRelation ordItemContractRelation) {
		return super.insert("insert", ordItemContractRelation);
	}

	public OrdItemContractRelation selectByPrimaryKey(Long id) {
		return super.get("selectByPrimaryKey", id);
	}

	public int updateByPrimaryKeySelective(OrdItemContractRelation ordItemContractRelation) {
		return super.update("updateByPrimaryKeySelective", ordItemContractRelation);
	}

	public int updateByPrimaryKey(OrdItemContractRelation ordItemContractRelation) {
		return super.update("updateByPrimaryKey", ordItemContractRelation);
	}
	
	public List<OrdItemContractRelation> findOrdItemContractRelationList(HashMap<String,Object> params){
		return super.getList("selectByParams", params);
	}
	
	public Integer findOrdItemContractRelationCounts(HashMap<String,Object> params){
		return super.get("selectByParamsCounts",params);
	}
}