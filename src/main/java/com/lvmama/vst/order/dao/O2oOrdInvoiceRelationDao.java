package com.lvmama.vst.order.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.O2oOrdInvoiceRelation;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * 
 * @ClassName: O2oOrdInvoiceRelationDao 
 * @Description: 订单发票申请Dao
 * @author: LuoGuang
 * @date: 2016-11-16 下午3:34:25
 */
@Repository
public class O2oOrdInvoiceRelationDao extends MyBatisDao {
	
	public O2oOrdInvoiceRelationDao() {
		super("O2O_ORD_INVOICE_RELATION");
	}
	
	public O2oOrdInvoiceRelation selectByInvoiceId(Long invoiceId){
    	return super.get("selectByInvoiceId", invoiceId);
    }
	
	public int getInvoiceCount(Map<String, Object> paramsMap){
		return super.get("getInvoiceCount", paramsMap);
	}
	
	public int insertSelective(O2oOrdInvoiceRelation o2oOrdInvoiceRelation){
		if (o2oOrdInvoiceRelation==null) {
			return 0;
		}
		return super.insert("insertSelective", o2oOrdInvoiceRelation);
	}
	
}
