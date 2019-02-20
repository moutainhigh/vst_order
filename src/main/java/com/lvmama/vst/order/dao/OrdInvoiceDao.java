package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdInvoice;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdInvoiceDao extends MyBatisDao{
	
	public OrdInvoiceDao() {
		super("ORD_INVOICE");
	}
	
	public int deleteByPrimaryKey(Long ordInvoiceId){
    	return super.delete("deleteByPrimaryKey", ordInvoiceId);
    }

	public int insert(OrdInvoice record){
    	return super.insert("insert", record);
    }

	public int insertSelective(OrdInvoice record){
    	return super.insert("insertSelective", record);
    }

	public OrdInvoice selectByPrimaryKey(Long ordInvoiceId){
    	return super.get("selectByPrimaryKey", ordInvoiceId);
    }

	public int updateByPrimaryKeySelective(OrdInvoice record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

	public int updateByPrimaryKey(OrdInvoice record){
    	return super.update("updateByPrimaryKey", record);
    }
	
	public Long getInvoiceAmountSum(Map<String,Object> param){
    	return super.get("getInvoiceAmountSum", param);
    }
	
	public Long getInvoiceCount(Map<String,Object> param){
		return super.get("getInvoiceCount", param);
	}
	
	public List<OrdInvoice> getOrdInvoiceListByParam(Map<String,Object> param){
    	return super.queryForList("getOrdInvoiceListByParam", param);
    }
	
	public List<OrdInvoice> selectOrdInvoiceListByParam(Map<String,Object> param){
    	return super.queryForList("selectOrdInvoiceListByParam", param);
    }
	
	public List<OrdInvoice> getOrdInvoiceListByParam2(Map<String,Object> param){
    	return super.queryForList("getOrdInvoiceListByParam2", param);
    }
	
	public List<OrdInvoice> getOrdInvoiceListByOrderId(Map<String,Object> param){
    	return super.queryForList("getOrdInvoiceListByOrderId", param);
    }
	
	 /**
     * 查询没有取消的订单数量
     * @param orderId
     * @param status
     * @return
     */
    public Long selectNotCancelInvoiceCountByOrderId(Map<String,Object> param) {
    	return super.get("selectNotCancelInvoiceCountByOrderId", param);
	}
    
    //根据状态查询信息
    public List<OrdInvoice> getStatusOrdInvoiceListByParam(Map<String,Object> param){
    	return super.queryForList("getStatusOrdInvoiceListByParam", param);
    }
	
    public List<OrdInvoice> getOrdInvoiceListByOrderIdList(List<Long> orderIds){
    	return super.queryForList("getOrdInvoiceListByOrderIdList", orderIds);
    }
}