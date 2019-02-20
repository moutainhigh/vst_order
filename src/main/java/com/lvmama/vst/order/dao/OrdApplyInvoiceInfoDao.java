package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import scala.annotation.target.param;

import com.lvmama.vst.back.order.po.OrdApplyInvoiceInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.vo.order.OrderInvoiceInfoVst;
import com.lvmama.vst.comm.vo.order.ApplyInvoiceInfoResult;
/**
 * @author Wangsizhi
 */
@Repository
public class OrdApplyInvoiceInfoDao extends MyBatisDao {
    
    public OrdApplyInvoiceInfoDao() {
        super("ORD_APPLY_INVOICE_INFO");
    }
    
    public int deleteByPrimaryKey(Long id){
        return super.delete("deleteByPrimaryKey", id);
    }
    
    public int insert(OrdApplyInvoiceInfo record){
        return super.insert("insert", record);
    }
    
    public int insertSelective(OrdApplyInvoiceInfo record){
        return super.insert("insertSelective", record);
    }
    
    public OrdApplyInvoiceInfo selectByPrimaryKey(Long id){
        return super.get("selectByPrimaryKey", id);
    }
    
    public List<OrdApplyInvoiceInfo> selectByOrderId(Long orderId){
        return super.queryForList("getOrdApplyInvoiceInfoByOrderId", orderId);
    }
    
    public OrdApplyInvoiceInfo selectAppliedInvoiceByParams(Map<String, Object> param){
        return super.get("selectAppliedInvoiceByParams", param);
    }
    
    public int updateByPrimaryKeySelective(OrdApplyInvoiceInfo record) {
        return super.update("updateByPrimaryKeySelective", record);
    }
    
    public int updateByPrimaryKey(OrdApplyInvoiceInfo record) {
        record.setUpdateTime(new Date());
        return super.update("updateByPrimaryKey", record);
    }
    
    public int updateStatusByPrimaryKey(OrdApplyInvoiceInfo record) {
        record.setUpdateTime(new Date());
        return super.update("updateStatusByPrimaryKey", record);
    }
    
    public List<OrdApplyInvoiceInfo> getPendingApplyInfoListByParam(int applyTimes) {
        return super.queryForList("getPendingApplyInfoListByParam", applyTimes);
    }

	public List<ApplyInvoiceInfoResult> getPreparyApplyInvoiceInfo(Map<String, Object> map) {
		
		  return super.queryForList("getPreparyApplyInvoiceInfo", map);
	}

	public OrderInvoiceInfoVst getVstOrderInvoiceInfo(Map<String, Object> map) {
		
		  return super.get("selectVstOrderInvoiceInfo", map);
	}
	
    public OrdApplyInvoiceInfo selectLatestApplyInvoiceByUserId(String userId){
        return super.get("selectLatestApplyInvoiceByUserId", userId);
    }
    
    public Long findSpecAppInvFullInfoListByUserIdCount(String userId) {
        return super.get("findSpecAppInvFullInfoListByUserIdCount", userId);
    }
    
    public List<OrdApplyInvoiceInfo> findSpecPageAppInvFullInfoListByUserId(Map<String, Object> map) {
        
        return super.queryForList("findSpecPageAppInvFullInfoListByUserId", map);
    }
    
    public List<OrdApplyInvoiceInfo> findSpecStatusApplyInvoiceByOrderId(Long orderId){
        
        return super.queryForList("findSpecStatusApplyInvoiceByOrderId", orderId);
    }

    public Long listAppInvFullInfoByConditionCount(Map<String, Object> map) {
        return super.get("listAppInvFullInfoByConditionCount", map);
    }
    
    public List<OrdApplyInvoiceInfo> listAppInvFullInfoByCondition(Map<String, Object> map) {
        
        return super.queryForList("listAppInvFullInfoByCondition", map);
    }
}
