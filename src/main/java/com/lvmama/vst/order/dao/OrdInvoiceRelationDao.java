package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdInvoiceRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdInvoiceRelationDao extends MyBatisDao {
	public OrdInvoiceRelationDao() {
		super("ORD_INVOICE_RELATION");
	}

	public int deleteByPrimaryKey(Long invoiceRelationId) {
		return super.delete("deleteByPrimaryKey", invoiceRelationId);
	}

	public int insert(OrdInvoiceRelation ordInvoiceRelation) {
		return super.insert("insert", ordInvoiceRelation);
	}

	public int insertSelective(OrdInvoiceRelation ordInvoiceRelation) {
		return super.insert("insertSelective", ordInvoiceRelation);
	}

	public OrdInvoiceRelation selectByPrimaryKey(Long invoiceRelationId) {
		return super.get("selectByPrimaryKey", invoiceRelationId);
	}

	public int updateByPrimaryKeySelective(OrdInvoiceRelation ordInvoiceRelation) {
		return super.update("updateByPrimaryKeySelective", ordInvoiceRelation);
	}

	public int updateByPrimaryKey(OrdInvoiceRelation ordInvoiceRelation) {
		return super.update("updateByPrimaryKey", ordInvoiceRelation);
	}
	
	public List<OrdInvoiceRelation> getListByParam(Map<String,Object> param){
    	return super.queryForList("getListByParam", param);
    }
	
	public Long selectInvoiceCountByOrderId(Long orderId) {
		return super.get("selectInvoiceCountByOrderId",orderId);
	}
	
}