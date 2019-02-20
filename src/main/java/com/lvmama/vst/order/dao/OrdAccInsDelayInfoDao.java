package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/** 
 * @Title: OrdAccInsDelayInfo.java 
 * @Package com.lvmama.vst.order.dao 
 * @Description: TODO 
 * @author Wangsizhi
 * @date 2017-2-14 下午4:35:07 
 * @version V1.0.0 
 */
@Repository("ordAccInsDelayInfoDao")
public class OrdAccInsDelayInfoDao extends MyBatisDao {

    public OrdAccInsDelayInfoDao() {
        super("ORD_ACC_INS_DELAY_INFO");
    }
    
    public int deleteByPrimaryKey(Long ordAccInsDelayInfoId){
        return super.delete("deleteByPrimaryKey", ordAccInsDelayInfoId);
    }

    public int insert(OrdAccInsDelayInfo record){
        return super.insert("insert", record);
    }

    public OrdAccInsDelayInfo selectByPrimaryKey(Long ordAccInsDelayInfoId){
        return super.get("selectByPrimaryKey", ordAccInsDelayInfoId);
    }
    
    public OrdAccInsDelayInfo selectByOrderId(Long orderId){
        return super.get("selectByOrderId", orderId);
    }
    
    public List<OrdAccInsDelayInfo> selectByParam(Map<String,Object> param){
        return super.queryForList("selectByParam", param);
    }

    public int updateByPrimaryKey(OrdAccInsDelayInfo record){
        return super.update("updateByPrimaryKey", record);
    }
    
    public List<OrdAccInsDelayInfo> getAccInsDelayTimeoutOrderList(Map<String,Object> param){
        return super.queryForList("getAccInsDelayTimeoutOrderList", param);
    }
    
    public List<OrdAccInsDelayInfo> getAccInsDelayHalfWaitTimeRemindOrderList(Map<String,Object> param){
        return super.queryForList("getAccInsDelayHalfWaitTimeRemindOrderList", param);
    }
}
