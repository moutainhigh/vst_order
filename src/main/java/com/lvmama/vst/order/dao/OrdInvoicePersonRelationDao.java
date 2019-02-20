package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdInvoicePersonRelation;
import com.lvmama.vst.back.order.po.OrdInvoiceRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdInvoicePersonRelationDao extends MyBatisDao{
	
	public OrdInvoicePersonRelationDao() {
		super("ORD_INVOICE_PERSON_RELATION");
	}
	
    public int deleteByPrimaryKey(Long invoicePersonRelationId){
    	return super.delete("deleteByPrimaryKey", invoicePersonRelationId);
    }

    public int insert(OrdInvoicePersonRelation record){
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdInvoicePersonRelation record){
    	return super.insert("insertSelective", record);
    }

    public OrdInvoicePersonRelation selectByPrimaryKey(Long invoicePersonRelationId){
    	return super.get("selectByPrimaryKey", invoicePersonRelationId);
    }

    public int updateByPrimaryKeySelective(OrdInvoicePersonRelation record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdInvoicePersonRelation record){
    	return super.update("updateByPrimaryKey", record);
    }
    
}